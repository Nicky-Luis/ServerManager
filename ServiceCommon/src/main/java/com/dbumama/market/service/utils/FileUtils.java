package com.dbumama.market.service.utils;

import com.google.common.collect.Lists;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public final class FileUtils {

    private static final Log LOG = Log.getLog(FileUtils.class);

	public static byte[] toByteArray(File f) throws IOException {
        if (!f.exists()) {  
            throw new FileNotFoundException();  
        }  
  
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());  
        BufferedInputStream in = null;  
        try {  
            in = new BufferedInputStream(new FileInputStream(f));  
            int buf_size = 1024;  
            byte[] buffer = new byte[buf_size];  
            int len = 0;  
            while (-1 != (len = in.read(buffer, 0, buf_size))) {  
                bos.write(buffer, 0, len);  
            }  
            return bos.toByteArray();  
        } catch (IOException e) {  
            e.printStackTrace();  
            throw e;  
        } finally {  
            try {  
                in.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
            bos.close();  
        }  
    }


    /**
     * 算法简述： 从某个给定的需查找的文件夹出发，搜索该文件夹的所有子文件夹及文件， 若为文件，则进行匹配，匹配成功则加入结果集，若为子文件夹，则进队列。 队列不空，重复上述操作，队列为空，程序结束，返回结果。
     *
     * @param baseDirName    查找的文件夹路径
     * @param targetFileName 需要查找的文件名
     */
    public static List<String> findFiles(String baseDirName, String targetFileName) {

        List<String> classFiles = Lists.newArrayList();
        File baseDir = new File(baseDirName);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            LOG.error("search error：" + baseDirName + "is not a dir！");
        } else {
            String[] files = baseDir.list();
            for (int i = 0; i < files.length; i++) {
                File file = new File(baseDirName + File.separator + files[i]);
                if (file.isDirectory()) {
                    classFiles.addAll(findFiles(baseDirName + File.separator + files[i], targetFileName));
                } else {
                    if (wildcardMatch(targetFileName, file.getName())) {
                        String fileName = file.getAbsolutePath();
                        String open = PathKit.getRootClassPath() + File.separator;
                        String close = ".class";
                        int start = fileName.indexOf(open);
                        int end = fileName.indexOf(close, start + open.length());
                        String className = fileName.substring(start + open.length(), end).replace(File.separator, ".");
                        classFiles.add(className);
                    }
                }
            }
        }
        return classFiles;
    }

    /**
     * 通配符匹配
     *
     * @param pattern  通配符模式
     * @param fileName 待匹配的字符串
     */
    public static boolean wildcardMatch(String pattern, String fileName) {
        int patternLength = pattern.length();
        int strLength = fileName.length();
        int strIndex = 0;
        char ch;
        for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
            ch = pattern.charAt(patternIndex);
            if (ch == '*') {
                // 通配符星号*表示可以匹配任意多个字符
                while (strIndex < strLength) {
                    if (wildcardMatch(pattern.substring(patternIndex + 1), fileName.substring(strIndex))) {
                        return true;
                    }
                    strIndex++;
                }
            } else if (ch == '?') {
                // 通配符问号?表示匹配任意一个字符
                strIndex++;
                if (strIndex > strLength) {
                    // 表示str中已经没有字符匹配?了。
                    return false;
                }
            } else {
                if ((strIndex >= strLength) || (ch != fileName.charAt(strIndex))) {
                    return false;
                }
                strIndex++;
            }
        }
        return strIndex == strLength;
    }

}
