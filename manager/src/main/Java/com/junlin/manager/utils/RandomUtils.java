package com.junlin.manager.utils;

import org.apache.commons.lang.RandomStringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by junlinhui eight on 2017/4/13.
 * 随机数工具类
 */
public class RandomUtils {
    /**
     * 获取word
     *
     * @return
     */
    private String generateRandomWord(int length) {
        String[] beforeShuffle = new String[]{"1", "2", "3", "4", "5", "6", "7",
                "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                "W", "X", "Y", "Z"};
        List<String> list = Arrays.asList(beforeShuffle);
        //洗牌
        Collections.shuffle(list);
        StringBuilder sb = new StringBuilder();
        for (String value : list) {
            sb.append(value);
        }
        String afterShuffle = sb.toString();
        return afterShuffle.substring(10, 10 + length);
    }


    /**
     * 获取随机数
     *
     * @param count 长度
     * @return
     */
    public static String getRandomWord(int count) {
        char[] baseString = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
                'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z'};
        return RandomStringUtils.random(count, baseString);
    }

    /**
     * 获取随机数
     *
     * @param count    长度
     * @param suffix 后缀
     * @return
     */
    public static String getRandomWord(int count, String suffix) {
        char[] baseString = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
                'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z'};
        return RandomStringUtils.random(count, baseString) + suffix;
    }
}
