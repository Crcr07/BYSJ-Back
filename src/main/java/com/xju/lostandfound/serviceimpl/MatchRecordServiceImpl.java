package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.entity.MatchRecord;
import com.xju.lostandfound.mapper.FoundItemMapper;
import com.xju.lostandfound.mapper.LostItemMapper;
import com.xju.lostandfound.mapper.MatchRecordMapper;
import com.xju.lostandfound.service.MatchAlgorithmService;
import com.xju.lostandfound.service.MatchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchRecordServiceImpl extends ServiceImpl<MatchRecordMapper, MatchRecord> implements MatchRecordService {

    @Autowired
    private LostItemMapper lostItemMapper;
    @Autowired
    private FoundItemMapper foundItemMapper;
    @Autowired
    private MatchAlgorithmService matchAlgorithmService;

    // 🌟 设定匹配及格线：匹配度大于 60% 才认为是疑似物品
    private static final double THRESHOLD = 0.6;

    @Override
    public void matchAfterLostPublished(LostItem lostItem) {
        // 1. 去数据库把所有“正在寻找失主”的招领物品查出来 (status = 0)
        QueryWrapper<FoundItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        List<FoundItem> foundItems = foundItemMapper.selectList(wrapper);

        System.out.println("【系统触发自动匹配】新发布了失物，正在扫描 " + foundItems.size() + " 条招领信息...");

        // 2. 挨个计算分数
        for (FoundItem foundItem : foundItems) {
            double score = matchAlgorithmService.calculateMatchScore(lostItem, foundItem);
            // 3. 超过及格线，保存记录！
            if (score >= THRESHOLD) {
                saveRecord(lostItem.getId(), foundItem.getId(), score);
                System.out.println("💡 发现高度匹配物品！失物ID:" + lostItem.getId() + " 招领ID:" + foundItem.getId() + " 得分:" + (score * 100) + "%");
            }
        }
    }

    @Override
    public void matchAfterFoundPublished(FoundItem foundItem) {
        // 1. 去数据库把所有“正在寻找物品”的失物查出来 (status = 0)
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        List<LostItem> lostItems = lostItemMapper.selectList(wrapper);

        System.out.println("【系统触发自动匹配】新发布了招领，正在扫描 " + lostItems.size() + " 条失物信息...");

        // 2. 挨个计算分数
        for (LostItem lostItem : lostItems) {
            double score = matchAlgorithmService.calculateMatchScore(lostItem, foundItem);
            // 3. 超过及格线，保存记录！
            if (score >= THRESHOLD) {
                saveRecord(lostItem.getId(), foundItem.getId(), score);
                System.out.println("💡 发现高度匹配物品！招领ID:" + foundItem.getId() + " 失物ID:" + lostItem.getId() + " 得分:" + (score * 100) + "%");
            }
        }
    }

    // 辅助方法：存入数据库
    private void saveRecord(Long lostId, Long foundId, double score) {
        MatchRecord record = new MatchRecord();
        record.setLostId(lostId);
        record.setFoundId(foundId);
        record.setMatchScore(score);
        record.setStatus(0); // 待用户确认
        record.setCreateTime(LocalDateTime.now());
        this.save(record);
    }
}