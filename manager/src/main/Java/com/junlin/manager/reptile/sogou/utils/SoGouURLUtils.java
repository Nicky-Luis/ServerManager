package com.junlin.manager.reptile.sogou.utils;

/**
 * Created by junlinhui eight on 2017/3/24.
 *
 */
public class SoGouURLUtils {
    /**
     * 获取搜狗文章的信息链接
     *
     * @param timestamp
     * @param sogouId
     * @return
     */
    public static String getArticleMessageURL(String timestamp, String sogouId) {
        return "http://mp.weixin.qq.com/mp/getcomment?src=3&ver=1&timestamp=" + timestamp + "&signature=" + sogouId +
                "&uin=&key=&pass_ticket=&wxtoken=&devicetype=&clientversion=0&x5=0&f=json";
    }
}
