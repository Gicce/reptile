package com.jandar.file.reptile.apple;

import com.jandar.file.utils.AnalyzeHtml;
import com.jandar.file.utils.OkHttpUtils;
import com.jandar.file.utils.RedisUtil;
import com.jandar.file.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @url.request:http://www.appletuan.com/price
 * @description: 苹果团爬虫
 * @author: Mr.Gao
 * @create: 2019-12-16 15:05
 **/
@Slf4j
@Component
public class AppleTuanRepilt {
    @Value("${createThreadSize}")
    private String createThreadSize;
    @Value("${processThreadSize}")
    private String processThreadSize;

    @Autowired
    private OkHttpUtils okHttpUtils;
    @Autowired
    private SqlUtils sqlUtils;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private AnalyzeHtml analyzeHtml;

    public void startup(){
        log.info("苹果团爬虫Repilt");
        String html = sqlUtils.getAppleNowHtml(redisUtil.get("newAppleHtmlId").toString());
    }

}

