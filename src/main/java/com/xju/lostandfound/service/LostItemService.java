package com.xju.lostandfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.model.dto.PublishItemDto;

/**
 * 失物业务接口
 */
public interface LostItemService extends IService<LostItem> {

    // 发布失物
    boolean publish(PublishItemDto dto, Long userId);
}