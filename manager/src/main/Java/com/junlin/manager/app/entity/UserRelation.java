package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;

/**
 * Created by junlinhui eight on 2017/4/18.
 * 用户之间的关系表
 */
public class UserRelation extends Model<UserRelation> {

    //utils
    public static final UserRelation dao = new UserRelation();

    /**
     * 添加关注
     *
     * @param uuid
     * @param fansUuid
     * @return
     */
    public static boolean addFollowRelation(String uuid, String fansUuid) {
        if (!isFollow(uuid, fansUuid)) {
            new UserRelation()
                    .set("uuid", uuid)
                    .set("fans_uuid", fansUuid)
                    .set("created_time", System.currentTimeMillis())
                    .set("updated_time", System.currentTimeMillis())
                    .save();
            newFollowNotice(uuid, fansUuid);
            return true;
        }
        return false;
    }

    /**
     * 取消关注
     *
     * @param uuid
     * @param fansUuid
     * @return
     */
    public static boolean deleteFollowRelation(String uuid, String fansUuid) {
        UserRelation follow = findFollowRelation(uuid, fansUuid);
        if (null != follow) {
            follow.delete();
            return true;
        }
        return false;
    }

    /***
     * 查找对应的关注信息
     * @param uuid
     * @param fansUuid
     * @return
     */
    public static UserRelation findFollowRelation(String uuid, String fansUuid) {
        return UserRelation.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_User_Relationship_Table
                + " WHERE fans_uuid = '" + fansUuid + "' AND uuid = '" + uuid + "'");
    }

    /***
     * 查找是否已经关注
     * @param uuid
     * @param fansUuid
     * @return
     */
    public static boolean isFollow(String uuid, String fansUuid) {
        UserRelation userRelation = UserRelation.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_User_Relationship_Table
                + " WHERE fans_uuid = '" + fansUuid + "' AND uuid = '" + uuid + "'");
        return null != userRelation;
    }


    /***
     * 查找粉丝的数量
     * @param uuid
     * @return
     */
    public static int findFansCount(String uuid) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_User_Relationship_Table
                + " WHERE uuid = '" + uuid + "'").intValue();
    }

    /***
     * 查找关注者的数量
     * @param uuid
     * @return
     */
    public static int findFollowCount(String uuid) {
        return Db.queryLong("SELECT COUNT(*) FROM " + GlobalConstants.Lite_User_Relationship_Table
                + " WHERE fans_uuid = '" + uuid + "'").intValue();
    }

    ////////////////////////////////////////////////////////////////////

    /**
     * 新的关注通知
     *
     * @param uuid
     * @param fans_uuid
     */
    private static void newFollowNotice(String uuid, String fans_uuid) {
        NoticeMessage.createNewNoticeMsg(
                NoticeMessage.Message_Type_User_Follow,
                uuid,
                fans_uuid,
                uuid,
                "新用户关注",
                "");
    }
}
