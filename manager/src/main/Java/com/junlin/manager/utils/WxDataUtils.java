package com.junlin.manager.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.junlin.manager.reptile.wechat.controller.WxController;
import com.junlin.manager.reptile.wechat.entity.WxArticleInfo;
import com.junlin.manager.reptile.wechat.entity.WxArticleMessage;
import com.junlin.manager.reptile.wechat.entity.WxPublicInfo;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static com.google.common.io.Resources.getResource;
import static com.junlin.manager.GlobalConstants.*;


/**
 * Created by junlinhui eight on 2017/3/7.
 * 微信数据操作类
 */
public class WxDataUtils {
    //logger
    private static Logger logger = Logger.getLogger(WxController.class.getName());

    /**
     * 解析历史文章数据
     *
     * @param content
     */
    public static boolean AnalysisHistoryJson(String content, String currentHistoryURL) {
        JSONObject rootJson = parseJsonObject(content);
        if (null == rootJson) {
            logger.info("获取到了html内容：" + content);
            content = StringEscapeUtils.unescapeJava(StringEscapeUtils.unescapeHtml4(content));
            rootJson = parseJsonObject(content);
            if (null == rootJson) {
                return false;
            }
        }
        //解析文章数据
        JSONArray articleArray = rootJson.getJSONArray("list");
        if (articleArray.size() <= 0) {
            return false;
        }
        //获取公众号的biz信息
        String biz = URLUtils.getURLParameter(currentHistoryURL).get("__biz");
        for (Object object : articleArray) {
            JSONObject jb = (JSONObject) object;
            //解析头条信息
            JSONObject headObj = jb.getJSONObject("app_msg_ext_info");
            //解析通的信息
            JSONObject commonObj = jb.getJSONObject("comm_msg_info");
            AnalysisArticleJson(biz, headObj, commonObj, true);

            // logger.info("解析后:" + article.toString());
            //如果是多条文章
            if (headObj.getString("is_multi").equals("1")) {
                JSONArray subArray = headObj.getJSONArray("multi_app_msg_item_list");
                for (Object subObject : subArray) {
                    JSONObject subObj = (JSONObject) subObject;
                    AnalysisArticleJson(biz, subObj, commonObj, true);
                }
            }
        }
        return true;
    }


    /**
     * 解析历史文章数据
     *
     * @param content
     * @param signature
     */
    public static void AnalysisArticle(String content, String signature) {
        //logger.info("\n\n ----赞-------" + content + "\n\n");
        //找到对应的文章
        WxArticleMessage article = WxArticleMessage.dao.findFirst("select * from " + Wx_Message_Table + " where sn=\'" + signature + "\' AND " +
                "created_time is NULL AND read_num ='0' AND like_num ='0'");
        if (null != article) {
            int readNum = 0;
            int likeNum = 0;
            try {
                //解析文章数据
                JSONObject json = JSONObject.parseObject(content);
                JSONObject object = json.getJSONObject("appmsgstat");
                if (null != object) {
                    readNum = Integer.valueOf(object.getString("read_num"));
                    likeNum = Integer.valueOf(object.getString("like_num"));
                }
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
            //如果还未更新，则进行更新
            if (article.get("created_time") == null) {
                article.set("read_num", readNum).set("like_num", likeNum).set("created_time", System.currentTimeMillis()).update();
            }
            logger.info("阅读数:" + readNum);
            logger.info("点赞数:" + likeNum);
        }
    }

    /**
     * 获取最新的需要抓取的文章链接
     *
     * @return
     */
    public static String getLatestArticleUrl() {
        Record record = Db.findFirst("select " + Wx_Article_Table + ".content_url from " + Wx_Article_Table + " inner join " + Wx_Message_Table
                + " on " + Wx_Message_Table + ".sn = " + Wx_Article_Table + ".sn WHERE " + Wx_Message_Table + ".read_num is NULL AND "
                + Wx_Message_Table + ".like_num is NULL AND " + Wx_Message_Table + ".created_time is NULL");
        //解析
        if (null == record) {
            return "";
        } else {
            return record.get("content_url");
        }
    }


    /**
     * 获取微信的真实的地址
     *
     * @param url
     * @param historyURL
     * @return
     */
    public static String getRealArticleUrl(String url, String historyURL) {
        String sn = URLUtils.getURLParameter(url).get("sn");
        String biz = URLUtils.getURLParameter(url).get("__biz");
        String mid = URLUtils.getURLParameter(url).get("mid");
        String idx = URLUtils.getURLParameter(url).get("idx");
        String chksm = URLUtils.getURLParameter(url).get("chksm");
        String scene = URLUtils.getURLParameter(url).get("scene");
        //解析历史链接的信息
        String key = URLUtils.getURLParameter(historyURL).get("key");
        String pass_ticket = URLUtils.getURLParameter(historyURL).get("pass_ticket");
        String uin = URLUtils.getURLParameter(historyURL).get("uin");
        String ascene = URLUtils.getURLParameter(historyURL).get("ascene");
        String version = URLUtils.getURLParameter(historyURL).get("version");

        //先把数据更新为0
        WxArticleMessage article = WxArticleMessage.dao.findFirst("select * from " + Wx_Message_Table + " where sn=\'" + sn + "\' AND created_time is " +
                "NULL AND " + Wx_Message_Table + ".read_num is NULL AND " + Wx_Message_Table + ".like_num is NULL");
        if (null != article) {
            article.set("read_num", 0).set("like_num", 0).update();
        }

        return "https://mp.weixin.qq.com/s?" +
                "__biz=" + biz +
                "&mid=" + mid +
                "&idx=" + idx +
                "&sn=" + sn +
                "&chksm=" + chksm +
                "&scene=" + scene +
                "&key=" + key +
                "&ascene=" + ascene +
                "&uin=" + uin +
                "&devicetype=android-19" +
                "&version=" + version +
                "&nettype=WIFI" +
                "&fontScale=100" +
                "&pass_ticket=" + pass_ticket +
                "&wx_header=1";
    }

    /**
     * DOM方法解析xml获取历史链接
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static List<String> GetHistoryUrlData(String file) throws Exception {
        List<String> dataList = new ArrayList<>();
        DocumentBuilderFactory dBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dBuilderFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(String.valueOf(getResource(file)));
        NodeList list = doc.getElementsByTagName("node");
        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            Element element = (Element) list.item(i);
            String name = element.getElementsByTagName("name").item(0).getTextContent();
            String url = element.getElementsByTagName("url").item(0).getTextContent();
            String biz = url.substring(url.indexOf("__biz="), url.indexOf("#")).replace("__biz=", "");
            dataList.add(url);

            WxPublicInfo wxPublicInfo = WxPublicInfo.dao.findFirst("select * from " + Wx_Public_Table + " where biz=\'" + biz + "\'");
            if (null == wxPublicInfo) {
                new WxPublicInfo().set("biz", biz).set("name", name).set("history_url", url).save();
            }
            logger.info("公众号名称：" + name + "\n历史链接：" + url + "\nbiz:" + biz);
        }
        return dataList;
    }

    /**
     * DOM方法解析xml获取历史链接
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static List<String> GetSoGouUrlData(String file) throws Exception {
        List<String> dataList = new ArrayList<>();
        DocumentBuilderFactory dBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dBuilderFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(String.valueOf(getResource(file)));
        NodeList list = doc.getElementsByTagName("node");
        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            Element element = (Element) list.item(i);
            String name = element.getElementsByTagName("name").item(0).getTextContent();
            String url = element.getElementsByTagName("url").item(0).getTextContent();
            String soGouId = element.getElementsByTagName("sogou_wxid").item(0).getTextContent();
            String wxId = element.getElementsByTagName("wxid").item(0).getTextContent();
            String biz = url.substring(url.indexOf("__biz="), url.indexOf("#")).replace("__biz=", "");

            logger.info("公众号名称：" + name + "\n历史链接：" + url + "\nbiz:" + biz);
            String soGouUrl = "http://weixin.sogou.com/weixin?type=2&ie=utf8&query=%E4%BA%86&tsn=1&ft=null&et=null&interation=null&wxid=" + soGouId +
                    "&usip=" + wxId + "&from=tool";
            dataList.add(soGouUrl);

            WxPublicInfo wxPublicInfo = WxPublicInfo.dao.findFirst("select * from " + Wx_Public_Table + " where biz=\'" + biz + "\'");
            if (null == wxPublicInfo) {
                new WxPublicInfo().set("biz", biz).set("name", name).set("history_url", url).save();
            }
        }
        return dataList;
    }

    /**
     * DOM方法解析xml获取历史链接
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static List<String> GetSoGouKeyWordURL(String file) throws Exception {
        List<String> dataList = new ArrayList<>();
        DocumentBuilderFactory dBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dBuilderFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(String.valueOf(getResource(file)));
        NodeList list = doc.getElementsByTagName("data");
        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            Element element = (Element) list.item(i);
            String name = element.getTextContent();
            logger.info(name);
            String soGouUrl = "http://weixin.sogou.com/weixin?type=2&ie=utf8&query=" + name + "&tsn=2&ft=&et" +
                    "=&interation=null&wxid=&usip=null&from=tool";
            dataList.add(soGouUrl);
        }
        return dataList;
    }

    ////////////////////////////////private method///////////////////////////////////

    /***
     * 解析保存文章信息
     * @param biz
     * @param contentObj
     * @param commonObj
     * @param isSub
     */
    private static void AnalysisArticleJson(String biz, JSONObject contentObj, JSONObject commonObj, boolean isSub) {
        String contentUrl = contentObj.getString("content_url");
        if (null == contentUrl || contentUrl.trim().equals("")) {
            logger.error("contentUrl 为空");
            return;
        }
        //获取文章签名
        String signature = URLUtils.getURLParameter(contentUrl).get("sn");
        //查询文章是否已经存在
        WxArticleInfo articleInfo = WxArticleInfo.dao.findFirst("select * from " + Wx_Article_Table + " where sn=\'" + signature + "\'");
        if (null == articleInfo) {
            new WxArticleInfo()
                    .set("biz", biz)
                    .set("author", contentObj.getString("author"))
                    .set("content", contentObj.getString("content"))
                    .set("content_url", contentUrl)
                    .set("copyright_stat", contentObj.getString("copyright_stat"))
                    .set("cover", contentObj.getString("cover"))
                    .set("digest", contentObj.getString("digest"))
                    .set("fileid", contentObj.getInteger("fileid"))
                    .set("is_multi", (isSub ? 2 : contentObj.getIntValue("is_multi")))
                    .set("source_url", (isSub ? contentUrl : contentObj.getString("source_url")))
                    .set("subtype", (isSub ? 0 : contentObj.getInteger("subtype")))
                    .set("title", contentObj.getString("title"))

                    .set("datetime", commonObj.getDouble("datetime"))
                    .set("fakeid", commonObj.getString("fakeid"))
                    .set("id", commonObj.getInteger("id"))
                    .set("status", commonObj.getInteger("status"))
                    .set("type", commonObj.getInteger("type"))
                    .set("sn", signature)
                    .set("created_time", System.currentTimeMillis()).save();
            //文章不存在再去保存阅读数据
            new WxArticleMessage().set("sn", signature).save();
        } else {
            logger.info("文章已经存在");
        }
        //每次查询都会新增一条信息，记录文章阅读数据
        //new WxArticleMessage().set("sn", signature).save();
    }

    /**
     * 判断字符串是否为合法的json
     *
     * @param jsonStr
     * @return
     */
    private static JSONObject parseJsonObject(String jsonStr) {
        JSONObject object = null;
        try {
            object = JSONObject.parseObject(jsonStr);
        } catch (Exception e) {
            object = null;
        }
        return object;
    }

    //测试
    public static void main(String[] args) {
        try {
            //GetHistoryUrlData("wx_public_data.xml");
            GetSoGouKeyWordURL("stock_data.xml");
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
        }
    }
}
