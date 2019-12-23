package com.jandar.file.crontask.connection;

import com.jandar.file.crontask.server.SpringReptileConfig;
import com.jandar.file.entity.CountReptileInfo;
import com.jandar.file.entity.ReptileInfo;
import com.jandar.file.utils.IpConfiguration;
import com.jandar.file.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.swing.*;

@Configuration
@EnableScheduling
@EnableAsync
@Slf4j
public class SpringCronTask1 implements SchedulingConfigurer {

    @Value("${reptile.redis.name}")
    private String ApiName;

    private final SpringReptileConfig config;
    private final HashOperations<String, String, Object> hashOperations;
    private final IpConfiguration ipConfiguration;
    private final SqlUtils sqlUtils;

    public SpringCronTask1(SpringReptileConfig config, HashOperations<String, String, Object> hashOperations, IpConfiguration ipConfiguration, SqlUtils sqlUtils) {
        this.config = config;
        this.hashOperations = hashOperations;
        this.ipConfiguration = ipConfiguration;
        this.sqlUtils = sqlUtils;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(() -> {
                    String port = String.valueOf(ipConfiguration.getPort());
                    int sqlCount = sqlUtils.sqlGetCount();
                    int waitCount = getReptileWaitCount(sqlCount, port);
                    int crawRate = getReptileCrawRate(port);
                    int waitTime;
                    if (crawRate == 0) {
                        waitTime = 0;
                    } else {
                        waitTime = waitCount / crawRate;
                    }
                    CountReptileInfo countReptileInfo = new CountReptileInfo();
                    countReptileInfo.setAllSaveCount(sqlCount);
                    countReptileInfo.setAllWaitCount(waitCount);
                    countReptileInfo.setCrawlRate(crawRate);
                    countReptileInfo.setWaitTime(waitTime);
                    hashOperations.put(ApiName + "Count", port, countReptileInfo);
                },
                triggerContext -> new CronTrigger(config.getCron())
                        .nextExecutionTime(triggerContext));
    }

    /**
     * 计算爬虫待爬取数
     *
     * @param SqlCount
     * @return
     */
    public Integer getReptileWaitCount(int SqlCount, String port) {
        ReptileInfo reptileInfo;
        if (hashOperations.hasKey(ApiName, port)) {
            reptileInfo = (ReptileInfo) hashOperations.get(ApiName, port);
            return reptileInfo.getAllDataCount() - SqlCount;
        } else {
            log.error("Redis服务找不到,map");
            return 0;
        }
    }

    /**
     * 计算爬虫速率
     * 1天/
     *
     * @return
     */
    public Integer getReptileCrawRate(String port) {
        if (hashOperations.hasKey(ApiName + port + "Minute", port)) {
            Integer CrawRate = (Integer) hashOperations.get(ApiName + port + "Minute", port);
            hashOperations.put(ApiName + port + "Minute", port, 0);
            return CrawRate * 60 * 24;
        } else {
            log.error("Redis 服务找不到,map");
            return 0;
        }
    }
}
