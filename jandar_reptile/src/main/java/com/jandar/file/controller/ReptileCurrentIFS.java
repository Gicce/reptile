package com.jandar.file.controller;

import com.alibaba.fastjson.JSONObject;
import com.jandar.file.entity.CountReptileInfo;
import com.jandar.file.entity.DayReptileInfo;
import com.jandar.file.entity.ReptileInfo;
import com.jandar.file.service.IMSReptileCurrentIFS;
import com.jandar.file.tool.CalendarUtils;
import com.jandar.file.utils.IpConfiguration;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
public class ReptileCurrentIFS {

    @Value("${reptile.redis.name}")
    private String apiName;

    private final IpConfiguration ipConfiguration;
    private final IMSReptileCurrentIFS ifs;
    private final HashOperations<String, String, Object> hashOperations;
    private final CalendarUtils calendarUtils;

    public ReptileCurrentIFS(IpConfiguration ipConfiguration, IMSReptileCurrentIFS ifs, HashOperations<String, String, Object> hashOperations, CalendarUtils calendarUtils) {
        this.ipConfiguration = ipConfiguration;
        this.hashOperations = hashOperations;
        this.ifs = ifs;
        this.calendarUtils = calendarUtils;
    }

    @ApiOperation(value = "爬虫运行信息")
    @GetMapping(value = "/spiderInfo")
    public JSONObject spiderInfo() {
        String port = String.valueOf(ipConfiguration.getPort());
        JSONObject json = new JSONObject();
        ReptileInfo info;
        DayReptileInfo dayReptileInfo;
        CountReptileInfo countReptileInfo;
        String todayTime = String.valueOf(calendarUtils.getTodayTime());
        if (hashOperations.hasKey(apiName, port) && hashOperations.hasKey(apiName + port, todayTime) && hashOperations.hasKey(apiName + "Count", port)) {
            info = (ReptileInfo) hashOperations.get(apiName, port);
            dayReptileInfo = (DayReptileInfo) hashOperations.get(apiName + port, todayTime);
            countReptileInfo = (CountReptileInfo) hashOperations.get(apiName + "Count", port);
        } else {
            json.put("status", false);
            json.put("data", "数据不存在");
            return json;
        }
        json.put("status", true);
        json.put("data", ifs.getData(info,dayReptileInfo,countReptileInfo));
        return json;
    }

}
