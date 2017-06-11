package com.junlin.manager.net;

import com.google.gson.JsonObject;

import retrofit2.Call;

/**
 * Created by junlinhui eight on 2017/2/23.
 * 接口集合
 */
public class APIInteractive {
    //请求对象
    private static APICollections request = null;
    //base链接
    private static String BaseUrl = "";

    /**
     * 初始化Retrofit
     *
     * @param url
     */
    private static void initRetrofit(String url) {
        if (null == request || !url.equals(BaseUrl)) {
            request = NetworkRequest.getRetrofitClient(url, APICollections.class);
            BaseUrl = url;
        }
    }

    /**
     * 获取代理ip信息
     *
     * @param callback
     */
    public static void getProxyInfo(String account, String proxyId, final INetworkResponse callback) {
        initRetrofit(NetworkRequest.XunDaiLiURL);
        Call<JsonObject> call = request.getProxyInfo(account, proxyId);
        NetworkRequest.netRequest(call, callback);
    }

    /**
     * 通过code 换取 session_key
     *
     * @param appID
     * @param secret
     * @param JsCode
     * @param callback
     */
    public static void getWeChatSession(String appID, String secret, String JsCode, final INetworkResponse callback) {
        initRetrofit(NetworkRequest.WeChatUrl);
        Call<JsonObject> call = request.getWeChatSession(appID, secret, JsCode, "authorization_code");
        NetworkRequest.netRequest(call, callback);
    }

}
