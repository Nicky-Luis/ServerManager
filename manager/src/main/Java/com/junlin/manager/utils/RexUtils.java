package com.junlin.manager.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式
 * 正则表达式 的用法主要是4种方面的使用
 * 匹配，分割，替换，获取.
 * 用一些简单的符号来代表代码的操作
 *
 * @author cyc
 */
public class RexUtils {
    /**
     * 获取查询的字符串
     * 将匹配的字符串取出
     */
    public static void getString(String str, String regx) {
        //1.将正在表达式封装成对象Patten 类来实现
        Pattern pattern = Pattern.compile(regx);
        //2.将字符串和正则表达式相关联
        Matcher matcher = pattern.matcher(str);
        //3.String 对象中的matches 方法就是通过这个Matcher和pattern来实现的。

        System.out.println(matcher.matches());
        //查找符合规则的子串
        while (matcher.find()) {
            //获取 字符串
            System.out.println(matcher.group());
            //获取的字符串的首位置和末位置
            System.out.println(matcher.start() + "--" + matcher.end());
        }
    }

    /**
     * 字符串的分割
     */
    public static void getDivision(String str, String regx) {
        String[] dataStr = str.split(regx);
        for (String s : dataStr) {
            System.out.println("正则表达式分割++" + s);
        }
    }

    /**
     * 字符串的替换
     */
    public static String getReplace(String str, String regx, String replaceStr) {
        String stri = str.replaceAll(regx, replaceStr);
        System.out.println("正则表达式替换" + stri);
        return stri;
    }

    /**
     * 字符串处理之匹配
     * String类中的match 方法
     */
    public static void getMatch(String str, String regx) {
        System.out.println("正则表达匹配" + str.matches(regx));
    }
}
