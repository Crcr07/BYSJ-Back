package com.xju.lostandfound.controller;

import com.xju.lostandfound.common.result.Result;
// import com.xju.lostandfound.common.utils.JwtUtils; // 暂时不用
import com.xju.lostandfound.model.vo.MatchRecordVo;
import com.xju.lostandfound.service.MatchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xju.lostandfound.common.utils.JwtUtils;
import java.util.List;

@RestController
@RequestMapping("/match")
public class MatchRecordController {

    @Autowired
    private MatchRecordService matchRecordService;

    /**
     * 查询我的智能匹配记录
     * GET /match/my-list
     */
    @GetMapping("/my-list")
    public Result<List<MatchRecordVo>> myList(@RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录");
        }
        String userIdStr = JwtUtils.getClaimsByToken(token);
        if (userIdStr == null) {
            return Result.error(401, "Token无效或已过期");
        }

        // 🌟 提取真实用户 ID
        Long currentUserId = Long.parseLong(userIdStr);

        List<MatchRecordVo> list = matchRecordService.getMatchListByUserId(currentUserId);
        return Result.success(list);
    }

    /**
     * 确认认领
     * POST /match/confirm/{recordId}
     */
    @PostMapping("/confirm/{recordId}")
    public Result<String> confirmMatch(@PathVariable Long recordId, @RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录");
        }
        String userIdStr = JwtUtils.getClaimsByToken(token);
        if (userIdStr == null) {
            return Result.error(401, "Token无效或已过期");
        }

        // 🌟 提取真实用户 ID
        Long currentUserId = Long.parseLong(userIdStr);

        boolean success = matchRecordService.confirmMatch(recordId, currentUserId);

        if (success) {
            return Result.success("确认成功！物品状态已更新为已找回/已认领。");
        } else {
            return Result.error("确认失败，该匹配记录可能不存在或已被处理。");
        }
    }
}