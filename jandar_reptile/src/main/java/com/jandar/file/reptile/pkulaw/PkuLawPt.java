package com.jandar.file.reptile.pkulaw;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jandar.file.entity.ContentPkulawV1;
import com.jandar.file.entity.DayReptileInfo;
import com.jandar.file.entity.ReptileInfo;
import com.jandar.file.tool.CalendarUtils;
import com.jandar.file.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * @description: 北大法宝普通案例
 * @author: Mr.Gao
 * @create: 2019-10--16 15:45
 **/
@Slf4j
@Component
public class PkuLawPt {

    @Value("${createThreadSize}")
    private String createThreadSize;
    @Value("${processThreadSize}")
    private String processThreadSize;
    @Value("${bfdate}")
    private String bfdate;
    @Value("${efdate}")
    private String efdate;
    @Value("${pageSize}")
    private String pageSize;
    @Value("${fileAddress}")
    private String fileAddress;
    @Value("${reptile.redis.name}")
    private String ApiName;

    private final SqlUtils sqlUtils;
    private final CalendarUtils calendarUtils;
    private final PkulawContent content;
    private final OkHttpUtils okHttpUtils;
    private final HashOperations<String, String, Object> hashOperations;
    private final IpConfiguration ipConfiguration;
    private Map<String, List<ContentPkulawV1>> listMap = Maps.newConcurrentMap();

    public PkuLawPt(SqlUtils sqlUtils, CalendarUtils calendarUtils, PkulawContent content, OkHttpUtils okHttpUtils, IpConfiguration ipConfiguration, @Qualifier("HashOperations") HashOperations<String, String, Object> hashOperations) {
        this.sqlUtils = sqlUtils;
        this.calendarUtils = calendarUtils;
        this.content = content;
        this.okHttpUtils = okHttpUtils;
        this.ipConfiguration = ipConfiguration;
        this.hashOperations = hashOperations;
    }

    /**
     * 爬虫启动接口
     */
    public void startup() {
        log.info("法宝-开始发布任务");
        String port = String.valueOf(ipConfiguration.getPort());
        ReptileInfo info;

        //线程总数
        int total = Integer.parseInt(createThreadSize + processThreadSize);
        //队列
        BlockingQueue<String> smallUrlQueue = new LinkedBlockingDeque<>();
        AtomicInteger createThreadEnd = new AtomicInteger(0);
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("in-thread-%d").build();
        ExecutorService mulThreadPool = new ThreadPoolExecutor(total, total, 0,
                TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(total), threadFactory, new ThreadPoolExecutor.AbortPolicy());

        List<PageThread> pageThreads = new ArrayList<>();
        //修改
        Map<String, String> data = getMoMate(whileGetHtml(okHttpUtils.getPTHomeHtml(bfdate, efdate)));
        //间隔
        int offsetPage = Integer.parseInt(Pattern.compile("[^0-9]").matcher(data.get("title")).replaceAll("")) / 40 + 1;
        //开始页数
        int startPage;
        if (StringUtils.isNotEmpty(pageSize)) {
            startPage = Integer.parseInt(pageSize);
        } else {
            startPage = 0;
        }
        //结束页
        int endPage = offsetPage;

        if (hashOperations.hasKey(ApiName, port)) {
            info = (ReptileInfo) hashOperations.get(ApiName, port);
        } else {
            info = new ReptileInfo();
            info.setStartTime(String.valueOf(System.currentTimeMillis()));
            info.setAllDataCount(Integer.parseInt(Pattern.compile("[^0-9]").matcher(data.get("title")).replaceAll("")));
            info.setThreadPoolSize(total);
            info.setProxyUseRate(1);
            hashOperations.put(ApiName, port, info);
        }

        PageThread.PageCallback pageCallback = (inStartPage, inEndPage, inSmallUrlQueue) -> {
            int currentPage = inStartPage;
            try {
                do {
                    if (inSmallUrlQueue.size() > 1000) {
                        log.info("当前队列{},等待10S", inSmallUrlQueue.size());
                        Thread.sleep(10000);
                    } else {
                        log.info("获取第{}页", currentPage);
                        String html = okHttpUtils.getPTHTml(currentPage, data.get("code"));
                        content.getUrl(html, inSmallUrlQueue);
                        content.writeToText("当前爬取在第" + currentPage + "页", calendarUtils.getEnDate());
                        log.info("当前队列Size:{}", inSmallUrlQueue.size());
                    }
                } while (++currentPage <= inEndPage);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                createThreadEnd.addAndGet(1);
            }
        };


        Runnable processThread = () -> {
            try {
                String threadName = Thread.currentThread().getName();
                if (!listMap.containsKey(threadName)) {
                    listMap.put(threadName, Lists.newArrayList());
                }
                while (true) {
                    if (createThreadEnd.get() == Integer.parseInt(createThreadSize) && smallUrlQueue.isEmpty()) {
                        if (!listMap.get(threadName).isEmpty()) {
                            int len = sqlUtils.insertList(listMap.get(threadName));
                            addCount(len, port);
                            listMap.get(threadName).clear();
                        }
                        log.info("完事~");
                        break;
                    }
                    if (smallUrlQueue.isEmpty()) {
                        log.info("队列为空，消费者等待500ms");
                        Thread.sleep(500);
                    } else {
                        String url = smallUrlQueue.poll();
                        if (url != null) {
                            if (listMap.get(threadName).size() >= 50) {
                                int len = sqlUtils.insertList(listMap.get(threadName));
                                addCount(len, port);
                                listMap.get(threadName).clear();
                            } else {
                                listMap.get(threadName).add(content.contentPkulawV1s(content.getContent(okHttpUtils.getHTml(url), url)));
                            }
                            log.info("处理URL:{},队列剩余:{}", url, smallUrlQueue.size());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };


        /**
         * 爬虫生产者
         */
        for (int i = 0; i < Integer.parseInt(createThreadSize); i++) {
            pageThreads.add(new PageThread(startPage, endPage, smallUrlQueue, pageCallback));
            startPage += offsetPage;
            endPage += offsetPage;
        }

        for (PageThread pageThread : pageThreads) {
            mulThreadPool.execute(pageThread);
        }

        /**
         * 爬虫消费者
         */
        for (int i = 0; i < Integer.parseInt(processThreadSize); i++) {
            mulThreadPool.execute(processThread);
        }

        //结束
        mulThreadPool.shutdown();
    }

    /**
     * 获取爬虫动态信息
     *
     * @param add
     */
    private synchronized void addCount(int add, String prot) {
        String todayTime = String.valueOf(calendarUtils.getTodayTime());
        DayReptileInfo dayReptileInfo;
        if (hashOperations.hasKey(ApiName + prot, todayTime)) {
            dayReptileInfo = (DayReptileInfo) hashOperations.get(ApiName + prot, todayTime);
            dayReptileInfo.setTodayCount(dayReptileInfo.getTodayCount() + add);
        } else {
            dayReptileInfo = new DayReptileInfo();
            dayReptileInfo.setTodayCount(add);
        }
        hashOperations.put(ApiName + prot, todayTime, dayReptileInfo);
        hashOperations.increment(ApiName + prot + "Minute", prot, add);
    }


    public String whileGetHtml(String html) {
        while (true) {
            if (StringUtils.isBlank(html)) {
                try {
                    log.error("主页为空:10S后重新执行");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        return html;
    }

    public Map<String, String> getMoMate(String html) {
        Map<String, String> date = new HashMap<>();
        Document body = Jsoup.parse(html);
        Element elements = body.select("div.tit > div.nav-txt > a").get(0);
        String title = elements.text();
        String code = body.select("div.page > a").get(0).attr("href").replaceAll(".*&w=", "");
        date.put("title", title);
        date.put("code", code);
        return date;
    }


}
