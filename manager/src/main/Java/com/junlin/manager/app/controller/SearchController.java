package com.junlin.manager.app.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.app.service.SearchService;
import com.junlin.manager.utils.DecoderUtils;

import org.apache.log4j.Logger;

/**
 * Created by junlinhui eight on 2017/4/25.
 * 搜索页面
 */
public class SearchController extends Controller {
    //logger
    private static Logger logger = Logger.getLogger(SearchController.class.getName());
    //每页的数量
    private final static int Page_Count = 10;

    /**
     * 查询排行榜
     */
    public void search() {
        String session = getPara("session");
        String page = getPara("page");
        String word = DecoderUtils.decoderContent(getPara("word"));

        logger.info("进行搜索："
                + "\nword:" + word
                + "\nsession:" + session
                + "\npage:" + page);
        renderJson(SearchService.startSearch(session, word, Integer.valueOf(page), Page_Count));
    }
}
