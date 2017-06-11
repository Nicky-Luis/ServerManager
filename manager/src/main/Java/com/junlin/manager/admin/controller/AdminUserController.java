package com.junlin.manager.admin.controller;

import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.junlin.manager.admin.entity.User;
import com.junlin.manager.admin.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Created by junlinhui eight on 2017/3/22.
 * 后台用户相关的一些
 */
public class AdminUserController extends Controller {

    //logger
    private final Logger logger = Logger.getLogger(AdminUserController.class.getName());

    /***
     * 后台管理登录
     */
    public void login() {
        //跨域许可
        //先获取到两个POST变量
        String username = getPara("username", "");
        String password = getPara("password", "");

        logger.info("用户名:" + username + ",密码:" + password);

        //开始登录
        UserService.startLogin(username, password, new UserService.ILoginCallback() {
            @Override
            public void onLoginResult(int code, User user) {
                String message = code == UserService.Succeed_Code ? "登录成功" : "用户名或者密码错误";
                //结果
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("code", code);
                resultMap.put("msg", message);
                if (null != user) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    String[] attrsName = user._getAttrNames();
                    Object[] attrsValue = user._getAttrValues();
                    for (int index = 0; index < attrsName.length; index++) {
                            map.put(attrsName[index], attrsValue[index]);
                    }
                    resultMap.put("user", map);
                }
                String jsonObject = new Gson().toJson(resultMap);
                renderJson(jsonObject);
            }
        });
    }
}
