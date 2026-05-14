package com.xju.lostandfound.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PublishItemDto {
    private String itemName;      // 物品名称
    private Integer categoryId;   // 类别ID
    private String location;      // 丢失/捡拾地点
    private String storageLocation; // 🌟 新增：暂存位置（招领物品特有）
    private String date;          // 时间字符串
    private String description;   // 描述
    private MultipartFile file;   // 图片文件
}