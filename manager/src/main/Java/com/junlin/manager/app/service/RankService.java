package com.junlin.manager.app.service;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Record;
import com.junlin.manager.app.utils.VerifyUtils;
import com.junlin.manager.utils.SqlHelper;
import com.mchange.v2.lang.StringUtils;

import org.apache.log4j.Logger;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Millisecond;
import org.ocpsoft.prettytime.units.Second;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.junlin.manager.GlobalConstants.Lite_Answer_Table;
import static com.junlin.manager.GlobalConstants.Lite_Question_Follow_Table;
import static com.junlin.manager.GlobalConstants.Lite_Question_Label_Table;
import static com.junlin.manager.GlobalConstants.Lite_Question_Table;
import static com.junlin.manager.GlobalConstants.Lite_Question_Target_Table;
import static com.junlin.manager.GlobalConstants.Lite_User_Table;
import static com.junlin.manager.utils.TimeUtils.setDeadlineFormat;

/**
 * Created by junlinhui eight on 2017/4/13.
 * 问题处理
 */
public class RankService {

    //logger
    private static Logger logger = Logger.getLogger(RankService.class.getName());

    /**
     * 查询热门问题榜单
     *
     * @param sessionKey
     * @param page
     * @param pageCount
     * @return
     */
    public static String findRanks(String sessionKey, int page, int pageCount, String label, String target) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }

        if (page > 0) {
            SqlHelper sqlHelper = getRankSqlHelper();

            //分页查询具体数据
            List<Record> records;
            if (StringUtils.nonEmptyString(label.trim())) {
                records = sqlHelper
                        .eq(Lite_Question_Label_Table + ".label", label)
                        .gruopBy(Lite_Question_Table + ".question_id")
                        .orderByDesc("count")
                        .paginate(page, pageCount);
            } else if (StringUtils.nonEmptyString(target.trim())) {
                records = sqlHelper
                        .eq(Lite_Question_Target_Table + ".target", target)
                        .gruopBy(Lite_Question_Table + ".question_id")
                        .orderByDesc("count")
                        .paginate(page, pageCount);
            } else {
                records = sqlHelper
                        .gruopBy(Lite_Question_Table + ".question_id")
                        .orderByDesc("count")
                        .paginate(page, pageCount);
            }

            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
            resultMap.put("count", records.size());
            resultMap.put("result", resultRanksJson(records));
        }

        String result = new Gson().toJson(resultMap);
        logger.info("查询热门问题榜单结果：" + result);
        return result;
    }

    /***
     * 获取榜单sql
     * @return
     */
    public static SqlHelper getRankSqlHelper() {
        String[] obtainParams = {
                Lite_Question_Table + ".question_id",
                Lite_Question_Target_Table + ".content AS target",
                Lite_Question_Table + ".depict",
                Lite_Question_Label_Table + ".content AS label",
                Lite_Question_Table + ".price",
                Lite_Question_Table + ".title",
                Lite_Question_Table + ".updated_time AS time",
                Lite_User_Table + ".nickName As userName",
                Lite_User_Table + ".avatarUrl As userHeadImg",
                Lite_Question_Table + ".deadline",
                "COUNT(distinct " + Lite_Question_Follow_Table + ".question_id) AS followCount",
                "COUNT(distinct " + Lite_Answer_Table + ".answer_id) AS answerCount",
                "COUNT(distinct " + Lite_Question_Follow_Table + ".question_id) + " +
                        "COUNT(distinct " + Lite_Answer_Table + ".answer_id) AS count",
        };

        return SqlHelper.fromTableName(Lite_Question_Table, obtainParams)
                .leftJoin(Lite_Question_Follow_Table, Lite_Question_Follow_Table + ".question_id", Lite_Question_Table + ".question_id")
                .leftJoin(Lite_Answer_Table, Lite_Answer_Table + ".question_id", Lite_Question_Table + ".question_id")
                .innerJoin(Lite_User_Table, Lite_User_Table + ".uuid", Lite_Question_Table + ".uuid")
                .innerJoin(Lite_Question_Label_Table, Lite_Question_Label_Table + ".label", Lite_Question_Table + ".label")
                .innerJoin(Lite_Question_Target_Table, Lite_Question_Target_Table + ".target", Lite_Question_Table + ".target");
    }

    /***
     * 返回结果设置
     * @param records
     * @return
     */
    public static List<Map<String, Object>> resultRanksJson(List<Record> records) {
        List<Map<String, Object>> dataMaps = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            String[] attrsName = record.getColumnNames();
            Object[] attrsValue = record.getColumnValues();

            //修改发布的时间的格式
            for (int index = 0; index < attrsName.length; index++) {
                if (attrsName[index].equals("time")) {
                    PrettyTime prettyTime = new PrettyTime(Locale.CHINESE);
                    prettyTime.removeUnit(Millisecond.class);
                    prettyTime.removeUnit(Second.class);
                    prettyTime.removeUnit(JustNow.class);
                    Double time = (Double) attrsValue[index];
                    dataMap.put(attrsName[index], prettyTime.format(new Date(time.longValue())).replace(" ", ""));
                }
                if (attrsName[index].equals("deadline")) {
                    setDeadlineFormat(dataMap, attrsName[index], attrsValue[index]);
                } else {
                    dataMap.put(attrsName[index], attrsValue[index]);
                }
            }
            dataMaps.add(dataMap);
        }
        return dataMaps;
    }

}
