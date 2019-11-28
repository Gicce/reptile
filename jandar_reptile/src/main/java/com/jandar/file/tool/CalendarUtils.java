package com.jandar.file.tool;

import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;


@Controller
public class CalendarUtils {

    /**
     * 获取昨天起始时间
     *
     * @return
     */
    public String getStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

    /**
     * 获取昨天结束时间
     *
     * @return
     */
    public String getEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public String getEnDate() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }

    /**
     * 获取月和日
     *
     * @return
     */
    public String getMMDD() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1 + "" + calendar.get(Calendar.DAY_OF_MONTH);
    }
}
