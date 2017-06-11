package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;

/**
 * Created by junlinhui eight on 2017/4/14.
 *
 */
public class QuestionImage extends Model<QuestionImage> {

    //utils
    public static final QuestionImage dao = new QuestionImage();

    /**
     * 创建新的图片关系
     * @param questionId
     * @param url
     */
    public static void newImageRelation(String questionId, String url) {
        new QuestionImage()
                .set("question_id", questionId)
                .set("url", url)
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
    }

    /***
     * 查找问题对应的图片通过id
     * @param questionId
     * @return
     */
    public static QuestionImage findQuestionImageById(String questionId) {
        return QuestionImage.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Question_Image_Table
                + " WHERE question_id = '" + questionId + "'");
    }
}