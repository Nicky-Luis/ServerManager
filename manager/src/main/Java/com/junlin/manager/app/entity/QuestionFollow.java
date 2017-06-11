package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;
import com.junlin.manager.utils.TimeUtils;

/**
 * Created by junlinhui eight on 2017/4/18.
 */
public class QuestionFollow extends Model<QuestionFollow> {

    //utils
    public static final QuestionFollow dao = new QuestionFollow();

    /**
     * 添加关注
     *
     * @param question_id
     * @param uuid
     */
    public static boolean addFollowRelation(String question_id, String uuid) {
        new QuestionFollow()
                .set("question_id", question_id)
                .set("uuid", uuid).set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
        newFollowNoticeMsg(question_id, uuid);
        return true;
    }

    /**
     * 取消关注
     *
     * @param question_id
     * @param uuid
     */
    public static boolean deleteFollowRelation(String question_id, String uuid) {
        QuestionFollow follow = findFollowRelation(question_id, uuid);
        if (null != follow) {
            follow.delete();
            return true;
        }
        return false;
    }

    /***
     * 查找对应的关注信息
     * @param questionId
     * @return
     */
    public static QuestionFollow findFollowRelation(String questionId, String uuid) {
        return QuestionFollow.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Question_Follow_Table
                + " WHERE question_id = '" + questionId + "' AND uuid = '" + uuid + "'");
    }

    /***
     * 查找问题的关注数量
     * @param questionId
     * @return
     */
    public static int findFollowCount(String questionId) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_Question_Follow_Table
                + " WHERE question_id = '" + questionId + "'").intValue();
    }

    /***
     * 查找用户关注的问题数
     * @param uuid
     * @return
     */
    public static int findUserFollowedCount(String uuid) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_Question_Follow_Table
                + " WHERE uuid = '" + uuid + "'").intValue();
    }

    ///////////////////////////////////////////////////

    /**
     * 关注问题 创建新的消息
     *
     * @param question_id
     * @param uuid
     */
    private static void newFollowNoticeMsg(String question_id, String uuid) {
        //问题新增关注，产生一条消息通知用户
        Question question = Question.findQuestionById(question_id);
        if (null != question) {
            String deadline = TimeUtils.timeStamp2Date((Double) question.get("deadline") / 1000, "YYYY/MM/dd");
            NoticeMessage.createNewNoticeMsg(
                    NoticeMessage.Message_Type_Question_Follow,
                    question_id,
                    uuid,
                    String.valueOf(question.get("uuid")),
                    "发布的问题被关注",
                    deadline);
        }
    }


}
