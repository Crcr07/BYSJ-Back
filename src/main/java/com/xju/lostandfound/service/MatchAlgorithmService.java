package com.xju.lostandfound.service;

import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;

/**
 * 智能匹配算法服务接口
 */
public interface MatchAlgorithmService {

    /**
     * 计算 失物 和 招领物品 之间的综合匹配得分
     *
     * @param lostItem  失物对象
     * @param foundItem 招领物品对象
     * @return 匹配得分 (范围: 0.0 到 1.0，越接近 1.0 越匹配)
     */
    double calculateMatchScore(LostItem lostItem, FoundItem foundItem);
}