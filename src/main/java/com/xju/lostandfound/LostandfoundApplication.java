package com.xju.lostandfound;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// 1. 扫描 Mapper 接口所在的包 (非常重要！)
@MapperScan("com.xju.lostandfound.mapper")
// 2. 开启定时任务 (为了后续做智能匹配算法的定时执行)
@EnableScheduling
public class LostandfoundApplication {

    public static void main(String[] args) {
        SpringApplication.run(LostandfoundApplication.class, args);
    }

}