package com.junlin.manager.reptile.wechat.controller;

import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.junlin.manager.utils.URLUtils;
import com.junlin.manager.utils.WxDataUtils;
import com.junlin.manager.reptile.wechat.entity.WxArticleMessage;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.junlin.manager.GlobalConstants.gNextHistoryQueue;

/**
 * Created by junlinhui eight on 2017/2/28.
 * 这个程序负责接收历史消息的json并解析后存入数据库
 */
public class WxController extends Controller {

    //logger
    private static Logger logger = Logger.getLogger(WxController.class.getName());
    //当前的历史链接
    private static String mCurrentHistoryURL = "";
    //是否已经获取到了历史文章数据
    private static volatile boolean mHasGetJsonFlag = false;
    //是否只爬取文章信息不爬取点赞阅读数
    private static volatile boolean mIsJustCrawlArticle = false;
    //采集的类型,单个与所有,采集搜狗最新的文章
    private final static int Crawl_Type_Single = 0;
    private final static int Crawl_Type_All = 1;
    //当前的类型
    private int mCurrentCrawlType = Crawl_Type_All;
    //是否已经初始化了
    public static volatile boolean mIsInit = false;


    public void index() {
        render("/page/index.html");
        //先获取到两个POST变量
        String str = getPara("str", "");
        String url = getPara("url", "");
        logger.info("内容:" + str);
        logger.info("链接:" + url);
    }

    /**
     * 获取历史文章信息
     */
    public void test() {
        renderText("测试..");
        mainTest();
    }

    /**
     * 获取历史文章信息
     */
    public void search() {
        //JSONObject json = new JSONObject();
        //WxArticleInfo model = WxArticleInfo.utils.findById(1);
        //json.put("list",model);
        //renderJson(json);
    }

    /**
     * 初始化获取微信历史链接
     */
    public void init() {
        renderText("已经初始化");
        if (mCurrentCrawlType == Crawl_Type_Single) {
            //如果是单个公众号
            mIsInit = true;
        } else {
            if (null == gNextHistoryQueue) {
                gNextHistoryQueue = new ConcurrentLinkedQueue<>();
            } else {
                gNextHistoryQueue.clear();
            }
            try {
                gNextHistoryQueue.addAll(WxDataUtils.GetHistoryUrlData("wx_public_data.xml"));
                mIsInit = true;
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
    }


    /**
     * 重置
     */
    public void reset() {
        renderText("已经重置");
        if (mCurrentCrawlType == Crawl_Type_All) {
            mIsInit = false;
            if (null != gNextHistoryQueue) {
                gNextHistoryQueue.clear();
                gNextHistoryQueue = null;
            }
        }
    }

    /**
     * 切换抓取的模式
     */
    public void mode() {
        mIsJustCrawlArticle = !mIsJustCrawlArticle;
        if (mIsJustCrawlArticle) {
            renderText("已经设置为只抓取文章基本信息模式");
        } else {
            renderText("已经设置为只抓取文章全部信息模式");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取历史文章信息
     */
    @ActionKey("/getMsgJson")
    public void getMsgJson() {
        if (!mIsInit) {
            renderText("程序未初始化");
            return;
        }
        try {
            String content = URLDecoder.decode(getPara("str", ""), "UTF-8");
            mCurrentHistoryURL = URLDecoder.decode(getPara("url", ""), "UTF-8");
            //logger.info("内容:" + content);
            logger.info("历史文章链接:" + mCurrentHistoryURL);
            //解析历史文章的内容
            WxDataUtils.AnalysisHistoryJson(content, mCurrentHistoryURL);
            mHasGetJsonFlag = true;
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
            e.printStackTrace();
        }
        renderText("获取历史文章列表");
    }

    /**
     * 返回下一页的微信历史链接
     */
    @ActionKey("/getWxHis")
    public void getWxHis() {
        if (!mIsInit) {
            renderText("程序未初始化");
        } else if (mCurrentCrawlType == Crawl_Type_Single) {
            //如果是采集单个公众号则不需要返回历史链接
            renderText("正在采集单个公众号");
        } else {
            int countTimer = 0;
            while (!mHasGetJsonFlag) {
                try {
                    //先等待0.1秒
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (countTimer >= 10) {
                    break;
                }
                countTimer++;
            }
            getNextHistoryUrl();
            mHasGetJsonFlag = false;
        }
    }

    /**
     * 解析阅读量与点赞量
     */
    @ActionKey("/getMsgExt")
    public void getMsgExt() {
        if (!mIsInit) {
            renderText("程序未初始化");
            return;
        }
        try {
            String content = URLDecoder.decode(getPara("str", ""), "UTF-8");
            String signature = URLUtils.getURLParameter(URLDecoder.decode(getPara("url", ""), "UTF-8")).get("sn");
            logger.info("链接:" + signature);
            logger.info("内容:" + content);
            WxDataUtils.AnalysisArticle(content, signature);
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
            e.printStackTrace();
        }
        renderText("获取阅读与点赞信息");
    }

    /***
     * 返回下一篇文章的地址
     */
    @ActionKey("/getWxPost")
    public void getWxPost() {
        if (!mIsInit) {
            renderText("程序未初始化");
            return;
        }

        if (getNextArticleUrl()) {
            if (mCurrentCrawlType == Crawl_Type_Single) {
                //如果是采集单个公众号则不需要返回历史链接
                renderText("已完成采集");
            } else if (gNextHistoryQueue.isEmpty()) {
                renderText("已完成采集");
            } else {
                String nextHistoryUrl = gNextHistoryQueue.poll();
                logger.info("返回下个公众号历史链接:" + nextHistoryUrl);
                renderText("<script>setTimeout(function(){window.location.href='" + nextHistoryUrl + "';},2000);</script>");
            }

        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 获取下一篇文章的链接
     *
     * @return 是否采集完成
     */
    private boolean getNextArticleUrl() {
        if (mIsJustCrawlArticle) {
            return true;
        }
        String url = WxDataUtils.getLatestArticleUrl();
        if (null == url || url.trim().equals("")) {
            return true;
        } else {
            String resultURL = WxDataUtils.getRealArticleUrl(url, mCurrentHistoryURL);
            logger.info("下一篇文章的地址为，cn :" + resultURL);
            renderText("<script>setTimeout(function(){window.location.href='" + resultURL + "';},2000);</script>");
            return false;
        }
    }

    /**
     * 获取下一个公众号的历史链接
     */
    private void getNextHistoryUrl() {
        if (getNextArticleUrl()) {
            //已经把文章采集完成了
            if (!gNextHistoryQueue.isEmpty()) {
                String nextHistoryUrl = gNextHistoryQueue.poll();
                logger.info("返回下个公众号历史链接:" + nextHistoryUrl);
                renderText("<script>setTimeout(function(){window.location.href='" + nextHistoryUrl + "';},2000);</script>");
            } else {
                renderText("历史链接为空,已经采集完成");
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 测试
     */
    private void mainTest() {
        WxArticleMessage article = WxArticleMessage.dao.findFirst("select * from wx_article_message where sn=\'78d4ca5193f48d770aa6ac17d51d025d\'");
        if (null != article) {
            //如果还未更新，则进行更新
            if (article.get("read_num") == null && article.get("like_num") == null) {
                //article.set("read_num", readNum).set("like_num", likeNum).set("created_time", System.currentTimeMillis()).update();
                logger.info("1-------------------");
            } else {
                logger.info("2-------------------");
            }
        }
    }
}
