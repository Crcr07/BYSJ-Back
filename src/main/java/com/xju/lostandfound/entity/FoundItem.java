package com.xju.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("lf_found_item")
public class FoundItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;          // 拾取者ID
    private String itemName;      // 物品名称
    private Integer categoryId;   // 分类ID
    private String foundLocation; // 拾取地点 (注意字段名和失物表不同)
    private LocalDateTime foundTime; // 拾取时间
    private String description;
    private String imageUrl;
    private String storageLocation; // 物品存放处
    private String ocrText;       // OCR识别文本
    private String imageFeature;  // 图像特征
    private Integer status;       // 0:待认领 1:已认领 2:已撤销
    private LocalDateTime createTime;
}