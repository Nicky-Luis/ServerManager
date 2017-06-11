package com.junlin.manager.app.service;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.junlin.manager.app.entity.LiteUser;
import com.junlin.manager.app.entity.NoticeMessage;
import com.junlin.manager.app.utils.VerifyUtils;
import com.junlin.manager.utils.SqlHelper;
import com.junlin.manager.utils.TimeUtils;
import com.mchange.v2.lang.StringUtils;

import org.apache.log4j.Logger;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Millisecond;
import org.ocpsoft.prettytime.units.Second;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.junlin.manager.GlobalConstants.Lite_Answer_Comment_Table;
import static com.junlin.manager.GlobalConstants.Lite_Answer_Table;
import static com.junlin.manager.GlobalConstants.Lite_Message_Notice_Table;
import static com.junlin.manager.GlobalConstants.Lite_Question_Table;
import static com.junlin.manager.GlobalConstants.Lite_Question_Target_Table;
import static com.junlin.manager.GlobalConstants.Lite_User_Table;
import static com.junlin.manager.app.entity.NoticeMessage.Message_Type_Answer_Comment;
import static com.junlin.manager.app.entity.NoticeMessage.Message_Type_Answer_Like;
import static com.junlin.manager.app.entity.NoticeMessage.Message_Type_Comment_Like;
import static com.junlin.manager.app.entity.NoticeMessage.Message_Type_Question_Follow;
import static com.junlin.manager.app.entity.NoticeMessage.Message_Type_Question_New_Answer;
import static com.junlin.manager.app.entity.NoticeMessage.Message_Type_User_Follow;
import static com.junlin.manager.app.entity.NoticeMessage.Message_Type_User_Message;

/**
 * Created by junlinhui eight on 2017/5/4.
 * 消息通知部分
 */
public class NoticeService {
    //logger
    private static Logger logger = Logger.getLogger(HomeService.class.getName());

    /**
     * 获取所有问题的通知
     *
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     */
    public static String searchQuestionNotice(String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        try {
            int page = Integer.valueOf(pageStr);
            if (page > 0) {
                //分页查询具体数据
                List<Record> records = doQuestionNoticeSql(page, pageCount, uuid);
                resultMap.put("msg", "操作成功");
                resultMap.put("count", records.size());
                resultMap.put("result", resultNoticeJson(records));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "操作失败");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 获取所有系统的通知
     *
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     */
    public static String searchSystemNotice(String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        try {
            int page = Integer.valueOf(pageStr);
            if (page > 0) {
                List<Record> recordList = doSystemNoticeSql(pageCount, uuid, page);

                resultMap.put("msg", "操作成功");
                resultMap.put("count", recordList.size());
                resultMap.put("result", resultNoticeJson(recordList));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "操作失败");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 获取所有用户的通知
     *
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     */
    public static String searchUserNotice(String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        try {
            int page = Integer.valueOf(pageStr);
            if (page > 0) {
                List<Record> recordList = doUserNoticeSql(pageCount, uuid, page);

                resultMap.put("msg", "操作成功");
                resultMap.put("count", recordList.size());
                resultMap.put("result", resultNoticeJson(recordList));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "操作失败");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 查询通知
     *
     * @param sessionKey
     * @param noticeId
     * @return
     */
    public static String searchNotice(String sessionKey, String noticeId) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }

        try {
            NoticeMessage noticeMessage = NoticeMessage.findNoticeById(noticeId);

            if (null != noticeMessage) {

                Map<String, Object> dataMap = new HashMap<String, Object>();
                String[] attrsName = noticeMessage._getAttrNames();
                Object[] attrsValue = noticeMessage._getAttrValues();

                //修改发布的时间的格式
                for (int index = 0; index < attrsName.length; index++) {
                    if (attrsName[index].equals("time")) {
                        TimeUtils.setTimePretty(dataMap, attrsName[index], attrsValue[index]);
                    } else {
                        dataMap.put(attrsName[index], attrsValue[index]);
                    }
                }

                //查询用户的信息
                LiteUser user = LiteUser.findUserByUUID(noticeMessage.getStr("generate_id"));
                if (null != user) {
                    Map<String, Object> userMap = new HashMap<String, Object>();
                    String[] names = user._getAttrNames();
                    Object[] values = user._getAttrValues();
                    //修改发布的时间的格式
                    for (int index = 0; index < names.length; index++) {
                        userMap.put(names[index], values[index]);
                    }
                    resultMap.put("msg", "操作成功");
                    resultMap.put("result", dataMap);
                    resultMap.put("user", userMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "操作失败");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /***
     * 发布留言回复
     * @param noticeId
     * @param sessionKey
     * @param content
     * @param userId
     * @return
     */
    public static String startReply(String noticeId, String sessionKey, String content, String userId) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String myUid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        if (StringUtils.nonEmptyString(content)) {
            NoticeMessage.createNewNoticeMsg(NoticeMessage.Message_Type_User_Message, noticeId, myUid, userId, content, "");
            resultMap.put("msg", "操作成功");
        }else {
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "操作失败");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /***
     * 发布留言回复
     * @param noticeId
     * @param sessionKey
     * @return
     */
    public static String startDelete(String noticeId, String sessionKey) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        NoticeMessage noticeMessage = NoticeMessage.findNoticeById(noticeId);
        if (null != noticeId) {
            noticeMessage.delete();
            resultMap.put("msg", "操作成功");
        } else {
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "操作失败");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }
    ///////////////////////////////////private method//////////////////////////////////////////

    /***
     * 返回结果设置
     * @param records
     * @return
     */
    private static List<Map<String, Object>> resultNoticeJson(List<Record> records) {
        List<Map<String, Object>> dataMaps = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            String[] attrsName = record.getColumnNames();
            Object[] attrsValue = record.getColumnValues();

            //修改发布的时间的格式
            for (int index = 0; index < attrsName.length; index++) {
                if (attrsName[index].equals("time")) {
                    PrettyTime prettyTime = new PrettyTime(Locale.CHINESE);
                    prettyTime.removeUnit(Second.class);
                    prettyTime.removeUnit(Millisecond.class);
                    prettyTime.removeUnit(JustNow.class);

                    Double time = (Double) attrsValue[index];
                    dataMap.put(attrsName[index], prettyTime.format(new Date(time.longValue())).replace(" ", ""));
                } else {
                    dataMap.put(attrsName[index], attrsValue[index]);
                }
            }
            dataMaps.add(dataMap);
        }
        return dataMaps;
    }


    /***
     * 进行feed流信息查询
     * @param pageNumber
     * @param pageSize
     * @param uuid
     * @return
     */
    private static List<Record> doQuestionNoticeSql(int pageNumber, int pageSize, String uuid) {
        //问题新增关注
        SqlHelper newFollowSqlHelper = getNewFollowSqlHelper(uuid);
        //问题新增回答
        SqlHelper newAnswerSqlHelper = getNewAnswerSqlHelper(uuid);
        //回答被点赞
        SqlHelper answerLikeSqlHelper = getAnswerLikeSqlHelper(uuid);
        //回答被评论
        SqlHelper answerCommentSqlHelper = getAnswerCommentSqlHelper(uuid);
        //评论被点赞
        SqlHelper commentLikeSqlHelper = getCommentLikeSqlHelper(uuid);

        Page<Record> pages = Db.paginate(pageNumber, pageSize, "select *", " from ("
                + newFollowSqlHelper.buildSql() + " UNION "
                + newAnswerSqlHelper.buildSql() + " UNION "
                + answerLikeSqlHelper.buildSql() + " UNION "
                + answerCommentSqlHelper.buildSql() + " UNION "
                + commentLikeSqlHelper.buildSql()
                + ") as temp ORDER BY time DESC");
        return pages.getList();
    }

    /**
     * 查询系统通知
     *
     * @param pageCount
     * @param uuid
     * @param page
     * @return
     */
    private static List<Record> doSystemNoticeSql(int pageCount, String uuid, int page) {
        //用户被关注
        SqlHelper userFollowSqlHelper = getUserFollowSqlHelper(uuid);
        Page<Record> pages = Db.paginate(page, pageCount, "select *", " from ("
                + userFollowSqlHelper.buildSql()
                + ") as temp ORDER BY time DESC");
        return pages.getList();
    }

    /**
     * 查询系统通知
     *
     * @param pageCount
     * @param uuid
     * @param page
     * @return
     */
    private static List<Record> doUserNoticeSql(int pageCount, String uuid, int page) {
        //用户被关注
        SqlHelper userFollowSqlHelper = getLeaveMessageSqlHelper(uuid);
        Page<Record> pages = Db.paginate(page, pageCount, "select *", " from ("
                + userFollowSqlHelper.buildSql()
                + ") as temp ORDER BY time DESC");
        return pages.getList();
    }

    /***
     * 用户被关注
     * @param uuid
     * @return
     */
    private static SqlHelper getUserFollowSqlHelper(String uuid) {
        String[] userFollowParams = {
                "'follow_user' AS title",
                Lite_Message_Notice_Table + ".notice_id",
                Lite_Message_Notice_Table + ".message_type AS msgType",
                Lite_Message_Notice_Table + ".content AS message",
                Lite_User_Table + ".nickName",
                Lite_User_Table + ".avatarUrl",
                Lite_Message_Notice_Table + ".content AS attachName",
                Lite_Message_Notice_Table + ".content AS attachDepict",
                Lite_Message_Notice_Table + ".updated_time AS time",
                Lite_Message_Notice_Table + ".content AS remark",
                Lite_Message_Notice_Table + ".flag",
                Lite_Message_Notice_Table + ".attach_id",
                Lite_Message_Notice_Table + ".generate_id",
        };

        return SqlHelper.fromTableName(Lite_Message_Notice_Table, userFollowParams)
                .leftJoin(Lite_User_Table, Lite_User_Table + ".uuid", Lite_Message_Notice_Table + ".generate_id")
                .eq(Lite_Message_Notice_Table + ".receive_uid", "'" + uuid + "'")
                .eq(Lite_Message_Notice_Table + ".message_type", "'" + Message_Type_User_Follow + "'");
    }

    /***
     * 用户被留言
     * @param uuid
     * @return
     */
    private static SqlHelper getLeaveMessageSqlHelper(String uuid) {
        String[] userFollowParams = {
                "'leave_message' AS title",
                Lite_Message_Notice_Table + ".notice_id",
                Lite_Message_Notice_Table + ".message_type AS msgType",
                Lite_Message_Notice_Table + ".content AS message",
                Lite_User_Table + ".nickName",
                Lite_User_Table + ".avatarUrl",
                Lite_Message_Notice_Table + ".content AS attachName",
                Lite_Message_Notice_Table + ".content AS attachDepict",
                Lite_Message_Notice_Table + ".updated_time AS time",
                Lite_Message_Notice_Table + ".content AS remark",
                Lite_Message_Notice_Table + ".flag",
                Lite_Message_Notice_Table + ".attach_id",
                Lite_Message_Notice_Table + ".generate_id",
        };

        return SqlHelper.fromTableName(Lite_Message_Notice_Table, userFollowParams)
                .leftJoin(Lite_User_Table, Lite_User_Table + ".uuid", Lite_Message_Notice_Table + ".generate_id")
                .eq(Lite_Message_Notice_Table + ".receive_uid", "'" + uuid + "'")
                .eq(Lite_Message_Notice_Table + ".message_type", "'" + Message_Type_User_Message + "'");
    }

    /**
     * 评论被点赞
     *
     * @param uuid
     * @return
     */
    private static SqlHelper getCommentLikeSqlHelper(String uuid) {
        String[] commentLikeParams = {
                "'like_comment' AS title",
                Lite_Message_Notice_Table + ".notice_id",
                Lite_Message_Notice_Table + ".message_type AS msgType",
                Lite_Message_Notice_Table + ".content AS message",
                Lite_User_Table + ".nickName",
                Lite_User_Table + ".avatarUrl",
                Lite_Message_Notice_Table + ".content AS attachName",
                Lite_Answer_Comment_Table + ".content AS attachDepict",
                Lite_Message_Notice_Table + ".updated_time AS time",
                Lite_Message_Notice_Table + ".content AS remark",
                Lite_Message_Notice_Table + ".flag",
                Lite_Message_Notice_Table + ".attach_id",
                Lite_Message_Notice_Table + ".generate_id",
        };

        return SqlHelper.fromTableName(Lite_Message_Notice_Table, commentLikeParams)
                .leftJoin(Lite_User_Table, Lite_User_Table + ".uuid", Lite_Message_Notice_Table + ".generate_id")
                .leftJoin(Lite_Answer_Comment_Table, Lite_Answer_Comment_Table + ".comment_id", Lite_Message_Notice_Table + ".attach_id")
                .eq(Lite_Message_Notice_Table + ".receive_uid", "'" + uuid + "'")
                .eq(Lite_Message_Notice_Table + ".message_type", "'" + Message_Type_Comment_Like + "'");
    }

    /**
     * 回答被评论
     *
     * @param uuid
     * @return
     */
    private static SqlHelper getAnswerCommentSqlHelper(String uuid) {
        String[] answerCommentParams = {
                Lite_Message_Notice_Table + ".notice_id",
                Lite_Message_Notice_Table + ".message_type AS msgType",
                Lite_Answer_Comment_Table + ".content AS message",
                Lite_User_Table + ".nickName",
                Lite_User_Table + ".avatarUrl",
                Lite_Question_Target_Table + ".content AS attachName",
                Lite_Answer_Table + ".content AS attachDepict",
                "'comment_answer' AS title",
                Lite_Message_Notice_Table + ".updated_time AS time",
                Lite_Message_Notice_Table + ".content AS remark",
                Lite_Message_Notice_Table + ".flag",
                Lite_Message_Notice_Table + ".attach_id",
                Lite_Message_Notice_Table + ".generate_id",
        };

        return SqlHelper.fromTableName(Lite_Message_Notice_Table, answerCommentParams)
                .leftJoin(Lite_Answer_Comment_Table, Lite_Answer_Comment_Table + ".comment_id", Lite_Message_Notice_Table + ".generate_id")
                .leftJoin(Lite_Answer_Table, Lite_Answer_Table + ".answer_id", Lite_Message_Notice_Table + ".attach_id")
                .leftJoin(Lite_Question_Table, Lite_Question_Table + ".question_id", Lite_Answer_Table + ".question_id")
                .leftJoin(Lite_User_Table, Lite_User_Table + ".uuid", Lite_Answer_Comment_Table + "" + ".uuid")
                .leftJoin(Lite_Question_Target_Table, Lite_Question_Target_Table + ".target", Lite_Question_Table + "" + ".target")
                .eq(Lite_Message_Notice_Table + ".receive_uid", "'" + uuid + "'")
                .eq(Lite_Message_Notice_Table + ".message_type", "'" + Message_Type_Answer_Comment + "'");
    }

    /**
     * 回答被点赞
     *
     * @param uuid
     * @return
     */
    private static SqlHelper getAnswerLikeSqlHelper(String uuid) {
        String[] answerLikeParams = {
                Lite_Message_Notice_Table + ".notice_id",
                Lite_Message_Notice_Table + ".message_type AS msgType",
                Lite_Message_Notice_Table + ".content AS message",
                Lite_User_Table + ".nickName",
                Lite_User_Table + ".avatarUrl",
                Lite_Question_Target_Table + ".content AS attachName",
                Lite_Answer_Table + ".content AS attachDepict",
                Lite_Question_Table + ".title",
                Lite_Message_Notice_Table + ".updated_time AS time",
                Lite_Message_Notice_Table + ".content AS remark",
                Lite_Message_Notice_Table + ".flag",
                Lite_Message_Notice_Table + ".attach_id",
                Lite_Message_Notice_Table + ".generate_id",
        };

        return SqlHelper.fromTableName(Lite_Message_Notice_Table, answerLikeParams)
                .leftJoin(Lite_Answer_Table, Lite_Answer_Table + ".answer_id", Lite_Message_Notice_Table + ".attach_id")
                .leftJoin(Lite_User_Table, Lite_User_Table + ".uuid", Lite_Message_Notice_Table + ".generate_id")
                .leftJoin(Lite_Question_Table, Lite_Question_Table + ".question_id", Lite_Answer_Table + ".question_id")
                .leftJoin(Lite_Question_Target_Table, Lite_Question_Target_Table + ".target", Lite_Question_Table + "" + ".target")
                .eq(Lite_Message_Notice_Table + ".receive_uid", "'" + uuid + "'")
                .eq(Lite_Message_Notice_Table + ".message_type", "'" + Message_Type_Answer_Like + "'");
    }

    /**
     * 问题新增回答
     *
     * @param uuid
     * @return
     */
    private static SqlHelper getNewAnswerSqlHelper(String uuid) {
        String[] newAnswerParams = {
                Lite_Message_Notice_Table + ".notice_id",
                Lite_Message_Notice_Table + ".message_type AS msgType",
                Lite_Answer_Table + ".content AS message",
                Lite_User_Table + ".nickName",
                Lite_User_Table + ".avatarUrl",
                Lite_Question_Target_Table + ".content AS attachName",
                Lite_Question_Table + ".depict AS attachDepict",
                Lite_Question_Table + ".title",
                Lite_Message_Notice_Table + ".updated_time AS time",
                Lite_Answer_Table + ".point AS remark",
                Lite_Message_Notice_Table + ".flag",
                Lite_Message_Notice_Table + ".attach_id",
                Lite_Message_Notice_Table + ".generate_id",
        };

        return SqlHelper.fromTableName(Lite_Message_Notice_Table, newAnswerParams)
                .leftJoin(Lite_Answer_Table, Lite_Answer_Table + ".answer_id", Lite_Message_Notice_Table + ".generate_id")
                .leftJoin(Lite_User_Table, Lite_User_Table + ".uuid", Lite_Answer_Table + ".uuid")
                .leftJoin(Lite_Question_Table, Lite_Question_Table + ".question_id", Lite_Message_Notice_Table + ".attach_id")
                .leftJoin(Lite_Question_Target_Table, Lite_Question_Target_Table + ".target", Lite_Question_Table + "" + ".target")

                .eq(Lite_Message_Notice_Table + ".receive_uid", "'" + uuid + "'")
                .eq(Lite_Message_Notice_Table + ".message_type", "'" + Message_Type_Question_New_Answer + "'");
    }

    /**
     * 问题新增关注
     *
     * @param uuid
     * @return
     */
    private static SqlHelper getNewFollowSqlHelper(String uuid) {
        String[] newFollowParams = {
                Lite_Message_Notice_Table + ".notice_id",
                Lite_Message_Notice_Table + ".message_type AS msgType",
                Lite_Message_Notice_Table + ".content AS message",
                Lite_User_Table + ".nickName",
                Lite_User_Table + ".avatarUrl",
                Lite_Question_Target_Table + ".content AS attachName",
                Lite_Question_Table + ".depict AS attachDepict",
                Lite_Question_Table + ".title",
                Lite_Message_Notice_Table + ".updated_time AS time",
                Lite_Message_Notice_Table + ".content AS remark",
                Lite_Message_Notice_Table + ".flag",
                Lite_Message_Notice_Table + ".attach_id",
                Lite_Message_Notice_Table + ".generate_id",
        };

        return SqlHelper.fromTableName(Lite_Message_Notice_Table, newFollowParams)
                .leftJoin(Lite_User_Table, Lite_User_Table + ".uuid", Lite_Message_Notice_Table + ".generate_id")
                .leftJoin(Lite_Question_Table, Lite_Question_Table + ".question_id", Lite_Message_Notice_Table + ".attach_id")
                .innerJoin(Lite_Question_Target_Table, Lite_Question_Target_Table + ".target", Lite_Question_Table + "" + ".target")

                .eq(Lite_Message_Notice_Table + ".receive_uid", "'" + uuid + "'")
                .eq(Lite_Message_Notice_Table + ".message_type", "'" + Message_Type_Question_Follow + "'");
    }
}
