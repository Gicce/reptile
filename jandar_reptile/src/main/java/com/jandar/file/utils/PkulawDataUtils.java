package com.jandar.file.utils;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

@Slf4j
@Service
public class PkulawDataUtils {

    /**
     * 数据源
     */
    private static DruidDataSource dataSource;

    private static JdbcTemplate jdbcTemplate;

    private static void init() throws IOException {
        Resource resource = new ClassPathResource("application.properties");
        Properties props = PropertiesLoaderUtils.loadProperties(resource);
        try {
            dataSource = new DruidDataSource();
            dataSource.setUrl(props.getProperty("datasource.data.url"));
            dataSource.setUsername(props.getProperty("datasource.data.username"));
            dataSource.setPassword(props.getProperty("datasource.data.password"));
            jdbcTemplate = new JdbcTemplate(dataSource);
        } catch (Exception e) {
            log.error("初始化数据库失败");
            System.exit(0);
        }
    }

    public static JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jdbcTemplate;
    }

}
