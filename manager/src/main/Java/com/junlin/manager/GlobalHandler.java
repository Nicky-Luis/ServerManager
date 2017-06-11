package com.junlin.manager;

import com.jfinal.handler.Handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by junlinhui eight on 2017/3/22.
 */
public class GlobalHandler extends Handler {
    @Override
    public void handle(String target, HttpServletRequest request,
                       HttpServletResponse response, boolean[] isHandled) {
        // 指定允许其他域名访问
        response.addHeader("Access-Control-Allow-Origin", "*");
        /// 响应类型
        response.addHeader("Access-Control-Allow-Methods", "POST,GET");
        // 响应头设置
        response.addHeader("Access-Control-Allow-Headers", "Content-Type,x-requested-with");

        nextHandler.handle(target, request, response, isHandled);
    }
}
