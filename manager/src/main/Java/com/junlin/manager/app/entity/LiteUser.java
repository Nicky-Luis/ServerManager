package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;
import com.junlin.manager.utils.RandomUtils;
import com.mchange.v2.lang.StringUtils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by junlinhui eight on 2017/4/10.
 * 小程序用户信息
 */
public class LiteUser extends Model<LiteUser> {
    //utils
    public static final LiteUser dao = new LiteUser();

    /**
     * 新建用户信息
     *
     * @param openid
     * @param userObject
     * @throws UnsupportedEncodingException
     */
    public static void newLiteUser(String openid, JSONObject userObject) throws UnsupportedEncodingException {
        new LiteUser()
                .set("uuid", RandomUtils.getRandomWord(5, String.valueOf(System.currentTimeMillis())))
                .set("openid", openid)
                .set("nickName", URLDecoder.decode(userObject.optString("nickName"), "UTF-8"))
                .set("gender", userObject.optString("gender"))
                .set("language", userObject.optString("language"))
                .set("city", userObject.optString("city"))
                .set("province", userObject.optString("province"))
                .set("country", userObject.optString("country"))
                .set("avatarUrl", userObject.optString("avatarUrl"))
                .set("created_time", System.currentTimeMillis())
                .set("updated_time", System.currentTimeMillis())
                .save();
    }

    /**
     * 通过session查找用户
     *
     * @param sessionKey
     * @return
     */
    public static LiteUser findUserBySession(String sessionKey) {
        String[] uuidArray = sessionKey.split(",");
        if (StringUtils.nonEmptyString(uuidArray[1])) {
            return dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_User_Table + " WHERE openid = '" + uuidArray[1] + "'");
        }
        return null;
    }

    /**
     * 通过uuid查找用户
     *
     * @param uuid
     * @return
     */
    public static LiteUser findUserByUUID(String uuid) {
        if (StringUtils.nonEmptyString(uuid)) {
            return dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_User_Table + " WHERE uuid = '" + uuid + "'");
        }
        return null;
    }
}
