package com.junlin.manager.app.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.app.service.QuestionService;
import com.junlin.manager.utils.DecoderUtils;

import org.apache.log4j.Logger;

/**
 * Created by junlinhui eight on 2017/4/10.
 * 问题
 */
public class QuestionController extends Controller {

    //logger
    private static Logger logger = Logger.getLogger(QuestionController.class.getName());
    //每一页的数据
    private final static int Page_Count = 10;

    /**
     * 创建新的问题
     */
    public void add() {
        String session = getPara("session");
        int target = getParaToInt("target", 0);
        String deadline = getPara("deadline");
        String descript = DecoderUtils.decoderContent(getPara("descript"));
        String title = DecoderUtils.decoderContent(getPara("title"));
        final int tagType = getParaToInt("tagType", 0);
        final int price = getParaToInt("price", 0);
        final int limit = getParaToInt("limit", 0);
        final int reward = getParaToInt("reward", 0);

        logger.info("创建新的问题："
                + "\nsession:" + session
                + "\ntagType:" + tagType
                + "\ntarget：" + target
                + "\ndeadline：" + deadline
                + "\ntitle：" + title
                + "\ndescript：" + descript
                + "\nprice：" + price
                + "\nlimit：" + limit
                + "\nreward：" + reward);

        renderJson(QuestionService.createNewQuestion(session, deadline, target, title,descript, tagType, price, limit, reward));
    }

    /**
     * 添加图片
     */
    public void addpic() {
        String questionId = getPara("question_id");
        String urls = getPara("urls");

        logger.info("上传图片信息："
                + "\nquestionId:" + questionId
                + "\nurls:" + urls);
        renderJson(QuestionService.saveQuestionImage(questionId, urls));
    }

    /**
     * 查询问题,根据问题的id
     */
    public void search() {
        String questionId = getPara("question_id");
        String session = getPara("session");

        logger.info("查询问题详细："
                + "\nquestionId:" + questionId
                + "\nsession:" + session);
        //获取缓存中的用户id
        renderJson(QuestionService.startSearch(questionId, session));
    }

    /**
     * 关注问题
     */
    public void follow() {
        String questionId = getPara("question_id");
        String session = getPara("session");
        boolean follow = getParaToBoolean("follow");

        logger.info("关注问题操作："
                + "\nquestionId:" + questionId
                + "\nsession:" + session
                + "\nfollow:" + follow);
        //获取缓存中的用户id
        renderJson(QuestionService.followHandler(questionId, session, follow));
    }

    /**
     * 查询问题的答案列表
     */
    public void answers() {
        String questionId = getPara("question_id");
        String session = getPara("session");
        String point = getPara("point");
        String page = getPara("page");
        logger.info("查找问题的答案："
                + "\nquestionId:" + questionId
                + "\nsession:" + session
                + "\npoint:" + point
                + "\npage:" + page);
        renderJson(QuestionService.findAnswers(questionId, session, point, Integer.valueOf(page), 10));
    }

    /**
     * 获取用户发布的问题列表
     */
    public void asked() {
        String session = getPara("session");
        String page = getPara("page");
        logger.info("获取用户发布的问题列表："
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(QuestionService.findAskQuestion(session, page, 10));
    }


    /**
     * 获取用户用户所关注的话题列表
     */
    public void followed() {
        String session = getPara("session");
        String page = getPara("page");
        logger.info("获取用户发布的问题列表："
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(QuestionService.findFollowed(session, page, 10));
    }

    /**
     * 删除话题以及话题下的所有内容
     */
    public void delete() {
        String session = getPara("session");
        String questionId = getPara("questionId");

        logger.info("删除发布的问题："
                + "\nsession:" + session
                + "\nquestionId:" + questionId);
        renderJson(QuestionService.deleteQuestion(session, questionId));
    }

}
