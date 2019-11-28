package com.jandar.file.service;

import com.jandar.file.tool.CalendarUtils;
import com.jandar.file.utils.IpConfiguration;
import com.jandar.file.utils.RedisUtil;
import com.jandar.file.utils.SqlUtils;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaochenyang
 */
@Slf4j
@Service
public class IMSReptileCurrentIFS {
    @Autowired
    private SqlUtils sqlUtils;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IpConfiguration ipConfiguration;
    @Autowired
    private CalendarUtils calendarUtils;

    /**
     * allSaveCount 已爬取数据
     * todayCount   今日爬取数据
     * allWaitCount 待爬数据
     * crawlRate    爬取速率
     * dataRate     完成百分比
     * waitTime     预计结束时间
     *
     * @param map
     * @return
     */
    public Map getData(Map<Object, Object> map) {
        int tableNumber = sqlUtils.ReturnRows();
        map.put("allSaveCount", tableNumber);
        map.put("todayCount", redisUtil.getScore("reptileData" + ipConfiguration.getPort(), calendarUtils.getMMDD()));
        map.put("allWaitCount", getAllWaitCount(map));
        map.put("crawlRate", getCrawlRate());
        map.put("dataRate", getDataRate(map));
        map.put("waitTime", getWaitTime(getAllWaitCount(map)));
        return map;
    }

    /**
     * 待爬数据
     * 数据总量-已爬取的数据.
     *
     * @return
     */
    private Integer getAllWaitCount(Map<Object, Object> map) {
        int alldata = Integer.parseInt(map.get("allDataCount").toString());
        if (alldata == 0) {
            log.error("无法计算总数据.");
            return 1;
        }
        int allSaveCount = new Double(redisUtil.getZsetScoreSum("reptileData" + ipConfiguration.getPort())).intValue();
        return alldata - allSaveCount;
    }

    /**
     * 爬取速率
     * 24小时制
     *
     * @return
     */
    public int getCrawlRate() {
        String data = null;
        try {
            data = redisUtil.get("everyDayData" + ipConfiguration.getPort()).toString();
        } catch (Exception e) {
            log.error("数据还未导入,请等待1小时后重试");
            return 1;
        }
        return Integer.parseInt(data) * 60 * 24;
    }

    /**
     * 完成百分比
     * 保留到.00
     *
     * @return
     */
    public String getDataRate(Map<Object, Object> map) {
        double alldata = Integer.parseInt(map.get("allDataCount").toString());
        if (alldata == 0) {
            log.error("无法计算总数据.");
            return "未知";
        }
        double replietData = redisUtil.getZsetScoreSum("reptileData" + ipConfiguration.getPort());
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format((replietData / alldata) * 100);
    }

    /**
     * 预计结束时间
     * 总量-当前数据/24小时数据
     *
     * @return
     */
    public int getWaitTime(int WaitCount) {
        return WaitCount / getCrawlRate();
    }
}