package com.junlin.manager.admin.service;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.junlin.manager.GlobalConstants;
import com.junlin.manager.reptile.sogou.entity.SoGouArticleStatus;
import com.junlin.manager.utils.TimeUtils;
import com.junlin.manager.utils.SqlHelper;
import com.junlin.manager.utils.URLUtils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.junlin.manager.admin.service.ArticleService.ISearchArticleCallback.Err_Code;
import static com.junlin.manager.admin.service.ArticleService.ISearchArticleCallback.Succeed_Code;

/**
 * Created by junlinhui eight on 2017/3/23.
 * 文章
 */
public class ArticleService {
    //logger
    private static final Logger logger = Logger.getLogger(ArticleService.class.getName());

    /**
     * 开始查找
     *
     * @param page
     */
    public static void startSearch(int page, int pageCount, int minRead, int minWord, long startTime, long endTime, ISearchArticleCallback
            callback) {
        if (null == callback) {
            logger.info("回调为空");
            return;
        }

        if (page > 0) {
            String[] obtainParams = {
                    GlobalConstants.SoGou_Article_Profile_Table + ".signature",
                    GlobalConstants.SoGou_Article_Profile_Table + ".title",
                    GlobalConstants.SoGou_Article_Profile_Table + ".url",
                    GlobalConstants.SoGou_Office_Wx_Table + ".wechat_name",
                    GlobalConstants.SoGou_Article_Message_Table + ".read_num",
                    GlobalConstants.SoGou_Article_Message_Table + ".like_num",
                    GlobalConstants.SoGou_Article_Message_Table + ".re_index",
                    GlobalConstants.SoGou_Article_Profile_Table + ".push_time",
                    "GROUP_CONCAT(" + GlobalConstants.Admin_User_Table + ".nick_name)  AS nick_name",
                    "GROUP_CONCAT(" + GlobalConstants.Sogou_Article_Status_Table + ".flag) AS flag",
                    GlobalConstants.SoGou_Article_Profile_Table + ".key_word",
                    GlobalConstants.SoGou_Article_Message_Table + ".comment_num",
                    GlobalConstants.SoGou_Article_Message_Table + ".updated_time"
            };

            SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.SoGou_Article_Profile_Table, obtainParams)
                    .innerJoin(GlobalConstants.SoGou_Office_Wx_Table, GlobalConstants.SoGou_Article_Profile_Table + ".biz", GlobalConstants
                            .SoGou_Office_Wx_Table + ".biz")
                    .innerJoin(GlobalConstants.SoGou_Article_Message_Table, GlobalConstants.SoGou_Article_Profile_Table + ".signature",
                            GlobalConstants.SoGou_Article_Message_Table + ".signature")
                    .leftJoin(GlobalConstants.Sogou_Article_Status_Table, GlobalConstants.SoGou_Article_Profile_Table + ".signature",
                            GlobalConstants.Sogou_Article_Status_Table + ".signature")
                    .leftJoin(GlobalConstants.Admin_User_Table, GlobalConstants.Sogou_Article_Status_Table + ".uuid", GlobalConstants
                            .Admin_User_Table + ".uuid")
                    .ge(GlobalConstants.SoGou_Article_Message_Table + ".read_num", minRead)
                    .ge(GlobalConstants.SoGou_Article_Profile_Table + ".word_count", minWord)
                    .ge(GlobalConstants.SoGou_Article_Message_Table + ".updated_time", startTime)
                    .le(GlobalConstants.SoGou_Article_Message_Table + ".updated_time", endTime);
            //先查询数量
            Long count = sqlHelper.queryCount(GlobalConstants.SoGou_Article_Profile_Table + ".signature");
            if (count <= 0) {
                callback.onSearchResult(Err_Code, 0L, null);
            } else {
                //查询具体数据
                List<Record> records = sqlHelper
                        .gruopBy(GlobalConstants.SoGou_Article_Profile_Table + ".signature")
                        .orderByDesc(GlobalConstants.SoGou_Article_Message_Table + ".re_index")
                        .paginate(page, pageCount);
                callback.onSearchResult(Succeed_Code, count, resultJsonSet(records));
            }
        } else {
            callback.onSearchResult(Err_Code, 0L, null);
        }
    }

    /**
     * 设置阅读状态
     *
     * @param uuid
     * @param url
     * @param flag
     * @param callback
     */
    public static void startSetReadFlag(String uuid, String url, int flag, String remark, ISetReadFlagCallback callback) {
        logger.info("用户id:" + uuid + "\nurl:" + url + "\nflag:" + flag + "\nremark:" + remark);
        if (StringUtils.isEmpty(url)) {
            return;
        }
        try {
            //保存文章的状态
            String signature = URLUtils.getURLParameter(url).get("sn");
            new SoGouArticleStatus()
                    .set("signature", signature)
                    .set("uuid", uuid)
                    .set("flag", flag)
                    .set("remark", remark)
                    .set("created_time", System.currentTimeMillis())
                    .set("updated_time", System.currentTimeMillis())
                    .save();
            callback.onResult(ISetReadFlagCallback.Succeed_Code);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onResult(ISetReadFlagCallback.Err_Code);
        }
    }

    /**
     * 批量设置阅读状态
     *
     * @param uuid
     * @param urls
     * @param flag
     * @param callback
     */
    public static void startBatchSetReadFlag(String uuid, String[] urls, int flag, String remark, ISetReadFlagCallback callback) {
        if (null == urls) {
            return;
        }
        for (String url : urls) {
            if (StringUtils.isEmpty(url)) {
                continue;
            }
            try {
                logger.info("用户id:" + uuid + "\nurl:" + url + "\nflag:" + flag + "\nremark:" + remark);
                //保存文章的状态
                String signature = URLUtils.getURLParameter(url).get("sn");
                new SoGouArticleStatus()
                        .set("signature", signature)
                        .set("uuid", uuid)
                        .set("flag", flag)
                        .set("remark", remark)
                        .set("created_time", System.currentTimeMillis())
                        .set("updated_time", System.currentTimeMillis())
                        .save();

                callback.onResult(ISetReadFlagCallback.Succeed_Code);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onResult(ISetReadFlagCallback.Err_Code);
            }
        }
    }

    /**
     * 获取关键字列表
     *
     * @param page
     * @param pageCount
     * @return
     */
    public static void startSearchKeyWord(int page, int pageCount, String name, ISearchKeyWordCallback callback) {
        logger.info("页面:" + page + "\n名字:" + name );
        String[] obtainParams = {
                GlobalConstants.SoGou_Key_Word_Table + ".word",
                GlobalConstants.SoGou_Key_Word_Table + ".weight",
        };

        SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.SoGou_Key_Word_Table, obtainParams);
        if (!StringUtils.isEmpty(name)) {
            sqlHelper.likeWith(GlobalConstants.SoGou_Key_Word_Table + ".word", name);
        }

        Long count = sqlHelper.queryCount(GlobalConstants.SoGou_Key_Word_Table + ".word");
        if (0 == count) {
            callback.onResult(ISearchKeyWordCallback.Err_Code, 0L, null);
        } else {
            List<Record> records = Db.paginate(page, pageCount, sqlHelper.buildSelectSQL(), sqlHelper.buildExceptSelectSQL()).getList();
            List<Map<String, Object>> soGouKeyWords = new ArrayList<>();
            for (Record record : records) {
                Map<String, Object> map = new HashMap<String, Object>();
                String[] attrsName = record.getColumnNames();
                Object[] attrsValue = record.getColumnValues();
                for (int index = 0; index < attrsName.length; index++) {
                    //修改发布的时间的格式
                    map.put(attrsName[index], attrsValue[index]);
                }
                soGouKeyWords.add(map);
            }
            callback.onResult(ISearchKeyWordCallback.Succeed_Code, count, soGouKeyWords);
        }
    }

    /***
     * 返回结果设置
     * @param records
     * @return
     */
    private static List<Map<String, Object>> resultJsonSet(List<Record> records) {
        List<Map<String, Object>> resultArticles = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> map = new HashMap<String, Object>();
            String[] attrsName = record.getColumnNames();
            Object[] attrsValue = record.getColumnValues();
            map.put("status", setArticleStatus(record));
            for (int index = 0; index < attrsName.length; index++) {
                //修改发布的时间的格式
                if (!attrsName[index].equals("flag") && !attrsName[index].equals("nick_name")) {
                    if (attrsName[index].equals("push_time")) {
                        Double time = record.getDouble("push_time");
                        map.put("push_time", TimeUtils.timeStamp2Date(time, null));
                    } else {
                        map.put(attrsName[index], attrsValue[index]);
                    }
                }
            }
            resultArticles.add(map);
        }

        return resultArticles;
    }

    /**
     * 解析文章的状态
     *
     * @param record
     * @return
     */
    private static Set<String> setArticleStatus(Record record) {
        Object flagObj = record.get("flag");
        Object nameObj = record.get("nick_name");
        Set<String> re = new HashSet<>();
        if (null != flagObj && null != nameObj) {
            String[] flagArray = flagObj.toString().split(",");
            String[] nameArray = nameObj.toString().split(",");
            int length = flagArray.length > nameArray.length ? nameArray.length : flagArray.length;
            for (int index = 0; index < length; index++) {
                if ("1".equals(flagArray[index])) {
                    re.add(nameArray[index] + "已读");
                } else if ("2".equals(flagArray[index])) {
                    re.add(nameArray[index] + "标记为垃圾文章");
                } else if ("3".equals(flagArray[index])) {
                    re.add(nameArray[index] + "标记为精品文章");
                } else {
                    re.add("未读");
                }
            }
        } else {
            re.add("未读");
        }
        return re;
    }

    ////////////////////////////////////回调/////////////////////////////

    public interface ISearchArticleCallback {
        //失败
        int Err_Code = -1;
        //成功
        int Succeed_Code = 1;

        void onSearchResult(int code, Long count, List<Map<String, Object>> resultArticles);
    }

    public interface ISetReadFlagCallback {
        //失败
        int Err_Code = -1;
        //成功
        int Succeed_Code = 1;

        void onResult(int code);
    }

    public interface ISearchKeyWordCallback {
        //失败
        int Err_Code = -1;
        //成功
        int Succeed_Code = 1;

        void onResult(int code, Long total, List<Map<String, Object>> words);
    }

    public static void main(String[] args) {
        String[] obtainParams = {
                GlobalConstants.SoGou_Article_Profile_Table + ".title",
                GlobalConstants.SoGou_Article_Profile_Table + ".url",
                GlobalConstants.SoGou_Office_Wx_Table + ".wechat_name",
                GlobalConstants.SoGou_Article_Message_Table + ".read_num",
                GlobalConstants.SoGou_Article_Message_Table + ".like_num",
                GlobalConstants.SoGou_Article_Message_Table + ".re_index",
                GlobalConstants.SoGou_Article_Profile_Table + ".push_time",
                GlobalConstants.Sogou_Article_Status_Table + ".flag",
                GlobalConstants.SoGou_Article_Profile_Table + ".key_word",
                GlobalConstants.SoGou_Article_Message_Table + ".comment_num",
                GlobalConstants.SoGou_Article_Message_Table + ".updated_time"
        };

        SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.SoGou_Article_Profile_Table, obtainParams)
                .innerJoin(GlobalConstants.SoGou_Office_Wx_Table, GlobalConstants.SoGou_Article_Profile_Table + ".biz", GlobalConstants
                        .SoGou_Office_Wx_Table + ".biz")
                .innerJoin(GlobalConstants.SoGou_Article_Message_Table, GlobalConstants.SoGou_Article_Profile_Table + ".signature",
                        GlobalConstants.SoGou_Article_Message_Table + ".signature")
                .leftJoin(GlobalConstants.Sogou_Article_Status_Table, GlobalConstants.SoGou_Article_Profile_Table + ".signature",
                        GlobalConstants.Sogou_Article_Status_Table + ".signature")

                .ge(GlobalConstants.SoGou_Article_Message_Table + ".read_num", 500)
                .ge(GlobalConstants.SoGou_Article_Profile_Table + ".word_count", 1200)
                .orderByDesc(GlobalConstants.SoGou_Article_Message_Table + ".re_index");

        logger.info("语句：" + sqlHelper.buildSql());
    }
}
