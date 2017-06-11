package com.junlin.manager.app.service;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.junlin.manager.GlobalConstants;
import com.junlin.manager.app.utils.VerifyUtils;
import com.junlin.manager.utils.SqlHelper;

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

import static com.junlin.manager.utils.TimeUtils.setDeadlineFormat;

/**
 * Created by junlinhui eight on 2017/4/13.
 * 问题处理
 */
public class HomeService {

    //logger
    private static Logger logger = Logger.getLogger(HomeService.class.getName());

    public static String findRankInfo(String sessionKey, int page, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }

        if (page > 0) {
            SqlHelper sqlHelper = RankService.getRankSqlHelper();

            //分页查询具体数据
            List<Record> records = sqlHelper
                    .gruopBy(GlobalConstants.Lite_Question_Table + ".question_id")
                    .orderByDesc("count")
                    .paginate(page, pageCount);
            resultMap.put("popular", findPopularLabel());
            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
            resultMap.put("count", records.size());
            resultMap.put("result", RankService.resultRanksJson(records));
        }

        String result = new Gson().toJson(resultMap);
        logger.info("查询热门问题榜单结果：" + result);
        return result;
    }

    /**
     * 获取feed流
     *
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     * @return
     */
    public static String findFeeds(String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        try {
            int page = Integer.valueOf(pageStr);
            if (page > 0) {
                //分页查询feed流的具体数据
                List<Record> records = doFeedSql(page, pageCount, uuid);
                resultMap.put("code", VerifyUtils.Code_Succeed);
                resultMap.put("msg", "操作成功");
                resultMap.put("count", records.size());
                resultMap.put("result", resultFeedsJson(records));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", VerifyUtils.Err_Code_Operate);
            resultMap.put("msg", "操作失败");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }


    ///////////////////////////////////private method//////////////////////////////////////////

    /***
     * 查找热门的四个标签
     * @return
     */
    private static Map<String, Object> findPopularLabel() {
        String[] obtainParams = {
                GlobalConstants.Lite_Question_Label_Table + ".label",
                GlobalConstants.Lite_Question_Label_Table + ".content",
                "COUNT(" + GlobalConstants.Lite_Question_Table + ".label) AS count",
        };

        SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Question_Label_Table, obtainParams)
                .innerJoin(GlobalConstants.Lite_Question_Table, GlobalConstants.Lite_Question_Table + ".label",
                        GlobalConstants.Lite_Question_Label_Table + ".label");
        //分页查询具体数据
        List<Record> records = sqlHelper
                .gruopBy(GlobalConstants.Lite_Question_Table + ".label")
                .orderByDesc("count")
                .paginate(1, 4);

        return resultPopularJson(records);
    }


    /***
     * 返回结果设置
     * @param records
     * @return
     */
    private static Map<String, Object> resultPopularJson(List<Record> records) {
        List<Map<String, Object>> labels = new ArrayList<>();
        List<Map<String, Object>> targets = new ArrayList<>();
        Map<String, Object> popularMap = new HashMap<>();
        for (Record record : records) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            String[] attrsName = record.getColumnNames();
            Object[] attrsValue = record.getColumnValues();

            for (int index = 0; index < attrsName.length; index++) {
                dataMap.put(attrsName[index], attrsValue[index]);
                if (attrsName[index].equals("label")) {
                    targets.add(findPopularTarget(String.valueOf(attrsValue[index])));
                }
            }
            labels.add(dataMap);
        }
        popularMap.put("labels", labels);
        popularMap.put("targets", targets);
        return popularMap;
    }

    /**
     * 查找标签下面最热门的标的
     *
     * @param label
     */
    private static Map<String, Object> findPopularTarget(String label) {
        String[] obtainParams = {
                GlobalConstants.Lite_Question_Target_Table + ".target",
                GlobalConstants.Lite_Question_Target_Table + ".content",
                GlobalConstants.Lite_Question_Table + ".label",
                "COUNT(" + GlobalConstants.Lite_Question_Table + ".target) AS count"};

        SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Question_Target_Table, obtainParams)
                .innerJoin(GlobalConstants.Lite_Question_Table,
                        GlobalConstants.Lite_Question_Table + ".target",
                        GlobalConstants.Lite_Question_Target_Table + ".target");
        //分页查询具体数据
        List<Record> records = sqlHelper
                .eq(GlobalConstants.Lite_Question_Table + ".label", label)
                .gruopBy(GlobalConstants.Lite_Question_Table + ".target")
                .orderByDesc("count")
                .paginate(1, 1);

        if (null != records && records.size() > 0) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            String[] attrsName = records.get(0).getColumnNames();
            Object[] attrsValue = records.get(0).getColumnValues();

            for (int index = 0; index < attrsName.length; index++) {
                dataMap.put(attrsName[index], String.valueOf(attrsValue[index]));
            }
            return dataMap;
        }
        return null;
    }

    /***
     * 返回结果设置
     * @param records
     * @return
     */
    private static List<Map<String, Object>> resultFeedsJson(List<Record> records) {
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
                    prettyTime.removeUnit(JustNow.class);
                    prettyTime.removeUnit(Second.class);

                    Double time = (Double) attrsValue[index];
                    dataMap.put(attrsName[index], prettyTime.format(new Date(time.longValue())).replace(" ", ""));
                } else if (attrsName[index].equals("deadline")) {
                    setDeadlineFormat(dataMap, attrsName[index], attrsValue[index]);
                } else {
                    dataMap.put(attrsName[index], attrsValue[index]);
                }
            }
            dataMaps.add(dataMap);
        }
        return dataMaps;
    }

    /***
     * 进行feed流信息查询
     * @param pageNumber
     * @param pageSize
     * @param uuid
     * @return
     */
    private static List<Record> doFeedSql(int pageNumber, int pageSize, String uuid) {
        //关注的人的回答
        String[] followAnswerParams = {
                GlobalConstants.Lite_Answer_Table + ".answer_id AS id",
                GlobalConstants.Lite_Question_Target_Table + ".content AS target",
                GlobalConstants.Lite_Question_Label_Table + ".content AS label",
                GlobalConstants.Lite_Question_Table + ".price",
                GlobalConstants.Lite_Question_Table + ".title",
                GlobalConstants.Lite_User_Table + ".nickName",
                GlobalConstants.Lite_User_Table + ".avatarUrl",
                GlobalConstants.Lite_Answer_Table + ".content AS content",
                GlobalConstants.Lite_Answer_Table + ".updated_time AS time",
                GlobalConstants.Lite_Question_Table + ".deadline",
                "0 as feedType",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Like_Table + ".created_time) AS likeCount",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Comment_Table + ".comment_id) AS replyCount",
        };

        SqlHelper followAnswerSqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Answer_Table, followAnswerParams)
                .innerJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                        GlobalConstants.Lite_Answer_Table + ".uuid")
                .innerJoin(GlobalConstants.Lite_User_Relationship_Table, GlobalConstants.Lite_User_Relationship_Table + ".uuid",
                        GlobalConstants.Lite_Answer_Table + ".uuid")
                .innerJoin(GlobalConstants.Lite_Question_Table, GlobalConstants.Lite_Question_Table + ".question_id",
                        GlobalConstants.Lite_Answer_Table + ".question_id")
                .leftJoin(GlobalConstants.Lite_Answer_Like_Table, GlobalConstants.Lite_Answer_Like_Table + ".answer_id",
                        GlobalConstants.Lite_Answer_Table + ".answer_id")
                .leftJoin(GlobalConstants.Lite_Answer_Comment_Table, GlobalConstants.Lite_Answer_Comment_Table + ".answer_id",
                        GlobalConstants.Lite_Answer_Table + ".answer_id")
                .innerJoin(GlobalConstants.Lite_Question_Label_Table, GlobalConstants.Lite_Question_Label_Table + ".label",
                        GlobalConstants.Lite_Question_Table + ".label")
                .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                        GlobalConstants.Lite_Question_Table + ".target")
                .eq(GlobalConstants.Lite_User_Relationship_Table + ".fans_uuid", "'" + uuid + "'")
                .gruopBy(GlobalConstants.Lite_Answer_Table + ".answer_id");

        //关注的人点赞的答案
        String[] followLikeParams = {
                GlobalConstants.Lite_Answer_Table + ".answer_id AS id",
                GlobalConstants.Lite_Question_Target_Table + ".content AS target",
                GlobalConstants.Lite_Question_Label_Table + ".content AS label",
                GlobalConstants.Lite_Question_Table + ".price",
                GlobalConstants.Lite_Question_Table + ".title",
                GlobalConstants.Lite_User_Table + ".nickName",
                GlobalConstants.Lite_User_Table + ".avatarUrl",
                GlobalConstants.Lite_Answer_Table + ".content AS content",
                GlobalConstants.Lite_Answer_Like_Table + ".updated_time AS time",
                GlobalConstants.Lite_Question_Table + ".deadline",
                "1 as feedType",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Like_Table + ".created_time) AS likeCount",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Comment_Table + ".comment_id) AS replyCount",
        };

        SqlHelper followLikeSqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Answer_Table, followLikeParams)
                .leftJoin(GlobalConstants.Lite_Answer_Like_Table, GlobalConstants.Lite_Answer_Like_Table + ".answer_id",
                        GlobalConstants.Lite_Answer_Table + ".answer_id")
                .leftJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                        GlobalConstants.Lite_Answer_Like_Table + ".uuid")
                .leftJoin(GlobalConstants.Lite_User_Relationship_Table, GlobalConstants.Lite_User_Relationship_Table + ".uuid",
                        GlobalConstants.Lite_Answer_Like_Table + ".uuid")
                .innerJoin(GlobalConstants.Lite_Question_Table, GlobalConstants.Lite_Question_Table + ".question_id",
                        GlobalConstants.Lite_Answer_Table + ".question_id")
                .leftJoin(GlobalConstants.Lite_Answer_Comment_Table, GlobalConstants.Lite_Answer_Comment_Table + ".answer_id",
                        GlobalConstants.Lite_Answer_Table + ".answer_id")
                .innerJoin(GlobalConstants.Lite_Question_Label_Table, GlobalConstants.Lite_Question_Label_Table + ".label",
                        GlobalConstants.Lite_Question_Table + ".label")
                .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                        GlobalConstants.Lite_Question_Table + ".target")
                .eq(GlobalConstants.Lite_User_Relationship_Table + ".fans_uuid", "'" + uuid + "'")
                .gruopBy(GlobalConstants.Lite_Answer_Table + ".answer_id");

        //关注的人关注了问题
        String[] followParams = {
                GlobalConstants.Lite_Question_Table + ".question_id AS id",
                GlobalConstants.Lite_Question_Target_Table + ".content AS target",
                GlobalConstants.Lite_Question_Label_Table + ".content AS label",
                GlobalConstants.Lite_Question_Table + ".price",
                GlobalConstants.Lite_Question_Table + ".title",
                GlobalConstants.Lite_User_Table + ".nickName",
                GlobalConstants.Lite_User_Table + ".avatarUrl",
                GlobalConstants.Lite_Question_Table + ".depict AS content",
                GlobalConstants.Lite_Question_Follow_Table + ".updated_time AS time",
                GlobalConstants.Lite_Question_Table + ".deadline",
                "2 as feedType",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Question_Follow_Table + ".created_time) AS likeCount",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Table + ".answer_id) AS replyCount",
        };

        SqlHelper followSqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Question_Table, followParams)
                .leftJoin(GlobalConstants.Lite_Question_Follow_Table, GlobalConstants.Lite_Question_Follow_Table + ".question_id",
                        GlobalConstants.Lite_Question_Table + ".question_id")
                .leftJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                        GlobalConstants.Lite_Question_Follow_Table + ".uuid")
                .leftJoin(GlobalConstants.Lite_User_Relationship_Table, GlobalConstants.Lite_User_Relationship_Table + ".uuid",
                        GlobalConstants.Lite_Question_Follow_Table + ".uuid")
                .leftJoin(GlobalConstants.Lite_Answer_Table, GlobalConstants.Lite_Answer_Table + ".question_id",
                        GlobalConstants.Lite_Question_Table + ".question_id")
                .innerJoin(GlobalConstants.Lite_Question_Label_Table, GlobalConstants.Lite_Question_Label_Table + ".label",
                        GlobalConstants.Lite_Question_Table + ".label")
                .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                        GlobalConstants.Lite_Question_Table + ".target")
                .eq(GlobalConstants.Lite_User_Relationship_Table + ".fans_uuid", "'" + uuid + "'")
                .gruopBy(GlobalConstants.Lite_Question_Table + ".question_id");

        //关注的人发布了问题
        String[] followPublishParams = {
                GlobalConstants.Lite_Question_Table + ".question_id AS id",
                GlobalConstants.Lite_Question_Target_Table + ".content AS target",
                GlobalConstants.Lite_Question_Label_Table + ".content AS label",
                GlobalConstants.Lite_Question_Table + ".price",
                GlobalConstants.Lite_Question_Table + ".title",
                GlobalConstants.Lite_User_Table + ".nickName",
                GlobalConstants.Lite_User_Table + ".avatarUrl",
                GlobalConstants.Lite_Question_Table + ".depict AS content",
                GlobalConstants.Lite_Question_Table + ".updated_time AS time",
                GlobalConstants.Lite_Question_Table + ".deadline",
                "3 as feedType",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Question_Follow_Table + ".created_time) AS likeCount",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Table + ".answer_id) AS replyCount",
        };

        SqlHelper followPublishSqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Question_Table, followPublishParams)
                .leftJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                        GlobalConstants.Lite_Question_Table + ".uuid")
                .leftJoin(GlobalConstants.Lite_User_Relationship_Table, GlobalConstants.Lite_User_Relationship_Table + ".uuid",
                        GlobalConstants.Lite_Question_Table + ".uuid")
                .leftJoin(GlobalConstants.Lite_Question_Follow_Table, GlobalConstants.Lite_Question_Follow_Table + ".question_id",
                        GlobalConstants.Lite_Question_Table + ".question_id")
                .leftJoin(GlobalConstants.Lite_Answer_Table, GlobalConstants.Lite_Answer_Table + ".question_id",
                        GlobalConstants.Lite_Question_Table + ".question_id")
                .innerJoin(GlobalConstants.Lite_Question_Label_Table, GlobalConstants.Lite_Question_Label_Table + ".label",
                        GlobalConstants.Lite_Question_Table + ".label")
                .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                        GlobalConstants.Lite_Question_Table + ".target")
                .eq(GlobalConstants.Lite_User_Relationship_Table + ".fans_uuid", "'" + uuid + "'")
                .gruopBy(GlobalConstants.Lite_Question_Table + ".question_id");

        //问题的动态
        String[] questionParams = {
                GlobalConstants.Lite_Answer_Table + ".answer_id AS id",
                GlobalConstants.Lite_Question_Target_Table + ".content AS target",
                GlobalConstants.Lite_Question_Label_Table + ".content AS label",
                GlobalConstants.Lite_Question_Table + ".price",
                GlobalConstants.Lite_Question_Table + ".title",
                GlobalConstants.Lite_User_Table + ".nickName",
                GlobalConstants.Lite_User_Table + ".avatarUrl",
                GlobalConstants.Lite_Answer_Table + ".content AS content",
                GlobalConstants.Lite_Answer_Table + ".updated_time AS time",
                GlobalConstants.Lite_Question_Table + ".deadline",
                "4 as feedType",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Like_Table + ".created_time) AS likeCount",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Comment_Table + ".comment_id) AS replyCount",
        };

        SqlHelper questionSqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Answer_Table, questionParams)
                .innerJoin(GlobalConstants.Lite_Question_Table, GlobalConstants.Lite_Question_Table + ".question_id",
                        GlobalConstants.Lite_Answer_Table + ".question_id")
                .innerJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                        GlobalConstants.Lite_Answer_Table + ".uuid")
                .leftJoin(GlobalConstants.Lite_Answer_Like_Table, GlobalConstants.Lite_Answer_Like_Table + ".answer_id",
                        GlobalConstants.Lite_Answer_Table + ".answer_id")
                .leftJoin(GlobalConstants.Lite_Question_Follow_Table, GlobalConstants.Lite_Question_Follow_Table + ".question_id",
                        GlobalConstants.Lite_Question_Table + ".question_id")
                .leftJoin(GlobalConstants.Lite_Answer_Comment_Table, GlobalConstants.Lite_Answer_Comment_Table + ".answer_id",
                        GlobalConstants.Lite_Answer_Table + ".answer_id")
                .innerJoin(GlobalConstants.Lite_Question_Label_Table, GlobalConstants.Lite_Question_Label_Table + ".label",
                        GlobalConstants.Lite_Question_Table + ".label")
                .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                        GlobalConstants.Lite_Question_Table + ".target")
                .eq(GlobalConstants.Lite_Question_Follow_Table + ".uuid", "'" + uuid + "'")
                .ne(GlobalConstants.Lite_Answer_Table + ".uuid", "'" + uuid + "'")
                .gruopBy(GlobalConstants.Lite_Answer_Table + ".answer_id");

        //用户自己的回答
        String[] myselfParams = {
                GlobalConstants.Lite_Answer_Table + ".answer_id AS id",
                GlobalConstants.Lite_Question_Target_Table + ".content AS target",
                GlobalConstants.Lite_Question_Label_Table + ".content AS label",
                GlobalConstants.Lite_Question_Table + ".price",
                GlobalConstants.Lite_Question_Table + ".title",
                GlobalConstants.Lite_User_Table + ".nickName",
                GlobalConstants.Lite_User_Table + ".avatarUrl",
                GlobalConstants.Lite_Answer_Table + ".content AS content",
                GlobalConstants.Lite_Answer_Table + ".updated_time AS time",
                GlobalConstants.Lite_Question_Table + ".deadline",
                "5 as feedType",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Like_Table + ".created_time) AS likeCount",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Comment_Table + ".comment_id) AS replyCount",
        };

        SqlHelper myselfHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Answer_Table, myselfParams)
                .innerJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                        GlobalConstants.Lite_Answer_Table + ".uuid")
                .innerJoin(GlobalConstants.Lite_Question_Table, GlobalConstants.Lite_Question_Table + ".question_id",
                        GlobalConstants.Lite_Answer_Table + ".question_id")
                .leftJoin(GlobalConstants.Lite_Answer_Like_Table, GlobalConstants.Lite_Answer_Like_Table + ".answer_id",
                        GlobalConstants.Lite_Answer_Table + ".answer_id")
                .leftJoin(GlobalConstants.Lite_Answer_Comment_Table, GlobalConstants.Lite_Answer_Comment_Table + ".answer_id",
                        GlobalConstants.Lite_Answer_Table + ".answer_id")
                .innerJoin(GlobalConstants.Lite_Question_Label_Table, GlobalConstants.Lite_Question_Label_Table + ".label",
                        GlobalConstants.Lite_Question_Table + ".label")
                .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                        GlobalConstants.Lite_Question_Table + ".target")
                .eq(GlobalConstants.Lite_Answer_Table + ".uuid", "'" + uuid + "'")
                .gruopBy(GlobalConstants.Lite_Answer_Table + ".answer_id");

        //自己发布的问题
        String[] myselfPublishParams = {
                GlobalConstants.Lite_Question_Table + ".question_id AS id",
                GlobalConstants.Lite_Question_Target_Table + ".content AS target",
                GlobalConstants.Lite_Question_Label_Table + ".content AS label",
                GlobalConstants.Lite_Question_Table + ".price",
                GlobalConstants.Lite_Question_Table + ".title",
                GlobalConstants.Lite_User_Table + ".nickName",
                GlobalConstants.Lite_User_Table + ".avatarUrl",
                GlobalConstants.Lite_Question_Table + ".depict AS content",
                GlobalConstants.Lite_Question_Table + ".updated_time AS time",
                GlobalConstants.Lite_Question_Table + ".deadline",
                "6 as feedType",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Question_Follow_Table + ".created_time) AS likeCount",
                "COUNT( DISTINCT " + GlobalConstants.Lite_Answer_Table + ".answer_id) AS replyCount",
        };

        SqlHelper myselfPublishSqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_Question_Table, myselfPublishParams)
                .innerJoin(GlobalConstants.Lite_User_Table, GlobalConstants.Lite_User_Table + ".uuid",
                        GlobalConstants.Lite_Question_Table + ".uuid")
                .leftJoin(GlobalConstants.Lite_Question_Follow_Table, GlobalConstants.Lite_Question_Follow_Table + ".question_id",
                        GlobalConstants.Lite_Question_Table + ".question_id")
                .leftJoin(GlobalConstants.Lite_Answer_Table, GlobalConstants.Lite_Answer_Table + ".question_id",
                        GlobalConstants.Lite_Question_Table + ".question_id")
                .innerJoin(GlobalConstants.Lite_Question_Label_Table, GlobalConstants.Lite_Question_Label_Table + ".label",
                        GlobalConstants.Lite_Question_Table + ".label")
                .innerJoin(GlobalConstants.Lite_Question_Target_Table, GlobalConstants.Lite_Question_Target_Table + ".target",
                        GlobalConstants.Lite_Question_Table + ".target")
                .eq(GlobalConstants.Lite_Question_Table + ".uuid", "'" + uuid + "'")
                .gruopBy(GlobalConstants.Lite_Question_Table + ".question_id");

        Page<Record> pages = Db.paginate(pageNumber, pageSize, "select *", " from ("
                + followAnswerSqlHelper.buildSql() + " UNION "
                + followLikeSqlHelper.buildSql() + " UNION "
                + followSqlHelper.buildSql() + " UNION "
                + followPublishSqlHelper.buildSql() + " UNION "
                + questionSqlHelper.buildSql() + " UNION "
                + myselfHelper.buildSql() + " UNION "
                + myselfPublishSqlHelper.buildSql()
                + ") as temp ORDER BY time DESC");
        return pages.getList();
    }
}
