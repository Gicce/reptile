package com.jandar.file.service;

import com.jandar.file.entity.CountReptileInfo;
import com.jandar.file.entity.DayReptileInfo;
import com.jandar.file.entity.ReptileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gaochenyang
 */
@Slf4j
@Service
public class IMSReptileCurrentIFS {

    /**
     * 爬虫信息
     * @param reptileInfo
     * @param dayReptileInfo
     * @param countReptileInfo
     * @return
     */
    public Map getData(ReptileInfo reptileInfo, DayReptileInfo dayReptileInfo, CountReptileInfo countReptileInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("allDataCount", reptileInfo.getAllDataCount());
        map.put("startTime", reptileInfo.getStartTime());
        map.put("threadPoolSize", reptileInfo.getThreadPoolSize());
        map.put("proxyUseRate", reptileInfo.getProxyUseRate());
        map.put("todayCount",dayReptileInfo.getTodayCount());
        map.put("allSaveCount",countReptileInfo.getAllSaveCount());
        map.put("allWaitCount",countReptileInfo.getAllWaitCount());
        map.put("crawlRate",countReptileInfo.getWaitTime());
        map.put("waitTime",countReptileInfo.getWaitTime());
        return map;
    }

}
