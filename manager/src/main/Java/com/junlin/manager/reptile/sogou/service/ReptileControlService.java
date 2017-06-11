package com.junlin.manager.reptile.sogou.service;

import com.jfinal.plugin.activerecord.Db;
import com.junlin.manager.reptile.sogou.entity.SoGouKeyWord;
import com.junlin.manager.reptile.sogou.helper.SpiderHelper;
import com.junlin.manager.reptile.sogou.reptile.SoGouWxProcessor;
import com.mchange.v2.lang.StringUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.utils.HttpConstant;

import static com.junlin.manager.GlobalConstants.SoGou_Key_Word_Table;
import static com.junlin.manager.GlobalConstants.mainURL;

/**
 * Created by junlinhui eight on 2017/3/24.
 * Controller
 */
public class ReptileControlService {

    //每一个Ip抓取的数量
    private final static int SpiderNum = 15;
    //每一个Ip抓取的数量
    private final static int SpiderWaitCount = 20;
    //logger
    private static Logger logger = Logger.getLogger(ReptileControlService.class.getName());
    //搜狗爬虫
    private static volatile Spider gSoGouSpider = null;
    //每次抓取的数量
    private static volatile int mSpiderPeriodCount = SpiderNum;
    //停止抓取等待的时间,20秒
    private static int mSpiderWaitCount = SpiderWaitCount;
    //关键字序号
    private static volatile int mKeyWordIndex = 1;
    //是否停止采集
    private static volatile boolean mReptileIsRun = false;

    /**
     * 重置爬虫
     */
    public static void resetReptileInfo() {
        mKeyWordIndex = 1;
        mReptileIsRun = false;
        mSpiderWaitCount = SpiderWaitCount;
        mSpiderPeriodCount = SpiderNum;
        if (null != gSoGouSpider) {
            gSoGouSpider.stop();
            gSoGouSpider = null;
        }
    }

    /**
     * 重置爬虫
     */
    public static void initReptileInfo() {
        mReptileIsRun = true;
    }


    /**
     * 获取下一个搜索的链接
     *
     * @return
     */
    public static String getNextSearchUrl() {
        String soGouUrl = "";
        SoGouKeyWord keyWord = SoGouKeyWord.dao.findFirst("select * from " + SoGou_Key_Word_Table + " where id=\'" + mKeyWordIndex + "\'");
        if (null != keyWord) {
            String word = keyWord.getStr("word");
            if (StringUtils.nonEmptyString(word)) {
                soGouUrl = "http://weixin.sogou.com/weixin?type=2&ie=utf8&query=" + word + "&tsn=2&ft=&et" +
                        "=&interation=null&wxid=&usip=null&from=tool";
            }
            mKeyWordIndex++;
        } else {
            mKeyWordIndex = 1;
        }
        logger.info("\n关键字序号:" + mKeyWordIndex + "\n新的搜索地址：" + soGouUrl + "\n");
        return soGouUrl;
    }

    /**
     * 开始搜狗采集
     */
    public static void startSoGouReptile() {
        if (null != gSoGouSpider) {
            logger.info("-采集程序正在运行...");
            return;
        }
        //去线程中执行
        new Thread(new Runnable() {
            @Override
            public void run() {
                //判断采集队列是否为空
                while (mReptileIsRun) {
                    logger.info("\n循环...");
                    if (ReptileControlService.isSubSpiderFinish()) {
                        ProxyService.getProxyInfo(new ProxyService.IGetProxyCallBack() {
                            @Override
                            public void getProxyResult(boolean isSucceed, String proxy) {
                                if (isSucceed) {
                                    ReptileControlService.startNewSoGouSpider(proxy);
                                } else {
                                    logger.error("\n获取代理失败...");
                                }
                            }
                        });
                    }
                    //睡眠一秒钟
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                //释放
                if (null != gSoGouSpider) {
                    gSoGouSpider.stop();
                    gSoGouSpider = null;
                }
                logger.info("\n已停止采集 mKeyWordIndex：" + mKeyWordIndex);
            }
        }).start();
    }


    /**
     * 创建Spider
     *
     * @param proxyInfo
     */
    public static void startNewSoGouSpider(String proxyInfo) {
        if (null != gSoGouSpider) {
            logger.info("采集程序仍在运行，不需要重新创建");
            return;
        }
        //代理
        SpiderHelper spiderHelper = new SpiderHelper(mainURL, proxyInfo);
        //获取comment
        PageProcessor processor = new SoGouWxProcessor(proxyInfo, spiderHelper.getCookie());
        //参数值
        NameValuePair[] values = new NameValuePair[2];
        values[0] = new BasicNameValuePair("client_url", spiderHelper.getMainURL());
        values[1] = new BasicNameValuePair("session_token", spiderHelper.getSessionToken());

        Map valueMap = new HashMap();
        valueMap.put("nameValuePair", values);

        //创建Spider
        gSoGouSpider = Spider.create(processor);
        String nextURL = getNextSearchUrl();
        if (StringUtils.nonEmptyString(nextURL)) {
            Request request = new Request(nextURL);
            request.setExtras(valueMap);
            request.setMethod(HttpConstant.Method.GET);
            gSoGouSpider.thread(8)
                    .addRequest(request)
                    .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000000)))
                    .run();
        } else {
            logger.info("目标数据为空，采集失败");
        }
    }

    /**
     * 本来采集是否完成
     *
     * @return boolean
     */
    public static boolean isSubSpiderFinish() {
        if (null == gSoGouSpider) {
            logger.info("采集程序未运行");
            return true;
        }

        if (mSpiderPeriodCount <= 0) {
            logger.info("本轮采集已经完成,状态：" + gSoGouSpider.getStatus());
            if (Spider.Status.Stopped == gSoGouSpider.getStatus() || mSpiderWaitCount <= 0) {
                gSoGouSpider.stop();
                gSoGouSpider = null;
                mSpiderPeriodCount = SpiderNum;
                mSpiderWaitCount = SpiderWaitCount;
                trunCateSignatureTable();
                return true;
            } else {
                mSpiderWaitCount--;
                logger.info("本轮采集已经完成,mSpiderWaitCount：" + mSpiderWaitCount);
                return false;
            }
        } else {
            logger.info("添加新的搜索链接");
            gSoGouSpider.addUrl(getNextSearchUrl());
            mSpiderPeriodCount--;
            return false;
        }
    }

    /**
     * 重置微信临时信息表
     */
    private static void trunCateSignatureTable(){
        Db.update("TRUNCATE TABLE sogou_wx_signature");
    }
}
