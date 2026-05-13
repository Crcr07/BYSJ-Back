package com.xju.lostandfound.service;

import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.entity.MatchRecord; // 新增导入

/**
 * 智能匹配算法服务接口
 */
public interface MatchAlgorithmService {

    /**
     * 计算失物和招领物品之间的综合匹配度，调用大模型工作流返回带有详细结果的匹配记录对象
     */
    MatchRecord calculateMatchScore(LostItem lostItem, FoundItem foundItem);
}