package com.jandar.file.reptile.pkulaw;


import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jandar.file.Application;
import com.jandar.file.entity.ContentPkulawV1;
import com.jandar.file.tool.CalendarUtils;
import com.jandar.file.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
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

    @Autowired
    private SqlUtils sqlUtils;
    @Autowired
    private CalendarUtils calendarUtils;
    @Autowired
    private PkulawContent content;
    @Autowired
    private OkHttpUtils okHttpUtils;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IpConfiguration ipConfiguration;


    public void startup() {
        Logger log = LoggerFactory.getLogger(Application.class);
        log.info("法宝-开始发布任务");
        final int PORT = ipConfiguration.getPort();
        int total = Integer.parseInt(createThreadSize + processThreadSize);
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
        Map<Object, Object> result = new HashMap<>();
        result.put("allDataCount", Pattern.compile("[^0-9]").matcher(data.get("title")).replaceAll(""));
        result.put("startTime", String.valueOf(System.currentTimeMillis()));
        result.put("threadPoolSize", String.valueOf(total));
        result.put("proxyUseRate", "1");
        if (redisUtil.hasKey(String.valueOf(PORT))) {
            redisUtil.del(String.valueOf(PORT));
            redisUtil.hmset(String.valueOf(PORT), result);
        } else {
            redisUtil.hmset(String.valueOf(PORT), result);
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
                        content.getUrl(html, smallUrlQueue);
                        content.writeToText("当前爬取在第" + currentPage + "页", calendarUtils.getEnDate());
                        log.info("当前队列Size:{}", inSmallUrlQueue.size());
                    }
                } while (++currentPage <= inEndPage);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                createThreadEnd.addAndGet(1);
            }
        };
        for (int i = 0; i < Integer.parseInt(createThreadSize); i++) {
            pageThreads.add(new PageThread(startPage, endPage, smallUrlQueue, pageCallback));
            startPage += offsetPage;
            endPage += offsetPage;
        }
        Runnable processThread = () -> {
            try {
                while (true) {
                    List<ContentPkulawV1> contentPkulawV1s = new ArrayList<>();
                    if (createThreadEnd.get() == Integer.parseInt(createThreadSize) && smallUrlQueue.isEmpty()) {
                        log.info("完事~");
                        break;
                    }
                    if (smallUrlQueue.isEmpty()) {
                        log.info("队列为空，消费者等待5ms");
                        Thread.sleep(500);
                    } else {
                        String url = smallUrlQueue.poll();
                        if (url != null) {
                            contentPkulawV1s.add(content.contentPkulawV1s(content.getContent(okHttpUtils.getHTml(url), url)));
                            sqlUtils.insertList(contentPkulawV1s);
                            log.info("处理URL:{},队列剩余:{}", url, smallUrlQueue.size());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        for (PageThread pageThread : pageThreads) {
            mulThreadPool.execute(pageThread);
        }

        for (int i = 0; i < Integer.parseInt(processThreadSize); i++) {
            mulThreadPool.execute(processThread);
        }
        mulThreadPool.shutdown();
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
