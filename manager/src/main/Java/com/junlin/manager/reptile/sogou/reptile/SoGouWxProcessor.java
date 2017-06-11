package com.junlin.manager.reptile.sogou.reptile;

import com.junlin.manager.reptile.sogou.ReptileConstants;
import com.junlin.manager.reptile.sogou.service.ReptileProcessService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;

import java.util.List;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.JsonPathSelector;
import us.codecraft.webmagic.selector.Selectable;

/**
 * Created by junlinhui eight on 2017/3/1.
 * 搜狗微信爬虫
 */
public class SoGouWxProcessor implements PageProcessor {

    //log对象
    private Logger logger = Logger.getLogger(SoGouWxProcessor.class);
    //site
    private Site site;

    /***
     * 构造，
     * @param proxyStr 代理
     * @param cookie cookie
     */
    public SoGouWxProcessor(String proxyStr, String cookie) {
        String[] tmp = proxyStr.split(":");
        HttpHost proxy = new HttpHost(tmp[1].substring(2), Integer.parseInt(tmp[2]), tmp[0]);

        this.site = Site.me().setRetryTimes(3).setHttpProxy(proxy).addHeader("Cookie", cookie).setSleepTime(100).setTimeOut(10 * 1000).setCharset
                ("UTF-8").setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 " +
                "Safari/537.36");
    }

    @Override
    public void process(Page page) {
        if (page.getUrl().regex(ReptileConstants.SOGOU_URL).match()) {
            logger.info("\n--------------------通过搜狗抓取文章列表----------------------------\n");
            getArticleList(page);
        } else if (page.getUrl().regex(ReptileConstants.COMMENT_URL).match()) {
            logger.info("\n--------------------通过文章获取评论点赞信息----------------------------\n");
            getArticleMessage(page);
        } else if (page.getUrl().regex(ReptileConstants.WX_ARTICLE_URL).match()) {
            logger.info("\n--------------------获取微信文章内容信息----------------------------\n");
            getArticleProfile(page);
        } else {
            logger.info("未匹配到链接");
        }
    }

    @Override
    public Site getSite() {
        //return ReptileConstants.SOGOU_WECHAT_PAGE;
        return site;
    }
    ////////////////////////////////////////////////////////////////////

    /**
     * 获取最新的链接
     *
     * @param page
     */

    private void getArticleList(Page page) {
        try {
            Html pageHtml = page.getHtml();
            //获取所有的文章链接
            List<Selectable> targetSelectableList = pageHtml.css("ul.news-list").css("li").nodes();
            logger.info("搜索到文章数为：" + targetSelectableList.size());
            if (CollectionUtils.isNotEmpty(targetSelectableList)) {
                for (Selectable targetNode : targetSelectableList) {
                    ReptileProcessService.analysisSoGouContent(page, targetNode);
                }
            } else {
                logger.error("没有搜索到相关的文章：\n" + page.getHtml());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.error("页面信息：" + page.getHtml());
        } finally {
            page.setSkip(true);
        }
    }


    /**
     * 获取评论内容
     *
     * @param page
     */
    private void getArticleMessage(Page page) {
        try {
            if (new JsonPathSelector("$.base_resp.ret").select(page.getRawText()).equals("-1")) {
                logger.error("获取点赞数据失败");
            } else {
                String likeNum = new JsonPathSelector("$.like_num").select(page.getRawText());
                String readNum = new JsonPathSelector("$.read_num").select(page.getRawText());
                String commentNum = new JsonPathSelector("$.elected_comment_total_cnt").select(page.getRawText());
                logger.info("\n点赞数：" + likeNum + "\n阅读数:" + readNum + "\n评论数：" + commentNum);

                //获取signature
                String soGouId = page.getUrl().regex("&signature=.*&uin=").replace("&signature=", "").replace("&uin=", "").toString();
                String signature = ReptileProcessService.getSignatureBySoGouId(soGouId);
                ReptileProcessService.updateArticleMessage(signature, readNum, likeNum, commentNum);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("错误的信息：" + e.toString());
        } finally {
            page.setSkip(true);
        }
    }

    /**
     * 获取微信文章信息
     *
     * @param page
     */
    private void getArticleProfile(Page page) {
        try {
            String articleTitle = page.getHtml().css("title").replace("<title>", "").replace("</title>", "").get();
            String weChatName = page.getHtml().css("strong.profile_nickname").replace("<strong class=\"profile_nickname\">", "").replace
                    ("</strong>", "").get();
            String weChatId = page.getHtml().css("span.profile_meta_value").replace("<span class=\"profile_meta_value\">", "").replace("</span>",
                    "").regex("^[a-zA-Z\\d_-]*$").get();
            String content = page.getHtml().css("div.rich_media_content").replace("</?[^>]+>", "").replace("&nbsp;", "")
                    .replace(" ", "").replace("\\n", "\n").replace("&gt;", "").get();
            int wordCount = content.length();
            //获取signature
            String signature = ReptileProcessService.getSignatureBySoGouId(page.getUrl().regex("&signature=.*").replace("&signature=", "").toString
                    ());
            //更新数据
            ReptileProcessService.updateArticleProfile(signature, articleTitle, wordCount, content);
            ReptileProcessService.updateOfficeWxInfo(weChatName, weChatName, weChatId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("错误的信息：" + e.toString());
        } finally {
            page.setSkip(true);
        }
    }

    public static void main(String args[]) {
        String testUrl = "http://weixin.sogou.com/weixin?type=2&ie=utf8&query=%E4%BA%86&tsn=1&ft=null&et=null&interation=null&wxid" +
                "=oIWsFt3CUA6HniQM4e_i7zncqWkk&usip=%E6%96%B0%E6%A6%9C&from=tool";
        //Spider.create(new SoGouWxProcessor()).addUrl(testUrl).thread(5).run();
    }

}
