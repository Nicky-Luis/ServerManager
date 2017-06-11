package com.junlin.manager.reptile.sogou;

import com.junlin.manager.reptile.sogou.helper.SpiderHelper;
import com.junlin.manager.reptile.sogou.reptile.SoGouWxProcessor;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;
import java.util.Map;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HttpConstant;

/**
 * Created by junlinhui eight on 2017/3/20.
 * 测试
 */
public class SpiderTest {
    //代理
    private final static String proxy = "http://115.221.114.152:31671";
    //main
    private final static String mainURL = "http://weixin.sogou.com/";


    public static void main(String[] args) {
        SpiderHelper spiderHelper = new SpiderHelper(mainURL, proxy);
        //获取comment
        PageProcessor spider = new SoGouWxProcessor(proxy, spiderHelper.getCookie());

        NameValuePair[] values = new NameValuePair[2];
        values[0] = new BasicNameValuePair("client_url", spiderHelper.getMainURL());
        values[1] = new BasicNameValuePair("session_token", spiderHelper.getSessionToken());

        Map nameValuePair = new HashMap();
        nameValuePair.put("nameValuePair", values);
        Request request = new Request("http://weixin.sogou" +
                ".com/weixin?type=2&query=%E4%BA%86+%E5%A4%9A%E7%9B%88%E8%B4%A2%E7%BB%8F&ie=utf8&s_from=input&_sug_=n&_sug_type_=1&w=01015002&oq" +
                "=&ri=63&sourceid=sugg&sut=0&sst0=1489982761366&lkt=0%2C0%2C0&p=40040108");
        request.setExtras(nameValuePair);
        request.setMethod(HttpConstant.Method.POST);

        Spider gSoGouSpider = Spider.create(spider);
        gSoGouSpider.thread(5).addRequest(request).run();
    }
}
