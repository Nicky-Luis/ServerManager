package com.junlin.manager.routes;

import com.jfinal.config.Routes;
import com.junlin.manager.app.controller.AnswerController;
import com.junlin.manager.app.controller.CommentController;
import com.junlin.manager.app.controller.HomeController;
import com.junlin.manager.app.controller.NoticeController;
import com.junlin.manager.app.controller.UserController;
import com.junlin.manager.app.controller.QuestionController;
import com.junlin.manager.app.controller.RankController;
import com.junlin.manager.app.controller.SearchController;
import com.junlin.manager.app.controller.SystemController;
import com.junlin.manager.app.controller.TargetController;
import com.junlin.manager.app.controller.PersonController;

/**
 * Created by junlinhui eight on 2017/4/7.
 * 小程序路由
 */
public class LiteRoutes extends Routes {
    @Override
    public void config() {
        add("/app/person", PersonController.class);
        add("/app/user", UserController.class);
        add("/app/question", QuestionController.class);
        add("/app/target", TargetController.class);
        add("/app/answer", AnswerController.class);
        add("/app/comment", CommentController.class);
        add("/app/rank", RankController.class);
        add("/app/search", SearchController.class);
        add("/app/home", HomeController.class);
        add("/app/notice", NoticeController.class);
        add("/app/system", SystemController.class);
    }
}
