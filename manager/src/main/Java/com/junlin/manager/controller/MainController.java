package com.junlin.manager.controller;

import com.jfinal.core.Controller;

import java.util.logging.Logger;

/**
 * Created by junlinhui eight on 2017/2/27.
 * main
 */
public class MainController extends Controller {

    //logger
    Logger logger = Logger.getLogger(MainController.class.getName());

    public void index() {
        //renderText("Hello 你好");
        render("/page/test.html");
    }

    public void test() {
        String name = getPara("account", "无");
        String password = getPara("password", "无");
        logger.info("name = " + name + ",password =" + password);

        render("/page/index.html");
    }

}
