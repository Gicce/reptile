package com.jandar.file.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author MS.gao
 * @Description 爬虫信息
 * @since 2019/12/22 15:34
 */
@Data
@ToString
public class ReptileInfo {

    /**
     * 爬虫总数
     */
    Integer allDataCount;

    /**
     * 项目运行时间
     */
    String startTime;

    /**
     * 爬虫线程数
     */
    Integer threadPoolSize;

    /**
     * 代理成功率
     */
    Integer proxyUseRate;
}
