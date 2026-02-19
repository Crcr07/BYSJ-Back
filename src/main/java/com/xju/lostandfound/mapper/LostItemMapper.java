package com.xju.lostandfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xju.lostandfound.entity.LostItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 失物信息 Mapper 接口
 * 关键点：必须继承 BaseMapper<LostItem>
 */
@Mapper
public interface LostItemMapper extends BaseMapper<LostItem> {
}