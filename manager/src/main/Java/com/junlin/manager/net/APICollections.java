package com.junlin.manager.net;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by junlinhui eight on 2017/2/23.
 */
public interface APICollections {

    /**
     * 获取用户代理ip地址
     *
     * @param account
     * @param proxyId
     * @return
     */
    @GET("ipagent//privateProxy/getDynamicIP/{account}/{proxyId}?returnType=2")
    Call<JsonObject> getProxyInfo(@Path("account") String account,
                                  @Path("proxyId") String proxyId);


    /**
     * 通过code 换取 session_key
     * @param AppID
     * @param secret
     * @param JsCode
     * @param grant_type
     * @return
     */
    @GET("sns/jscode2session")
    Call<JsonObject> getWeChatSession(@Query("appid") String AppID,
                                      @Query("secret") String secret,
                                      @Query("js_code") String JsCode,
                                      @Query("grant_type") String grant_type);


}
