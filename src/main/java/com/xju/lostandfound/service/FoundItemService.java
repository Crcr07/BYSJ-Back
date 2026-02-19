package com.xju.lostandfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.model.dto.PublishItemDto;

public interface FoundItemService extends IService<FoundItem> {
    boolean publish(PublishItemDto dto, Long userId);
}