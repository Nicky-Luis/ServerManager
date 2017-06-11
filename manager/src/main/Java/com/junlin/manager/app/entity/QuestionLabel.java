package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by junlinhui eight on 2017/4/13.
 * 问题label
 */
public class QuestionLabel extends Model<QuestionLabel> {
    //utils
    public static final QuestionLabel dao = new QuestionLabel();
    /**
     * 创建新的标的
     *
     * @param uuid
     * @param label
     * @param content
     */
    public static void newQuestionLabel(String uuid, String label, String content) {
        new QuestionLabel()
                .set("uuid", uuid)
                .set("label", label)
                .set("content", content)
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
    }
}
