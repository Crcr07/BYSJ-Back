package com.xju.lostandfound.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 读取你在 application.yml 里配置的绝对路径
    @Value("${campus.file-upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径最后以分隔符结尾
        String path = new File(uploadPath).getAbsolutePath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }

        // 将网络请求 /images/** 映射到本地磁盘的绝对路径
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + path);
    }
}