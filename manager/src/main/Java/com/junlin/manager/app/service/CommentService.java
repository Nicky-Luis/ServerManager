package com.junlin.manager.app.service;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Record;
import com.junlin.manager.app.entity.Comment;
import com.junlin.manager.app.entity.CommentLike;
import com.junlin.manager.app.utils.VerifyUtils;
import com.junlin.manager.utils.RandomUtils;
import com.junlin.manager.utils.SqlHelper;
import com.mchange.v2.lang.StringUtils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.junlin.manager.GlobalConstants.Lite_Answer_Comment_Table;
import static com.junlin.manager.GlobalConstants.Lite_Comment_Like_Table;
import static com.junlin.manager.GlobalConstants.Lite_User_Table;
import static com.junlin.manager.utils.TimeUtils.setTimePretty;

/**
 * Created by junlinhui eight on 2017/4/13.
 * 问题处理
 */
public class CommentService {

    //logger
    private static Logger logger = Logger.getLogger(CommentService.class.getName());

    /**
     * 创建新的评论
     *
     * @param sessionKey
     * @param answerId
     * @param replyUUID
     * @param comment
     * @return
     */
    public static String createNewComment(String sessionKey, String answerId, String replyUUID, String comment) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        if (!StringUtils.nonEmptyString(comment.trim())) {
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "内容不能为空");
            return new Gson().toJson(resultMap);
        }

        //生成评论的id
        String commentId = RandomUtils.getRandomWord(15);
        Comment question = Comment.findCommentById(commentId);
        if (null == question) {
            Comment.newComment(answerId, replyUUID, comment, uuid, commentId);
            resultMap.put("msg", "操作成功");
            resultMap.put("comment_id", commentId);
        } else {
            logger.error("questionId 已经存在");
        }
        return new Gson().toJson(resultMap);
    }


    /**
     * 查询回答的评论数
     *
     * @param answerId
     * @param sessionKey
     * @return
     */
    public static String startSearch(String answerId, String sessionKey, int page, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        if (!StringUtils.nonEmptyString(answerId)) {
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "参数错误");
            return new Gson().toJson(resultMap);
        }

        String[] obtainParams = {
                Lite_Answer_Comment_Table + ".comment_id",
                Lite_Answer_Comment_Table + ".content",
                Lite_Answer_Comment_Table + ".updated_time AS time",
                Lite_User_Table + ".nickName AS userName",
                Lite_User_Table + ".avatarUrl AS userHeadImg",
                "COUNT(" + Lite_Comment_Like_Table + ".uuid) AS likeCount",
        };

        SqlHelper sqlHelper = SqlHelper.fromTableName(Lite_Answer_Comment_Table, obtainParams)
                .innerJoin(Lite_User_Table, Lite_User_Table + ".uuid", Lite_Answer_Comment_Table + ".uuid")
                .leftJoin(Lite_Comment_Like_Table, Lite_Comment_Like_Table + ".comment_id", Lite_Answer_Comment_Table + ".comment_id")
                .eq(Lite_Answer_Comment_Table + ".answer_id", "'" + answerId + "'");

        //分页查询具体数据
        List<Record> records = sqlHelper.gruopBy(Lite_Answer_Comment_Table + ".comment_id")
                .orderByDesc("time").paginate(page, pageCount);

        resultMap.put("msg", "操作成功");
        resultMap.put("count", records.size());
        resultMap.put("result", resultCommentsJson(records, uuid));

        //打印结果
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 给评论点赞
     *
     * @param commentId
     * @param sessionKey
     * @param follow
     * @return
     */
    public static String likeHandler(String answerId,String commentId, String sessionKey, boolean follow) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        boolean result;
        //给评论点赞
        if (follow) {
            result = CommentLike.addLikeRelation(answerId,commentId, uuid);
        } else {
            result = CommentLike.deleteLikeRelation(commentId, uuid);
        }
        //结果
        if (result) {
            resultMap.put("msg", "操作成功");
        }
        return new Gson().toJson(resultMap);
    }


    ///////////////////////////////////private method//////////////////////////////////////////

    /***
     * 返回结果设置
     * @param records
     * @return
     */
    private static List<Map<String, Object>> resultCommentsJson(List<Record> records, String uuid) {
        List<Map<String, Object>> dataMaps = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            String[] attrsName = record.getColumnNames();
            Object[] attrsValue = record.getColumnValues();

            for (int index = 0; index < attrsName.length; index++) {
                //修改发布的时间的格式
                switch (attrsName[index]) {
                    case "comment_id":
                        //查询用户是否已经关注,关注数量
                        dataMap.put("comment_id", String.valueOf(attrsValue[index]));
                        dataMap.put("isLike", CommentLike.isLike(String.valueOf(attrsValue[index]), uuid));
                        break;

                    case "time":
                        setTimePretty(dataMap, attrsName[index], attrsValue[index]);
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

}
