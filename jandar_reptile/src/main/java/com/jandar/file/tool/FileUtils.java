package com.jandar.file.tool;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Objects;

@Slf4j
@Controller
public class FileUtils {

    /**
     * 请求地址
     */
    @Value("${url.request}")
    private String url;

    /**
     * 文件保存地址
     */
    @Value("${file.address}")
    private String FILE_ADDRESS;

    /**
     * okhttp
     */
    OkHttpClient client = new OkHttpClient();

    @Autowired
    private CalendarUtils calendarUtils;

    private String filepath;

    /**
     * 下载文件到本地
     *
     * @param filename
     * @param data
     * @throws IOException
     */
    public void saveFile(String filename, byte[] data) {
        try {
            if (data != null) {
                filepath = FILE_ADDRESS + filename + calendarUtils.getEnDate() + ".zip";
                log.info("文件输出地址 : " + filepath);
                File file = new File(filepath);
                if (file.exists()) {
                    log.error("文件存在！重新生成");
                    file.delete();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data, 0, data.length);
                fos.flush();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * okhttp 获取文件byte
     *
     * @return
     */
    public byte[] getFileBtye() {
        String urlVlaue = url + "?startDate=" + calendarUtils.getStartDate() + "&endDate=" + calendarUtils.getEndDate() + "";
        log.info("请求参数--开始时间: " + calendarUtils.getStartDate() + " 结束时间: " + calendarUtils.getEndDate());
        Request request = new Request.Builder()
                .url(urlVlaue)
                .get()
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return Objects.requireNonNull(response.body()).bytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * web下载zip文件
     * @param startDate
     * @param response
     */
    public void downLoadZip(String startDate, HttpServletResponse response) {
        String realPath = !StringUtils.isEmpty(startDate) ?
                FILE_ADDRESS + "支付宝投诉信息" + startDate + ".zip" : filepath;
        String fileName = realPath.replace(FILE_ADDRESS, "");
        response.setHeader("content-disposition", "attachment;filename=" + fileName);
        response.setContentType("application/octet-stream; charset=UTF-8");
        try {
            InputStream in = new FileInputStream(realPath);
            int len = 0;
            byte[] buffer = new byte[1024];
            OutputStream out = response.getOutputStream();
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
