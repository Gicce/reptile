package com.jandar.file.reptile.motto;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jandar.file.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    private RedisUtil redisUtil;
    @Autowired
    private IpConfiguration ipConfiguration;

    private final String MOTTO_URL = "https://www.geyanw.com/";

    public void start() {
        final int PORT = ipConfiguration.getPort();
        log.info("格言网爬虫启动,地址:{}", "https://www.geyanw.com/");
        redisUtil.incr("Reptile" + PORT, 1);
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
        Map<Object, Object> result = new HashMap<>();
        result.put("allDataCount", "0");
        result.put("threadPoolSize", String.valueOf(total));
        result.put("proxyUseRate", "1");
        redisUtil.hmset(String.valueOf(PORT), result);
        redisUtil.set("mottoCallback", "1");
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
                        redisUtil.set("mottoCallback", "0");
                        break;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                downLatch.countDown();
            }
        };

        //爬取的数据。
        Runnable runnable = () -> {
            try {
                while (true) {
                    if (redisUtil.get("mottoCallback").equals("0") && smallUrlQueue.isEmpty()) {
                        log.info("完事了");
                        break;
                    }
                    if (!smallUrlQueue.isEmpty()) {
                        String url = smallUrlQueue.poll();
                        if (url != null) {
                            htmlUtils.analysisData(okHttpUtils.getMottoHtml(url));
                            log.info("处理URL:{},队列剩余:{}", url, smallUrlQueue.size());
                        }
                    } else {
                        log.info("队列为空，等待{}ms", 2);
                        Thread.sleep(200);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
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
            redisUtil.decr("Reptile" + ipConfiguration.getPort(), 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("完事了");
    }
}
