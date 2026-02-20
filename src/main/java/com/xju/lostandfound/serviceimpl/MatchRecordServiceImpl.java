package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.entity.MatchRecord;
import com.xju.lostandfound.mapper.FoundItemMapper;
import com.xju.lostandfound.mapper.LostItemMapper;
import com.xju.lostandfound.mapper.MatchRecordMapper;
import com.xju.lostandfound.model.vo.MatchRecordVo;
import com.xju.lostandfound.service.MatchAlgorithmService;
import com.xju.lostandfound.service.MatchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchRecordServiceImpl extends ServiceImpl<MatchRecordMapper, MatchRecord> implements MatchRecordService {

    @Autowired
    private LostItemMapper lostItemMapper;

    @Autowired
    private FoundItemMapper foundItemMapper;

    @Autowired
    private MatchAlgorithmService matchAlgorithmService;

    // 设定匹配及格线：匹配度大于 60% 才认为是疑似物品
    private static final double THRESHOLD = 0.6;

    @Override
    public void matchAfterLostPublished(LostItem lostItem) {
        QueryWrapper<FoundItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        List<FoundItem> foundItems = foundItemMapper.selectList(wrapper);

        System.out.println("【系统触发自动匹配】新发布了失物，正在扫描 " + foundItems.size() + " 条招领信息...");

        for (FoundItem foundItem : foundItems) {
            double score = matchAlgorithmService.calculateMatchScore(lostItem, foundItem);
            if (score >= THRESHOLD) {
                saveRecord(lostItem.getId(), foundItem.getId(), score);
                System.out.println("💡 发现高度匹配物品！失物ID:" + lostItem.getId() + " 招领ID:" + foundItem.getId() + " 得分:" + (score * 100) + "%");
            }
        }
    }

    @Override
    public void matchAfterFoundPublished(FoundItem foundItem) {
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        List<LostItem> lostItems = lostItemMapper.selectList(wrapper);

        System.out.println("【系统触发自动匹配】新发布了招领，正在扫描 " + lostItems.size() + " 条失物信息...");

        for (LostItem lostItem : lostItems) {
            double score = matchAlgorithmService.calculateMatchScore(lostItem, foundItem);
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

    // ==========================================
    // 🌟 查询我的匹配记录 (彻底解决冲突的方法)
    // ==========================================
    @Override
    public List<MatchRecordVo> getMatchListByUserId(Long userId) {
        List<MatchRecordVo> voList = new ArrayList<>();

        // 1. 查出所有“待确认(0)”的匹配记录，按匹配得分从高到低排序
        QueryWrapper<MatchRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0).orderByDesc("match_score");
        List<MatchRecord> records = this.list(wrapper);

        // 2. 遍历记录，组装物品详情
        for (MatchRecord record : records) {
            LostItem lostItem = lostItemMapper.selectById(record.getLostId());
            FoundItem foundItem = foundItemMapper.selectById(record.getFoundId());

            if (lostItem != null && foundItem != null) {
                // 3. 核心：如果这个匹配记录的失主是当前用户，或者是拾取者是当前用户，就展示给他！
                if (lostItem.getUserId().equals(userId) || foundItem.getUserId().equals(userId)) {
                    MatchRecordVo vo = new MatchRecordVo();
                    vo.setRecordId(record.getId());
                    vo.setMatchScore(record.getMatchScore());
                    vo.setStatus(record.getStatus());
                    vo.setCreateTime(record.getCreateTime());

                    // 安全起见，清空敏感冗长的算法特征，不传给前端
                    lostItem.setOcrText(null);
                    lostItem.setImageFeature(null);
                    foundItem.setOcrText(null);
                    foundItem.setImageFeature(null);

                    vo.setLostItem(lostItem);
                    vo.setFoundItem(foundItem);

                    voList.add(vo);
                }
            }
        }
        return voList;
    }

    // ==========================================
    // 🌟 确认认领逻辑 (带事务控制)
    // ==========================================
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务，如果中途报错，三张表的数据全部回滚，保证数据一致性
    public boolean confirmMatch(Long recordId, Long userId) {
        // 1. 查出这条匹配记录
        MatchRecord record = this.getById(recordId);
        if (record == null || record.getStatus() != 0) {
            return false; // 记录不存在，或者已经不是待确认状态了
        }

        // 2. 将这条匹配记录的状态改为 1 (已确认匹配)
        record.setStatus(1);
        this.updateById(record);

        // 3. 将对应的失物状态改为 1 (已找回)
        LostItem lostItem = new LostItem();
        lostItem.setId(record.getLostId());
        lostItem.setStatus(1);
        lostItemMapper.updateById(lostItem);

        // 4. 将对应的招领物品状态改为 1 (已认领)
        FoundItem foundItem = new FoundItem();
        foundItem.setId(record.getFoundId());
        foundItem.setStatus(1);
        foundItemMapper.updateById(foundItem);

        return true;
    }
}

