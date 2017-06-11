package com.junlin.manager.app.service;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Record;
import com.junlin.manager.app.entity.QuestionTarget;
import com.junlin.manager.app.utils.VerifyUtils;
import com.junlin.manager.utils.SqlHelper;
import com.mchange.v2.lang.StringUtils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.junlin.manager.GlobalConstants.Lite_Question_Target_Table;

/**
 * Created by junlinhui eight on 2017/4/13.
 * 问题处理
 */
public class TargetService {

    //logger
    private static Logger logger = Logger.getLogger(TargetService.class.getName());


    /**
     * 搜索标的
     */
    public static String startSearchTarget(String sessionKey, String keyWord, String page, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }

        String[] obtainParams = {
                Lite_Question_Target_Table + ".content",
                Lite_Question_Target_Table + ".target",};

        SqlHelper sqlHelper = SqlHelper.fromTableName(Lite_Question_Target_Table, obtainParams)
                .likeWith(Lite_Question_Target_Table + ".content", keyWord);

        //分页查询具体数据
        int pageNum = 0;
        resultMap.put("exist", QuestionTarget.isTargetExist(keyWord));
        resultMap.put("code", 1);
        resultMap.put("msg", "操作成功");
        try {
            pageNum = Integer.valueOf(page);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pageNum > 0) {
            List<Record> records = sqlHelper.paginate(pageNum, pageCount);
            resultMap.put("count", records.size());
            resultMap.put("result", targetSearchToJson(keyWord, records));
        } else {
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "参数错误");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }


    /**
     * 用户创建新的标的
     *
     * @param sessionKey
     * @param content
     * @return
     */
    public static String createQuestionTarget(String sessionKey, String content) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");
        if (StringUtils.nonEmptyString(content)) {
            int target = QuestionTarget.getTargetCount();
            QuestionTarget.newQuestionTarget(uuid, target, content);
            resultMap.put("msg", "操作成功");
            resultMap.put("target", target);
        } else {
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "操作失败");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    ///////////////////////////////////private method//////////////////////////////////////////


    /***
     * 返回结果设置
     * @param records
     * @return
     */
    private static List<Map<String, Object>> targetSearchToJson(String keyWord, List<Record> records) {
        List<Map<String, Object>> dataMaps = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            String[] attrsName = record.getColumnNames();
            Object[] attrsValue = record.getColumnValues();

            //修改发布的时间的格式
            for (int index = 0; index < attrsName.length; index++) {
                switch (attrsName[index]) {
                    case "content": {
                        String orgStr = String.valueOf(attrsValue[index] == null ? "" : attrsValue[index]);
                        if (StringUtils.nonEmptyString(orgStr) && orgStr.contains(keyWord)) {
                            int wordIndex = orgStr.indexOf(keyWord);
                            int startIndex = wordIndex > 20 ? wordIndex - 20 : 0;
                            int endIndex = wordIndex + keyWord.length();
                            String[] subArray = new String[]{
                                    orgStr.substring(startIndex, wordIndex),
                                    keyWord,
                                    orgStr.substring(endIndex, orgStr.length())};
                            dataMap.put(attrsName[index], subArray);
                        } else {
                            dataMap.put(attrsName[index], orgStr);
                        }
                    }
                    break;

                    default:
                        dataMap.put(attrsName[index], attrsValue[index]);
                        break;
                }
            }
            dataMaps.add(dataMap);
        }
        return dataMaps;
    }
}
