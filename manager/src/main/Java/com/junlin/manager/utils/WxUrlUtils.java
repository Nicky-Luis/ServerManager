package com.junlin.manager.utils;

import java.util.Map;

/**
 * Created by junlinhui eight on 2017/3/3.
 * 历史url解析
 */
public class WxUrlUtils {

    /**
     * 获取下一页地址,当前页为第一页
     *
     * @param firstPageUrl
     * @return String
     */
    public static String getNextHistoryUrlFromFirst(String firstPageUrl) {
        Map<String, String> parameterMap = URLUtils.getURLParameter(firstPageUrl);
        return "/mp/getmasssendmsg?" +
                "__biz=" + parameterMap.get("__biz") +
                "&uin=" + parameterMap.get("uin") +
                "&key=" + parameterMap.get("key") +
                "&f=json" +
                "&frommsgid=1000010220" +
                "&count=10" +
                "&uin=MTM0OTMyNTk4" +
                "&key=" + parameterMap.get("key") +
                "&pass_ticket=" + parameterMap.get("pass_ticket") +
                "&wxtoken=" +
                "&x5=0" +
                "&f=json";
    }

    /**
     * 获取下一页地址
     *
     * @param pageUrl
     * @return String
     */
    public static String getNextHistoryUrl(String pageUrl) {
        Map<String, String> parameterMap = URLUtils.getURLParameter(pageUrl);
        return "/mp/getmasssendmsg?" +
                "__biz=" + parameterMap.get("__biz") +
                "&uin=" + parameterMap.get("uin") +
                "&key=" + parameterMap.get("key") +
                "&f=json" +
                "&frommsgid=" + (Integer.valueOf(parameterMap.get("frommsgid")) - 10) +
                "&count=10" +
                "&uin=MTM0OTMyNTk4" +
                "&key=" + parameterMap.get("key") +
                "&pass_ticket=" + parameterMap.get("pass_ticket") +
                "&wxtoken=" +
                "&x5=0" +
                "&f=json";
    }

}
