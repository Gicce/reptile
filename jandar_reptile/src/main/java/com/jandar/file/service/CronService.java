package com.jandar.file.service;

import com.jandar.file.tool.CalendarUtils;
import com.jandar.file.utils.IpConfiguration;
import com.jandar.file.utils.OkHttpUtils;
import com.jandar.file.utils.RedisUtil;
import com.jandar.file.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.test.context.jdbc.Sql;


@Component
@Slf4j
public class CronService {

    @Value("${url.request}")
    private String url;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IpConfiguration ipConfiguration;
    @Autowired
    private CalendarUtils calendarUtils;
    @Autowired
    private OkHttpUtils okHttpUtils;
    @Autowired
    private SqlUtils sqlUtils;

    //        @Scheduled(cron = "0 0 0-23 * * ? ")
    @Scheduled(cron = "1 * * * * ?")
    public void everydayData() {
        redisUtil.set("everyDayData" + ipConfiguration.getPort(), "0");
    }

//    @Scheduled(cron = "0 0 0 * * ?")
    @Scheduled(cron = "1 * * * * ?")
    public void getAppleHtml() {
        String html = okHttpUtils.getHTml(url);
        if (!html.equals("null")) {
            Element body = Jsoup.parse(html);
            Element box = body.select("#page-main > div > div.col-md-8.col-lg-8.col-sm-8 > div:nth-child(2)").get(0);
            int SqlHtmlId = sqlUtils.insertAppleHtml(box.html(), "2019-12-16 00:00:00");
            redisUtil.set("newAppleHtmlId", String.valueOf(SqlHtmlId));
        }
    }

}
