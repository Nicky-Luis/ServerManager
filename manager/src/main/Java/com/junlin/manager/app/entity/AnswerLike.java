package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;

import java.util.List;

/**
 * Created by junlinhui eight on 2017/4/14.
 * 点赞操作
 */
public class AnswerLike extends Model<AnswerLike> {

    //utils
    public static final AnswerLike dao = new AnswerLike();

    /**
     * 添加点赞信息
     *
     * @param answer_id
     * @param uuid
     * @return
     */
    public static boolean addLike(String answer_id, String uuid) {
        if (!isLike(answer_id, uuid)) {
            new AnswerLike()
                    .set("uuid", uuid)
                    .set("answer_id", answer_id)
                    .set("created_time", System.currentTimeMillis())
                    .set("updated_time", System.currentTimeMillis())
                    .save();
            newAnswerLikeMsg(answer_id, uuid);
            return true;
        }
        return false;
    }


    /***
     * 删除点赞信息
     */
    public static void deleteByAnswerId(String answerId) {
        Db.update("DELETE  FROM " + GlobalConstants.Lite_Answer_Like_Table + " WHERE answer_id = '" + answerId + "'");
    }

    /***通过回答ID删除一系列
     *
     * @param answerIds
     */
    public static void deleteByAnswerByIds(List<String> answerIds) {
        if (answerIds != null && answerIds.size() > 0) {
            StringBuilder answerValue = new StringBuilder();
            answerValue.append("'").append(answerIds.get(0)).append("'");
            for (int index = 1; index < answerIds.size(); index++) {
                answerValue.append(",'").append(answerIds.get(index)).append("'");
            }
            Db.update("DELETE FROM " + GlobalConstants.Lite_Answer_Like_Table + " WHERE answer_id IN (" + answerValue.toString() + ")");
        }
    }

    /**
     * 取消点赞
     *
     * @param answer_id
     * @param uuid
     * @return
     */
    public static boolean deleteLike(String answer_id, String uuid) {
        AnswerLike follow = findLikeRelation(answer_id, uuid);
        if (null != follow) {
            follow.delete();
            return true;
        }
        return false;
    }

    /***
     * 查找点赞数
     * @param answerId
     * @return
     */
    public static int findLikeCount(String answerId) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_Answer_Like_Table
                + " WHERE answer_id = '" + answerId + "'").intValue();
    }

    /***
     * 查找是否已经点赞
     * @param answerId
     * @param uuid
     * @return
     */
    public static boolean isLike(String answerId, String uuid) {
        AnswerLike result = AnswerLike.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Answer_Like_Table
                + " WHERE answer_id = '" + answerId + "' AND uuid = '" + uuid + "'");
        return result != null;
    }

    /***
     * 查找是否已经关注
     * @param answerId
     * @param uuid
     * @return
     */
    public static AnswerLike findLikeRelation(String answerId, String uuid) {
        return AnswerLike.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Answer_Like_Table
                + " WHERE answer_id = '" + answerId + "' AND uuid = '" + uuid + "'");
    }

    /////////////////////////////////////////////////////////////////////////

    /**
     * 添加新的的点赞
     *
     * @param answerId
     * @param uuid
     */
    private static void newAnswerLikeMsg(String answerId, String uuid) {
        //评论被点赞产生一条通知，通知用户
        Answer answer = Answer.findAnswerById(answerId);
        if (null != answer) {
            NoticeMessage.createNewNoticeMsg(
                    NoticeMessage.Message_Type_Answer_Like,
                    answerId,
                    uuid,
                    String.valueOf(answer.get("uuid")),
                    "给回答点赞",
                    "");
        }
    }
}