package com.junlin.manager.admin.controller;

import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.junlin.manager.admin.service.ArticleService;
import com.junlin.manager.admin.service.UserService;
import com.junlin.manager.utils.TimeUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by junlinhui eight on 2017/3/23.
 * 文章处理
 */
public class AdminArticleController extends Controller {

    //logger
    private final Logger logger = Logger.getLogger(AdminArticleController.class.getName());

    /***
     * 搜索文章
     */
    public void search() {
        //跨域许可
        //先获取到两个POST变量
        int page = getParaToInt("page", 1);
        String startTime = getPara("startTime");
        String endTime = getPara("endTime");
        int minRead = getParaToInt("minRead", 500);
        int minWord = getParaToInt("minWord", 1000);

        long start = TimeUtils.getLocalTimeFromUTC(startTime);
        long end = TimeUtils.getLocalTimeFromUTC(endTime);
        logger.info("\n页面:" + page + "\n开始时间：" + start + "\n结束时间：" + end + "\n最低字数：" + minWord + "\n最低阅读：" + minRead);
        //开始登录
        ArticleService.startSearch(page, 20, minRead, minWord, start, end, new ArticleService.ISearchArticleCallback() {
            @Override
            public void onSearchResult(int code, Long count, List<Map<String, Object>> resultArticles) {

                String message = code == UserService.Succeed_Code ? "获取成功" : "获取失败";
                //结果
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("code", code);
                resultMap.put("msg", message);
                resultMap.put("total", count);
                resultMap.put("result", resultArticles);
                //获取json
                String jsonObject = new Gson().toJson(resultMap);
                logger.info("数据为：" + jsonObject);
                renderJson(jsonObject);
            }
        });
    }

    /***
     * 记录阅读文章
     */
    public void read() {
        //先获取到两个参数
        String uuid = getPara("uuid", "");
        String url = getPara("url", "");
        int flag = getParaToInt("flag", 0);
        logger.info("用户id:" + uuid + "\n url:" + url + "\nflag:" + flag);

        ArticleService.startSetReadFlag(uuid, url, flag, "", new ArticleService.ISetReadFlagCallback() {
            @Override
            public void onResult(int code) {
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("code", code);
                if (code == ArticleService.ISetReadFlagCallback.Succeed_Code) {
                    resultMap.put("msg", "成功");
                } else {
                    resultMap.put("msg", "失败");
                }
                //获取json
                String jsonObject = new Gson().toJson(resultMap);
                renderJson(jsonObject);
            }
        });
    }

    /***
     * 编辑文章状态
     */
    public void edit() {
        //先获取到两个参数
        String uuid = getPara("uuid", "");
        String url = getPara("url", "");
        int flag = getParaToInt("flag", 0);
        String remark = getPara("remark", "");

        ArticleService.startSetReadFlag(uuid, url, flag, remark, new ArticleService.ISetReadFlagCallback() {
            @Override
            public void onResult(int code) {
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("code", code);
                if (code == ArticleService.ISetReadFlagCallback.Succeed_Code) {
                    resultMap.put("msg", "操作成功");
                } else {
                    resultMap.put("msg", "操作失败");
                }
                //获取json
                String jsonObject = new Gson().toJson(resultMap);
                renderJson(jsonObject);
            }
        });
    }

    /***
     * 批量编辑文章状态
     */
    public void batch() {
        //先获取到两个参数
        String uuid = getPara("uuid", "");
        String urls = getPara("urls", "");
        int flag = getParaToInt("flag", 0);
        String remark = getPara("remark", "");

        logger.info("urls是:" + urls);
        ArticleService.startBatchSetReadFlag(uuid, urls.split(","), flag, remark, new ArticleService.ISetReadFlagCallback() {
            @Override
            public void onResult(int code) {
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("code", code);
                if (code == ArticleService.ISetReadFlagCallback.Succeed_Code) {
                    resultMap.put("msg", "批量操作成功");
                } else {
                    resultMap.put("msg", "操作失败");
                }
                //获取json
                String jsonObject = new Gson().toJson(resultMap);
                renderJson(jsonObject);
            }
        });
    }

    //获取关键字
    public void keyword() {
        //先获取到两个参数
        int page = getParaToInt("page", 1);
        String name = getPara("name", "");
        try {
            name =  new String(name.getBytes("iso8859_1"),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ArticleService.startSearchKeyWord(page, 50, name, new ArticleService.ISearchKeyWordCallback() {
            @Override
            public void onResult(int code, Long total, List<Map<String, Object>> words) {
                String message = code == ArticleService.ISearchKeyWordCallback.Succeed_Code ? "获取成功" : "获取失败";
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("message", message);
                resultMap.put("total", total);
                resultMap.put("result", words);
                //获取json
                String jsonObject = new Gson().toJson(resultMap);
                logger.info("数据为：" + jsonObject);
                renderJson(jsonObject);
            }
        });
    }

}
