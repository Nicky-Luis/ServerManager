package com.junlin.manager.reptile.sogou.helper;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.selector.Html;

/**
 * Created by junlinhui eight on 2017/3/20.
 * 获取cookie
 */
public class SpiderHelper {

    //cookie
    private String cookie;
    //session
    private String session_token;

    //url
    private String mainURL;

    public SpiderHelper(String domain, String proxyStr) {
        this.mainURL = domain;
        String[] tmp = proxyStr.split(":");
        HttpHost proxy = new HttpHost(tmp[1].substring(2), Integer.parseInt(tmp[2]), tmp[0]);
        Site site = Site.me().setRetryTimes(3).setHttpProxy(proxy).setSleepTime(100).setTimeOut(10 * 1000).setCharset("UTF-8")
                .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");

        GPHttpClientDownloader downloader = new GPHttpClientDownloader();
        Request request = new Request(mainURL);

        this.setCookie(request, site, downloader);
        this.setParameters(request, site, downloader);
    }

    /**
     * 绑定token
     *
     * @param request
     * @param site
     * @param downloader
     */
    private void setCookie(Request request, Site site, GPHttpClientDownloader downloader) {
        CloseableHttpResponse httpResponse = downloader.downloadForResponse(request, site.toTask());

        Header headers[] = httpResponse.getHeaders("Set-Cookie");
        this.cookie = "hl=en; ";
        // this.cookie = "";
        for (int i = 0; i < headers.length; i++) {
            String tmp[] = headers[i].getValue().split(";");
            this.cookie += tmp[0] + ";";
        }
        // System.out.println("cookie: " + this.cookie);
    }


    /**
     * 绑定其他的参数
     *
     * @param request
     * @param site
     * @param downloader
     */
    private void setParameters(Request request, Site site, GPHttpClientDownloader downloader) {
        Html contentHtml = downloader.download(request, site.toTask()).getHtml();
        this.session_token = contentHtml.regex("'XSRF_TOKEN': \"(.*?)\"").toString();
    }


    /**
     * 获取cookie
     *
     * @return
     */
    public String getCookie() {
        return cookie;
    }


    /**
     * 获取session
     *
     * @return
     */
    public String getSessionToken() {
        return session_token;
    }

    /**
     * 获取mainURL
     * @return
     */
    public String getMainURL() {
        return mainURL;
    }
}




