package com.junlin.manager.reptile.sogou.service;

import com.junlin.manager.reptile.sogou.entity.SoGouArticle;
import com.junlin.manager.reptile.sogou.entity.SoGouArticleMessage;
import com.junlin.manager.reptile.sogou.entity.SoGouArticleProfile;
import com.junlin.manager.reptile.sogou.entity.SoGouOfficeWxInfo;
import com.junlin.manager.reptile.sogou.entity.SoGouSignature;
import com.junlin.manager.reptile.sogou.utils.SoGouURLUtils;
import com.junlin.manager.utils.URLUtils;

import org.apache.log4j.Logger;

import java.text.DecimalFormat;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Selectable;

import static com.junlin.manager.GlobalConstants.SoGou_Article_Message_Table;
import static com.junlin.manager.GlobalConstants.SoGou_Article_Profile_Table;
import static com.junlin.manager.GlobalConstants.SoGou_Office_Wx_Table;
import static com.junlin.manager.GlobalConstants.SoGou_Wx_Signature_Table;

/**
 * Created by junlinhui eight on 2017/3/24.
 */
public class ReptileProcessService {

    //log对象
    private static Logger logger = Logger.getLogger(ReptileProcessService.class);

    /**
     * 通过搜狗id获取文章标示
     *
     * @param soGouId
     * @return
     */
    public static String getSignatureBySoGouId(String soGouId) {
        SoGouSignature signatureInfo = SoGouSignature.dao.findFirst("select * from " + SoGou_Wx_Signature_Table +
                " where sogou_id=\'" + soGouId + "\'");
        if (null == signatureInfo) {
            return "";
        }
        return signatureInfo.get("signature");
    }

    /**
     * 解析搜狗页面信息
     *
     * @param page
     * @param targetNode
     */
    public static void analysisSoGouContent(Page page, Selectable targetNode) throws Exception {
        String articleURL = targetNode.css("div.txt-box").links().regex("http://mp\\.weixin\\.qq\\.com/s\\?.*").get();
        //获取文章的真实的链接地址
        String realURL = URLUtils.getWxRedirectInfo(articleURL);
        String signature = URLUtils.getURLParameter(realURL).get("sn");
        if (!ReptileProcessService.isArticleExist(signature)) {
            page.addTargetRequest(articleURL);
            logger.info("***添加文章链接：" + articleURL);
        } else {
            logger.info("***文章已经存在");
        }
        String soGouId = articleURL.substring(articleURL.indexOf("&signature=")).replaceAll("&signature=", "");
        //添加评论点赞链接
        if (!isArticleMessageExist(signature)) {
            String timestamp = URLUtils.getURLParameter(articleURL).get("timestamp");
            String commentURL = SoGouURLUtils.getArticleMessageURL(timestamp, soGouId);
            logger.info("***添加评论链接：" + commentURL);
            page.addTargetRequest(commentURL);
        } else {
            logger.info("***文章评论信息已经存在");
        }

        //保存Signature
        saveSoGouSignature(soGouId, signature, articleURL);
        //关键字
        String keyWord = URLUtils.getURLParameter(page.getUrl().get()).get("query");
        ReptileProcessService.saveSoGouContent(targetNode, keyWord, articleURL, realURL, signature);
    }

    /**
     * 保存signature信息
     *
     * @param sogou_id
     * @param signature
     * @param url
     */
    private static void saveSoGouSignature(String sogou_id, String signature, String url) {
        SoGouSignature signatureInfo = SoGouSignature.dao.findFirst("select * from " + SoGou_Wx_Signature_Table +
                " where sogou_id=\'" + sogou_id + "\'");
        if (null == signatureInfo) {
            new SoGouSignature()
                    .set("sogou_id", sogou_id)
                    .set("signature", signature)
                    .set("url", url)
                    .set("created_time", System.currentTimeMillis())
                    .set("updated_time", System.currentTimeMillis())
                    .save();
        } else {
            logger.info("signatureInfo信息已经存在");
        }
    }

    /***
     * 保存搜狗文章数据
     * @param targetNode
     * @param articleURL
     * @param realURL
     * @param signature
     */
    private static void saveSoGouContent(Selectable targetNode, String keyWord, String articleURL, String realURL, String signature) {
        if (ReptileProcessService.isArticleExist(signature)) {
            logger.info("文章信息已经存在");
            return;
        }
        //获取文章信息
        Selectable textBody = targetNode.css("div.txt-box");
        String pushTime = textBody.regex("timeConvert.{12}").get().replace("timeConvert('", "");
        String wxName = textBody.css("div.s-p").css("a").regex(">.*<").replace(">", "").replace("<", "").get();
        String biz = URLUtils.getURLParameter(realURL).get("__biz");

        //保存文章信息
        new SoGouArticleProfile()
                .set("signature", signature)
                .set("key_word", keyWord)
                .set("biz", biz)
                .set("push_time", Double.valueOf(pushTime))
                .set("url", realURL)
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();

        logger.info("signature:" + signature + "\n发布时间：" + pushTime + "\n文章链接:" + articleURL + "\n公众号:" + wxName);
        if (ReptileProcessService.isOfficeWxExist(biz)) {
            logger.info("公众号信息已经存在");
            return;
        }
        //保存公众号信息
        new SoGouOfficeWxInfo().set("wechat_name", wxName).set("biz", biz).save();
    }

    /**
     * 判断文章是否存在
     *
     * @param signature
     * @return
     */
    private static boolean isArticleExist(String signature) {
        SoGouArticle articleInfo = SoGouArticle.dao.findFirst("select * from " + SoGou_Article_Profile_Table +
                " where signature=\'" + signature + "\'");
        return null != articleInfo;
    }

    /**
     * 判断文章附加信息是否存在
     *
     * @param signature
     * @return
     */
    private static boolean isArticleMessageExist(String signature) {
        SoGouArticleMessage message = SoGouArticleMessage.dao.findFirst("select * from " + SoGou_Article_Message_Table +
                " where signature=\'" + signature + "\'");
        if (null != message) {
            double updatedTime = message.getDouble("updated_time");
            if ((System.currentTimeMillis() - updatedTime) <= 30 * 1000) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取到搜狗文章
     *
     * @param signature
     * @return
     */

    public static void updateArticleProfile(String signature, String articleTitle, int wordCount, String content) {
        SoGouArticleProfile article = SoGouArticleProfile.dao.findFirst("select * from " + SoGou_Article_Profile_Table +
                " where signature=\'" + signature + "\'");
        if (null == article) {
            logger.info("文章信息不存在，保存文章信息失败");
            return;
        }
        //暴力去掉一些4位编码的非法字符
        articleTitle = articleTitle.replaceAll("[\\x{10000}-\\x{10FFFF}]", "");
        content = content.replaceAll("[\\x{10000}-\\x{10FFFF}]", "");
        //当前时间的时间戳
        String currentTime = String.valueOf(System.currentTimeMillis());
        article.set("updated_time", currentTime)
                .set("title", articleTitle)
                .set("word_count", wordCount)
                .set("content", content).update();
    }

    /***
     * 判断公众号信息是否存在
     * @param biz
     * @return
     */
    private static boolean isOfficeWxExist(String biz) {
        //保存公众号信息
        SoGouOfficeWxInfo soGouPublicWxInfo = SoGouOfficeWxInfo.dao.findFirst("select * from " + SoGou_Office_Wx_Table + " " +
                "where biz=\'" + biz + "\'");
        return null != soGouPublicWxInfo;
    }

    /***
     * 更新公众号信息
     * @param name
     * @param weChatName
     * @param weChatId
     */
    public static void updateOfficeWxInfo(String name, String weChatName, String weChatId) {
        //保存公众号信息
        SoGouOfficeWxInfo soGouPublicWxInfo = SoGouOfficeWxInfo.dao.findFirst("select * from " + SoGou_Office_Wx_Table + " where wechat_name=\'" +
                name + "\'");
        if (null != soGouPublicWxInfo) {
            soGouPublicWxInfo.set("wechat_name", weChatName).set("wechat_id", weChatId).update();
        } else {
            logger.info("公众号信息不存在，保存文章信息失败");
        }
    }

    /***
     * 更新文章附加信息
     * @param signature
     * @param readNum
     * @param likeNum
     * @param commentNum
     */
    public static void updateArticleMessage(String signature, String readNum, String likeNum, String commentNum) {
        if (null == signature || signature.length() <= 2) {
            logger.error("signature 信息错误，保存文章附加信息失败");
            return;
        }
        try {
            //文章不存在都会保存相关的评论信息
            if (!isArticleMessageExist(signature)) {
                new SoGouArticleMessage()
                        .set("signature", signature)
                        .set("read_num", readNum)
                        .set("like_num", likeNum)
                        .set("comment_num", commentNum)
                        .set("re_index", getReIndex(readNum, likeNum))
                        .set("created_time", System.currentTimeMillis())
                        .set("updated_time", System.currentTimeMillis())
                        .save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 获取爆文指数
     * @param readNum
     * @param likeNum
     * @return
     */
    private static String getReIndex(String readNum, String likeNum) {
        String reIndex = "0";
        try {
            float value = 0;
            if (0 != Integer.valueOf(readNum)) {
                value = Float.valueOf(Integer.valueOf(likeNum)) / Integer.valueOf(readNum);
            }
            //格式化小数，不足的补0
            DecimalFormat df = new DecimalFormat("0.0000");
            reIndex = df.format(value * 100);
            logger.info("数据为：" + reIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reIndex;
    }

    public static void main(String args[]) {
        logger.info("" + getReIndex("237", "7"));
    }
}
