package com.junlin.manager.reptile.sogou.reptile;

import org.apache.log4j.Logger;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * Created by junlinhui eight on 2017/2/28.
 * 测试
 */
public class WeChatProcess implements PageProcessor {

    //log对象
    Logger logger = Logger.getLogger(WeChatProcess.class);
    //site
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    @Override
    public void process(Page page) {
        String results = page.getRawText();//new JsonPathSelector("$").select(page.getRawText());
        logger.info("获取到的结果：" + results);
    }

    @Override
    public Site getSite() {
        return site;
    }


    public static void main(String[] args) {
        final String testUrl1="http://mp.weixin.qq.com/s?__biz=MzAwMjE1NjcxMg==&mid=2654652060&idx=1&sn=22d0793e298ee3747cd11311d0ad7e80&chksm" +
                "=8100d75bb6775e4d03f09cd98ad7223ea0905fa049353767613d5e8256c6f8d365e1986814f5&scene=4&key" +
                "=9d3c3d5e586f5d46d6d8f0398eabc02197a0ca1517f2eeb5907258042435f58bfe69849d54726ee5bd1d0bd0ca8269d4f23ba9be0cf221a90a3d7228d671f10d169b0031571d755d10b66b30903047f1&ascene=1&uin=MTM0OTMyNTk4&devicetype=android-19&version=26050434&nettype=WIFI&abtest_cookie=AQABAAgAAQBDhh4AAAA%3D&pass_ticket=Cui7ByFTQ3uKV%2B0kau4uXGkCKFQltYTFZjBRpH1q5ZQ%3D&wx_header=1";
        final String testUrl2 = "http://mp.weixin.qq.com/mp/getappmsgext?__biz=MzAwMjE1NjcxMg==&appmsg_type=9&mid=2654652060&sn=22d0793e298ee3747cd11311d0ad7e80&idx=1" +
                "&scene=4&title=%E5%A6%82%E4%BD%95%E7%94%A8%E7%89%B9%E7%A7%8D%E9%83%A8%E9%98%9F%E7%9A%84%E5%8A%9E%E6%B3%95%E6%89%BE%E5%88%B0%E4%BD" +
                "%A0%E7%9A%84%E6%96%B0%E5%AA%92%E4%BD%93%E7%89%B9%E7%A7%8D%E5%85%B5%EF%BC%9F&ct=1486645546&abtest_cookie=AQABAAgAAQBDhh4AAAA" +
                "=&devicetype=android-19&version=/mmbizwap/zh_CN/htmledition/js/appmsg/index341b97.js&f=json&r=0.3943317162338644&is_need_ad=1" +
                "&comment_id=1338201884&is_need_reward=0&both_ad=1&reward_uin_count=0&msg_daily_idx=1&uin=MTM0OTMyNTk4&key" +
                "=9d3c3d5e586f5d46d6d8f0398eabc02197a0ca1517f2eeb5907258042435f58bfe69849d54726ee5bd1d0bd0ca8269d4f23ba9be0cf221a90a3d7228d671f10d169b0031571d755d10b66b30903047f1&pass_ticket=Cui7ByFTQ3uKV%25252B0kau4uXGkCKFQltYTFZjBRpH1q5ZQ%25253D&wxtoken=66397905&devicetype=android-19&clientversion=26050434&x5=0&f=json";

        Spider spider = Spider.create(new WeChatProcess());
        //专栏信息
        spider.addUrl(testUrl1);
        //添加好友关系信息
       // spider.addUrl(testUrl2);
        spider.thread(8);
        spider.start();
    }

}
