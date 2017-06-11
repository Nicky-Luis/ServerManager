package com.junlin.manager.app.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.app.service.RankService;
import com.junlin.manager.utils.DecoderUtils;

import org.apache.log4j.Logger;

/**
 * Created by junlinhui eight on 2017/4/25.
 * 榜单
 */
public class RankController extends Controller {
    //logger
    private static Logger logger = Logger.getLogger(RankController.class.getName());
    //每页的数量
    private final static int Page_Count = 10;

    /**
     * 查询排行榜
     */
    public void search() {
        String session = getPara("session");
        String page = getPara("page");
        String label = DecoderUtils.decoderContent(getPara("label"));
        String target = DecoderUtils.decoderContent(getPara("target"));

        logger.info("查找排行榜："
                + "\nsession:" + session
                + "\npage:" + page
                + "\nlabel:" + label
                + "\ntarget:" + target);
        renderJson(RankService.findRanks(session, Integer.valueOf(page), Page_Count, label, target));
    }

}
