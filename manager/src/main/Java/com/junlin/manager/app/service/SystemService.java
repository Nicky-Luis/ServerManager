package com.junlin.manager.app.service;

import com.google.gson.Gson;
import com.junlin.manager.app.entity.Feedback;
import com.junlin.manager.app.utils.VerifyUtils;
import com.mchange.v2.lang.StringUtils;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by junlinhui eight on 2017/4/7.
 * 逻辑层
 */
public class SystemService {

    //logger
    private static Logger logger = Logger.getLogger(SystemService.class.getName());

    /***
     * 用户提交意见反馈
     * @param sessionKey
     * @return
     */
    public static String startUploadFeedback(String sessionKey, String content) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");


        if (StringUtils.nonEmptyString(content.trim())) {
            Feedback.newFeedback(uuid,content);
            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
        } else {
            resultMap.put("code", VerifyUtils.Err_Code_Operate);
            resultMap.put("msg", "操作失败");
        }
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

}
