package com.junlin.manager.app.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.app.service.CommentService;
import com.junlin.manager.utils.DecoderUtils;

import org.apache.log4j.Logger;

/**
 * Created by junlinhui eight on 2017/4/10.
 * 评论
 */
public class CommentController extends Controller {

    //logger
    private static Logger logger = Logger.getLogger(PersonController.class.getName());

    /**
     * 添加评论
     */
    public void add() {
        String session = getPara("session");
        String answerId = getPara("answerId");
        String replyUUID = getPara("replyUUID");
        String comment =  DecoderUtils.decoderContent(getPara("comment"));

        //获取缓存中的用户id
        logger.info("创建新的评论："
                + "\nsession:" + session
                + "\nanswerId:" + answerId
                + "\nreplyUUID：" + replyUUID
                + "\ncomment：" + comment);

        renderJson(CommentService.createNewComment(session, answerId, replyUUID, comment));
    }

    /**
     * 查询评论
     */
    public void search() {
        String answerId = getPara("answerId");
        String session = getPara("session");
        int pageNum = getParaToInt("page",1);

        logger.info("查询评论信息："
                + "\nanswerId:" + answerId
                + "\nsession:" + session);
        renderJson(CommentService.startSearch(answerId, session, pageNum, 10));
    }

    /**
     * 给评论点赞
     */
    public void like() {
        String commentId = getPara("commentId");
        String session = getPara("session");
        boolean like = getParaToBoolean("like");
        String answerId = getPara("answerId");

        logger.info("给评论点赞操作："
                + "\ncommentId:" + commentId
                + "\nsession:" + session
                + "\nanswerId:" + answerId
                + "\nlike:" + like);
        //获取缓存中的用户id
        renderJson(CommentService.likeHandler(answerId,commentId, session, like));
    }

}
