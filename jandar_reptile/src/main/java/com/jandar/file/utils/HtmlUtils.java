package com.jandar.file.utils;

import com.alibaba.fastjson.JSONObject;
import com.jandar.file.tool.CalendarUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: Html解析
 * @author: Mr.Gao
 * @create: 2019-10-20 09:50
 **/
@Service
@Slf4j
public class HtmlUtils {
    @Autowired
    private PkulawContent content;
    @Autowired
    private OkHttpUtils okHttpUtils;
    @Autowired
    private CalendarUtils calendarUtils;
    @Autowired
    private SqlUtils sqlUtils;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IpConfiguration ipConfiguration;

    public List<String> analysisMottoHtml(String html) {
        Element motto = Jsoup.parse(html);
        Elements elements = motto.select("div#nav > ul > li");
        int index = 0;
        List<String> list = new ArrayList<>();
        for (Element item : elements) {
            if (index++ == 0) {
                continue;
            }
            list.add(content.Matcher_getString(item.html(), "(?<=href=\").*?(?=\")"));
        }
        return list;
    }

    public void analysisData(String html) {
        Element body = Jsoup.parse(html);
        List<JSONObject> list = new ArrayList<>();
        Element element = body.select("div.position").get(0);
        Matcher filter_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE).matcher(element.html());
        String filterHtml = filter_html.replaceAll("");
        Matcher filterSTRN = Pattern.compile("\\s*|\t|\n|\n|&nbsp;", Pattern.CASE_INSENSITIVE).matcher(filterHtml);
        String p_html = filterSTRN.replaceAll("");
        Matcher p_special = Pattern.compile("\\&[a-zA-Z]{1,10};", Pattern.CASE_INSENSITIVE).matcher(p_html);
        String htmlText = p_special.replaceAll("");
        //当前位置:主页励志名言查看格言：100句改变自己的励志名言
        String type_one = content.Matcher_getString(htmlText, "(?<=主页).*?(?=查看格言)");
        String type_two = content.Matcher_getString(htmlText, "(?<=查看格言：).*");
        Element time = body.select("div.info").get(0);
        String release_time = content.Matcher_getString(time.text(), "(?<=时间:).*?(?=来源)");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = null;
        try {
            date = sdf.parse(release_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Elements contents = body.select("div.content > p");
        contents.stream().filter(item -> !item.html().startsWith("&nbsp;")).forEach(item -> {
            JSONObject mottos = new JSONObject();
            mottos.put("release_time", calendar.getTime());
            mottos.put("type_one", type_one);
            mottos.put("type_two", type_two);
            mottos.put("content", content.MatcherON_getString(item.html(), "[、.^(?!0)(?:[0-9]{1,3}|1000)$]"));
            list.add(mottos);
            redisUtil.incr("successCount" + ipConfiguration.getPort(), 1);
            redisUtil.incrementScore("reptileData" + ipConfiguration.getPort(), calendarUtils.getMMDD(), 1);
        });

        sqlUtils.insertMottoList(list);
    }

    public String getUrl(String url) {
        String html = null;
        try {
            html = okHttpUtils.getMottoHtml(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element body = Jsoup.parse(html);
        Elements elements = body.select("ul.pagelist > li");
        String getHttpUrl = "";
        for (Element element : elements) {
            getHttpUrl = content.Matcher_getString(content.Matcher_getString(element.html(), "(?<=href=\").*?(?=\")"), ".*(?=.html)");
            if (StringUtils.isNotEmpty(getHttpUrl)) {
                break;
            }
        }
        getHttpUrl = getHttpUrl.substring(0, getHttpUrl.length() - 1);
        return getHttpUrl;
    }

    public boolean analyseHtml(String html, Queue<String> inSmallUrlQueue) {
        Element body = Jsoup.parse(html);
        Elements list = body.select("#p_left > div > ul:nth-child(2) > li");
        if (list.size() > 0) {
            list.forEach(item -> {
                String url = "https://www.geyanw.com" +
                        content.Matcher_getString(item.select("h2").html()
                                , "(?<=href=\").*?(?=\")");
                log.info("获取的url:{}", url);
                inSmallUrlQueue.add(url);
            });
            return true;
        } else {
            return false;
        }
    }
}
