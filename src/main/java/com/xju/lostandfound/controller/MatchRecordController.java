package com.xju.lostandfound.controller;

import com.xju.lostandfound.common.result.Result;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.entity.MatchRecord;
import com.xju.lostandfound.model.vo.MatchRecordVo;
import com.xju.lostandfound.service.FoundItemService;
import com.xju.lostandfound.service.LostItemService;
import com.xju.lostandfound.service.MatchAlgorithmService;
import com.xju.lostandfound.service.MatchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/match")
public class MatchRecordController {

    @Autowired
    private MatchRecordService matchRecordService;

    @Autowired
    private LostItemService lostItemService;

    @Autowired
    private FoundItemService foundItemService;

    @Autowired
    private MatchAlgorithmService matchAlgorithmService;

    @GetMapping("/my-list")
    public Result<List<MatchRecordVo>> myList(@RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录");
        }
        String userIdStr = JwtUtils.getClaimsByToken(token);
        if (userIdStr == null) {
            return Result.error(401, "Token无效或已过期");
        }

        Long currentUserId = Long.parseLong(userIdStr);
        List<MatchRecordVo> list = matchRecordService.getMatchListByUserId(currentUserId);
        return Result.success(list);
    }

    @PostMapping("/manual")
    public Result<String> manualMatch(@RequestParam Long lostId,
                                      @RequestParam Long foundId,
                                      @RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录后操作");
        }

        LostItem lostItem = lostItemService.getById(lostId);
        FoundItem foundItem = foundItemService.getById(foundId);

        if (lostItem == null || foundItem == null) {
            return Result.error("匹配失败：物品信息不存在或已被下架");
        }

        MatchRecord record = matchAlgorithmService.calculateMatchScore(lostItem, foundItem);

        if (record == null) {
            return Result.error("系统异常：连接 Coze 大模型失败，请检查项目配置文件中的密钥配置。");
        }

        if (record.getStatus() != null && record.getStatus() == 0) {
            matchRecordService.save(record);
            return Result.success("🎉 匹配成功！AI 判定两件物品高度相似，请前往【我的智能匹配】确认。");
        }

        return Result.error("匹配提示：经 AI 深度分析，特征维度重合度仅为 " + record.getMatchScore() + "%，判定非同一物品。");
    }

    @PostMapping("/confirm/{recordId}")
    public Result<String> confirmMatch(@PathVariable Long recordId, @RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录");
        }
        String userIdStr = JwtUtils.getClaimsByToken(token);
        if (userIdStr == null) {
            return Result.error(401, "Token无效或已过期");
        }

        Long currentUserId = Long.parseLong(userIdStr);
        boolean success = matchRecordService.confirmMatch(recordId, currentUserId);

        if (success) {
            return Result.success("确认成功！物品状态已更新为已找回或已认领。");
        } else {
            return Result.error("确认失败，该匹配记录可能不存在或已被处理。");
        }
    }
}