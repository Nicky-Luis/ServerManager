package com.junlin.manager.app.service;

import com.google.gson.Gson;
import com.junlin.manager.app.entity.Answer;
import com.junlin.manager.app.entity.LiteUser;
import com.junlin.manager.app.entity.NoticeMessage;
import com.junlin.manager.app.entity.Question;
import com.junlin.manager.app.entity.QuestionFollow;
import com.junlin.manager.app.entity.UserRelation;
import com.junlin.manager.app.utils.VerifyUtils;
import com.mchange.v2.lang.StringUtils;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by junlinhui eight on 2017/4/7.
 * 逻辑层
 */
public class UserService {

    //logger
    private static Logger logger = Logger.getLogger(UserService.class.getName());

    /***
     * 获取用户的信息
     * @param sessionKey
     * @param userId
     * @return
     */
    public static String startSearchPerson(String sessionKey, String userId) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        //获取我自己的id
        String myUid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        if (StringUtils.nonEmptyString(userId)) {
            LiteUser user = LiteUser.findUserByUUID(userId);
            //是否互相关注了，0:双方都未关注，1：双方互相关注，2：我关注对方，3：对方关注我
            if (StringUtils.nonEmptyString(myUid)) {
                int result = 0;
                boolean followMe = UserRelation.isFollow(myUid, userId);
                boolean followUser = UserRelation.isFollow(userId, myUid);
                if (followMe && followUser) {
                    result = 1;
                } else if (followUser) {
                    result = 2;
                } else if (followMe) {
                    result = 3;
                }
                resultMap.put("followStatus", result);
            }

            resultMap.put("msg", "操作成功");
            resultMap.put("user", resultUserJson(user));
            resultMap.put("followed", QuestionFollow.findUserFollowedCount(userId));
            resultMap.put("ask", Question.findPublishCount(userId));
            resultMap.put("answer", Answer.findUserAnswerCount(userId));
        } else {
            resultMap.put("msg", "参数错误");
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
        }
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 创建消息
     *
     * @param sessionKey
     * @param userId
     * @param content
     * @return
     */
    public static String startLeaveMessage(String sessionKey, String userId, String content) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        //获取我自己的id
        String myUid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");
        if (StringUtils.nonEmptyString(userId) && StringUtils.nonEmptyString(content)) {
            newLeaveMessageNotice(myUid, userId, content);
            resultMap.put("msg", "操作成功");
        } else {
            resultMap.put("msg", "参数错误");
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
        }
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }


    /////////////////////////////////////////////////////////////////////

    /**
     * 创建新的留言通知
     *
     * @param myUid
     * @param receiveId
     * @param content
     */
    private static void newLeaveMessageNotice(String myUid, String receiveId, String content) {
        NoticeMessage.createNewNoticeMsg(
                NoticeMessage.Message_Type_User_Message,
                myUid,
                myUid,
                receiveId,
                content,
                "");
    }

    /***
     * 返回结果设置
     * @param user
     * @return
     */
    private static Map<String, Object> resultUserJson(LiteUser user) {
        if (null == user) {
            return null;
        }
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String[] attrsName = user._getAttrNames();
        Object[] attrsValue = user._getAttrValues();


        //修改发布的时间的格式
        for (int index = 0; index < attrsName.length; index++) {
            dataMap.put(attrsName[index], attrsValue[index]);
        }
        return dataMap;
    }

}
