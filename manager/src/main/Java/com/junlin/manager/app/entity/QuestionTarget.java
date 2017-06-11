package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;

/**
 * Created by junlinhui eight on 2017/4/13.
 * 问题target
 */
public class QuestionTarget extends Model<QuestionTarget> {
    //utils
    public static final QuestionTarget dao = new QuestionTarget();

    /**
     * 创建新的标的
     *
     * @param uuid
     * @param target
     * @param content
     */
    public static boolean newQuestionTarget(String uuid, int target, String content) {
        new QuestionTarget()
                .set("uuid", uuid)
                .set("target", target)
                .set("content", content)
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
        return true;
    }

    /**
     * 判断标的是否存在
     */
    public static QuestionTarget getTarget(String content) {
        return dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Question_Target_Table + " WHERE content = '" + content + "'");
    }

    /**
     * 判断标的是否存在
     */
    public static boolean isTargetExist(String content) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_Question_Target_Table
                + " WHERE content = '" + content + "'").intValue() > 0;
    }

    /**
     * 查找标的的个数
     */
    public static int getTargetCount() {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_Question_Target_Table).intValue();
    }
}
