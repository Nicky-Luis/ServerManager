package com.junlin.manager.app.utils;

import com.google.common.base.Preconditions;
import com.jfinal.plugin.ehcache.CacheKit;
import com.junlin.manager.app.entity.LiteUser;
import com.mchange.v2.lang.StringUtils;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by junlinhui eight on 2017/5/3.
 * session 统一管理
 */
public class VerifyUtils {

    private static Logger logger = Logger.getLogger(VerifyUtils.class.getName());

    //操作成功
    public final static int Code_Succeed = 1;
    //session为空
    private final static int Err_Code_Session_Null = -1;
    //session不存在
    private final static int Err_Code_Session_Not_Exist = -2;
    //没有找对应的用户信息
    private final static int Err_Code_User_Not_Exist = -3;
    //用户uuid为空
    private final static int Err_Code_UUID_Null = -4;
    //操作失败
    public final static int Err_Code_Operate = -5;
    //参数错了
    public final static int Err_Code_Parameter = -6;


    /**
     * 校验session是否有误
     *
     * @param session
     * @return
     */
    public static Map<String, Object> checkSession(String session) {
        Map<String, Object> result = null;
        try {
            Preconditions.checkNotNull(session, "session为空，参数错误");
        } catch (Exception e) {
            e.printStackTrace();
            result = new HashMap<>();
            result.put("code", Err_Code_Session_Null);
            result.put("msg", "session为空，参数错误");
            return result;
        }
        String sessionKey = CacheKit.get("userCache", session);
        try {
            Preconditions.checkNotNull(sessionKey, "session不存在，请检查是否过期");
        } catch (Exception e) {
            e.printStackTrace();
            result = new HashMap<>();
            result.put("code", Err_Code_Session_Not_Exist);
            result.put("msg", "登录信息无效");
            return result;
        }
        LiteUser user = LiteUser.findUserBySession(sessionKey);
        try {
            Preconditions.checkNotNull(user, "没有找对应的用户信息");
        } catch (Exception e) {
            e.printStackTrace();
            result = new HashMap<>();
            result.put("code", Err_Code_User_Not_Exist);
            result.put("msg", "没有找对应的用户信息");
            return result;
        }
        String uuid = user.getStr("uuid");
        if (StringUtils.nonEmptyString(uuid)) {
            result = new HashMap<>();
            result.put("code", Code_Succeed);
            result.put("uuid", uuid);
            return result;
        } else {
            logger.error("用户uuid为空");
            result = new HashMap<>();
            result.put("code", Err_Code_UUID_Null);
            result.put("msg", "用户uuid为空");
            return result;
        }
    }
}
