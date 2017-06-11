package com.junlin.manager.app.controller;

import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.plugin.ehcache.CacheKit;
import com.junlin.manager.app.service.PersonService;
import com.junlin.manager.utils.DecoderUtils;
import com.junlin.manager.utils.RandomUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by junlinhui eight on 2017/4/7.
 * 用户个人中心
 */
public class PersonController extends Controller {

    //logger
    private static Logger logger = Logger.getLogger(PersonController.class.getName());
    //超时flag
    private static volatile boolean rendFlag = false;

    /**
     * 登录
     */
    public void login() {
        rendFlag = false;
        final String user = getPara("user");
        final String code = getPara("code");
        logger.info("======开始登录:\ncode:" + code + "\n用户信息：" + user);

        // 得到HttpSession
        PersonService.getSessionInfo(user, code, new PersonService.ILiteCallback() {
            @Override
            public synchronized void onGetResult(int code, String result) {
                if (rendFlag) {
                    return;
                }

                String _3rdKey = "";
                if (StringUtils.isNotEmpty(result)) {
                    _3rdKey = RandomUtils.getRandomWord(168);
                    CacheKit.put("userCache", _3rdKey, result);
                    logger.info("保存session成功：" + result);
                }

                //返回给小程序
                renderJson(PersonService.getLoginJson(code, _3rdKey));
                rendFlag = true;
            }
        });

        //超时设置
        timeOutRender();
    }

    /**
     * 获取用户关注的问题，回答的问题，发布的问题信息
     */
    public void message() {
        String session = getPara("session");

        logger.info("查询问题信息："
                + "\nsession:" + session);
        renderJson(PersonService.startSearchMessage(session));
    }

    /**
     * 获取用户的所有的粉丝
     */
    public void fans() {
        String session = getPara("session");
        String page = getPara("page");
        logger.info("获取用户的所有的粉丝："
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(PersonService.startGetAllFans(session, page, 10));
    }

    /**
     * 获取用户的所有关注的人
     */
    public void followed() {
        String session = getPara("session");
        String page = getPara("page");
        logger.info("获取用户的所有关注的人："
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(PersonService.startGetAllFollowed(session, page, 10));
    }

    /**
     * 关注用户处理
     */
    public void follow() {
        String uuid = getPara("uuid");
        String session = getPara("session");
        boolean follow = getParaToBoolean("follow");

        logger.info("关注用户与取消关注用户："
                + "\nuuid:" + uuid
                + "\nsession:" + session
                + "\nfollow:" + follow);
        //获取缓存中的用户id
        renderJson(PersonService.followHandler(uuid, session, follow));
    }



    /**
     * 获取用户信息
     */
    public void info() {
        String session = getPara("session");
        logger.info("获取用户的信息："
                + "\nsession:" + session);
        renderJson(PersonService.startGetUserInfo(session));
    }

    /**
     * 修改用户的签名
     */
    public void signature() {
        String session = getPara("session");
        String signature = DecoderUtils.decoderContent(getPara("signature"));

        logger.info("修改用户的签名："
                + "\nsession:" + session
                + "\nsignature:" + signature);
        renderJson(PersonService.startEditSignature(session, signature));
    }

    ////////////////////////////////private method//////////////////////////////////////////


    /**
     * 超时设置
     */
    private synchronized void timeOutRender() {
        int count = 0;
        while (!rendFlag) {
            if (count >= 10) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        if (rendFlag) {
            return;
        }
        //返回超时
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", -1);
        resultMap.put("msg", "超时");
        String jsonObject = new Gson().toJson(resultMap);
        renderJson(jsonObject);
        rendFlag = true;
    }
}
