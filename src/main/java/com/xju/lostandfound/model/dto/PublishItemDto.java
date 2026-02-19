package com.xju.lostandfound.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Data
public class PublishItemDto {
    private String itemName;    // 物品名称
    private Integer categoryId; // 分类ID
    private String location;    // 丢失/拾取地点
    private String date;        // 丢失/拾取时间 (前端传字符串 "2023-01-01")
    private String description; // 描述
    private MultipartFile file; // 🌟 图片文件对象
}