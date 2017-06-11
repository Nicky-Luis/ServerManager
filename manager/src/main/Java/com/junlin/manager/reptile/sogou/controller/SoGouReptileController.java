package com.junlin.manager.reptile.sogou.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.reptile.sogou.service.ReptileControlService;

import org.apache.log4j.Logger;

/**
 * Created by junlinhui eight on 2017/3/22.
 * 搜狗相关的一些
 */
public class SoGouReptileController extends Controller {

    //logger
    private static Logger logger = Logger.getLogger(SoGouReptileController.class.getName());


    /**
     * 获取历史文章信息
     */
    public void init() {
        renderText("已经初始化");
        ReptileControlService.initReptileInfo();
    }

    /**
     * 开始抓取搜狗
     */
    public void start() {
        logger.info("\n\n------------------------开启搜索爬虫服务-----------------------------\n");
        renderText("开始采集...");
        ReptileControlService.startSoGouReptile();
    }


    /**
     * 重置
     */
    public void reset() {
        renderText("已经重置");
        ReptileControlService.resetReptileInfo();
    }

}
