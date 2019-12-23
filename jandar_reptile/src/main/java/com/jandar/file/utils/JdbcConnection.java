package com.jandar.file.utils;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author admin
 */
@Slf4j
@Service
public class JdbcConnection {

    @Value("${datasource.data.url}")
    private String url;
    @Value("${datasource.data.username}")
    private String username;
    @Value("${datasource.data.password}")
    private String password;

    private JdbcTemplate jdbcTemplate;

    private void init() {
        try {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            jdbcTemplate = new JdbcTemplate(dataSource);
        } catch (Exception e) {
            log.error("初始化数据库失败");
            System.exit(0);
        }
    }

    public JdbcTemplate getJdbcTemplate() {
        if (jdbcTemplate == null) {
            init();
        }
        return jdbcTemplate;
    }

}
