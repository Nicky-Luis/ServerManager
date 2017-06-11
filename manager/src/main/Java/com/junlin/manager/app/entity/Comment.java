package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;

import java.util.List;

/**
 * Created by junlinhui eight on 2017/4/18.
 */
public class Comment extends Model<Comment> {
    //utils
    public static final Comment dao = new Comment();


    /***
     * 创建新的评论
     * @param answerId
     * @param replyUUID
     * @param comment
     * @param uuid
     * @param commentId
     */
    public static void newComment(String answerId, String replyUUID, String comment, String uuid, String commentId) {
        new Comment()
                .set("comment_id", commentId)
                .set("answer_id", answerId)
                .set("uuid", uuid)
                .set("reply_uuid", replyUUID)
                .set("content", comment)
                .set("flag", 0)
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
        //新的通知
        newCommentNotice(answerId, comment, commentId);
    }

    /***
     * 删除评论
     * @param answerId
     * @return
     */
    public static void deleteByAnswerId(String answerId) {
        Db.update("DELETE  FROM " + GlobalConstants.Lite_Answer_Comment_Table + " WHERE answer_id = '" + answerId + "'");
    }


    /***
     * 通过回答ID删除一系列
     * @param answerIds
     */
    public static void deleteByAnswerByIds(List<String> answerIds) {
        if (answerIds != null && answerIds.size() > 0) {
            StringBuilder answerValue = new StringBuilder();
            answerValue.append("'").append(answerIds.get(0)).append("'");
            for (int index = 1; index < answerIds.size(); index++) {
                answerValue.append(",'").append(answerIds.get(index)).append("'");
            }
            Db.update("DELETE FROM " + GlobalConstants.Lite_Answer_Comment_Table + " WHERE answer_id IN (" + answerValue.toString() + ")");
        }
    }


    /***
     * 查找答案通过id
     * @param commentID
     * @return
     */
    public static Comment findCommentById(String commentID) {
        return Comment.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Answer_Comment_Table
                + " WHERE comment_id = '" + commentID + "'");
    }

    /***
     * 查找评论
     * @param answer_id
     * @return
     */
    public static List<Comment> findCommentByAnswerId(String answer_id) {
        return Comment.dao.find("SELECT * FROM " + GlobalConstants.Lite_Answer_Comment_Table
                + " WHERE answer_id = '" + answer_id + "'");
    }

    /**
     * 查找回答的评论数
     *
     * @param answerID
     * @return
     */
    public static int findCommentCount(String answerID) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_Answer_Comment_Table
                + " WHERE answer_id = '" + answerID + "'").intValue();
    }

    //utils


    /***
     * 查找对应的信息
     * @param answerId
     * @param uuid
     * @return
     */
    public static Comment findCommentRelation(String answerId, String uuid) {
        return Comment.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Answer_Comment_Table
                + " WHERE answer_id = '" + answerId + "' AND uuid = '" + uuid + "'");
    }

    ////////////////////////////////////////////////

    /***
     * 产生一条新的评论通知 给用户
     * @param answerId
     * @param comment
     * @param commentId
     */
    private static void newCommentNotice(String answerId, String comment, String commentId) {
        //产生一条通知，通知用户
        Answer answer = Answer.findAnswerById(answerId);
        if (null != answer) {
            NoticeMessage.createNewNoticeMsg(
                    NoticeMessage.Message_Type_Answer_Comment,
                    answerId,
                    commentId,
                    String.valueOf(answer.get("uuid")),
                    comment,
                    "");
        }
    }
}
