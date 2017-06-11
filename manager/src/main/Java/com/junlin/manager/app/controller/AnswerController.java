package com.junlin.manager.app.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.app.service.AnswerService;
import com.junlin.manager.utils.DecoderUtils;

import org.apache.log4j.Logger;

/**
 * Created by junlinhui eight on 2017/4/19.
 * 回答模块
 */
public class AnswerController extends Controller {

    //logger
    private static Logger logger = Logger.getLogger(AnswerController.class.getName());

    /**
     * 创建新的回答
     */
    public void add() {
        String session = getPara("session");
        String questionId = getPara("questionId");
        String point = getPara("point");
        String content = DecoderUtils.decoderContent(getPara("content"));
        logger.info("创建新的回答："
                + "\nsessionKey:" + session
                + "\nquestionId:" + questionId
                + "\npoint：" + point
                + "\ncontent：" + content);

        renderJson(AnswerService.createNewAnswer(session, questionId, point, content));
    }

    /**
     * 删除回答
     */
    public void delete(){
        String session = getPara("session");
        String answerId = getPara("answerId");
        logger.info("删除发布的回答："
                + "\nsessionKey:" + session
                + "\nanswerId:" + answerId);

        renderJson(AnswerService.deleteAnswer(session, answerId));
    }
    /**
     * 添加图片
     */
    public void addpic() {
        String answerId = getPara("answerId");
        String urls = getPara("urls");

        logger.info("上传回答的图片信息："
                + "\nanswerId:" + answerId
                + "\nurls:" + urls);
        renderJson(AnswerService.saveAnswerImage(answerId, urls));
    }

    /**
     * 查找答案详情
     */
    public void search() {
        String session = getPara("session");
        String answerId = getPara("answerId");

        logger.info("查询答案详情："
                + "\nsession:" + session
                + "\nanswerId:" + answerId);
        renderJson(AnswerService.searchAnswer(session, answerId));
    }

    /**
     * 查找答案信息
     */
    public void message() {
        String session = getPara("session");
        String answerId = getPara("answerId");

        //获取缓存中的用户id
        logger.info("查询答案信息："
                + "\nsession:" + session
                + "\nanswerId:" + answerId);
        renderJson(AnswerService.searchAnswerMessage(session, answerId));
    }


    /**
     * 点赞操作
     */
    public void like() {
        String answerId = getPara("answerId");
        String session = getPara("session");
        boolean like = getParaToBoolean("like");

        logger.info("点赞处理："
                + "\nanswerId:" + answerId
                + "\nsession:" + session
                + "\nlike:" + like);
        renderJson(AnswerService.likeHandler(answerId, session, like));
    }

    /**
     *获取用户用户所回答过的话题
     */
    public void answered() {
        String session = getPara("session");
        String page = getPara("page");
        logger.info("获取用户所回答过的问题列表："
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(AnswerService.findAllAnswered( session, page, 10));
    }
}
