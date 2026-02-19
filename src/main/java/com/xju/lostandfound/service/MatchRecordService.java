package com.xju.lostandfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.entity.MatchRecord;

public interface MatchRecordService extends IService<MatchRecord> {
    // 发布失物后触发扫描
    void matchAfterLostPublished(LostItem lostItem);
    // 发布招领后触发扫描
    void matchAfterFoundPublished(FoundItem foundItem);
}