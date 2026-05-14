package com.xju.lostandfound.common.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUtils {

    /**
     * 将文件保存到服务器本地硬盘
     * @param file 前端上传的文件
     * @param path 本地存储路径 (来自 application.yml 中的 campus.file-upload-path)
     * @return 返回保存后的文件名
     */
    public static String upload(MultipartFile file, String path) {
        if (file.isEmpty()) {
            return null;
        }

        // 1. 确保目录存在
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 2. 生成不重复的文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;

        // 3. 保存文件到本地
        File dest = new File(path + fileName);
        try {
            file.transferTo(dest);
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("本地文件上传失败: " + e.getMessage());
        }
    }
}