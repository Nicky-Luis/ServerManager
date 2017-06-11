package com.junlin.manager.reptile.sogou.service;

import com.junlin.manager.net.APIInteractive;
import com.junlin.manager.net.INetworkResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Created by junlinhui eight on 2017/3/27.
 * 代理相关的处理
 */
public class ProxyService {

    //logger
    private static Logger logger = Logger.getLogger(ProxyService.class.getName());
    //Proxy 账户
    private static final String Proxy_Account = "DD20173277912nlxvgo";
    //Proxy id
    private static final String Proxy_Id = "5707cecab79811e6802371d9ec16a600";
    //是否正在拨号获取ip
    private static volatile boolean mIsDialeNow = false;

    //是否正在拨号获取ip
    public static boolean ismIsDialeNow() {
        return mIsDialeNow;
    }


    /**
     * 拨号获取ip地址
     *
     * @param callBack
     */
    public static void getProxyInfo(final IGetProxyCallBack callBack) {
        if (null == callBack) {
            logger.info("-------没有设置回调-------");
            return;
        } else if (mIsDialeNow) {
            logger.info("-------拨号正在进行，请等待上次拨号完成-------");
            return;
        }

        mIsDialeNow = true;
        logger.info("\n\n-------开始拨号获取ip地址-----\n");
        APIInteractive.getProxyInfo(Proxy_Account, Proxy_Id, new INetworkResponse() {
            @Override
            public void onFailure(int code) {
                callBack.getProxyResult(false, "");
                mIsDialeNow = false;
                logger.error("\n\n获取ip地址失败：" + code + "\n\n");
            }

            @Override
            public void onSucceed(JSONObject result) {
                logger.info("\n\n返回结果：" + result + "\n\n");
                //解析获取ip与端口
                if (result.optString("ERRORCODE").equals("0")) {
                    JSONObject jsonObject = (JSONObject) result.opt("RESULT");
                    String hostName = jsonObject.optString("wanIp");
                    String port = jsonObject.optString("proxyport");
                    String proxy = "http://" + hostName + ":" + port;
                    callBack.getProxyResult(true, proxy);
                }
                //等到创建成功之后再重置
                mIsDialeNow = false;
            }
        });
    }


    ////////////////////////////////回调///////////////////////////////////////
    public interface IGetProxyCallBack {
        void getProxyResult(boolean isSucceed, String proxy);
    }
}
