package com.junlin.manager.app.entity;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.junlin.manager.GlobalConstants;
import com.junlin.manager.utils.RandomUtils;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by junlinhui eight on 2017/4/18.
 * 通知类
 */
public class NoticeMessage extends Model<NoticeMessage> {

    //utils
    public static final NoticeMessage dao = new NoticeMessage();
    //logger
    private static Logger logger = Logger.getLogger(NoticeMessage.class.getName());

    //问题通知类型
    public final static int Message_Type_Question_Follow = 11;//发布的问题被关注
    public final static int Message_Type_Question_New_Answer = 12;//发布的问题被回答
    public final static int Message_Type_Question_Add_Reward = 13;//发布的问题被添加悬赏金额
    public final static int Message_Type_Question_Offer_Reward = 14;//发布的问题被奖励给回答者
    //回答通知类型
    public final static int Message_Type_Answer_Like = 21;//发布的回答被点赞
    public final static int Message_Type_Answer_Comment = 22;//发布的回答被评论
    public final static int Message_Type_Answer_Reward = 23;//发布的回答被奖励
    //评论通知类型
    public final static int Message_Type_Comment_Reply = 31;//发布的评论被回复
    public final static int Message_Type_Comment_Like = 32;//发布的评论被点赞
    //用户系统通知类型
    public final static int Message_Type_User_Follow = 41;//被关注
    public final static int Message_Type_User_Message = 42;//被留言
    //财富通知类型
    public final static int Message_Type_Wealth_Pay = 51;//支付
    public final static int Message_Type_Wealth_Encashment = 52;//提现
    //系统通知类型
    public final static int Message_Type_System_FeedBack = 61;//反馈
    public final static int Message_Type_System_Warning = 62;//警告


    /**
     * 创建新的通知
     *
     * @param type
     * @param attachId
     * @param generateId
     * @param receiveId
     * @param content
     */
    public static void createNewNoticeMsg(int type, String attachId, String generateId, String receiveId, String content, String remarks) {
        logger.info("type:" + type
                + "\nattachId:" + attachId
                + "\ngenerateId:" + generateId
                + "\nreceiveId:" + receiveId
                + "\ncontent:" + content);
        //生成通知的id
        String noticeId = RandomUtils.getRandomWord(30);
        logger.info("生成noticeId：" + noticeId);
        NoticeMessage message = NoticeMessage.findNoticeById(noticeId);
        if (null == message) {
            new NoticeMessage()
                    .set("notice_id", noticeId)
                    .set("message_type", type)
                    .set("attach_id", attachId)
                    .set("generate_id", generateId)
                    .set("receive_uid", receiveId)
                    .set("content", content.length() > 300 ? content.substring(0, 300) : content)
                    .set("remarks", remarks)
                    .set("flag", 0)
                    .set("created_time", System.currentTimeMillis())
                    .set("updated_time", System.currentTimeMillis())
                    .save();
        }
    }

    /**
     * 通过id查找对象
     *
     * @param noticeId
     * @return
     */
    public static NoticeMessage findNoticeById(String noticeId) {
        return NoticeMessage.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_Message_Notice_Table
                + " WHERE notice_id = '" + noticeId + "'");
    }

    /***
     * 通过id
     * @param uuid
     * @return
     */
    public static List<NoticeMessage> findNoticeMessage(String uuid) {
        return NoticeMessage.dao.find("SELECT * FROM " + GlobalConstants.Lite_Message_Notice_Table
                + " WHERE receive_id = '" + uuid + "'");
    }


    /***
     * 删除通过attachId
     * @param attachId
     * @return
     */
    public static void deleteByAttachId(String attachId) {
        Db.update("DELETE FROM " + GlobalConstants.Lite_Message_Notice_Table + " WHERE attach_id = '" + attachId + "'");
    }

    /***
     * 通过回答ID删除一系列
     * @param idList
     */
    public static void deleteByByIds(List<String> idList) {
        if (idList != null && idList.size() > 0) {
            StringBuilder answerValue = new StringBuilder();
            answerValue.append("'").append(idList.get(0)).append("'");
            for (int index = 1; index < idList.size(); index++) {
                answerValue.append(",'").append(idList.get(index)).append("'");
            }
            Db.update("DELETE FROM " + GlobalConstants.Lite_Message_Notice_Table + " WHERE attach_id IN (" + answerValue.toString() + ")");
        }
    }

}
