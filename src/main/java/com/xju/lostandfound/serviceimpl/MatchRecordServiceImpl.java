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
import com.xju.lostandfound.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchRecordServiceImpl extends ServiceImpl<MatchRecordMapper, MatchRecord> implements MatchRecordService {

    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private CreditRecordService creditRecordService;

    @Autowired
    @Lazy
    private LostItemService lostItemService;

    @Autowired
    @Lazy
    private FoundItemService foundItemService;

    @Autowired
    private LostItemMapper lostItemMapper;

    @Autowired
    private FoundItemMapper foundItemMapper;

    @Autowired
    private MatchAlgorithmService matchAlgorithmService;

    @Autowired
    @Lazy
    private NoticeService noticeService;

    // ==========================================
    // 🌟 失物发布后触发的异步匹配 (已加入并发优化)
    // ==========================================
    @Async
    @Override
    public void matchAfterLostPublished(LostItem lostItem) {
        // 1. 数据库前置过滤：仅查询“待认领”且“分类一致”的物品
        QueryWrapper<FoundItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        if (lostItem.getCategoryId() != null) {
            wrapper.eq("category_id", lostItem.getCategoryId());
        }

        List<FoundItem> foundItems = foundItemMapper.selectList(wrapper);

        System.out.println("【系统触发异步匹配】前置过滤完毕，准备并发扫描 " + foundItems.size() + " 条同类招领信息...");

        // 2. 开启并行流多线程并发调用大模型接口
        foundItems.parallelStream().forEach(foundItem -> {
            try {
                MatchRecord record = matchAlgorithmService.calculateMatchScore(lostItem, foundItem);

                if (record != null && record.getStatus() != null && record.getStatus() == 0) {
                    this.save(record);
                    System.out.println("💡 发现高度匹配物品！失物ID:" + lostItem.getId() + " 招领ID:" + foundItem.getId());

                    noticeService.sendNotice(
                            lostItem.getUserId(),
                            "✨ AI智能匹配完成",
                            "系统为您发布的失物 [" + lostItem.getItemName() + "] 找到了高度疑似物品，请前往【我的智能匹配】确认！",
                            2
                    );
                }
            } catch (Exception e) {
                System.err.println("并发匹配过程中发生异常，失物ID:" + lostItem.getId() + " 招领ID:" + foundItem.getId() + "，原因：" + e.getMessage());
            }
        });
    }

    // ==========================================
    // 🌟 招领发布后触发的异步匹配 (已加入并发优化)
    // ==========================================
    @Async
    @Override
    public void matchAfterFoundPublished(FoundItem foundItem) {
        // 1. 数据库前置过滤
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        if (foundItem.getCategoryId() != null) {
            wrapper.eq("category_id", foundItem.getCategoryId());
        }

        List<LostItem> lostItems = lostItemMapper.selectList(wrapper);

        System.out.println("【系统触发异步匹配】前置过滤完毕，准备并发扫描 " + lostItems.size() + " 条同类失物信息...");

        // 2. 开启并行流并发匹配
        lostItems.parallelStream().forEach(lostItem -> {
            try {
                MatchRecord record = matchAlgorithmService.calculateMatchScore(lostItem, foundItem);

                if (record != null && record.getStatus() != null && record.getStatus() == 0) {
                    this.save(record);
                    System.out.println("💡 发现高度匹配物品！招领ID:" + foundItem.getId() + " 失物ID:" + lostItem.getId());

                    noticeService.sendNotice(
                            foundItem.getUserId(),
                            "✨ AI智能匹配完成",
                            "系统为您发布的招领 [" + foundItem.getItemName() + "] 找到了疑似失主，请前往【我的智能匹配】确认！",
                            2
                    );
                }
            } catch (Exception e) {
                System.err.println("并发匹配过程中发生异常，招领ID:" + foundItem.getId() + " 失物ID:" + lostItem.getId() + "，原因：" + e.getMessage());
            }
        });
    }

    // ==========================================
    // 查询我的匹配记录
    // ==========================================
    @Override
    public List<MatchRecordVo> getMatchListByUserId(Long userId) {
        List<MatchRecordVo> voList = new ArrayList<>();

        QueryWrapper<MatchRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0).orderByDesc("match_score");
        List<MatchRecord> records = this.list(wrapper);

        for (MatchRecord record : records) {
            LostItem lostItem = lostItemMapper.selectById(record.getLostId());
            FoundItem foundItem = foundItemMapper.selectById(record.getFoundId());

            if (lostItem != null && foundItem != null) {
                if (lostItem.getUserId().equals(userId) || foundItem.getUserId().equals(userId)) {
                    MatchRecordVo vo = new MatchRecordVo();
                    vo.setRecordId(record.getId());
                    vo.setMatchScore(record.getMatchScore());
                    vo.setStatus(record.getStatus());
                    vo.setCreateTime(record.getCreateTime());
                    vo.setMatchReason(record.getMatchReason());
                    vo.setMatchedFields(record.getMatchedFields());
                    vo.setRiskLevel(record.getRiskLevel());

                    // 安全起见，清空敏感特征不传给前端
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
    // 确认认领逻辑 (带事务控制与拾金不昧积分奖励)
    // =========================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmMatch(Long recordId, Long currentUserId) {
        MatchRecord record = this.getById(recordId);
        if (record == null || record.getStatus() != 0) {
            return false;
        }

        record.setStatus(1);
        this.updateById(record);

        Long lostItemId = record.getLostId();
        Long foundItemId = record.getFoundId();

        LostItem lostItem = lostItemService.getById(lostItemId);
        if (lostItem != null) {
            lostItem.setStatus(1);
            lostItemService.updateById(lostItem);
        }

        FoundItem foundItem = foundItemService.getById(foundItemId);
        if (foundItem != null) {
            foundItem.setStatus(1);
            foundItemService.updateById(foundItem);

            Long finderUserId = foundItem.getUserId();

            // 防刷分机制：若归还者不是失主本人，则给予信用积分奖励
            if (!finderUserId.equals(currentUserId)) {
                creditRecordService.changeUserCredit(finderUserId, 10, "拾金不昧：成功归还失物");
            }
        }

        return true;
    }
}