package com.junlin.manager;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by junlinhui eight on 2017/3/15.
 * 常量
 */
public class GlobalConstants {
    //文章的数据表
    public final static String Wx_Article_Table = "wx_article_info";
    //文章的信息表
    public final static String Wx_Message_Table = "wx_article_message";
    //公众号信息表
    public final static String Wx_Public_Table = "wx_public_info";
    //搜狗微信表
    public final static String SoGou_Article_Table = "sogou_article";
    //搜狗文章内容表
    public final static String SoGou_Article_Profile_Table = "sogou_article_profile";
    //搜狗文章信息表
    public final static String SoGou_Article_Message_Table = "sogou_article_message";
    //搜狗公众号信息表
    public final static String SoGou_Office_Wx_Table = "sogou_wx_office_info";
    //搜狗公众号信息表
    public final static String SoGou_Wx_Signature_Table = "sogou_wx_signature";
    //搜狗关键字
    public final static String SoGou_Key_Word_Table = "sogou_key_word";
    //文章状态
    public final static String Sogou_Article_Status_Table = "sogou_article_status";

    //////////////////////////////////////管理员数据库///////////////////////////////
    //管理员用户表
    public final static String Admin_User_Table = "admin_user";

    /////////////////////////////////////小程序数据库///////////////////////////////
    //小程序用户信息表
    public final static String Lite_User_Table = "app_user";
    //小程序用户关系表
    public final static String Lite_User_Relationship_Table = "app_user_relationship";
    //小程序问题信息表
    public final static String Lite_Question_Table = "app_question";
    //小程序问题标签表
    public final static String Lite_Question_Label_Table = "app_question_label";
    //小程序问题标的表
    public final static String Lite_Question_Target_Table = "app_question_target";
    //小程序问题图片对应表
    public final static String Lite_Question_Image_Table = "app_question_image";
    //小程序问题关注表
    public final static String Lite_Question_Follow_Table = "app_question_follow";
    //小程序问题编辑记录表
    public final static String Lite_Question_Update_Table = "app_question_update";
    //小程序问题编辑记录表
    public final static String Lite_Answer_Table = "app_answer";
    //小程序答案的图片
    public final static String Lite_Answer_Image_Table = "app_answer_image";
    //小程序回答评论表
    public final static String Lite_Answer_Comment_Table = "app_answer_comment";
    //小程序回答点赞
    public final static String Lite_Answer_Like_Table = "app_answer_like";
    //小程序评论点赞表
    public final static String Lite_Comment_Like_Table = "app_comment_like";
    //小程序评论点赞表
    public final static String Lite_Message_Notice_Table = "app_message_notice";
    //小程序意见反馈
    public final static String Lite_Feedback_Table = "app_feedback";


    //下一页历史地址
    public static ConcurrentLinkedQueue<String> gNextHistoryQueue;
    //main
    public final static String mainURL = "http://weixin.sogou.com/";
}
