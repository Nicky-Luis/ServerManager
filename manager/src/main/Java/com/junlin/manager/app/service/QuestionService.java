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
import com.junlin.manager.app.entity.Question;
import com.junlin.manager.app.entity.QuestionFollow;
import com.junlin.manager.app.entity.QuestionImage;
import com.junlin.manager.app.utils.VerifyUtils;
import com.junlin.manager.utils.RandomUtils;
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

import static com.junlin.manager.utils.TimeUtils.setDeadlineFormat;

/**
 * Created by junlinhui eight on 2017/4/13.
 * 问题处理
 */
public class QuestionService {

    //logger
    private static Logger logger = Logger.getLogger(QuestionService.class.getName());

    /**
     * 创建新的问题
     *
     * @param sessionKey
     * @param deadline
     * @param target
     * @param depict
     * @param tagType
     * @param price
     * @param limit
     * @param reward
     * @return
     */
    public static String createNewQuestion(String sessionKey, String deadline, int target, String title, String depict,
                                           int tagType, int price, int limit, int reward) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        Long deadLineTime = TimeUtils.getTimestamp(deadline + " 00:00:00");
        logger.error("deadLineTime：" + deadLineTime);
        if (deadLineTime <= System.currentTimeMillis()) {
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "时间范围有误");
            return new Gson().toJson(resultMap);
        }

//        if (!StringUtils.nonEmptyString(depict.trim())) {
//            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
//            resultMap.put("msg", "描述不能为空");
//            return new Gson().toJson(resultMap);
//        }

        if (!StringUtils.nonEmptyString(title.trim())) {
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "标题不能为空");
            return new Gson().toJson(resultMap);
        }


        //生成问题id
        String questionId = RandomUtils.getRandomWord(15);
        Question question = Question.findQuestionById(questionId);
        if (null == question) {
            Question.newQuestion(target, depict, tagType, price, limit, reward, uuid, deadLineTime, questionId, title);
            resultMap.put("msg", "操作成功");
            resultMap.put("question_id", questionId);
        } else {
            logger.error("questionId 已经存在");
        }
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }


    /**
     * 保存问题的图片
     *
     * @param questionId
     * @param urls
     * @return
     */
    public static String saveQuestionImage(String questionId, String urls) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", -1);
        resultMap.put("msg", "保存图片失败");
        if (!StringUtils.nonEmptyString(questionId) || !StringUtils.nonEmptyString(urls)) {
            return new Gson().toJson(resultMap);
        }

        //解析

        String[] urlList = urls.split(",");
        if (urlList.length > 0) {
            for (String url : urlList) {
                logger.info("url:" + url);
                QuestionImage.newImageRelation(questionId, url);
            }
            resultMap.put("code", 0);
            resultMap.put("msg", "成功");
        }
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }


    /**
     * 查询问题详细信息
     *
     * @param questionId
     * @param sessionKey
     * @return
     */
    public static String startSearch(String questionId, String sessionKey) {

        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        String[] obtainParams = {
                GlobalConstants.Lite_Question_Table + ".deadline",
                GlobalConstants.Lite_Question_Target_Table + ".content AS targetName",
                GlobalConstants.Lite_Question_Table + ".target",
                GlobalConstants.Lite_Question_Table + ".uuid",
                GlobalConstants.Lite_Question_Table + ".title",
                GlobalConstants.Lite_Question_Table + ".depict AS descriptionTxt",
                GlobalConstants.Lite_Question_Table + ".label AS flag",
                GlobalConstants.Lite_Question_Table + ".price",
                GlobalConstants.Lite_Question_Table + ".reward_limit",
                GlobalConstants.Lite_Question_Table + ".reward_type",
                GlobalConstants.Lite_Question_Table + ".created_time",
                GlobalConstants.Lite_User_Table + ".nickName AS userName",
                GlobalConstants.Lite_User_Table + ".avatarUrl AS userHeadImg",
                "GROUP_CONCAT(" + GlobalConstants.Lite_Question_Image_Table + ".url)  AS imageArray"
        };

        SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Question_Table, obtainParams)
                .innerJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid", GlobalConstants.Lite_Question_Table + ".uuid")
                .leftJoin(GlobalConstants.Lite_Question_Image_Table, GlobalConstants.Lite_Question_Image_Table + ".question_id",
                        GlobalConstants.Lite_Question_Table + ".question_id")
                .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                        GlobalConstants.Lite_Question_Table + ".target")
                .eq(GlobalConstants.Lite_Question_Table + ".question_id", "'" + questionId + "'");
        Record record = sqlHelper.findFirst();
        //查询用户是否已经关注,关注数量
        QuestionFollow followRelation = QuestionFollow.findFollowRelation(questionId, uuid);
        int followCount = QuestionFollow.findFollowCount(questionId);
        int answerCount = Answer.findAnswerCount(questionId);
        boolean isMyself = record.getStr("uuid").equals(uuid);

        resultMap.put("msg", "成功");
        resultMap.put("result", resultSearchJson(isMyself, record, followRelation != null, followCount, answerCount));
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 关注与取消关注问题
     *
     * @param questionId
     * @param sessionKey
     * @param follow
     * @return
     */
    public static String followHandler(String questionId, String sessionKey, boolean follow) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        boolean resultValue;
        //添加关注
        if (follow) {
            resultValue = QuestionFollow.addFollowRelation(questionId, uuid);
        } else {
            resultValue = QuestionFollow.deleteFollowRelation(questionId, uuid);
        }
        //结果
        if (resultValue) {
            resultMap.put("msg", "操作成功");
        } else {
            resultMap.put("msg", "操作失败");
        }
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 查询问题的回答
     *
     * @param questionId
     * @param sessionKey
     * @param point
     * @param page
     * @param pageCount
     * @return
     */
    public static String findAnswers(String questionId, String sessionKey, String point, int page, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        if (page > 0) {
            String[] obtainParams = {
                    GlobalConstants.Lite_Answer_Table + ".answer_id",
                    GlobalConstants.Lite_Answer_Table + ".point",
                    GlobalConstants.Lite_Answer_Table + ".content",
                    GlobalConstants.Lite_Answer_Table + ".updated_time As time",
                    GlobalConstants.Lite_User_Table + ".nickName As userName",
                    GlobalConstants.Lite_User_Table + ".avatarUrl As userHeadImg",
                    "COUNT(" + GlobalConstants.Lite_Answer_Like_Table + ".answer_id) AS likeCount",
            };

            SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Answer_Table, obtainParams)
                    .innerJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                            GlobalConstants.Lite_Answer_Table + ".uuid")
                    .leftJoin(GlobalConstants.Lite_Answer_Like_Table, GlobalConstants.Lite_Answer_Like_Table + ".answer_id",
                            GlobalConstants.Lite_Answer_Table + ".answer_id")
                    .eq(GlobalConstants.Lite_Answer_Table + ".question_id", "'" + questionId + "'")
                    .eq(GlobalConstants.Lite_Answer_Table + ".point", point);
            //分页查询具体数据
            List<Record> records = sqlHelper
                    .gruopBy(GlobalConstants.Lite_Answer_Table + ".answer_id")
                    .orderByDesc("likeCount")
                    .paginate(page, pageCount);

            resultMap.put("msg", "操作成功");
            resultMap.put("count", records.size());
            resultMap.put("result", resultAnswersJson(records));
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }


    /**
     * 查询用户所发布的问题列表
     *
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     * @return
     */
    public static String findAskQuestion(String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");
        getAskQuestion(pageStr, pageCount, resultMap, uuid);

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 查询用户所发布的问题列表
     *
     * @param uuid
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     * @return
     */
    public static String findAskQuestion(String uuid, String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        getAskQuestion(pageStr, pageCount, resultMap, uuid);

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 查询用户已经关注的话题列表
     *
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     * @return
     */
    public static String findFollowed(String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");
        getUserFollowedQuestion(pageStr, pageCount, resultMap, uuid);

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 查询用户已经关注的话题列表
     *
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     * @return
     */
    public static String findFollowed(String uuid, String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        getUserFollowedQuestion(pageStr, pageCount, resultMap, uuid);

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 删除用户发布的问题
     *
     * @param sessionKey
     * @param questionId
     * @return
     */
    public static String deleteQuestion(String sessionKey, String questionId) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        List<Answer> answerList = Answer.findAnswerListByQuestionId(questionId);
        if (null != answerList) {
            List<String> list = new ArrayList<>();
            for (Answer answer : answerList) {
                list.add(answer.getStr("answer_id"));
            }
            try {
                Answer.deleteByAnswerByIds(list);
                AnswerImage.deleteByAnswerByIds(list);
                AnswerLike.deleteByAnswerByIds(list);
                CommentLike.deleteByAnswerByIds(list);
                Comment.deleteByAnswerByIds(list);
                NoticeMessage.deleteByByIds(list);
                Question.deleteQuestionById(questionId);
                NoticeMessage.deleteByAttachId(questionId);
                resultMap.put("code", VerifyUtils.Code_Succeed);
                resultMap.put("msg", "成功");
            } catch (Exception e) {
                e.printStackTrace();
                resultMap.put("code", VerifyUtils.Err_Code_Operate);
                resultMap.put("msg", "操作失败");
            }
        } else {
            resultMap.put("code", VerifyUtils.Err_Code_Operate);
            resultMap.put("msg", "操作失败");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }
    ///////////////////////////////////private method//////////////////////////////////////////

    /***
     * 获取用户关注的问题
     * @param pageStr
     * @param pageCount
     * @param resultMap
     * @param uuid
     */
    private static void getUserFollowedQuestion(String pageStr, int pageCount, Map<String, Object> resultMap, String uuid) {
        int page = Integer.valueOf(pageStr);
        if (page > 0) {
            String[] obtainParams = {
                    GlobalConstants.Lite_Question_Table + ".question_id",
                    GlobalConstants.Lite_Question_Target_Table + ".target",
                    GlobalConstants.Lite_Question_Target_Table + ".content AS subject",
                    GlobalConstants.Lite_Question_Table + ".deadline",
                    GlobalConstants.Lite_Question_Table + ".depict",
                    GlobalConstants.Lite_Question_Table + ".title",
                    GlobalConstants.Lite_Question_Label_Table + ".label",
                    GlobalConstants.Lite_Question_Label_Table + ".content AS flag",
                    GlobalConstants.Lite_Question_Table + ".price",
                    GlobalConstants.Lite_Question_Table + ".updated_time AS time",
                    GlobalConstants.Lite_User_Table + ".nickName As userName",
                    GlobalConstants.Lite_User_Table + ".avatarUrl As userHeadImg",
                    "COUNT(distinct " + GlobalConstants.Lite_Question_Follow_Table + ".question_id) AS followCount",
                    "COUNT(distinct " + GlobalConstants.Lite_Answer_Table + ".answer_id) AS answerCount",
            };

            SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Question_Table, obtainParams)
                    .leftJoin(GlobalConstants.Lite_Question_Follow_Table, GlobalConstants.Lite_Question_Follow_Table + ".question_id",
                            GlobalConstants.Lite_Question_Table + ".question_id")
                    .leftJoin(GlobalConstants.Lite_Answer_Table, GlobalConstants.Lite_Answer_Table + ".question_id",
                            GlobalConstants.Lite_Question_Table + ".question_id")
                    .innerJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                            GlobalConstants.Lite_Question_Table + ".uuid")
                    .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                            GlobalConstants.Lite_Question_Table + ".target")
                    .leftJoin(GlobalConstants.Lite_Question_Label_Table, GlobalConstants.Lite_Question_Label_Table + ".label",
                            GlobalConstants.Lite_Question_Table + ".label")
                    .eq(GlobalConstants.Lite_Question_Follow_Table + ".uuid", "'" + uuid + "'");
            //分页查询具体数据
            List<Record> records = sqlHelper
                    .gruopBy(GlobalConstants.Lite_Question_Table + ".question_id")
                    .orderByDesc("time")
                    .paginate(page, pageCount);

            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
            resultMap.put("count", records.size());
            resultMap.put("result", resultQuestionJson(records));
        }
    }

    /**
     * 获取用户发布的问题
     *
     * @param pageStr
     * @param pageCount
     * @param resultMap
     * @param uuid
     */
    private static void getAskQuestion(String pageStr, int pageCount, Map<String, Object> resultMap, String uuid) {
        int page = Integer.valueOf(pageStr);
        if (page > 0) {
            String[] obtainParams = {
                    GlobalConstants.Lite_Question_Table + ".question_id",
                    GlobalConstants.Lite_Question_Table + ".deadline",
                    GlobalConstants.Lite_Question_Table + ".title",
                    GlobalConstants.Lite_Question_Target_Table + ".target",
                    GlobalConstants.Lite_Question_Target_Table + ".content AS subject",
                    GlobalConstants.Lite_Question_Table + ".depict",
                    GlobalConstants.Lite_Question_Label_Table + ".label",
                    GlobalConstants.Lite_Question_Label_Table + ".content AS flag",
                    GlobalConstants.Lite_Question_Table + ".price",
                    GlobalConstants.Lite_Question_Table + ".updated_time AS time",
                    GlobalConstants.Lite_User_Table + ".nickName As userName",
                    GlobalConstants.Lite_User_Table + ".avatarUrl As userHeadImg",
                    "COUNT(distinct " + GlobalConstants.Lite_Question_Follow_Table + ".question_id) AS followCount",
                    "COUNT(distinct " + GlobalConstants.Lite_Answer_Table + ".answer_id) AS answerCount",
            };

            SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Question_Table, obtainParams)
                    .leftJoin(GlobalConstants.Lite_Question_Follow_Table, GlobalConstants.Lite_Question_Follow_Table + ".question_id",
                            GlobalConstants.Lite_Question_Table + ".question_id")
                    .leftJoin(GlobalConstants.Lite_Answer_Table, GlobalConstants.Lite_Answer_Table + ".question_id",
                            GlobalConstants.Lite_Question_Table + ".question_id")
                    .innerJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                            GlobalConstants.Lite_Question_Table + ".uuid")
                    .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                            GlobalConstants.Lite_Question_Table + ".target")
                    .leftJoin(GlobalConstants.Lite_Question_Label_Table, GlobalConstants.Lite_Question_Label_Table + ".label",
                            GlobalConstants.Lite_Question_Table + ".label")
                    .eq(GlobalConstants.Lite_Question_Table + ".uuid", "'" + uuid + "'");
            //分页查询具体数据
            List<Record> records = sqlHelper
                    .gruopBy(GlobalConstants.Lite_Question_Table + ".question_id")
                    .orderByDesc("time")
                    .paginate(page, pageCount);

            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "操作成功");
            resultMap.put("count", records.size());
            resultMap.put("result", resultQuestionJson(records));
        } else {
            resultMap.put("code", 1);
            resultMap.put("msg", "操作失败");
        }
    }

    /***
     * 返回结果设置
     * @param record
     * @param isFollow
     * @param followCount
     * @param answerCount
     * @return
     */
    private static Map<String, Object> resultSearchJson(boolean isMyself, Record record, boolean isFollow, int followCount, int answerCount) {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String[] attrsName = record.getColumnNames();
        Object[] attrsValue = record.getColumnValues();
        for (int index = 0; index < attrsName.length; index++) {
            //修改发布的时间的格式
            if (null != attrsName[index]) {
                switch (attrsName[index]) {
                    case "imageArray": {
                        if (null != attrsValue[index]) {
                            dataMap.put(attrsName[index], (String.valueOf(attrsValue[index])).split(","));
                        } else {
                            dataMap.put(attrsName[index], "");
                        }
                    }
                    break;

                    case "deadline":
                        setDeadlineFormat(dataMap, attrsName[index], attrsValue[index]);
                        break;

                    default:
                        dataMap.put(attrsName[index], attrsValue[index]);
                        break;
                }
            }
        }
        //是否关注了该问题
        dataMap.put("isFollow", isFollow);
        dataMap.put("followCount", followCount);
        dataMap.put("answerCount", answerCount);
        dataMap.put("isMyself", isMyself);

        return dataMap;
    }

    /***
     * 返回结果设置
     * @param records
     * @return
     */
    private static List<Map<String, Object>> resultAnswersJson(List<Record> records) {
        List<Map<String, Object>> dataMaps = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            String[] attrsName = record.getColumnNames();
            Object[] attrsValue = record.getColumnValues();

            for (int index = 0; index < attrsName.length; index++) {
                //修改发布的时间的格式
                switch (attrsName[index]) {
                    case "time": {
                        PrettyTime prettyTime = new PrettyTime(Locale.CHINESE);
                        prettyTime.removeUnit(JustNow.class);
                        prettyTime.removeUnit(Second.class);
                        prettyTime.removeUnit(Millisecond.class);

                        Double time = (Double) attrsValue[index];
                        dataMap.put(attrsName[index], prettyTime.format(new Date(time.longValue())).replace(" ", ""));
                    }
                    break;

                    case "content": {
                        String content = attrsValue[index].toString();
                        if (content.length() > 300) {
                            String subContent = content.substring(0, 300);
                            dataMap.put(attrsName[index], subContent);
                        } else {
                            dataMap.put(attrsName[index], content);
                        }
                    }
                    break;

                    case "likeCount": {
                        if (null == attrsValue[index]) {
                            dataMap.put(attrsName[index], 0);
                        } else {
                            dataMap.put(attrsName[index], attrsValue[index]);
                        }
                    }
                    break;

                    default:
                        dataMap.put(attrsName[index], attrsValue[index]);
                        break;
                }
            }
            dataMaps.add(dataMap);
        }
        return dataMaps;
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

            PrettyTime prettyTime = new PrettyTime(Locale.CHINESE);
            prettyTime.removeUnit(Second.class);
            prettyTime.removeUnit(JustNow.class);
            prettyTime.removeUnit(Millisecond.class);

            //修改发布的时间的格式
            for (int index = 0; index < attrsName.length; index++) {
                if (attrsName[index].equals("time")) {
                    Double time = (Double) attrsValue[index];
                    dataMap.put(attrsName[index], prettyTime.format(new Date(time.longValue())).replace(" ", ""));
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
