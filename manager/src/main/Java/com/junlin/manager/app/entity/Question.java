package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;

/**
 * Created by junlinhui eight on 2017/4/13.
 * 问题entity
 */
public class Question extends Model<Question> {
    //utils
    public static final Question dao = new Question();

    /**
     * 创建新的问题
     *
     * @param target
     * @param depict
     * @param tagType
     * @param price
     * @param limit
     * @param reward
     * @param uuid
     * @param deadLineTime
     * @param questionId
     */
    public static void newQuestion(int target, String depict, int tagType, int price, int limit, int reward, String uuid, Long
            deadLineTime, String questionId, String title) {
        new Question()
                .set("uuid", uuid)
                .set("question_id", questionId)
                .set("deadline", deadLineTime)
                .set("target", target)
                .set("title", title)
                .set("depict", depict)
                .set("label", tagType)
                .set("price", price)
                .set("reward_limit", limit)
                .set("reward_type", reward)
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
    }

    /***
     * 查找问题通过id
     * @param questionId
     * @return
     */
    public static Question findQuestionById(String questionId) {
        return Question.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Question_Table
                + " WHERE question_id = '" + questionId + "'");
    }

    /***
     * 删除答案通过question_id
     * @param questionId
     * @return
     */
    public static void deleteQuestionById(String questionId) {
        Db.update("DELETE FROM " + GlobalConstants.Lite_Question_Table + " WHERE question_id = '" + questionId + "'");
    }


    /***
     * 查找用户发布的问题数
     * @param uuid
     * @return
     */
    public static int findPublishCount(String uuid) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_Question_Table
                + " WHERE uuid = '" + uuid + "'").intValue();
    }

}
