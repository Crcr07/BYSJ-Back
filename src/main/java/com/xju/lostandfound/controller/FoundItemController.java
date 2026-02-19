package com.xju.lostandfound.controller;

import com.xju.lostandfound.common.result.Result;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.model.dto.PublishItemDto;
import com.xju.lostandfound.service.FoundItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/item/found")
public class FoundItemController {

    @Autowired
    private FoundItemService foundItemService;

    /**
     * 发布招领
     * POST /item/found/publish
     */
    @PostMapping("/publish")
    public Result<String> publish(PublishItemDto dto, @RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录");
        }
        String username = JwtUtils.getClaimsByToken(token);
        if (username == null) {
            return Result.error(401, "Token无效或已过期");
        }

        // 暂时写死一个拾取者的ID
        Long currentUserId = 1001L;

        boolean success = foundItemService.publish(dto, currentUserId);
        return success ? Result.success("发布招领成功") : Result.error("发布失败");
    }
}