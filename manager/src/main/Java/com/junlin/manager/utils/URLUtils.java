package com.junlin.manager.utils;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by junlinhui eight on 2017/3/1.
 * url
 */
public class URLUtils {

    //log对象
    private static Logger logger = Logger.getLogger(URLUtils.class);

    /**
     * 获取重定向之后的网址信息
     *
     * @param url
     * @return
     */
    public static String getWxRedirectInfo(String url) {
        String resultUrl = url;
        String finallyUrl = url + "&pass_ticket=qMx7ntinAtmqhVn+C23mCuwc9ZRyUp20kIusGgbFLi0=&uin=MTc1MDA1NjU1&ascene=1";
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(finallyUrl);
        try {
            //将HttpContext对象作为参数传给execute()方法,则HttpClient会把请求响应交互过程中的状态信息存储在HttpContext中
            HttpResponse response = httpClient.execute(httpGet, httpContext);
            //获取重定向之后的主机地址信息
            HttpHost targetHost = (HttpHost) httpContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            //获取实际的请求对象的URI,即重定向之后的地址
            HttpUriRequest realRequest = (HttpUriRequest) httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
            resultUrl = targetHost.getHostName() + realRequest.getURI();
        } catch (Exception e) {
            logger.error("获取文章真实链接出错");
            e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return resultUrl;
    }

    /**
     * @param URL 带分隔的url参数
     * @return
     */
    public static Map<String, String> getURLParameter(String URL) {
        URL = URL.substring(URL.indexOf("?") + 1, URL.length()).replace("&amp;", "&");
        Map<String, String> map = new HashMap<>();
        String[] param = URL.split("&");
        for (String keyvalue : param) {
            String key = keyvalue.substring(0, keyvalue.indexOf("="));
            String value = keyvalue.substring(keyvalue.indexOf("=") + 1, keyvalue.length());
            map.put(key, value);
        }
        return map;
    }

    /////////////////////////////////////////////////////////////////

    public static void main(String args[]) {

        logger.info("sn:" + getURLParameter("http://mp.weixin.qq.com/s?__biz=MzAwMjE1NjcxMg==&amp;mid=2654652060&amp;idx=5&amp;" +
                "sn=fea06990fb0cbab64318fde145accb28&amp;chksm=8100d75bb6775e4d4dcaaa2c9f6a16ae6a4f0a6302d8751ed33d85bd091f72cc37c833b596f6&amp;" +
                "scene=4#wechat_redirect").get("sn"));
    }
}
