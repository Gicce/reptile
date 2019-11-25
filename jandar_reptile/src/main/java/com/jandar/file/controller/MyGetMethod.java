package com.jandar.file.controller;

import com.alibaba.fastjson.JSONObject;
import com.jandar.file.utils.IpConfiguration;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
public class MyGetMethod {

    @Autowired
    private IpConfiguration ipConfiguration;

    @ApiOperation(value = "爬虫运行信息")
    @GetMapping(value = "/spiderInfo")
    public JSONObject spiderInfo() {
        JSONObject json = new JSONObject();
        json.put("status", true);
        json.put("data", ipConfiguration.getPort());
        return json;
    }

}
