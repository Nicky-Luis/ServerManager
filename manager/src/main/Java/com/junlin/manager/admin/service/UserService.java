package com.junlin.manager.admin.service;

import com.junlin.manager.GlobalConstants;
import com.junlin.manager.admin.entity.User;
import com.mchange.v2.lang.StringUtils;

import java.util.logging.Logger;

/**
 * Created by junlinhui eight on 2017/3/22.
 * 登录操作
 */
public class UserService {

    //logger
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    //用户名或者密码错误
    private final static int Err_Code = 400;
    //登录成功
    public final static int Succeed_Code = 200;

    /***
     * 登录验证
     * @param userName
     * @param password
     * @param callback
     */
    public static void startLogin(String userName, String password, ILoginCallback callback) {
        if (null == callback) {
            logger.info("回调为空");
            return;
        }

        if (StringUtils.nonEmptyString(userName) && StringUtils.nonEmptyString(password)) {
            User user = User.dao.findFirst("SELECT uuid,user_name,nick_name,sex,birthday,address,avatar " +
                    "FROM " + GlobalConstants.Admin_User_Table + " WHERE user_name = '" + userName + "' AND pass_word = '" + password + "'");
            if (null == user) {
                callback.onLoginResult(Err_Code, null);
            } else {
                callback.onLoginResult(Succeed_Code, user);
            }
        } else {
            callback.onLoginResult(Err_Code, null);
        }
    }

    ////////////////////////////////////登录回调/////////////////////////////

    public interface ILoginCallback {
        void onLoginResult(int code, User user);
    }
}
