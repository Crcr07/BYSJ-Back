package com.xju.lostandfound.serviceimpl;

import com.xju.lostandfound.service.CreditRecordService;
import org.springframework.context.annotation.Lazy;
import com.xju.lostandfound.entity.User;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;


@Service
public class MatchRecordServiceImpl extends ServiceImpl<MatchRecordMapper, MatchRecord> implements MatchRecordService {

    @Autowired
    private UserService userService;

    // 🌟 重点查这里：必须要有下面这三行代码！
    @Autowired
    @Lazy
    private CreditRecordService creditRecordService;

    @Autowired
    @Lazy  // 🌟 核心修复：加上延迟加载注解，打破死循环
    private LostItemService lostItemService;

    @Autowired
    @Lazy  // 🌟 核心修复：加上延迟加载注解
    private FoundItemService foundItemService;

    @Autowired
    private LostItemMapper lostItemMapper;

    @Autowired
    private FoundItemMapper foundItemMapper;

    @Autowired
    private MatchAlgorithmService matchAlgorithmService;

    // 注意：原本的 THRESHOLD 常量已经被删除，因为大模型匹配的及格线阈值判定已经在 MatchAlgorithmServiceImpl 内部完成

    @Override
    public void matchAfterLostPublished(LostItem lostItem) {
        QueryWrapper<FoundItem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        List<FoundItem> foundItems = foundItemMapper.selectList(wrapper);

        System.out.println("【系统触发自动匹配】新发布了失物，正在扫描 " + foundItems.size() + " 条招领信息...");

        for (FoundItem foundItem : foundItems) {
            // 【重构核心】：接收大模型工作流返回的完整 MatchRecord 对象
            MatchRecord record = matchAlgorithmService.calculateMatchScore(lostItem, foundItem);

            // 如果记录不为空，并且大模型判定的得分达到了阈值(状态在之前已被设为0代表待确认)，则存入数据库
            if (record != null && record.getStatus() != null && record.getStatus() == 0) {
                this.save(record);
                System.out.println("💡 发现高度匹配物品！失物ID:" + lostItem.getId() + " 招领ID:" + foundItem.getId());
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
            // 【重构核心】：接收大模型工作流返回的完整 MatchRecord 对象
            MatchRecord record = matchAlgorithmService.calculateMatchScore(lostItem, foundItem);

            if (record != null && record.getStatus() != null && record.getStatus() == 0) {
                this.save(record);
                System.out.println("💡 发现高度匹配物品！招领ID:" + foundItem.getId() + " 失物ID:" + lostItem.getId());
            }
        }
    }

    // 注意：原本旧的私有方法 saveRecord(Long lostId, Long foundId, double score) 已经被删除，
    // 因为 Coze 返回的记录已经是拼装好的对象，直接使用 this.save(record) 保存。

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
                    vo.setMatchReason(record.getMatchReason());
                    vo.setMatchedFields(record.getMatchedFields());
                    vo.setRiskLevel(record.getRiskLevel());

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
    // =========================================

    @Override
    @Transactional(rollbackFor = Exception.class) // 🌟 开启数据库事务，同生共死！
    public boolean confirmMatch(Long recordId, Long currentUserId) {
        // 1. 查询匹配记录
        MatchRecord record = this.getById(recordId);
        if (record == null || record.getStatus() != 0) {
            return false; // 记录不存在或已被处理
        }

        // 2. 更新匹配记录状态为 1 (已确认)
        record.setStatus(1);
        this.updateById(record);

        // 3. 获取对应的失物和招领物品
        Long lostItemId = record.getLostId();    // 🌟 去掉 Item，改成 getLostId()
        Long foundItemId = record.getFoundId();  // 🌟 去掉 Item，改成 getFoundId()

        // 4. 更新失物状态为已找回 (status = 1)
        LostItem lostItem = lostItemService.getById(lostItemId);
        if (lostItem != null) {
            lostItem.setStatus(1);
            lostItemService.updateById(lostItem);
        }

        // 5. 更新招领物品状态为已认领 (status = 1)
        FoundItem foundItem = foundItemService.getById(foundItemId);
        if (foundItem != null) {
            foundItem.setStatus(1);
            foundItemService.updateById(foundItem);

            // ================= 🌟 核心加分逻辑开始 =================
            // 6. 给拾金不昧的好心人加 10 分！
            Long finderUserId = foundItem.getUserId(); // 获取捡到东西的人的 ID

            // 防刷分机制：如果捡到东西的人就是丢东西的人自己，则不加分
            if (!finderUserId.equals(currentUserId)) {
                // 🚀 直接调用封装好的方法：加 10 分，并写入流水原因！
                creditRecordService.changeUserCredit(finderUserId, 10, "拾金不昧：成功归还失物");
            }
            // ================= 🌟 核心加分逻辑结束 =================
        } // 🌟 修复：补上这个大括号，用来闭合 if (foundItem != null)

        return true;
    } // 闭合 confirmMatch 方法
} // 闭合 MatchRecordServiceImpl 类