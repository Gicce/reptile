package com.jandar.file.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @description: okhttp
 * @author: Mr.Gao
 * @create: 2019-10-17 09:45
 **/
@Slf4j
@Service
public class OkHttpUtils {

    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();


    public String getPTHomeHtml(String bfdate, String efdate) {
        String url = "http://143.80.1.92:8267/SearchResult.aspx?k=0";
        try {
            RequestBody body = new FormBody.Builder()
                    .add("ckbchoose", "1")
                    .add("rdoType", "1")
                    .add("SearchSort", "1")
                    .add("db", "pfnl_pt")
                    .add("menuname", "case")
                    .add("bfdate", bfdate)
                    .add("efdate", efdate)
                    .add("chkIsSummary", "1")
                    .add("WebUCSearchFrom1$check_gaojijs", "1")
                    .build();
            Request request = new Request.Builder()
                    .post(body)
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            log.error("页面请求报错: " + e.toString());
            return "";
        }
    }

    public String getHTml(int pageNo, String w) {
        String url = "http://143.80.1.92:8267/SearchResult.aspx?t=1&p=" + pageNo + "&ix=0&w=" + w;
        try {
            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            log.error("页面请求报错: " + e.toString());
            return "";
        }
    }

    public String getJxHtml(String bfdate) {
        String url = "http://143.80.1.92:8267/SearchResult.aspx?k=0";
        try {
            RequestBody body = new FormBody.Builder()
                    .add("ckbchoose", "1")
                    .add("rdoType", "1")
                    .add("SearchSort", "1")
                    .add("db", "pfnl")
                    .add("menuname", "case")
                    .add("chkIsSummary", "1")
                    .add("WebUCSearchFrom1$check_gaojijs", "1")
                    .add("bfdate", bfdate)
                    .build();
            Request request = new Request.Builder()
                    .post(body)
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            log.error("页面请求报错: " + e.toString());
            return "";
        }
    }

    public String getCjHtml() {
        String url = "http://143.80.1.92:8267/SearchResult.aspx?k=0";
        try {
            RequestBody body = new FormBody.Builder()
                    .add("ckbchoose", "1")
                    .add("rdoType", "1")
                    .add("db", "atr")
                    .add("menuname", "case")
                    .add("chkIsSummary", "1")
                    .add("WebUCSearchFrom1$check_gaojijs", "1")
                    .build();
            Request request = new Request.Builder()
                    .post(body)
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            log.error("页面请求报错: " + e.toString());
            return "";
        }
    }

    public String getBDHtml(String bfdate) {
        String url = "http://143.80.1.92:8267/SearchResult.aspx?k=0";
        try {
            RequestBody body = new FormBody.Builder()
                    .add("ckbchoose", "1")
                    .add("rdoType", "1")
                    .add("SearchSort", "1")
                    .add("db", "pal")
                    .add("menuname", "case")
                    .add("chkIsSummary", "1")
                    .add("WebUCSearchFrom1$check_gaojijs", "1")
                    .add("bfdate", bfdate)
                    .build();
            Request request = new Request.Builder()
                    .post(body)
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            log.error("页面请求报错: " + e.toString());
            return "";
        }
    }

    public String getPTHTml(int pageNo, String w) {
        String url = "http://143.80.1.92:8267/SearchResult.aspx?t=1&p=" + pageNo + "&ix=0&w=" + w;
        try {
            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            log.error("页面请求报错: " + e.toString());
            return "";
        }
    }

    /**
     * 请求数据
     *
     * @param url
     * @return
     */
    public String getHTml(String url) {
        try {
            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            log.error("页面请求报错: " + e.toString());
            return "";
        }
    }

    public String getMottoHtml(String url) throws IOException {
        Response response = null;
        try {
            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            response = call.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.body() != null ? new String(response.body().bytes(), "gb2312") : null;
    }
}
