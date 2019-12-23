package com.jandar.file.utils;

import com.alibaba.fastjson.JSONObject;
import com.jandar.file.entity.ContentPkulawV1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;

@Slf4j
@Service
public class SqlUtils {

    @Value("${contentTable}")
    private String ContentTable;
    @Value("${htmlTable}")
    private String HtmlTable;
    @Value("${libraryName}")
    private String LibraryName;

    private final JdbcConnection jdbcConnection;

    public SqlUtils(JdbcConnection jdbcConnection) {
        this.jdbcConnection = jdbcConnection;
    }

    /**
     * 数据库insert 插入
     *
     * @param
     * @return
     */
    public synchronized int[] insertHtml(JSONObject document) {
        String sql = "insert into " + HtmlTable + " (id,html,type) SELECT ?,?,? FROM DUAL WHERE NOT EXISTS(select id from " + HtmlTable + " where id=? )";
        int success = jdbcConnection.getJdbcTemplate().update(sql,
                document.getString("id"),
                document.getString("content_html"),
                document.getString("type"),
                document.getString("id"));
        if (success != 1) {
            log.error("数据已存在！,插入失败");
        }
        return null;
    }

    /**
     * 数据库insert 插入
     *
     * @param contentPkulaw
     * @return
     */
    public synchronized int insertList(List<ContentPkulawV1> contentPkulaw) {
        String sql = "insert into " + ContentTable + " (id,case_name,case_type,case_code,Instrument_type,closing_date,trial_court,trial_procedure,case_plot,judgment_result,judging_crime,penalty,trial_judge,release_date,case_characteristics,adjudication_org,adjudication_date,full_text,source_url,case_db) SELECT ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? FROM DUAL WHERE NOT EXISTS(SELECT id FROM " + ContentTable + " WHERE id = ?)";
        int[] result = jdbcConnection.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                ContentPkulawV1 contentPkulawV1 = contentPkulaw.get(i);
                preparedStatement.setLong(1, contentPkulawV1.getId());
                preparedStatement.setString(2, contentPkulawV1.getCaseName());
                preparedStatement.setString(3, contentPkulawV1.getCaseType());
                preparedStatement.setString(4, contentPkulawV1.getCaseCode());
                preparedStatement.setString(5, contentPkulawV1.getInstrumentType());
                preparedStatement.setString(6, contentPkulawV1.getClosingDate());
                preparedStatement.setString(7, contentPkulawV1.getTrialCourt());
                preparedStatement.setString(8, contentPkulawV1.getTrialProcedure());
                preparedStatement.setString(9, contentPkulawV1.getCasePlot());
                preparedStatement.setString(10, contentPkulawV1.getJudgmentResult());
                preparedStatement.setString(11, contentPkulawV1.getJudgingCrime());
                preparedStatement.setString(12, contentPkulawV1.getPenalty());
                preparedStatement.setString(13, contentPkulawV1.getTrialJudge());
                preparedStatement.setString(14, contentPkulawV1.getReleaseDate());
                preparedStatement.setString(15, contentPkulawV1.getCaseCharacteristics());
                preparedStatement.setString(16, contentPkulawV1.getAdjudicationOrg());
                preparedStatement.setString(17, contentPkulawV1.getAdjudicationDate());
                preparedStatement.setString(18, contentPkulawV1.getFullText());
                preparedStatement.setString(19, contentPkulawV1.getSourceUrl());
                preparedStatement.setString(20, contentPkulawV1.getCaseDb());
                preparedStatement.setLong(21, contentPkulawV1.getId());
            }

            @Override
            public int getBatchSize() {
                return contentPkulaw.size();
            }
        });
        int success = 0;
        for (int i : result) {
            if (i == 1) {
                success++;
            }
        }
        return success;
    }

    public synchronized int[] insertMottoList(List<JSONObject> list) {
        String sql = "insert into " + ContentTable + " (type_one,type_two,content,sequence,release_time,create_time) value (?,?,?,'0',?,SYSDATE());";
        int[] result = jdbcConnection.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                JSONObject jsonObject = list.get(i);
                log.info("mysql插入:{}", jsonObject.getString("content"));
                preparedStatement.setString(1, jsonObject.getString("type_one"));
                preparedStatement.setString(2, jsonObject.getString("type_two"));
                preparedStatement.setString(3, jsonObject.getString("content"));
                preparedStatement.setDate(4, new java.sql.Date(jsonObject.getDate("release_time").getTime()));
            }

            @Override
            public int getBatchSize() {
                return list.size();
            }
        });
        return result;
    }

    public int ReturnRows() {
        String sql = "select table_rows from information_schema.tables where table_schema='" + LibraryName + "' AND table_name = '" + ContentTable + "';";
        try {
            return jdbcConnection.getJdbcTemplate().queryForObject(sql, Integer.class);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 单个插入html，并返回id
     *
     * @param html
     * @param updateTime
     * @return
     */
    public int insertAppleHtml(String html, String updateTime) {
        final String sql = "INSERT INTO " + ContentTable + " (html,update_time) VALUES (?,?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcConnection.getJdbcTemplate().update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, html);
                ps.setString(2, updateTime);
                return ps;
            }
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public String getAppleNowHtml(String id) {
        String sql = "SELECT html FROM " + ContentTable + " WHERE id=" + id + ";";
        try {
            return jdbcConnection.getJdbcTemplate().queryForObject(sql, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    public Integer sqlGetCount() {
        String sql = "select count(*) from " + ContentTable + "; ";
        try {
            return jdbcConnection.getJdbcTemplate().queryForObject(sql, Integer.class);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
