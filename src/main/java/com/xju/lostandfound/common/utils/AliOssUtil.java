package com.xju.lostandfound.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Component
public class AliOssUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    /**
     * 文件上传至阿里云 OSS
     * @param file 前端传来的文件对象
     * @return 返回上传成功后的完整图片公网 URL
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        try {
            // 1. 获取文件输入流
            InputStream inputStream = file.getInputStream();

            // 2. 构建不重复的文件名 (避免重名覆盖)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;

            // 3. 创建 OSSClient 实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 4. 上传文件
            ossClient.putObject(bucketName, fileName, inputStream);

            // 5. 关闭 OSSClient
            ossClient.shutdown();

            // 6. 拼接并返回可以直接访问的图片 URL
            return "https://" + bucketName + "." + endpoint + "/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传至阿里云OSS失败: " + e.getMessage());
        }
    }
}