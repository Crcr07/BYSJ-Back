package com.xju.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xju.lostandfound.common.result.Result;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.model.dto.PageQueryDto;
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

        Long currentUserId = 1001L;
        boolean success = foundItemService.publish(dto, currentUserId);

        return success ? Result.success("发布招领成功") : Result.error("发布失败");
    }

    /**
     * 分页查询招领列表
     * GET /item/found/list
     */
    @GetMapping("/list")
    public Result<Page<FoundItem>> list(PageQueryDto queryDto) {
        Page<FoundItem> page = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        QueryWrapper<FoundItem> wrapper = new QueryWrapper<>();

        wrapper.eq("status", 0).orderByDesc("create_time");

        if (queryDto.getKeyword() != null && !queryDto.getKeyword().trim().isEmpty()) {
            wrapper.and(w -> w.like("item_name", queryDto.getKeyword())
                    .or()
                    .like("description", queryDto.getKeyword()));
        }

        if (queryDto.getCategoryId() != null) {
            wrapper.eq("category_id", queryDto.getCategoryId());
        }

        Page<FoundItem> result = foundItemService.page(page, wrapper);

        if (result.getRecords() != null) {
            for (FoundItem item : result.getRecords()) {
                item.setOcrText(null);
                item.setImageFeature(null);
            }
        }

        return Result.success(result);
    }
}