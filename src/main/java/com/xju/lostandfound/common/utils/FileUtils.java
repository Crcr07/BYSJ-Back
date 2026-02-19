package com.xju.lostandfound.common.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUtils {

    /**
     * 上传文件
     * @param file 前端传来的文件对象
     * @param uploadPath 存储的根路径 (从 application.yml 读取)
     * @return 存储后的文件名 (例如: uuid.jpg)
     */
    public static String upload(MultipartFile file, String uploadPath) {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 1. 获取原文件名 (例如: photo.jpg)
        String originalFilename = file.getOriginalFilename();

        // 2. 获取文件后缀 (例如: .jpg)
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 3. 生成唯一文件名 (使用 UUID 防止覆盖, 例如: 550e8400-e29b....jpg)
        String fileName = UUID.randomUUID().toString() + suffix;

        // 4. 创建文件对象
        File dest = new File(uploadPath + fileName);

        // 5. 如果目录不存在，自动创建
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            // 6. 核心操作：将内存中的文件写入磁盘
            file.transferTo(dest);
            return fileName; // 返回文件名供数据库存储
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}