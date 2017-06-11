package com.junlin.manager.net;


import org.json.JSONObject;

/**
 * Created by junlinhui eight on 2017/2/23.
 */
public interface INetworkResponse {
    //操作失败
    int ERR_RESULT_FAILURE = -1;
    //操作成功
    int ECODE_RESULT_SUCCEED = 1;
    //数据解析失败
    int ERR_ANALYSIS_DATA = 2;

    void onFailure(int code);

    void onSucceed(JSONObject result);
}
