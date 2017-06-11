package com.junlin.manager.app.controller;

import com.jfinal.core.Controller;
import com.junlin.manager.app.service.SystemService;
import com.junlin.manager.utils.DecoderUtils;

import org.apache.log4j.Logger;

/**
 * Created by junlinhui eight on 2017/4/7.
 * 小程序Controller
 */
public class SystemController extends Controller {

    //logger
    private static Logger logger = Logger.getLogger(SystemController.class.getName());

    /**
     * 提交意见反馈
     */
    public void feedback() {
        String session = getPara("session");
        String feedback =  DecoderUtils.decoderContent(getPara("content"));

        logger.info("用户提交意见反馈："
                + "\nsession:" + session
                + "\nfeedback:" + feedback);
        renderJson(SystemService.startUploadFeedback(session,feedback));
    }

}
