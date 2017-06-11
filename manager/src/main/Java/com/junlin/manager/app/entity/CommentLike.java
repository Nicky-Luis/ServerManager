package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;

import java.util.List;

/**
 * Created by junlinhui eight on 2017/4/18.
 */
public class CommentLike extends Model<CommentLike> {
    //utils
    public static final CommentLike dao = new CommentLike();

    /***
     * 查找评论数
     * @param commentId
     * @return
     */
    public static int findCountById(String commentId) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_Comment_Like_Table
                + " WHERE answer_id = '" + commentId + "'").intValue();
    }


    /***
     * 查找点赞
     * @param commentId
     * @return
     */
    public static List<CommentLike> findCommentByAnswerId(String commentId) {
        return CommentLike.dao.find("SELECT * FROM " + GlobalConstants.Lite_Comment_Like_Table
                + " WHERE comment_id = '" + commentId + "'");
    }

    /***
     * 删除评论的点赞
     * @param answerId
     * @return
     */
    public static void deleteByAnswerId(String answerId) {
        Db.update("DELETE  FROM " + GlobalConstants.Lite_Comment_Like_Table + " WHERE answer_id = '" + answerId + "'");
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
            Db.update("DELETE FROM " + GlobalConstants.Lite_Comment_Like_Table + " WHERE answer_id IN (" + answerValue.toString() + ")");
        }
    }

    /***
     * 查找是否已经点赞
     * @param commentId
     * @param uuid
     * @return
     */
    public static boolean isLike(String commentId, String uuid) {
        CommentLike result = CommentLike.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Comment_Like_Table
                + " WHERE comment_id = '" + commentId + "' AND uuid = '" + uuid + "'");
        return result != null;
    }


    /***
     * 查找对应的信息
     * @param commentId
     * @param uuid
     * @return
     */
    public static CommentLike findCommentRelation(String commentId, String uuid) {
        return CommentLike.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Comment_Like_Table
                + " WHERE comment_id = '" + commentId + "' AND uuid = '" + uuid + "'");
    }


    /**
     * 添加点赞
     *
     * @param comment_id
     * @param uuid
     */
    public static boolean addLikeRelation(String answerId, String comment_id, String uuid) {
        new CommentLike()
                .set("answer_id", answerId)
                .set("comment_id", comment_id)
                .set("uuid", uuid)
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
        newCommentMsg(comment_id, uuid);
        return true;
    }

    /***
     * 给评论点赞
     * @param comment_id
     * @param uuid
     */
    private static void newCommentMsg(String comment_id, String uuid) {
        //产生一条通知，通知用户
        Comment comment = Comment.findCommentById(comment_id);
        if (null != comment) {
            NoticeMessage.createNewNoticeMsg(
                    NoticeMessage.Message_Type_Comment_Like,
                    comment_id,
                    uuid,
                    String.valueOf(comment.get("uuid")),
                    "发布新的评论",
                    "");
        }
    }

    /**
     * 取消点赞
     *
     * @param comment_id
     * @param uuid
     */
    public static boolean deleteLikeRelation(String comment_id, String uuid) {
        CommentLike follow = findCommentRelation(comment_id, uuid);
        if (null != follow) {
            follow.delete();
            return true;
        }
        return false;
    }

}
