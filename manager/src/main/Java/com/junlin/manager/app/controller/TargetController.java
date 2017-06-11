package com.junlin.manager.app.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.app.service.TargetService;
import com.junlin.manager.utils.DecoderUtils;

import org.apache.log4j.Logger;

/**
 * Created by junlinhui eight on 2017/4/10.
 * 问题
 */
public class TargetController extends Controller {

    //logger
    private static Logger logger = Logger.getLogger(TargetController.class.getName());
    //每一页的数据
    private final static int Page_Count = 10;

    /**
     * 搜索标的
     */
    public void search() {
        String session = getPara("session");
        String page = getPara("page");
        String word = DecoderUtils.decoderContent(getPara("word"));

        logger.info("进行标的搜索："
                + "\nword:" + word
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(TargetService.startSearchTarget(session, word, page, Page_Count));
    }


    /**
     * 创建新的标的
     */
    public void add() {
        String session = getPara("session");
        String content = DecoderUtils.decoderContent(getPara("content"));

        logger.info("创建新的标的："
                + "\nword:" + content
                + "\nsession:" + session);
        renderJson(TargetService.createQuestionTarget(session, content));
    }
}
