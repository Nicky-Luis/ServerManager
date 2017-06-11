package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;
import com.mchange.v2.lang.StringUtils;

/**
 * Created by junlinhui eight on 2017/4/10.
 * 小程序用户信息
 */
public class Feedback extends Model<Feedback> {
    //utils
    public static final Feedback dao = new Feedback();

    /**
     * 意见反馈
     *
     * @param uuid
     * @param content
     */
    public static void newFeedback(String uuid, String content) {
        new Feedback()
                .set("uuid", uuid)
                .set("content", content)
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
    }


    /**
     * 通过uuid查找用户
     *
     * @param uuid
     * @return
     */
    public static Feedback findFeedbackByUUID(String uuid) {
        if (StringUtils.nonEmptyString(uuid)) {
            return dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_User_Table + " WHERE uuid = '" + uuid + "'");
        }
        return null;
    }
}
