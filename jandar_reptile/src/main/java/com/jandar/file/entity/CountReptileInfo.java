package com.jandar.file.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CountReptileInfo {

    /**
     * 已爬取数据
     */
    Integer allSaveCount;

    /**
     * 待爬数据 总数-（数据库已爬+启动所有数据)
     */
    Integer allWaitCount;

    /**
     * 爬取速率 24小时能爬多少
     */
    Integer crawlRate;

    /**
     * 预计结束时间 带爬取数据/爬取速率(天)
     */
    Integer waitTime;
}
