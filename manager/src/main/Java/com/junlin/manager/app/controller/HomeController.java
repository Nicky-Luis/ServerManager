package com.junlin.manager.app.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.app.service.HomeService;

import org.apache.log4j.Logger;

/**
 * Created by junlinhui eight on 2017/4/25.
 * 主页
 */
public class HomeController extends Controller {
    //logger
    private static Logger logger = Logger.getLogger(HomeController.class.getName());
    //每页的数量固定为3个
    private final static int Popular_Page_Count = 1;
    //
    private final static int Feed_Page_Count = 10;

    /**
     * 查询排行榜
     */
    public void popular() {
        String session = getPara("session");
        logger.info("查找排名前一的问题与热门标的：" + "\nsession:" + session);
        renderJson(HomeService.findRankInfo(session, 1, Popular_Page_Count));
    }

    /**
     * 查询feed流
     */
    public void feed() {
        String page = getPara("page");
        String session = getPara("session");
        logger.info("查询feed流信息："
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(HomeService.findFeeds(session, page, Feed_Page_Count));
    }
}
