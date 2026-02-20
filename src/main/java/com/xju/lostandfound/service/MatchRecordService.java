package com.xju.lostandfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.entity.MatchRecord;
import com.xju.lostandfound.model.vo.MatchRecordVo;

import java.util.List;

public interface MatchRecordService extends IService<MatchRecord> {

    // 发布失物后触发扫描
    void matchAfterLostPublished(LostItem lostItem);

    // 发布招领后触发扫描
    void matchAfterFoundPublished(FoundItem foundItem);

    // 查询用户的智能匹配记录
    List<MatchRecordVo> getMatchListByUserId(Long userId);

    //确认认领
    boolean confirmMatch(Long recordId, Long userId);
}