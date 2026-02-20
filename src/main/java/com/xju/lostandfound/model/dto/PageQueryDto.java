package com.xju.lostandfound.model.dto;

import lombok.Data;

@Data
public class PageQueryDto {
    private Integer current = 1; // 当前页码 (默认第1页)
    private Integer size = 10;   // 每页显示条数 (默认10条)
    private String keyword;      // 搜索关键词 (可选)
    private Integer categoryId;  // 分类ID (可选，用于分类筛选)
}