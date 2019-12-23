package com.jandar.file.utils;

import com.alibaba.fastjson.JSONObject;
import com.jandar.file.entity.ContentPkulawV1;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.jdbc.Sql;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PkulawContent {

    @Value("${fileAddress}")
    private String fileAddress;
    @Autowired
    private SqlUtils sqlUtils;
    /**
     * 过滤html
     */
    private final String regEx_html = "<[^>]+>";
    /**
     * 过滤空格、换行符
     */
    private final String rn_html = "\\s*|\t|\r|\n|&nbsp;";
    private final String special = "\\&[a-zA-Z]{1,10};";

    /**
     * 解析url中的hmlt并插入
     *
     * @param html
     * @return
     */
    public void getUrl(String html, Queue<String> smallUrlQueue) {
        try {
            if (StringUtils.isNotBlank(html)) {
                Element doc = Jsoup.parse(html);
                Elements tables = doc.getElementsByClass("tb-list");
                if (tables.size() > 0) {
                    for (int i = 0; i < tables.size(); i++) {
                        Element element = tables.get(i);
                        String url = "http://143.80.1.92:8267/" + element.select("a").get(0).attr("href");
                        smallUrlQueue.add(url);
                    }
                }
            } else {
                log.error("html 为空", html);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<JSONObject> findList(String html, Map<String, String> map) {
        try {
            List<JSONObject> list = new ArrayList<>();
            if (StringUtils.isNotBlank(html)) {
                Element document = Jsoup.parse(html);
                String contentHtml = document.select(".qw-xg-1 > table").get(0).html();
                JSONObject json = new JSONObject();
                json.put("type", map.get("type"));
                json.put("title", map.get("title"));
                json.put("pubDate", map.get("pubDate"));
                json.put("url", map.get("url"));
                json.put("content_html", contentHtml);
                json.put("id", map.get("id"));
                json.put("case_code", map.get("caseCode"));
                list.add(json);
            } else {
                log.error("html 为空", html);
            }
            return list;
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    public JSONObject getContent(String html, String url) {
        try {
            JSONObject json = new JSONObject();
            if (StringUtils.isNotBlank(html)) {
                Element document = Jsoup.parse(html);
                String contentHtml = document.select(".qw-xg-1 > table").get(0).html();
                Matcher typs = Pattern.compile("(?<=db=).*?(?=&)").matcher(url);
                String type = null;
                while (typs.find()) {
                    type = typs.group();
                }
                Matcher ids = Pattern.compile("(?<=gid=).*").matcher(url);
                String id = null;
                while (ids.find()) {
                    id = ids.group();
                }
                json.put("content_html", contentHtml);
                json.put("type", type);
                json.put("id", id);
                json.put("url", url);
                sqlUtils.insertHtml(json);
            } else {
                log.error("html 为空 {}", html);
            }
            return json;
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }


    public ContentPkulawV1 contentPkulawV1s(JSONObject object) {
        Long id = object.getLong("id");
        String sourceUrl = object.getString("url");
        String caseDb = object.getString("type");
        ContentPkulawV1 pkulawV1 = new ContentPkulawV1();
        if (StringUtils.isNotBlank(id.toString())) {
            pkulawV1.setId(id);
        }
        if (StringUtils.isNotBlank(sourceUrl)) {
            pkulawV1.setSourceUrl(sourceUrl);
        }
        if (StringUtils.isNotBlank(caseDb)) {
            pkulawV1.setCaseDb(caseDb);
        }
        String contentHtml = object.getString("content_html");
        if (StringUtils.isNotBlank(contentHtml)) {
            Matcher filter_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE).matcher(contentHtml);
            String filterHtml = filter_html.replaceAll("");
            Matcher filterSTRN = Pattern.compile(rn_html, Pattern.CASE_INSENSITIVE).matcher(filterHtml);
            String p_html = filterSTRN.replaceAll("");
            Matcher p_special = Pattern.compile(special, Pattern.CASE_INSENSITIVE).matcher(p_html);
            String htmlText = p_special.replaceAll("");
            pkulawV1.setCaseName(Matcher_getString(htmlText, ".*(?=【案由分类)"));
            pkulawV1.setCaseCode(Matcher_getString(htmlText, "(?<=案件字号】).*?(?=【)"));
            pkulawV1.setClosingDate(Matcher_getString(htmlText, "(?<=审结日期】).*?(?=【)"));
            pkulawV1.setCaseType(Matcher_getString(htmlText, "(?<=案由分类】).*?(?=【)"));
            pkulawV1.setInstrumentType(Matcher_getString(htmlText, "(?<=文书类型】).*?(?=【)"));
            pkulawV1.setTrialCourt(Matcher_getString(htmlText, "(?<=审理法院】).*?(?=【)"));
            pkulawV1.setTrialProcedure(Matcher_getString(htmlText, "(?<=审理程序】).*?(?=【)"));
            pkulawV1.setCasePlot(Matcher_getString(htmlText, "(?<=案件情节】).*?(?=【)"));
            pkulawV1.setJudgmentResult(Matcher_getString(htmlText, "(?<=判决结果】).*?(?=【)"));
            pkulawV1.setJudgingCrime(Matcher_getString(htmlText, "(?<=判定罪名】).*?(?=【)"));
            pkulawV1.setPenalty(Matcher_getString(htmlText, "(?<=刑罚】).*?(?=【)"));
            pkulawV1.setTrialJudge(Matcher_getString(htmlText, "(?<=审理法官】).*?(?=【)"));
            pkulawV1.setReleaseDate(Matcher_getString(htmlText, "(?<=发布日期】).*?(?=【)"));
            pkulawV1.setCaseCharacteristics(Matcher_getString(htmlText, "(?<=案例特征】).*?(?=【)"));
            pkulawV1.setAdjudicationOrg(Matcher_getString(htmlText, "(?<=裁决机构】).*?(?=【)"));
            pkulawV1.setAdjudicationDate(Matcher_getString(htmlText, "(?<=裁决日期】).*?(?=【)"));
            pkulawV1.setFullText(Matcher_getString(htmlText, "(?<=全文】).*"));
        }
        return pkulawV1;
    }

    public void writeToText(String musicInfo, String dateTime) throws IOException {
        String path = fileAddress + dateTime + "page.txt";
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(musicInfo);
        bw.flush();
        bw.close();
        fw.close();
    }

    public String Matcher_getString(String html, String regex) {
        Matcher value = Pattern.compile(regex).matcher(html);
        String data = "";
        while (value.find()) {
            if (StringUtils.isNotBlank(value.group())) {
                data = value.group();
            }
        }
        return data;
    }

    public String MatcherON_getString(String html, String regex) {
        Matcher value = Pattern.compile(regex).matcher(html);
        return value.replaceAll("").trim();
    }
}
