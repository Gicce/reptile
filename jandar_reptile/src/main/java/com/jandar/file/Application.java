package com.jandar.file;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jandar.file.entity.ContentPkulawV1;
import com.jandar.file.reptile.motto.ClimbMotto;
import com.jandar.file.reptile.pkulaw.*;
import com.jandar.file.tool.CalendarUtils;
import com.jandar.file.utils.OkHttpUtils;
import com.jandar.file.utils.PkulawContent;
import com.jandar.file.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.expression.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


/**
 * 爬虫启动程序
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Controller
@Slf4j
public class Application implements ApplicationRunner {
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
    private PkuLawPt pkuLawPt;
    @Autowired
    private PkulawJX pkulawJX;
    @Autowired
    private ClimbMotto motto;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        pkuLawPt.startup();
//        pkulawJX.status();
        motto.start();
    }
}
