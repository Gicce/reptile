package com.jandar.file.reptile.motto;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jandar.file.utils.HtmlUtils;
import com.jandar.file.utils.OkHttpUtils;
import com.jandar.file.utils.PkulawContent;
import com.jandar.file.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @description: 格言网 https://www.geyanw.com/
 * @author: Mr.Gao
 * @create: 2019-10-20 09:21
 **/
@Component
@Slf4j
public class ClimbMotto {
    @Value("${createThreadSize}")
    private String createThreadSize;
    @Value("${processThreadSize}")
    private String processThreadSize;

    @Autowired
    private OkHttpUtils okHttpUtils;
    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private PkulawContent content;
    @Autowired
    private RedisUtil redisUtil;
    private final String MOTTO_URL = "https://www.geyanw.com/";

    public void start() {
        log.info("格言网爬虫启动,地址:{}", "https://www.geyanw.com/");
        redisUtil.incr("Reptile",1);
        String mottoHtml = null;
        try {
            mottoHtml = okHttpUtils.getMottoHtml(MOTTO_URL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BlockingQueue<String> smallUrlQueue = new LinkedBlockingDeque<>();
        //生产线程数
        int createSize = Integer.parseInt(createThreadSize);
        //消费线程数
        int processSize = Integer.parseInt(processThreadSize);
        //总线程数
        int total = createSize + processSize;
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("in-thread-%d").build();
        ExecutorService mulThreadPools = new ThreadPoolExecutor(processSize, processSize, 0,
                TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(processSize), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        ExecutorService proDukTionPools = new ThreadPoolExecutor(createSize, createSize, 0,
                TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(10), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        CountDownLatch downLatch = new CountDownLatch(total);

        MottoThread.MottoCallback mottoCallback = (url, inSmallUrlQueue) -> {
            try {
                AtomicInteger mottoPage = new AtomicInteger(0);
                while (true) {
                    String htmlList = okHttpUtils.getMottoHtml(url + htmlUtils.getUrl(url) + mottoPage.addAndGet(1) + ".html");
                    if (htmlUtils.analyseHtml(htmlList, inSmallUrlQueue)) {
                        log.info("当前队列:{}", inSmallUrlQueue.size());
                    } else {
                        log.error("获取数据异常，跳出循环");
                        break;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                downLatch.countDown();
            }
        };


        Runnable runnable = () -> {
            try {
                AtomicInteger pageNo = new AtomicInteger(0);
                while (true) {
                    if (!smallUrlQueue.isEmpty()) {
                        String url = smallUrlQueue.poll();
                        if (url != null) {
                            htmlUtils.analysisData(okHttpUtils.getMottoHtml(url));
                            log.info("处理URL:{},队列剩余:{}", url, smallUrlQueue.size());
                        }
                    } else {
                        log.info("队列为空，等待{}ms", 2);
                        Thread.sleep(200);
                        pageNo.addAndGet(1);
                    }
                    if (pageNo.get() > 100) {
                        pageNo.set(0);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                downLatch.countDown();
            }
        };

        htmlUtils.analysisMottoHtml(mottoHtml).forEach(item -> {
            String html = MOTTO_URL + item.substring(1);
            proDukTionPools.execute(new MottoThread(html, smallUrlQueue, mottoCallback));
        });
        for (int i = 0; i < processSize; i++) {
            mulThreadPools.execute(runnable);
        }
        try {
            downLatch.await();
            proDukTionPools.shutdown();
            mulThreadPools.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("完事了");
    }
}
