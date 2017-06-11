package com.junlin.manager.routes;

import com.jfinal.config.Routes;
import com.junlin.manager.admin.controller.AdminArticleController;
import com.junlin.manager.admin.controller.AdminUserController;
import com.junlin.manager.controller.FileController;
import com.junlin.manager.controller.MainController;

/**
 * Created by junlinhui eight on 2017/3/22.
 * 后台管理路由
 */
public class AdminRoutes extends Routes {

    @Override
    public void config() {
        add("/", MainController.class);
        add("/file", FileController.class);
        add("/admin/user", AdminUserController.class);
        add("/admin/article", AdminArticleController.class);
    }
}
