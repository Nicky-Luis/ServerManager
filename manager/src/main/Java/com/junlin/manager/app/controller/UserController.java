package com.junlin.manager.app.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.app.service.AnswerService;
import com.junlin.manager.app.service.PersonService;
import com.junlin.manager.app.service.QuestionService;
import com.junlin.manager.app.service.UserService;
import com.junlin.manager.utils.DecoderUtils;

import org.apache.log4j.Logger;

/**
 * Created by junlinhui eight on 2017/5/26.
 * 用户主页，别人的主页
 */
public class UserController extends Controller {

    private static Logger logger = Logger.getLogger(UserController.class.getName());

    /**
     * 查找用户的信息
     */
    public void search() {
        String session = getPara("session");
        String userId = getPara("userId");

        logger.info("查找用户的信息："
                + "\nsession:" + session
                + "\nuserId:" + userId);
        //获取缓存中的用户id
        renderJson(UserService.startSearchPerson(session, userId));
    }

    /**
     * 发布留言
     */
    public void leavemessage() {
        String session = getPara("session");
        String userId = getPara("userId");
        String content = DecoderUtils.decoderContent(getPara("content"));


        logger.info("给用户用户留言："
                + "\nsession:" + session
                + "\nuserId:" + userId
                + "\ncontent" + content);
        //获取缓存中的用户id
        renderJson(UserService.startLeaveMessage(session, userId, content));
    }


    /**
     * 获取用户发布的问题列表
     */
    public void asked() {
        String session = getPara("session");
        String page = getPara("page");
        String userId = getPara("userId");
        logger.info("获取用户发布的问题列表："
                + "\nsession:" + session
                + "\nuserId:" + userId
                + "\npage:" + page);
        renderJson(QuestionService.findAskQuestion(userId, session, page, 10));
    }


    /**
     * 获取用户用户所关注的话题列表
     */
    public void followed() {
        String session = getPara("session");
        String page = getPara("page");
        String userId = getPara("userId");
        logger.info("获取用户关注的问题列表："
                + "\nsession:" + session
                + "\nuserId:" + userId
                + "\npage:" + page);
        renderJson(QuestionService.findFollowed(userId, session, page, 10));
    }

    /**
     * 获取用户用户所回答过的话题
     */
    public void answered() {
        String session = getPara("session");
        String page = getPara("page");
        String userId = getPara("userId");
        logger.info("获取用户所回答过的问题列表："
                + "\nsession:" + session
                + "\nuserId:" + userId
                + "\npage:" + page);
        renderJson(AnswerService.findAllAnswered(userId, session, page, 10));
    }

    /**
     * 获取用户的所有的粉丝
     */
    public void fans() {
        String session = getPara("session");
        String page = getPara("page");
        String userId = getPara("userId");
        logger.info("获取用户的所有的粉丝："
                + "\nsession:" + session
                + "\nuserId:" + userId
                + "\npage:" + page);
        renderJson(PersonService.startGetAllFans(userId, session, page, 10));
    }

    /**
     * 获取用户的所有关注的人
     */
    public void userfollowed() {
        String session = getPara("session");
        String page = getPara("page");
        String userId = getPara("userId");

        logger.info("获取用户的所有关注的人："
                + "\nsession:" + session
                + "\nuserId:" + userId
                + "\npage:" + page);
        renderJson(PersonService.startGetAllFollowed(userId,session, page, 10));
    }
}
