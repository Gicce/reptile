package com.jandar.file.controller;

import com.alibaba.fastjson.JSONObject;
import com.jandar.file.service.IMSReptileCurrentIFS;
import com.jandar.file.utils.IpConfiguration;
import com.jandar.file.utils.RedisUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/api")
public class ReptileCurrentIFS {

    @Autowired
    private IpConfiguration ipConfiguration;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IMSReptileCurrentIFS ifs;

    @ApiOperation(value = "爬虫运行信息")
    @GetMapping(value = "/spiderInfo")
    public JSONObject spiderInfo() {
        JSONObject json = new JSONObject();
        try {
            if (Integer.parseInt(redisUtil.get("Reptile" + ipConfiguration.getPort()).toString()) > 1) {
                json.put("status", false);
                json.put("data", "有多个爬虫启动");
                return json;
            }
        } catch (Exception e) {
            e.printStackTrace();
            json.put("status", false);
            json.put("data", "没有爬虫启动");
            return json;
        }

        Map data = redisUtil.hmget(String.valueOf(ipConfiguration.getPort()));
        json.put("status", true);
        json.put("data", ifs.getData(data));
        return json;
    }

}
