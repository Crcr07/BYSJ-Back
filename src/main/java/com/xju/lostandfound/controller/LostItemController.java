package com.xju.lostandfound.controller;

import com.xju.lostandfound.common.result.Result;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.model.dto.PublishItemDto;
import com.xju.lostandfound.service.LostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController // 🌟 必须有这个，Spring 才会把它当成接口类
@RequestMapping("/item/lost") // 🌟 必须有这个，映射前缀路径
public class LostItemController {

    @Autowired
    private LostItemService lostItemService;

    /**
     * 发布失物接口
     * POST /item/lost/publish
     */
    @PostMapping("/publish") // 🌟 必须有这个，映射具体路径
    public Result<String> publish(PublishItemDto dto, @RequestHeader(value = "token", required = false) String token) {

        // 1. 简单的登录校验
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请求头缺少 token，请先登录");
        }
        String username = JwtUtils.getClaimsByToken(token);
        if (username == null) {
            return Result.error(401, "Token 无效或已过期，请重新登录");
        }

        // 2. 模拟获取当前登录用户的 ID (实际应从数据库查)
        Long currentUserId = 1000L;

        // 3. 调用 Service 执行发布逻辑
        boolean success = lostItemService.publish(dto, currentUserId);

        if (success) {
            return Result.success("发布成功");
        } else {
            return Result.error(500, "发布失败，请检查服务器日志");
        }
    }
}