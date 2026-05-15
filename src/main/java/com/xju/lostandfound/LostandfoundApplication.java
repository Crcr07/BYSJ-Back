package com.xju.lostandfound;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync // 🌟 核心修改：开启Spring异步多线程支持
@SpringBootApplication
// 1. 扫描 Mapper 接口所在的包
@MapperScan("com.xju.lostandfound.mapper")
// 2. 开启定时任务
@EnableScheduling
public class LostandfoundApplication {

    public static void main(String[] args) {
        SpringApplication.run(LostandfoundApplication.class, args);
    }

}