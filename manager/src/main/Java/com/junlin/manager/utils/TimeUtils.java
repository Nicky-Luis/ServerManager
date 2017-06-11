package com.junlin.manager.utils;

import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Millisecond;
import org.ocpsoft.prettytime.units.Second;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by junlinhui eight on 2017/3/23.
 * 时间格式化
 */
public class TimeUtils {
    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的字符串
     * @param format
     * @return
     */
    public static String timeStamp2Date(Double seconds, String format) {
        if (seconds == null || seconds.equals("null")) {
            return "";
        }

        if (format == null || format.isEmpty()) format = "YY-MM-dd HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(seconds.longValue() * 1000));
    }


    /**
     * 日期格式字符串转换成时间戳
     *
     * @param date_str 字符串日期
     * @param format   如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimeStamp(String date_str, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 取得当前时间戳（精确到秒）
     *
     * @return
     */
    public static String timeStamp() {
        long time = System.currentTimeMillis();
        String t = String.valueOf(time / 1000);
        return t;
    }

    /**
     * 将UTC时间戳
     *
     * @param UTCTime
     * @return
     */
    public static long getLocalTimeFromUTC(String UTCTime) {
        if (UTCTime.contains("Z") || UTCTime.contains("z")) {
            //注意是空格+UTC
            UTCTime = UTCTime.replace("Z", " UTC");
            //注意格式化的表达式
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
            try {
                Date date = format.parse(UTCTime);
                return date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            return Long.valueOf(UTCTime);
        }
        return 0;
    }

    /**
     * 获取时间戳
     *
     * @param user_time
     * @return
     */
    public static long getTimestamp(String user_time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d = sdf.parse(user_time);
            return d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 时间格式化为显示的时间
     *
     * @param dataMap
     * @param key
     * @param o
     */
    public static void setTimePretty(Map<String, Object> dataMap, String key, Object o) {
        PrettyTime prettyTime = new PrettyTime(Locale.CHINESE);
        prettyTime.removeUnit(JustNow.class);
        prettyTime.removeUnit(Millisecond.class);
        prettyTime.removeUnit(Second.class);

        Double time = (Double) o;
        dataMap.put(key, prettyTime.format(new Date(time.longValue())).replace(" ", ""));
    }

    /**
     * 设置截止日期格式
     *
     * @param dataMap
     * @param key
     * @param o
     */
    public static void setDeadlineFormat(Map<String, Object> dataMap, String key, Object o) {
        if (null != o) {
            String time = TimeUtils.timeStamp2Date((Double) o / 1000, "YYYY/MM/dd");
            dataMap.put(key, time);
        } else {
            dataMap.put(key, "");
        }
    }

}
