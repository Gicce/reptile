package com.jandar.file.test;

import com.jandar.file.FileApplication;
import com.jandar.file.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class SpringBootMainTest {

    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void text1() {
        redisUtil.incr("爬虫", 1);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        redisUtil.incr("爬虫", 1);
        log.info(redisUtil.get("爬虫").toString());
    }
}
