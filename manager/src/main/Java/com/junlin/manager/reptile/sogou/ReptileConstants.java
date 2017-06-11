package com.junlin.manager.reptile.sogou;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;

import us.codecraft.webmagic.Site;

/**
 * Created by junlinhui eight on 2017/2/24.
 * 常量
 */
public class ReptileConstants {
    //log对象
    Logger logger = Logger.getLogger(ReptileConstants.class);
    //雪球site
    public final static Site SNOWBALL_SITE = new Site().setRetryTimes(3).setSleepTime(100)
            //添加cookie之前一定要先设置主机地址，否则cookie信息不生效
            .setDomain("www.xueqiu.com")
            //添加抓包获取的cookie信息
            .addCookie("Hm_lpvt_1db88642e346389874251b5a1eded6e3", "1488444254")
            .addCookie("Hm_lvt_1db88642e346389874251b5a1eded6e3", "1487903089,1488158753,1488332139,1488444254")
            .addCookie("__utma", "1.1514813053.1487741169.1488368311.1488442384.25")
            .addCookie("__utmb", "1.5.10.1488442384")
            .addCookie("__utmc", "1")
            .addCookie("__utmt", "1")
            .addCookie("__utmz", "1.1487741169.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)")
            .addCookie("aliyungf_tc", "AQAAAMoE4xDvSgsAq6uDd7")
            .addCookie("s", "5r11tblb8u")
            .addCookie("u", "201487741077649")
            .addCookie("u.sig", "tTWDaXK_i7T8ycbilkgxpHN9hSE")
            .addCookie("webp", "0")
            .addCookie("xq_a_token", "0febfb72c2a426b55220a7ff3bc6ed1eb82b0801")
            .addCookie("xq_r_token", "f9a3b2e0ac8f2fe9661e392afd59795642a68605")
            //添加请求头，有些网站会根据请求头判断该请求是由浏览器发起还是由爬虫发起的
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, sdch, br")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("Connection", "keep-alive")
            .addHeader("Cookie", "u=201487741077649; u.sig=tTWDaXK_i7T8ycbilkgxpHN9hSE; s=5r11tblb8u; webp=0; " +
                    "aliyungf_tc=AQAAAMoE4xDvSgsAq6uDd7/EVsC/tVu/; xq_a_token=0febfb72c2a426b55220a7ff3bc6ed1eb82b0801; " +
                    "xq_r_token=f9a3b2e0ac8f2fe9661e392afd59795642a68605; __utmt=1; __utma=1.1514813053.1487741169.1488368311.1488442384.25; " +
                    "__utmb=1.5.10.1488442384; __utmc=1; __utmz=1.1487741169.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); " +
                    "Hm_lvt_1db88642e346389874251b5a1eded6e3=1487903089,1488158753,1488332139,1488444254; " +
                    "Hm_lpvt_1db88642e346389874251b5a1eded6e3=1488444254")
            .addHeader("Host", "xueqiu.com")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

    //专栏链接：//xueqiu.com/1722979527/column?page=1#2
    public static final String COLUMN_PAGE_RESURLT = ".*/.*/column\\?page=.*";
    //专栏链接：https://xueqiu.com/1722979527/column?page=1
    public static final String COLUMN_RESURLT = ".*/\\d*/\\d*";
    //专栏链接：https://xueqiu.com/1722979527/column?page=1
    public static final String COLUMN_URL = "https://xueqiu\\.com/.*/column\\?page=.*";
    //文章链接：https://xueqiu.com/statuses/show.json?id=81006469
    public static final String ARITICLE_URL = "https://xueqiu\\.com/statuses/show\\.json\\?id=.*";
    //用户主页：https://xueqiu.com/cubes/list.json?user_id=1722979527&_=1487829045962
    public static final String USER_URL = "https://xueqiu\\.com/cubes/list\\.json\\?user_id=.*";
    //朋友关系: https://xueqiu.com/friendships/groups/members.json?page=1&uid=1722979527&gid=0&_=1487813064939
    public static final String FRIEND_URL = "https://xueqiu\\.com/friendships/groups/members\\.json\\?page=.*";
    //股票数据:https://xueqiu.com/stock/cata/stocklist.json?page=1&size=10&order=desc&orderby=percent&type=11%2C12&_=1488340605459
    public static final String STOCK_URL = "https://xueqiu\\.com/stock/cata/stocklist\\.json\\?page=.*";

    //////////////////////////////////////搜狗微信公众号相关部分///////////////////////////////////////////////////////////////
    //搜狗微信页面
    public static final Site SOGOU_WECHAT_PAGE1 = Site.me()
            .setRetryTimes(3)
            .setSleepTime(1000)
            .setHttpProxy(new HttpHost("117.94.201.16", 46860))
            //.setHttpProxyPool(new JunLinProxyPool())
            .setTimeOut(10000);

    public static final Site SOGOU_WECHAT_PAGE = new Site()
            .setHttpProxy(new HttpHost("117.94.201.16", 46860))
            // .setUsernamePasswordCredentials(new UsernamePasswordCredentials("",""))
            .setRetryTimes(3)
            .setSleepTime(100)
            //添加cookie之前一定要先设置主机地址，否则cookie信息不生效
            .setDomain("weixin.sogou.com")
            //添加抓包获取的cookie信息
            .addCookie("ABTEST", "1488188471")
            .addCookie("CXID", "B1DC081C637D10ADA5F21903D1709573")
            .addCookie("IPLOC", "CN4401")
            .addCookie("JSESSIONID", "aaaeas4LSAu_3gsVLooQv")
            .addCookie("SMYUV", "1487727315343812")
            .addCookie("SNUID", "4D4C6491E6E3AF5B6129F9F1E74E6116")
            .addCookie("SUID", "62C783774C238B0A58AD488A0005E907")
            .addCookie("SUV", "1487727315342884")
            .addCookie("ad", "tyllllllll2YrUA3lllllVAT6i6lllllNc54Ykllllwllllllklll5")
            .addCookie("sct", "44")
            .addCookie("weixinIndexVisited", "1")
            .addCookie("noticeLoginFlag", "1")
            .addCookie("pgv_pvi", "7939663872")
            .addCookie("pgv_pvid", "9454691300")
            .addCookie("pgv_si", "s965158912")
            .addCookie("remember_acct", "junlin_1980")
            //添加请求头，有些网站会根据请求头判断该请求是由浏览器发起还是由爬虫发起的
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Accept-Encoding", "gzip, deflate, sdch, br")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("Connection", "keep-alive")
//            .addHeader("Cookie", "SUV=1487727315342884; SMYUV=1487727315343812; CXID=B1DC081C637D10ADA5F21903D1709573; ABTEST=0|1488188471|v1; " +
//                    "weixinIndexVisited=1; SNUID=5852788CFBFEB12BB05C9B0BFB14BF85; pgv_pvi=7081949184; JSESSIONID=aaaHwMKEO9QGAc_12mwRv; " +
//                    "PHPSESSID=04lcqhpe0j943uqepbihl47vb6; SUIR=5852788CFBFEB12BB05C9B0BFB14BF85; sw_uuid=738885406; sg_uuid=9413765979; " +
//                    "ssuid=8061727820; ad=0lllllllll2YrUA3lllllV0aI3ylllllNc54Ykllllclllll9ylll5@@@@@@@@@@; SUID=62C783774C238B0A58AD488A0005E907; " +
//                    "pgv_si=s9644068864; IPLOC=CN3212; sct=115")
            //.addHeader("Host", "mp.weixin.qq.com")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");


    //狗搜索页面:http://weixin.sogou.com/weixin?type=2&ie=utf8&query=格力电器+SZ000651&tsn=1&ft=&et=&interation=null&wxid=&usip=null&from=tool
    public static final String SOGOU_URL = "http://weixin\\.sogou\\.com/weixin.*";
    //微信文章页面:http://mp.weixin.qq.com/s?src=3&timestamp=1488356225&ver=1&signature=2I1VzFIrnvTQBPW7e8T2OawfxUh23pkNJP-bjnpJFF7i8wwYKTzg9VfjWwyQhTYYu
    // *9rfs88elzZr2FvYBdvakaveUxmDMemzsbPTLP7SRRqn8QRjUR*mPBu5Tjc2FYUHv8CGqlGzCmNiKSRfxv6geIiT8T0ccuWxJ-7TPsk=
    public static final String WX_ARTICLE_URL = "http://mp\\.weixin\\.qq\\.com/s\\?src=.*";
    //文章评论页面:http://mp.weixin.qq.com/mp/getcomment?src=3&ver=1&timestamp=1488355468&signature=-DNMWEh5wbTxH98WwPy3IbPF*S1EY
    // *KKQHhKUwg4dRabMXF5in4QoMqNjT9WPcgB79kyuTEA8Y71i-oWrGRXHrrf3cCpO1zYXjAb0vScbYiI41O2fped20ILf4vn8visy62QpM
    // -F0XfP0znyscjKI6M9sxWO0yr3vwBXqlQHcTU%3D&&uin=&key=&pass_ticket=&wxtoken=&devicetype=&clientversion=0&x5=0&f=json
    public static final String COMMENT_URL = "http://mp\\.weixin\\.qq\\.com/mp/getcomment\\?src=.*";
    //文章点赞信息：http://weixin.sogou.com//websearch/weixin/pc/anti_article
    // .jsp?t=1489646822138&signature=Tnk5SysXZIaeFRqxRVq-UTBYAgXSRI3yoWB5wKhMQCX1VbIfidB5s5q8UDkVlgRJt1aUfoiDJL4L78TMY14jXH3JaMjsr*byCkNf
    // *ZkoUh2mjlA2E5lZGDcVUR283r1kZa8WB4T*oeGMAe6i2eR7lJdGLV6DLxzBID90UJFk4IMMWnm4HViQhX*PPC
    // -Gy1OG5pcvNmyUlrlNaQEMhM9erlYttsKriVMXMbJNJjJ3JmbE5iJMG1-UgpEKBm49Kuwh
    public static final String ARTICLE_MESSAGE_URL = "http://weixin\\.sogou\\.com//websearch/weixin/pc/anti_article\\.jsp\\?.*";
}
