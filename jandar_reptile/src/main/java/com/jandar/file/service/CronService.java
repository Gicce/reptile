package com.jandar.file.service;

import com.jandar.file.tool.CalendarUtils;
import com.jandar.file.utils.IpConfiguration;
import com.jandar.file.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class CronService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IpConfiguration ipConfiguration;
    @Autowired
    private CalendarUtils calendarUtils;

    //        @Scheduled(cron = "0 0 0-23 * * ? ")
    @Scheduled(cron = "1 * * * * ?")
    public void everydayData() {
        double newData = redisUtil.getScore("reptileData" + ipConfiguration.getPort(), calendarUtils.getMMDD());
        String value = String.valueOf(new Double(newData).intValue());
        if (getObjcet(redisUtil.get("everyDayData" + ipConfiguration.getPort())) > 0) {
            redisUtil.set("everyDayData" + ipConfiguration.getPort(), String.valueOf(newData - getObjcet(redisUtil.get("bianliang1"))));
            redisUtil.set("bianliang1", value);
        } else {
            redisUtil.set("bianliang1", value);
            redisUtil.set("everyDayData" + ipConfiguration.getPort(), value);
        }
    }

    public double getObjcet(Object o) {
        return Double.parseDouble(o.toString());
    }
}
