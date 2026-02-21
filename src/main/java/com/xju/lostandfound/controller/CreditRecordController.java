package com.xju.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xju.lostandfound.common.result.Result;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.entity.CreditRecord;
import com.xju.lostandfound.service.CreditRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/credit")
public class CreditRecordController {

    @Autowired
    private CreditRecordService creditRecordService;

    /**
     * 查询当前登录用户的积分流水明细
     */
    @GetMapping("/my-records")
    public Result<List<CreditRecord>> getMyRecords(@RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录");
        }
        String userIdStr = JwtUtils.getClaimsByToken(token);
        if (userIdStr == null) {
            return Result.error(401, "Token无效");
        }

        Long currentUserId = Long.parseLong(userIdStr);

        // 查出该用户的所有积分流水，按时间倒序排列 (最新的在最上面)
        QueryWrapper<CreditRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", currentUserId).orderByDesc("create_time");

        List<CreditRecord> list = creditRecordService.list(wrapper);
        return Result.success(list);
    }
}