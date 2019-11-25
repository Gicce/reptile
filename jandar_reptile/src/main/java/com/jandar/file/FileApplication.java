package com.jandar.file;

import com.alibaba.fastjson.JSONObject;
import com.jandar.file.utils.RedisUtil;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@RequestMapping("/api")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
public class FileApplication {
    @Autowired
    private RedisUtil redisUtil;

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }

//    /**
//     * 爬虫运行信息
//     *
//     * @param
//     * @return
//     */
//    @ApiOperation(value = "爬虫运行信息")
//    @GetMapping(value = "/spiderInfo")
//    public JSONObject spiderInfo() {
//        JSONObject json = new JSONObject();
//        if (Integer.parseInt((String) redisUtil.get("Reptile")) == 0) {
//            json.put("status", false);
//            json.put("data", "没有爬虫启动");
//            return json;
//        }
//        if (Integer.parseInt((String) redisUtil.get("Reptile")) > 1) {
//            json.put("status", false);
//            json.put("data", "有多个爬虫启动");
//            return json;
//        }
//        json.put("status", true);
//        json.put("data", data);
//        return json;
//    }


}
