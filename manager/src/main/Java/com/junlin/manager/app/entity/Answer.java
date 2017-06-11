package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;
import com.junlin.manager.utils.TimeUtils;

import java.util.List;

/**
 * Created by junlinhui eight on 2017/4/18.
 */
public class Answer extends Model<Answer> {
    //utils
    public static final Answer dao = new Answer();

    /**
     * 创建新的 回答
     *
     * @param questionId
     * @param point
     * @param content
     * @param uuid
     * @param answerId
     */
    public static void newAnswer(String questionId, String point, String content, String uuid, String answerId) {
        new Answer()
                .set("uuid", uuid)
                .set("answer_id", answerId)
                .set("question_id", questionId)
                .set("point", point)
                .set("content", content)
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
        newAnswerNotice(questionId, content, answerId);
    }

    /***
     * 删除答案通过id
     * @param answerId
     * @return
     */
    public static void deleteByAnswerById(String answerId) {
        Db.update("DELETE FROM " + GlobalConstants.Lite_Answer_Table + " WHERE answer_id = '" + answerId + "'");
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
            Db.update("DELETE FROM " + GlobalConstants.Lite_Answer_Table + " WHERE answer_id IN (" + answerValue.toString() + ")");
        }
    }

    /***
     * 查找答案通过id
     * @param answerId
     * @return
     */
    public static Answer findAnswerById(String answerId) {
        return Answer.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Answer_Table
                + " WHERE answer_id = '" + answerId + "'");
    }

    /***
     * 查找答案通过问题id
     * @param questionId
     * @return
     */
    public static List<Answer> findAnswerListByQuestionId(String questionId) {
        return Answer.dao.find("SELECT * FROM " + GlobalConstants.Lite_Answer_Table + " WHERE question_id = '" + questionId + "'");
    }

    /**
     * 查找问题的回答数
     *
     * @param questionId
     * @return
     */
    public static int findAnswerCount(String questionId) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_Answer_Table
                + " WHERE question_id = '" + questionId + "'").intValue();
    }

    /***
     * 查找用户回答的数量
     * @param uuid
     * @return
     */
    public static int findUserAnswerCount(String uuid) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_Answer_Table
                + " WHERE uuid = '" + uuid + "'").intValue();
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 添加新的通知
     *
     * @param questionId
     * @param content
     * @param answerId
     */
    private static void newAnswerNotice(String questionId, String content, String answerId) {
        //新增回答，产生一条消息，通知用户
        Question question = Question.findQuestionById(questionId);
        if (null != question) {
            String deadline = TimeUtils.timeStamp2Date((Double) question.get("deadline") / 1000, "YYYY/MM/dd");
            NoticeMessage.createNewNoticeMsg(
                    NoticeMessage.Message_Type_Question_New_Answer,
                    questionId,
                    answerId,
                    String.valueOf(question.get("uuid")),
                    content,
                    deadline);
        }
    }
}
