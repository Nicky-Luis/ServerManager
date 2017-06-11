package com.junlin.manager.app.service;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Record;
import com.junlin.manager.GlobalConstants;
import com.junlin.manager.app.entity.Answer;
import com.junlin.manager.app.entity.AnswerImage;
import com.junlin.manager.app.entity.AnswerLike;
import com.junlin.manager.app.entity.Comment;
import com.junlin.manager.app.entity.CommentLike;
import com.junlin.manager.app.entity.NoticeMessage;
import com.junlin.manager.app.entity.UserRelation;
import com.junlin.manager.app.utils.VerifyUtils;
import com.junlin.manager.utils.RandomUtils;
import com.junlin.manager.utils.SqlHelper;
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

import static com.junlin.manager.GlobalConstants.Lite_Answer_Table;
import static com.junlin.manager.utils.TimeUtils.setDeadlineFormat;

/**
 * Created by junlinhui eight on 2017/4/19.
 */
public class AnswerService {
    //logger
    private static Logger logger = Logger.getLogger(AnswerService.class.getName());

    /**
     * 创建新的回答
     *
     * @param sessionKey
     * @param questionId
     * @param point
     * @param content
     * @return
     */
    public static String createNewAnswer(String sessionKey, String questionId, String point, String content) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");


        if (!StringUtils.nonEmptyString(content.trim())) {
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "内容不能为空");
            return new Gson().toJson(resultMap);
        }

        //生成问题id
        String answerId = RandomUtils.getRandomWord(18);
        logger.error("生成AnswerId：" + answerId);
        Answer answer = Answer.findAnswerById(answerId);
        if (null == answer) {
            //创建一条新的回答
            Answer.newAnswer(questionId, point, content, uuid, answerId);

            resultMap.put("code", VerifyUtils.Code_Succeed);
            resultMap.put("msg", "成功");
            resultMap.put("answerId", answerId);
        } else {
            logger.error("AnswerId 已经存在");
        }
        return new Gson().toJson(resultMap);
    }


    /**
     * 用户删除回答
     *
     * @param sessionKey
     * @param answerId
     * @return
     */
    public static String deleteAnswer(String sessionKey, String answerId) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }

        try {
            Answer.deleteByAnswerById(answerId);
            AnswerImage.deleteByAnswerId(answerId);
            AnswerLike.deleteByAnswerId(answerId);
            CommentLike.deleteByAnswerId(answerId);
            Comment.deleteByAnswerId(answerId);
            NoticeMessage.deleteByAttachId(answerId);
            resultMap.put("code", VerifyUtils.Code_Succeed);
            resultMap.put("msg", "成功");
            resultMap.put("answerId", answerId);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", VerifyUtils.Err_Code_Operate);
            resultMap.put("msg", "操作失败");
        }

        return new Gson().toJson(resultMap);
    }

    /**
     * 保存回答的图片
     *
     * @param answerId
     * @param urls
     * @return
     */
    public static String saveAnswerImage(String answerId, String urls) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", -1);
        resultMap.put("msg", "失败");
        if (!StringUtils.nonEmptyString(answerId) || !StringUtils.nonEmptyString(urls)) {
            return new Gson().toJson(resultMap);
        }

        String[] urlList = urls.split(",");
        if (urlList.length > 0) {
            for (String url : urlList) {
                logger.info("url:" + url);
                AnswerImage.newAnswerImage(answerId, url);
            }
            resultMap.put("code", 1);
            resultMap.put("msg", "成功");
        }
        return new Gson().toJson(resultMap);
    }

    /**
     * 搜索回答详情
     *
     * @param sessionKey
     * @param answerId
     * @return
     */
    public static String searchAnswer(String sessionKey, String answerId) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        if (!StringUtils.nonEmptyString(answerId)) {
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "参数有误");
            return new Gson().toJson(resultMap);
        }

        //查找
        String[] obtainParams = {
                Lite_Answer_Table + ".point",
                Lite_Answer_Table + ".content",
                Lite_Answer_Table + ".updated_time As time",
                GlobalConstants.Lite_User_Table + ".nickName As answerName",
                GlobalConstants.Lite_User_Table + ".avatarUrl As answerHeadImg",
                GlobalConstants.Lite_User_Table + ".signature As signature",
                GlobalConstants.Lite_User_Table + ".uuid",
                GlobalConstants.Lite_Question_Table + ".question_id",
                "GROUP_CONCAT(" + GlobalConstants.Lite_Answer_Image_Table + ".url) AS imageArray"
        };

        SqlHelper sqlHelper = SqlHelper.fromTableName(Lite_Answer_Table, obtainParams)
                .innerJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                        Lite_Answer_Table + ".uuid")
                .leftJoin(GlobalConstants.Lite_Answer_Image_Table, GlobalConstants.Lite_Answer_Image_Table + ".answer_id",
                        Lite_Answer_Table + ".answer_id")
                .innerJoin(GlobalConstants.Lite_Question_Table, GlobalConstants.Lite_Question_Table + ".question_id",
                        Lite_Answer_Table + ".question_id")
                .eq(Lite_Answer_Table + ".answer_id", "'" + answerId + "'");
        //分页查询具体数据
        Record record = sqlHelper.findFirst();

        resultMap.put("msg", "操作成功");
        resultMap.put("result", resultAnswerJson(record, uuid));
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }


    /***
     * 查找答案的信息
     * @param sessionKey
     * @param answerId
     * @return
     */
    public static String searchAnswerMessage(String sessionKey, String answerId) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        if (!StringUtils.nonEmptyString(answerId)) {
            resultMap.put("msg", "参数有误");
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            return new Gson().toJson(resultMap);
        }

        //操作成功
        resultMap.put("code", 1);
        resultMap.put("msg", "操作成功");
        resultMap.put("isLike", AnswerLike.isLike(answerId, uuid));
        resultMap.put("likeCount", AnswerLike.findLikeCount(answerId));
        resultMap.put("commentCount", Comment.findCommentCount(answerId));
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 点赞与取消
     *
     * @param answerId
     * @param sessionKey
     * @param like
     * @return
     */
    public static String likeHandler(String answerId, String sessionKey, boolean like) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        boolean result;
        //添加关注
        if (like) {
            result = AnswerLike.addLike(answerId, uuid);
        } else {
            result = AnswerLike.deleteLike(answerId, uuid);
        }
        //结果
        if (result) {
            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
        }
        return new Gson().toJson(resultMap);
    }


    /**
     * 查询用户所回答过的列表
     *
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     * @return
     */
    public static String findAllAnswered(String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");
        getUserAnswered(pageStr, pageCount, resultMap, uuid);

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 查询用户所回答过的列表
     *
     * @param uuid
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     * @return
     */
    public static String findAllAnswered(String uuid, String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        getUserAnswered(pageStr, pageCount, resultMap, uuid);

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    //////////////////////////////////private method/////////////////////

    /**
     * 获取用户回答过的问题
     *
     * @param pageStr
     * @param pageCount
     * @param resultMap
     * @param uuid
     */
    private static void getUserAnswered(String pageStr, int pageCount, Map<String, Object> resultMap, String uuid) {
        int page = Integer.valueOf(pageStr);
        if (page > 0) {
            String[] obtainParams = {
                    GlobalConstants.Lite_Question_Table + ".question_id",
                    GlobalConstants.Lite_Question_Table + ".title",
                    GlobalConstants.Lite_Question_Table + ".deadline",
                    GlobalConstants.Lite_Question_Target_Table + ".target",
                    GlobalConstants.Lite_Question_Target_Table + ".content AS targetName",
                    Lite_Answer_Table + ".answer_id",
                    Lite_Answer_Table + ".content",
                    GlobalConstants.Lite_Question_Label_Table + ".label",
                    GlobalConstants.Lite_Question_Label_Table + ".content AS labelName",
                    GlobalConstants.Lite_Question_Table + ".price",
                    Lite_Answer_Table + ".updated_time AS time",
                    "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Comment_Table + ".comment_id) AS commentCount",
                    "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Like_Table + ".created_time) AS likeCount",
            };

            SqlHelper sqlHelper = SqlHelper.fromTableName(Lite_Answer_Table, obtainParams)
                    .innerJoin(GlobalConstants.Lite_Question_Table, GlobalConstants.Lite_Question_Table + ".question_id",
                            Lite_Answer_Table + ".question_id")
                    .leftJoin(GlobalConstants.Lite_Answer_Comment_Table, GlobalConstants.Lite_Answer_Comment_Table + ".answer_id",
                            Lite_Answer_Table + ".answer_id")
                    .leftJoin(GlobalConstants.Lite_Answer_Like_Table, GlobalConstants.Lite_Answer_Like_Table + ".answer_id",
                            Lite_Answer_Table + ".answer_id")
                    .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                            GlobalConstants.Lite_Question_Table + ".target")
                    .leftJoin(GlobalConstants.Lite_Question_Label_Table, GlobalConstants.Lite_Question_Label_Table + ".label",
                            GlobalConstants.Lite_Question_Table + ".label")
                    .eq(Lite_Answer_Table + ".uuid", "'" + uuid + "'");
            //分页查询具体数据
            List<Record> records = sqlHelper
                    .gruopBy(Lite_Answer_Table + ".answer_id")
                    .orderByDesc("time")
                    .paginate(page, pageCount);

            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
            resultMap.put("count", records.size());
            resultMap.put("result", resultQuestionJson(records));
        }
    }


    /***
     * 返回结果设置
     * @param record
     * @return
     */
    private static Map<String, Object> resultAnswerJson(Record record, String fansID) {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String[] attrsName = record.getColumnNames();
        Object[] attrsValue = record.getColumnValues();
        //是否关注
        String uuid = record.getStr("uuid");
        if (StringUtils.nonEmptyString(uuid)) {
            //用户自己
            dataMap.put("isSelf", uuid.equals(fansID));
            //是否已经关注了该用户
            dataMap.put("isFollow", UserRelation.isFollow(uuid, fansID));
        }

        for (int index = 0; index < attrsName.length; index++) {
            //修改发布的时间的格式
            if (attrsName[index].equals("time")) {
                PrettyTime prettyTime = new PrettyTime(Locale.CHINESE);
                prettyTime.removeUnit(Second.class);
                prettyTime.removeUnit(JustNow.class);
                prettyTime.removeUnit(Millisecond.class);
                Double time = (Double) attrsValue[index];
                dataMap.put(attrsName[index], prettyTime.format(new Date(time.longValue())).replace(" ", ""));
            } else if (attrsName[index].equals("imageArray")) {
                if (null != attrsValue[index]) {
                    dataMap.put(attrsName[index], ((String) attrsValue[index]).split(","));
                } else {
                    dataMap.put(attrsName[index], "");
                }
            } else {
                dataMap.put(attrsName[index], attrsValue[index]);
            }
        }
        return dataMap;
    }


    /***
     * 返回结果设置
     * @param records
     * @return
     */
    private static List<Map<String, Object>> resultQuestionJson(List<Record> records) {
        List<Map<String, Object>> dataMaps = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            String[] attrsName = record.getColumnNames();
            Object[] attrsValue = record.getColumnValues();


            //修改发布的时间的格式
            for (int index = 0; index < attrsName.length; index++) {
                if (attrsName[index].equals("time")) {
                    PrettyTime prettyTime = new PrettyTime(Locale.CHINESE);
                    prettyTime.removeUnit(JustNow.class);
                    prettyTime.removeUnit(Millisecond.class);
                    prettyTime.removeUnit(Second.class);
                    Double time = (Double) attrsValue[index];
                    String value = prettyTime.format(new Date(time.longValue())).replace(" ", "");
                    dataMap.put(attrsName[index], value);
                } else if (attrsName[index].equals("deadline")) {
                    setDeadlineFormat(dataMap, attrsName[index], attrsValue[index]);
                } else {
                    dataMap.put(attrsName[index], attrsValue[index]);
                }
            }
            dataMaps.add(dataMap);
        }
        return dataMaps;
    }
}
