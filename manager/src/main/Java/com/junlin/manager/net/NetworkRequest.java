package com.junlin.manager.net;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * Created by nicky on 2016/12/29.
 * 网络请求
 */

public class NetworkRequest {

    //log对象
    private static Logger logger = LogManager.getLogger(NetworkRequest.class.getName());
    //超时40秒
    private static final int DEFAULT_TIMEOUT = 40;
    //讯代理
    public static final String XunDaiLiURL = "http://www.xdaili.cn/";
    //微信服务器
    public static final String WeChatUrl = "https://api.weixin.qq.com/";

    //构造方法私有
    private NetworkRequest() {
    }

    /**
     * 创建 RetrofitManage 服务
     */
    public static <T> T getRetrofitClient(String baseURL, final Class<T> clss) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())//使用自己创建的OkHttp
                .addConverterFactory(ScalarsConverterFactory.create()) //增加返回值为String的支持
                .addConverterFactory(GsonConverterFactory.create())//增加返回值为Gson的支持(以实体类返回)
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //增加返回值为Oservable<T>的支持
                .build();

        return retrofit.create(clss);
    }


    public static void test(Call<JsonObject> call, final INetworkResponse callback) {
        //请求
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    logger.debug(" response.isSuccessful()：" + response.isSuccessful());
                    logger.debug(" response.body()：" + response.body());
                    logger.debug(" response.message()：" + response.message());
                    logger.debug(" response.errorBody()：" + response.errorBody());
                    logger.debug(" response.raw()：" + response.raw().code());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                //网络问题会走该回调
                error(t);
                logger.error("Net Request Failure," + t.getMessage());
            }
        });
    }

    /**
     * 通用的网络请求模块
     *
     * @param call     请求call
     * @param callback 回调
     */
    public static void netRequest2(Call<JsonArray> call, final INetworkResponse callback) {
        //请求
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.body() == null) {
                    logger.error("data is null");
                    callback.onFailure(INetworkResponse.ERR_ANALYSIS_DATA);
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().getAsJsonArray().toString());
                        JSONObject object = new JSONObject();
                        object.put("results", jsonArray);
                        callback.onSucceed(object);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onFailure(INetworkResponse.ERR_ANALYSIS_DATA);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                //网络问题会走该回调
                error(t);
                logger.error("Net Request Failure," + t.getMessage());
                callback.onFailure(INetworkResponse.ERR_RESULT_FAILURE);
            }
        });
    }

    /**
     * 通用的网络请求模块
     *
     * @param call     请求call
     * @param callback 回调
     */
    public static void netRequest(Call<JsonObject> call, final INetworkResponse callback) {
        //请求
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.body() == null) {
                    logger.error("data is null");
                    callback.onFailure(INetworkResponse.ERR_ANALYSIS_DATA);
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        callback.onSucceed(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onFailure(INetworkResponse.ERR_ANALYSIS_DATA);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                error(t);
                logger.error("Net Request Failure," + t.getMessage());
                callback.onFailure(INetworkResponse.ERR_RESULT_FAILURE);
            }
        });
    }

    private static void error(Throwable t) {
        //网络问题会走该回调
        if (t instanceof SocketTimeoutException) {
            logger.error("请求超时");
        } else if (t instanceof ConnectException) {
            logger.error("连接错误");
        } else if (t instanceof RuntimeException) {
            logger.error("错误");
        }
    }

    /**
     * 创建bean对象
     *
     * @param bean
     * @return
     */
    public static RequestBody createJsonString(Object bean) {
        Gson gson = new Gson();
        //通过Gson将Bean转化为Json字符串形式
        String route = gson.toJson(bean);
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), route);
    }
    ////////////////////////////////////private method/////////////////////////////////////

    /**
     * 获取okhttp对象,在这里进行相关的配置
     *
     * @return OkHttpClient
     */
    private static OkHttpClient getOkHttpClient() {
        //日志显示级别
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
        //新建log拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                logger.debug("Message:" + message);
            }
        });

        //新建header
        Interceptor headInterceptor = new Interceptor() {

            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .addHeader("Accept-Encoding", "gzip, deflate, sdch, br")
                        .addHeader("Accept-Language", "zh-CN,zh;q=0.8")
                        .addHeader("Cache-Control", "max-age=0")
                        .addHeader("Connection", "keep-alive")
                        .addHeader("Upgrade-Insecure-Requests", "1")
                        .build();
                //设置Content-Type
//                request.newBuilder().post(new RequestBody() {
//
//                    @Override
//                    public MediaType contentType() {
//                        return MediaType.parse("image/jpeg");
//                    }
//
//                    @Override
//                    public void writeTo(BufferedSink sink) throws IOException {
//                    }
//                });
                return chain.proceed(request);
            }
        };

        loggingInterceptor.setLevel(level);
        //定制OkHttp
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        //OkHttp进行添加拦截器loggingInterceptor
        httpClientBuilder.addInterceptor(loggingInterceptor);
        //添加header
        httpClientBuilder.addInterceptor(headInterceptor);

        return httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

}
