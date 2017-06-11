package com.junlin.manager.routes;

import com.jfinal.config.Routes;
import com.junlin.manager.reptile.sogou.controller.SoGouReptileController;
import com.junlin.manager.reptile.wechat.controller.WxController;

/**
 * Created by junlinhui eight on 2017/3/22.
 * 测试路由
 */
public class SystemTestRoutes extends Routes {
    @Override
    public void config() {
        add("/app", WxController.class);
        add("/sogou", SoGouReptileController.class);
    }
}
