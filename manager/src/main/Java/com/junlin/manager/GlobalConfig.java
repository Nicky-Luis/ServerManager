package com.junlin.manager;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.template.Engine;
import com.junlin.manager.admin.entity.User;
import com.junlin.manager.app.entity.Answer;
import com.junlin.manager.app.entity.AnswerImage;
import com.junlin.manager.app.entity.AnswerLike;
import com.junlin.manager.app.entity.Comment;
import com.junlin.manager.app.entity.CommentLike;
import com.junlin.manager.app.entity.Feedback;
import com.junlin.manager.app.entity.LiteUser;
import com.junlin.manager.app.entity.NoticeMessage;
import com.junlin.manager.app.entity.Question;
import com.junlin.manager.app.entity.QuestionFollow;
import com.junlin.manager.app.entity.QuestionImage;
import com.junlin.manager.app.entity.QuestionLabel;
import com.junlin.manager.app.entity.QuestionTarget;
import com.junlin.manager.app.entity.UserRelation;
import com.junlin.manager.reptile.sogou.entity.SoGouArticle;
import com.junlin.manager.reptile.sogou.entity.SoGouArticleMessage;
import com.junlin.manager.reptile.sogou.entity.SoGouArticleProfile;
import com.junlin.manager.reptile.sogou.entity.SoGouArticleStatus;
import com.junlin.manager.reptile.sogou.entity.SoGouKeyWord;
import com.junlin.manager.reptile.sogou.entity.SoGouOfficeWxInfo;
import com.junlin.manager.reptile.sogou.entity.SoGouSignature;
import com.junlin.manager.reptile.wechat.entity.WxArticleInfo;
import com.junlin.manager.reptile.wechat.entity.WxArticleMessage;
import com.junlin.manager.reptile.wechat.entity.WxPublicInfo;
import com.junlin.manager.routes.AdminRoutes;
import com.junlin.manager.routes.LiteRoutes;
import com.junlin.manager.routes.SystemTestRoutes;

import java.util.logging.Logger;

/**
 * Created by junlinhui eight on 2017/2/27.
 * 全局配置
 */
public class GlobalConfig extends JFinalConfig {


    //logger
    private final Logger logger = Logger.getLogger(GlobalConfig.class.getName());

    @Override
    public void configConstant(Constants me) {
        // 加载少量必要配置，随后可用PropKit.get(...)获取值
        PropKit.use("db.properties");
        me.setDevMode(PropKit.getBoolean("devMode", false));
    }

    @Override
    public void configRoute(Routes routes) {
        routes.add(new AdminRoutes());
        routes.add(new SystemTestRoutes());
        routes.add(new LiteRoutes());
    }

    @Override
    public void configEngine(Engine engine) {

    }

    @Override
    public void configPlugin(Plugins me) {
        addTabMap(me);
        //配置缓存插件
        me.add(new EhCachePlugin(PathKit.getRootClassPath() + "/ehcache.xml"));
    }

    @Override
    public void configInterceptor(Interceptors interceptors) {
    }

    @Override
    public void configHandler(Handlers handlers) {
        handlers.add(new GlobalHandler());
    }

    /////////////////////private method///////////////////////

    /**
     * 添加数据表映射
     *
     * @param me
     */
    private void addTabMap(Plugins me) {
        String url = PropKit.get("jdbcUrl");
        String name = PropKit.get("username");
        String password = PropKit.get("password");
        logger.info("de info,url:" + url + ",name:" + name + ",password:" + password + "\n");
        C3p0Plugin c3p0Plugin = new C3p0Plugin(url, name, password);
        ActiveRecordPlugin arp = new ActiveRecordPlugin(c3p0Plugin);

        //设置sql语句可见
        arp.setShowSql(true);
        //微信文章监听
        arp.addMapping(GlobalConstants.Wx_Article_Table, "wid", WxArticleInfo.class);
        arp.addMapping(GlobalConstants.Wx_Message_Table, "aid", WxArticleMessage.class);
        arp.addMapping(GlobalConstants.Wx_Public_Table, "wid", WxPublicInfo.class);
        //搜狗抓取
        arp.addMapping(GlobalConstants.SoGou_Article_Table, "id", SoGouArticle.class);
        arp.addMapping(GlobalConstants.SoGou_Article_Message_Table, "id", SoGouArticleMessage.class);
        arp.addMapping(GlobalConstants.SoGou_Article_Profile_Table, "id", SoGouArticleProfile.class);
        arp.addMapping(GlobalConstants.SoGou_Office_Wx_Table, "id", SoGouOfficeWxInfo.class);
        arp.addMapping(GlobalConstants.SoGou_Wx_Signature_Table, "id", SoGouSignature.class);
        arp.addMapping(GlobalConstants.SoGou_Key_Word_Table, "id", SoGouKeyWord.class);
        arp.addMapping(GlobalConstants.Sogou_Article_Status_Table, "id", SoGouArticleStatus.class);
        //后台管理员
        arp.addMapping(GlobalConstants.Admin_User_Table, "uid", User.class);
        //小程序相关表
        arp.addMapping(GlobalConstants.Lite_User_Table, "id", LiteUser.class);
        arp.addMapping(GlobalConstants.Lite_Question_Table, "id", Question.class);
        arp.addMapping(GlobalConstants.Lite_Question_Image_Table, "id", QuestionImage.class);
        arp.addMapping(GlobalConstants.Lite_Question_Follow_Table, "id", QuestionFollow.class);
        arp.addMapping(GlobalConstants.Lite_Question_Label_Table, "id", QuestionLabel.class);
        arp.addMapping(GlobalConstants.Lite_Question_Target_Table, "id", QuestionTarget.class);
        arp.addMapping(GlobalConstants.Lite_Answer_Table, "id", Answer.class);
        arp.addMapping(GlobalConstants.Lite_Answer_Image_Table, "id", AnswerImage.class);
        arp.addMapping(GlobalConstants.Lite_User_Relationship_Table, "id", UserRelation.class);
        arp.addMapping(GlobalConstants.Lite_Answer_Like_Table, "id", AnswerLike.class);
        arp.addMapping(GlobalConstants.Lite_Answer_Comment_Table, "id", Comment.class);
        arp.addMapping(GlobalConstants.Lite_Comment_Like_Table, "id", CommentLike.class);
        arp.addMapping(GlobalConstants.Lite_Message_Notice_Table, "id", NoticeMessage.class);
        arp.addMapping(GlobalConstants.Lite_Feedback_Table, "id", Feedback.class);


        me.add(c3p0Plugin);
        me.add(arp);
    }
}
