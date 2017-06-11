package com.junlin.manager.app.service;

import com.google.gson.Gson;
import com.jfinal.plugin.activerecord.Record;
import com.junlin.manager.GlobalConstants;
import com.junlin.manager.app.LiteConstants;
import com.junlin.manager.app.entity.Answer;
import com.junlin.manager.app.entity.LiteUser;
import com.junlin.manager.app.entity.Question;
import com.junlin.manager.app.entity.QuestionFollow;
import com.junlin.manager.app.entity.UserRelation;
import com.junlin.manager.app.utils.VerifyUtils;
import com.junlin.manager.net.APIInteractive;
import com.junlin.manager.net.INetworkResponse;
import com.junlin.manager.utils.SqlHelper;
import com.mchange.v2.lang.StringUtils;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by junlinhui eight on 2017/4/7.
 * 逻辑层
 */
public class PersonService {

    //logger
    private static Logger logger = Logger.getLogger(PersonService.class.getName());

    /**
     * 获取小程序Session
     *
     * @param JsCode
     */
    public static void getSessionInfo(final String user, String JsCode, final ILiteCallback callback) {

        //网络请求获取session_key
        APIInteractive.getWeChatSession(LiteConstants.AppID, LiteConstants.AppSecret, JsCode, new INetworkResponse() {
            @Override
            public void onFailure(int code) {
                logger.error("登录失败");
                if (null == callback) {
                    return;
                }
                callback.onGetResult(ILiteCallback.Err_Code, null);
            }

            @Override
            public void onSucceed(JSONObject result) {
                logger.info("登录结果" + result.toString());
                if (null == callback) {
                    return;
                }

                if (result.has("openid") && result.has("session_key")) {
                    String openid = result.optString("openid");
                    String session_key = result.optString("session_key");
                    saveUserInfo(user, openid);
                    callback.onGetResult(ILiteCallback.Succeed_Code, session_key + "," + openid);
                } else {
                    callback.onGetResult(ILiteCallback.Err_Code, "");
                }
            }
        });
    }


    /***
     * 获取登录成功后的json string
     * @param code
     * @param _3rdKey
     * @return
     */
    public static String getLoginJson(int code, String _3rdKey) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", code);
        resultMap.put("msg", "失败");
        resultMap.put("session", "");
        if (code == ILiteCallback.Succeed_Code) {
            resultMap.put("msg", "成功");
            resultMap.put("session", _3rdKey);
        }
        return new Gson().toJson(resultMap);
    }

    /***
     * 获取登录成功后的json string
     * @param sessionKey
     * @return
     */
    public static String startSearchMessage(String sessionKey) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        try {
            //分页查询具体数据
            resultMap.put("msg", "操作成功");
            resultMap.put("ask", Question.findPublishCount(uuid));
            resultMap.put("followed", QuestionFollow.findUserFollowedCount(uuid));
            resultMap.put("answer", Answer.findUserAnswerCount(uuid));
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("code", VerifyUtils.Err_Code_Parameter);
            resultMap.put("msg", "操作失败");
        }

        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 关注用户与取消关注用户
     *
     * @param uuid
     * @param sessionKey
     * @param follow
     * @return
     */
    public static String followHandler(String uuid, String sessionKey, boolean follow) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String fans_uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        boolean result;
        //添加关注
        if (follow) {
            result = UserRelation.addFollowRelation(uuid, fans_uuid);
        } else {
            result = UserRelation.deleteFollowRelation(uuid, fans_uuid);
        }
        //结果
        if (result) {
            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
            //是否互相关注了，0:双方都未关注，1：双方互相关注，2：我关注对方，3：对方关注我
            if (StringUtils.nonEmptyString(fans_uuid)) {
                int value = 0;
                boolean followUser = UserRelation.isFollow(uuid, fans_uuid);
                boolean followMe = UserRelation.isFollow(fans_uuid, uuid);
                if (followMe && followUser) {
                    value = 1;
                } else if (followMe) {
                    value = 3;
                } else if (followUser) {
                    value = 2;
                }
                resultMap.put("followStatus", value);
            }
        }
        return new Gson().toJson(resultMap);
    }

    /***
     * 获取所有的粉丝数
     * @param sessionKey
     * @return
     */
    public static String startGetAllFans(String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");
        getUserFans(pageStr, pageCount, resultMap, uuid);
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /**
     * 获取所有的粉丝数
     *
     * @param uuid
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     * @return
     */
    public static String startGetAllFans(String uuid, String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }

        getUserFans(pageStr, pageCount, resultMap, uuid);
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }


    /***
     * 获取所有关注的人
     * @param sessionKey
     * @return
     */
    public static String startGetAllFollowed(String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");
        startGetFollowed(pageStr, pageCount, resultMap, uuid);
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }


    /**
     * 获取所有关注的人
     *
     * @param uuid
     * @param sessionKey
     * @param pageStr
     * @param pageCount
     * @return
     */
    public static String startGetAllFollowed(String uuid, String sessionKey, String pageStr, int pageCount) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        startGetFollowed(pageStr, pageCount, resultMap, uuid);
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /***
     * 获取用户的手机号码
     * @param sessionKey
     * @return
     */
    public static String startGetUserInfo(String sessionKey) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        LiteUser user = LiteUser.findUserByUUID(uuid);
        if (null != user) {
            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
            resultMap.put("phone", user.get("phone"));
            resultMap.put("signature", user.get("signature"));
        } else {
            resultMap.put("code", VerifyUtils.Err_Code_Operate);
            resultMap.put("msg", "操作失败");
        }
        String result = new Gson().toJson(resultMap);
        logger.info("结果：" + result);
        return result;
    }

    /***
     * 修改用户的签名
     * @param sessionKey
     * @return
     */
    public static String startEditSignature(String sessionKey, String signature) {
        Map<String, Object> resultMap = VerifyUtils.checkSession(sessionKey);
        if ((Integer) resultMap.get("code") != VerifyUtils.Code_Succeed) {
            return new Gson().toJson(resultMap);
        }
        String uuid = (String) resultMap.get("uuid");
        resultMap.remove("uuid");

        LiteUser user = LiteUser.findUserByUUID(uuid);
        if (StringUtils.nonEmptyString(signature)) {
            user.set("signature", signature);
            user.update();
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
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取所有关注的人
     *
     * @param pageStr
     * @param pageCount
     * @param resultMap
     * @param uuid
     */
    private static void startGetFollowed(String pageStr, int pageCount, Map<String, Object> resultMap, String uuid) {
        int page = Integer.valueOf(pageStr);
        if (page > 0) {
            String[] obtainParams = {
                    GlobalConstants.Lite_User_Table + ".uuid",
                    GlobalConstants.Lite_User_Table + ".signature",
                    GlobalConstants.Lite_User_Table + ".nickName",
                    GlobalConstants.Lite_User_Table + ".avatarUrl",
                    GlobalConstants.Lite_User_Relationship_Table + ".created_time As time",
            };

            SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_User_Table, obtainParams)
                    .leftJoin(GlobalConstants.Lite_User_Relationship_Table, GlobalConstants.Lite_User_Relationship_Table + ".uuid",
                            GlobalConstants.Lite_User_Table + ".uuid")
                    .eq(GlobalConstants.Lite_User_Relationship_Table + ".fans_uuid", "'" + uuid + "'");
            //分页查询具体数据
            List<Record> records = sqlHelper
                    .orderByDesc("time")
                    .paginate(page, pageCount);

            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
            resultMap.put("count", records.size());
            resultMap.put("result", resultUserJson(records, uuid));
        }
    }

    /**
     * 获取用户的粉丝
     *
     * @param pageStr
     * @param pageCount
     * @param resultMap
     * @param uuid
     */
    private static void getUserFans(String pageStr, int pageCount, Map<String, Object> resultMap, String uuid) {
        int page = Integer.valueOf(pageStr);
        if (page > 0) {
            String[] obtainParams = {
                    GlobalConstants.Lite_User_Table + ".uuid",
                    GlobalConstants.Lite_User_Table + ".signature",
                    GlobalConstants.Lite_User_Table + ".nickName",
                    GlobalConstants.Lite_User_Table + ".avatarUrl",
                    GlobalConstants.Lite_User_Relationship_Table + ".created_time As time",
            };

            SqlHelper sqlHelper = SqlHelper.fromTableName(GlobalConstants.Lite_User_Table, obtainParams)
                    .leftJoin(GlobalConstants.Lite_User_Relationship_Table, GlobalConstants.Lite_User_Relationship_Table + ".fans_uuid",
                            GlobalConstants.Lite_User_Table + ".uuid")
                    .eq(GlobalConstants.Lite_User_Relationship_Table + ".uuid", "'" + uuid + "'");
            //分页查询具体数据
            List<Record> records = sqlHelper
                    .orderByDesc("time")
                    .paginate(page, pageCount);

            resultMap.put("code", 1);
            resultMap.put("msg", "操作成功");
            resultMap.put("count", records.size());
            resultMap.put("result", resultUserJson(records, uuid));
        }
    }

    /**
     * 保存用户信息
     *
     * @param userInfo
     * @param openid
     */
    private static void saveUserInfo(String userInfo, String openid) {
        try {
            JSONObject userObject = new JSONObject(userInfo);
            LiteUser user = LiteUser.dao.findFirst("SELECT * FROM " + GlobalConstants.Lite_User_Table + " WHERE openid = '" + openid + "'");
            if (null == user) {
                LiteUser.newLiteUser(openid, userObject);
                logger.info("保存用户信息成功");
            } else {
                logger.info("用户信息已存在");
            }
        } catch (JSONException e) {
            logger.error("保存用户信息失败");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /***
     * 返回结果设置
     * @param records
     * @return
     */
    private static List<Map<String, Object>> resultUserJson(List<Record> records, String uuid) {
        List<Map<String, Object>> dataMaps = new ArrayList<>();
        for (Record record : records) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            String[] attrsName = record.getColumnNames();
            Object[] attrsValue = record.getColumnValues();

            //是否关注
            String fansID = record.getStr("uuid");
            if (StringUtils.nonEmptyString(fansID)) {
                //是否互相关注了
                dataMap.put("isFollowEach", UserRelation.isFollow(fansID, uuid));
            }
            //修改发布的时间的格式
            for (int index = 0; index < attrsName.length; index++) {
                dataMap.put(attrsName[index], attrsValue[index]);
            }
            dataMaps.add(dataMap);
        }
        return dataMaps;
    }

    ////////////////////////////////////操作回调/////////////////////////////

    public interface ILiteCallback {
        //失败
        int Err_Code = -1;
        //成功
        int Succeed_Code = 1;

        void onGetResult(int code, String result);
    }
}
