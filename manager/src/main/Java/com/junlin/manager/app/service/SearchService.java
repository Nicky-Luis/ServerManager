package com.junlin.manager.app.service;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Record;
import com.junlin.manager.GlobalConstants;
import com.junlin.manager.app.utils.VerifyUtils;
import com.junlin.manager.utils.SqlHelper;
import com.mchange.v2.lang.StringUtils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.junlin.manager.GlobalConstants.*;
import static com.junlin.manager.utils.TimeUtils.setDeadlineFormat;
import static com.junlin.manager.utils.TimeUtils.setTimePretty;

/**
 * Created by junlinhui eight on 2017/4/13.
 * 搜索部分
 */
public class SearchService {

    //logger
    private static Logger logger = Logger.getLogger(SearchService.class.getName());

    /**
     * 开始进行关键字的搜索
     *
     * @param sessionKey
     * @param keyWord
     * @param page
     * @param pageCount
     * @return
     */
    public static String startSearch(String sessionKey, String keyWord, int page, int pageCount) {

        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        if (page > 0) {
            String[] obtainParams = {
                    Lite_Question_Table + ".question_id",
                    Lite_Question_Label_Table + ".label",
                    Lite_Question_Table + ".price",
                    Lite_Question_Table + ".title",
                    Lite_Question_Table + ".deadline",
                    Lite_Question_Table + ".updated_time AS time",
                    Lite_Answer_Table + ".point",
                    "CONCAT(" + Lite_Question_Target_Table + ".content" + ",CONCAT(" + Lite_Question_Table
                            + ".depict,IFNULL(GROUP_CONCAT(" + Lite_Answer_Table + ".content),''))) AS content",
                    "COUNT(distinct " + Lite_Question_Follow_Table + ".question_id) AS followCount",
                    "COUNT(distinct " + Lite_Answer_Table + ".answer_id) AS answerCount",
            };

            SqlHelper sqlHelper = SqlHelper.fromTableName(Lite_Question_Table, obtainParams)
                    .leftJoin(Lite_Question_Follow_Table, Lite_Question_Follow_Table + ".question_id", Lite_Question_Table + ".question_id")
                    .leftJoin(Lite_Answer_Table, Lite_Answer_Table + ".question_id", Lite_Question_Table + ".question_id")
                    .leftJoin(Lite_Question_Target_Table, Lite_Question_Target_Table + ".target", Lite_Question_Table + ".target")
                    .leftJoin(Lite_Question_Label_Table, Lite_Question_Label_Table + ".label", Lite_Question_Table + ".label")
                    .likeWith(Lite_Question_Target_Table + ".content", keyWord)
                    .likeOr(Lite_Question_Table + ".depict", keyWord)
                    .likeOr(Lite_Question_Table + ".title", keyWord)
                    .likeOr(Lite_Answer_Table + ".content", keyWord);

            //分页查询具体数据
            List<Record> records = sqlHelper
                    .gruopBy(GlobalConstants.Lite_Question_Table + ".question_id")
                    .paginate(page, pageCount);

            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
            resultMap.put("count", records.size());
            resultMap.put("result", resultSearchJson(keyWord, records));
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
    private static List<Map<String, Object>> resultSearchJson(String keyWord, List<Record> records) {
        List<Map<String, Object>> dataMaps = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            String[] attrsName = record.getColumnNames();
            Object[] attrsValue = record.getColumnValues();

            //修改发布的时间的格式
            for (int index = 0; index < attrsName.length; index++) {
                switch (attrsName[index]) {
                    case "title": {
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
                            //是否包含关键字
                            dataMap.put("inTitle", true);
                        } else {
                            dataMap.put(attrsName[index], orgStr);
                            dataMap.put("inTitle", false);
                        }
                    }
                    break;

                    case "content": {
                        String orgStr = String.valueOf(attrsValue[index] == null ? "" : attrsValue[index]);
                        if (StringUtils.nonEmptyString(orgStr) && orgStr.contains(keyWord)) {
                            int wordIndex = orgStr.indexOf(keyWord);
                            int startIndex = wordIndex > 30 ? wordIndex - 30 : 0;
                            int endIndex = wordIndex + keyWord.length();

                            String[] subArray = new String[]{
                                    orgStr.substring(startIndex, wordIndex),
                                    keyWord,
                                    orgStr.substring(endIndex, orgStr.length())};
                            dataMap.put(attrsName[index], subArray);
                            //是否包含关键字
                            dataMap.put("inContent", true);
                        } else {
                            dataMap.put(attrsName[index], orgStr);
                            dataMap.put("inContent", false);
                        }
                    }
                    break;

                    case "time":
                        setTimePretty(dataMap, attrsName[index], attrsValue[index]);
                        break;

                    case "`deadline":
                        setDeadlineFormat(dataMap, attrsName[index], attrsValue[index]);
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
