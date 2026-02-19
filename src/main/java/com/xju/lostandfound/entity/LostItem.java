package com.xju.lostandfound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 失物信息实体类
 * 对应数据库表: lf_lost_item
 */
@Data // 🌟 核心注解：自动生成 setUserId, getUserId, toString 等方法
@TableName("lf_lost_item") // 指定数据库表名
public class LostItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID (自增)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发布者用户ID
     * 对应数据库字段: user_id
     */
    private Long userId;

    /**
     * 物品名称
     * 对应数据库字段: item_name
     */
    private String itemName;

    /**
     * 物品分类ID
     * 对应数据库字段: category_id
     */
    private Integer categoryId;

    /**
     * 丢失地点
     * 对应数据库字段: lost_location
     */
    private String lostLocation;

    /**
     * 丢失时间
     * 对应数据库字段: lost_time
     */
    private LocalDateTime lostTime;

    /**
     * 详细描述
     * 对应数据库字段: description
     */
    private String description;

    /**
     * 图片路径 (存储文件名, 例如: uuid.jpg)
     * 对应数据库字段: image_url
     */
    private String imageUrl;

    /**
     * OCR识别出的文字 (用于关键词模糊匹配)
     * 毕设核心字段
     * 对应数据库字段: ocr_text
     */
    private String ocrText;

    /**
     * 图像特征值 (用于相似度计算)
     * 毕设核心字段
     * 对应数据库字段: image_feature
     */
    private String imageFeature;

    /**
     * 状态 0:寻找中 1:已找回 2:已撤销
     * 对应数据库字段: status
     */
    private Integer status;

    /**
     * 发布时间
     * 对应数据库字段: create_time
     */
    private LocalDateTime createTime;
}