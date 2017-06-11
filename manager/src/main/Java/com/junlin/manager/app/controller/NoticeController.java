package com.junlin.manager.app.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.app.service.NoticeService;
import com.junlin.manager.utils.DecoderUtils;

import org.apache.log4j.Logger;


/**
 * Created by junlinhui eight on 2017/4/25.
 * 通知页面
 */
public class NoticeController extends Controller {
    //logger
    private static Logger logger = Logger.getLogger(NoticeController.class.getName());
    //每页的数量
    private final static int Page_Count = 10;

    /**
     * 查询问题的动态
     */
    public void question() {
        String session = getPara("session");
        String page = getPara("page");

        logger.info("查询问题相关的通知："
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(NoticeService.searchQuestionNotice(session, page, Page_Count));
    }

    /**
     * 查询系统的动态
     */
    public void system() {
        String session = getPara("session");
        String page = getPara("page");

        logger.info("查询系统相关的通知："
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(NoticeService.searchSystemNotice(session, page, Page_Count));
    }

    /**
     * 查询系统的动态
     */
    public void user() {
        String session = getPara("session");
        String page = getPara("page");

        logger.info("查询用户相关的通知："
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(NoticeService.searchUserNotice(session, page, Page_Count));
    }

    /**
     * 查找通知
     */
    public void search(){
        String session = getPara("session");
        String noticeId = getPara("noticeId");

        logger.info("查询通知："
                + "\nsession:" + session
                + "\nnoticeId:" + noticeId);
        renderJson(NoticeService.searchNotice(session,noticeId));
    }
    /**
     * 回复留言
     */
    public void reply() {
        String session = getPara("session");
        String userId = getPara("userId");
        String noticeId = getPara("noticeId");
        String content = DecoderUtils.decoderContent(getPara("content"));

        logger.info("用户回复留言的通知："
                + "\nsession:" + session
                + "\nuserId:" + userId
                + "\nnoticeId:" + noticeId
                + "\ncontent:" + content);
        renderJson(NoticeService.startReply(noticeId, session, content, userId));
    }

    /**
     * 删除通知
     */
    public void delete() {
        String session = getPara("session");
        String noticeId = getPara("noticeId");

        logger.info("用户删除通知："
                + "\nsession:" + session
                + "\nnoticeId:" + noticeId);
        renderJson(NoticeService.startDelete(noticeId, session));
    }
}
