package com.junlin.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.upload.UploadFile;
import com.junlin.manager.utils.RandomUtils;
import com.junlin.manager.utils.ZipUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.request.CreateFolderRequest;
import com.qcloud.cos.request.UploadFileRequest;
import com.qcloud.cos.sign.Credentials;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by junlinhui eight on 2017/4/12.
 * 文件处理
 */
public class FileController extends Controller {
    //logger
    Logger logger = Logger.getLogger(FileController.class.getName());
    // 50M
    private final int MAXSize = 50 * 1024 * 1024;
    //允许的图片类型
    private final String[] imgTypes = {".jpg", ".gif", ".bmp", ".png", ".jpeg", ".ico"};

    /**
     * 上传文件
     */
 /*   public void upload() {
        logger.info("收到文件上传");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            String path = new SimpleDateFormat("yyyyMMdd").format(new Date());
            UploadFile file = getFile("fileData", "temp", MAXSize);
            File source = file.getFile();
            String fileName = file.getFileName();
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String prefix;
            //判断是不是图片
            if (imgTypes[0].equals(extension)
                    || imgTypes[1].equals(extension)
                    || imgTypes[2].equals(extension)
                    || imgTypes[3].equals(extension)
                    || imgTypes[4].equals(extension)
                    || imgTypes[5].equals(extension)) {
                prefix = "image";
                fileName = RandomUtils.getRandomWord(20, extension);
            } else {
                prefix = "file";
            }

            FileInputStream fis = new FileInputStream(source);


            File targetDir = new File(PathKit.getWebRootPath() + "/" + prefix + "/user/" + path);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            File target = new File(targetDir, fileName);
            if (!target.exists()) {
                target.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(target);
            byte[] bts = new byte[300];
            while (fis.read(bts, 0, 300) != -1) {
                fos.write(bts, 0, 300);
            }
            fos.close();
            fis.close();
            resultMap.put("error", 0);
            resultMap.put("picUrl", "/" + prefix + "/user/" + path + "/" + fileName);
            source.delete();

        } catch (FileNotFoundException e) {
            resultMap.put("error", -1);
            resultMap.put("message", "上传出现错误，请稍后再上传");
        } catch (IOException e) {
            resultMap.put("error", -1);
            resultMap.put("message", "文件写入服务器出现错误，请稍后再上传");
        }
        renderJson(new Gson().toJson(resultMap));
    }
*/

    /**
     * 上传文件
     */
    public void upload() {
        logger.info("收到文件上传");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            String path = new SimpleDateFormat("yyyyMMdd").format(new Date());
            UploadFile file = getFile("fileData", "temp", MAXSize);
            File source = file.getFile();
            String fileName = file.getFileName();
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String prefix;
            //判断是不是图片
            if (imgTypes[0].equals(extension)
                    || imgTypes[1].equals(extension)
                    || imgTypes[2].equals(extension)
                    || imgTypes[3].equals(extension)
                    || imgTypes[4].equals(extension)
                    || imgTypes[5].equals(extension)) {
                prefix = "image";
                fileName = RandomUtils.getRandomWord(20, extension);
            } else {
                prefix = "file";
            }

            String picPath = uploadToQcloud("/" + prefix + "/user/" + path + "/", source.getAbsolutePath(), fileName);
            resultMap.put("error", 0);
            resultMap.put("picUrl", picPath);
            source.delete();
        } catch (Exception e) {
            resultMap.put("error", -1);
            resultMap.put("message", "上传出现错误，请稍后再上传");
        }

        renderJson(new Gson().toJson(resultMap));
    }

    /***
     * 下载文件
     */
    public void download() {
        String path = getPara(0);
        String img = PathKit.getWebRootPath() + "/img/u/" + path.replaceAll("_", "/");
        ZipUtils.zip(img + PathKit.getWebRootPath() + "/img/temp/" + path);
        renderFile("/img/temp/" + path + ".zip");
    }


    /**
     * 初始化腾讯云存储
     *
     * @param cosPath
     * @param filePath
     * @param fileName
     */
    private String uploadToQcloud(String cosPath, String filePath, String fileName) {
        long appId = 1253453910;
        String secretId = "AKIDFMH0Xtn3qRr690pARWRFRXTDnALLIlAO";
        String secretKey = "A7uVxDGAsUyGPOBlDTXP5ZrwphQxISSF";
        // 设置要操作的bucket
        String bucketName = "junlinbucket";
        // 初始化秘钥信息
        Credentials cred = new Credentials(appId, secretId, secretKey);
        // 初始化客户端配置
        ClientConfig clientConfig = new ClientConfig();
        // 设置bucket所在的区域，比如华南园区：gz； 华北园区：tj；华东园区：sh ；
        clientConfig.setRegion("gz");
        // 初始化cosClient
        COSClient cosClient = new COSClient(clientConfig, cred);
        //创建文件目录
        CreateFolderRequest createFolderRequest = new CreateFolderRequest(bucketName, cosPath);
        String createFolderRet = cosClient.createFolder(createFolderRequest);

        logger.info("创建路径结果：" + createFolderRet);

        String path = cosPath + fileName;
        UploadFileRequest uploadFileRequest = new UploadFileRequest(bucketName, path, filePath);
        String uploadFileRet = cosClient.uploadFile(uploadFileRequest);
        cosClient.shutdown();
        logger.info("返回结果：" + uploadFileRet);

        JSONObject jsonObject = JSONObject.parseObject(uploadFileRet);
        if (jsonObject == null) {
            return null;
        }
        if (jsonObject.getInteger("code") == 0) {
            JSONObject object = jsonObject.getJSONObject("data");
            if (object == null) {
                return null;
            }
            String url= object.getString("source_url");
            return url.substring(url.indexOf("/image"));
        }
        return "";
    }
}
