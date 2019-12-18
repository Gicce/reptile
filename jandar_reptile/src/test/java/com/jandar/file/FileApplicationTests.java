package com.jandar.file;

import com.jandar.file.utils.AnalyzeHtml;
import com.jandar.file.utils.OkHttpUtils;
import com.jandar.file.utils.RedisUtil;
import com.jandar.file.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class FileApplicationTests {

    @Value("${url.request}")
    private String url;
    @Autowired
    private OkHttpUtils okHttpUtils;
    @Autowired
    private AnalyzeHtml analyzeHtml;
    @Autowired
    private SqlUtils sqlUtils;
    @Autowired
    private RedisUtil redisUtil;
    @Test
    public void contextLoads() {
        String html = sqlUtils.getAppleNowHtml(redisUtil.get("newAppleHtmlId").toString());
        analyzeHtml.analyseHtml(html);

//        List<Map<String,String>> list = new ArrayList<>();
//        Map<String,String> map = new HashMap<>();
//        map.put("name","gcy");
//        map.put("pass","123");
//        map.put("passnoe","123");
//        list.add(map);
//        list.forEach(item -> {
//            log.info(item.toString());
//        });

    }

}
