package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;

import java.util.List;

/**
 * Created by junlinhui eight on 2017/4/14.
 */
public class AnswerImage extends Model<AnswerImage> {

    //utils
    public static final AnswerImage dao = new AnswerImage();


    /**
     * 创建新的回答图片
     *
     * @param answerId
     * @param url
     */
    public static void newAnswerImage(String answerId, String url) {
        new AnswerImage()
                .set("answer_id", answerId)
                .set("url", url)
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
    }


    /***
     * 删除图片信息
     */
    public static void deleteByAnswerId(String answerId) {
        Db.update("DELETE  FROM " + GlobalConstants.Lite_Answer_Image_Table + " WHERE answer_id = '" + answerId + "'");
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
            Db.update("DELETE FROM " + GlobalConstants.Lite_Answer_Image_Table + " WHERE answer_id IN (" + answerValue.toString() + ")");
        }
    }

    /***
     * 查找问题对应的图片通过id
     * @param questionId
     * @return
     */
    public static AnswerImage findQuestionImageById(String questionId) {
        return AnswerImage.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Answer_Image_Table
                + " WHERE question_id = '" + questionId + "'");
    }
}