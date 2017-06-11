package com.junlin.manager.utils;

import com.mchange.v2.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by junlinhui eight on 2017/5/27.
 * 解码
 */
public class DecoderUtils {
    /**
     * url解码
     * @param content
     * @return
     */
    public static String decoderContent(String content) {
        if (StringUtils.nonEmptyString(content)) {
            try {
                content = URLDecoder.decode(content, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            content = "";
        }
        return content;
    }
}
