package com.yupi.utils;


import cn.hutool.core.io.resource.ClassPathResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片处理工具类
 */

@Slf4j
public  class ImageUtils {

    //图片存储目录(相对于resource)
    private static final String IMAGE_DIR="images";

    //项目根目录下的图片目录
    private static final String PROJECT_IMAGE_DIR=System.getProperty("user.dir")+"/images";

    private ImageUtils(){}

    /**
     * 加载图片并转换为Base64
     * @param imagePath
     * @return
     */
    public static String loadImageAsBase64FromResources(String imagePath){
        ClassPathResource resource = new ClassPathResource(IMAGE_DIR + "/" + imagePath);
        InputStream inputStream = resource.getStream();
        try {
            byte[] imageBytes = inputStream.readAllBytes();
            String base64=Base64.getEncoder().encodeToString(imageBytes);
            log.info("成功加载图片：{},大小：{}bytes",imagePath,imageBytes.length);
            return base64;
        } catch (IOException e) {
            log.error("加载图片失败：{}",imagePath,e);
            throw new RuntimeException("加载图片失败："+imagePath,e);
        }
    }

    /**
     * 加载项目下的图片并转换为Base64
     * @param imagePath
     * @return
     */
    public static String loadImageAsBase64FromProject(String imagePath){
        Path path = Paths.get(PROJECT_IMAGE_DIR, imagePath);
        try {
            byte[] imageBytes = Files.readAllBytes(path);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            log.info("成功加载图片：{},大小：{}bytes", imagePath, imageBytes.length);
            return base64;
        } catch (IOException e) {
            log.error("加载图片失败：{}", imagePath, e);
            throw new RuntimeException("加载图片失败：" + imagePath, e);
        }
    }

    public static String saveImage(byte[] imageData,String fileName){
        //创建目录
        File dir = new File(PROJECT_IMAGE_DIR);
        if(!dir.exists()){
            dir.mkdirs();
        }

        //保存文件
        Path path=Paths.get(PROJECT_IMAGE_DIR,fileName);
        try {
            Files.write(path,imageData);
            log.info("成功保存图片：{}",path);
            return fileName;
        } catch (IOException e) {
            log.error("保存图片失败：{}",path,e);
            throw new RuntimeException("保存图片失败："+fileName,e);
        }
    }


    /**
     * 获取图片的 MIME 类型
     */
    public static String getImageMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}
