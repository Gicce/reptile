package com.jandar.file.reptile.pkulaw;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jandar.file.entity.ContentPkulawV1;
import com.jandar.file.tool.CalendarUtils;
import com.jandar.file.utils.OkHttpUtils;
import com.jandar.file.utils.PkulawContent;
import com.jandar.file.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Component
@Slf4j
public class PkulawJX {

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

    @Autowired
    private SqlUtils sqlUtils;
    @Autowired
    private CalendarUtils calendarUtils;
    @Autowired
    private PkulawContent content;
    @Autowired
    private OkHttpUtils okHttpUtils;

    private class InThread extends Thread {
        private String code;
        private int threaSize;
        private int endSize;
        private String jxal_number;
        private BlockingQueue<String> smallUrlQueue;

        public InThread(String jxal_number, String code, int endSize, Integer threaSize, BlockingQueue<String> smallUrlQueue) {
            this.jxal_number = jxal_number;
            this.code = code;
            this.endSize = endSize;
            this.threaSize = threaSize;
            this.smallUrlQueue = smallUrlQueue;
        }

        @Override
        public void run() {
            try {
                AtomicInteger pageNo = new AtomicInteger(0);
                ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("in-thread-%d").build();
                ExecutorService mulThreadPool = new ThreadPoolExecutor(threaSize, threaSize, 0,
                        TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(threaSize), threadFactory, new ThreadPoolExecutor.AbortPolicy());
                CountDownLatch downLatch = new CountDownLatch(threaSize);
                Runnable runnable = () -> {
                    try {
                        while (true) {
                            if (smallUrlQueue.size() > 1000) {
                                log.info("当前队列{},等待10S", smallUrlQueue.size());
                                Thread.sleep(10000);
                            } else {
                                log.info("开始{} ,第{}页", jxal_number, pageNo.addAndGet(1));
                                String html = okHttpUtils.getHTml(pageNo.get(), code);
                                content.getUrl(html, smallUrlQueue);
                                content.writeToText("开始 " + jxal_number + " 第" + pageNo.get() + "页", calendarUtils.getEnDate());
                                log.info("当前队列Size:{}", smallUrlQueue.size());
                                if (pageNo.get() > (endSize / 40) + 1) {
                                    log.info("页面爬取上限 切换");
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("请求异常");
                        e.printStackTrace();
                    } finally {
                        downLatch.countDown();
                    }
                };
                for (int i = 0; i < threaSize; i++) {
                    mulThreadPool.execute(runnable);
                }
                try {
                    downLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("完事了");
                mulThreadPool.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void status() {
        BlockingQueue<String> smallUrlQueue = new LinkedBlockingDeque<>();
        String pageHomeHtml = okHttpUtils.getJxHtml(bfdate);
        int total = Integer.parseInt(processThreadSize);
        int createSize = Integer.parseInt(createThreadSize);
        if (StringUtils.isBlank(pageHomeHtml)) {
            log.error("主页为空");
            System.exit(0);
        }
        Document body = Jsoup.parse(pageHomeHtml);
        ExecutorService mulThreadPool = Executors.newFixedThreadPool(createSize);
        Elements elements = body.select("div.side > div.sub > div.nav-txt > a");
        elements.forEach(item -> {
            String jxal_number = item.text();
            int endSize = Integer.parseInt(Pattern.compile("[^0-9]").matcher(item.text()).replaceAll("").trim());
            String code = item.attr("href").replaceAll(".*&w=", "");
            mulThreadPool.execute(new InThread(jxal_number, code, endSize, createSize, smallUrlQueue));
        });

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("in-thread-%d").build();
        ExecutorService mulThreadPools = new ThreadPoolExecutor(total, total, 0,
                TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(total), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        CountDownLatch downLatch = new CountDownLatch(total);
        Runnable processThread = () -> {
            try {
                AtomicInteger pageNo = new AtomicInteger(0);
                while (true) {
                    if (!smallUrlQueue.isEmpty()) {
                        String url = smallUrlQueue.poll();
                        if (url != null) {
                            List<ContentPkulawV1> contentPkulawV1s = new ArrayList<>();
                            contentPkulawV1s.add(content.contentPkulawV1s(content.getContent(okHttpUtils.getHTml(url), url)));
                            sqlUtils.insertList(contentPkulawV1s);
                            log.info("处理URL:{},队列剩余:{}", url, smallUrlQueue.size());
                        }
                    } else {
                        log.info("队列为空，等待{}ms", 2);
                        Thread.sleep(200);
                        pageNo.addAndGet(1);
                    }
                    if (pageNo.get() > 200) {
                        pageNo.set(0);
                        break;
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                downLatch.countDown();
            }
        };

        for (int i = 0; i < Integer.parseInt(processThreadSize); i++) {
            mulThreadPools.execute(processThread);
        }
        try {
            downLatch.await();
            mulThreadPools.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("ALL END");
    }
}
