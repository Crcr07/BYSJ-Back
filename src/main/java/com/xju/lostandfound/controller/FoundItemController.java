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

import java.util.List;

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

        // 1. 恢复 Token 校验
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录");
        }

        // 2. 解析 Token
        String userIdStr = JwtUtils.getClaimsByToken(token);
        if (userIdStr == null) {
            return Result.error(401, "Token无效或已过期");
        }

        // 3. 转换为真实用户 ID
        Long currentUserId = Long.parseLong(userIdStr);

        // 4. 使用真实的 ID 进行发布
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

    // ================== 我的发布专属接口 ==================

    // 1. 查询当前用户发布的所有“招领”
    @GetMapping("/my")
    public Result<List<FoundItem>> getMyFoundItems(@RequestHeader(value = "token", required = false) String token) {
        if (token == null) return Result.error(401, "未登录");
        Long userId = Long.parseLong(JwtUtils.getClaimsByToken(token));
        QueryWrapper<FoundItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        return Result.success(foundItemService.list(wrapper));
    }

    // 2. 标记招领为“已认领/下架”
    @PutMapping("/my/resolve/{id}")
    public Result<String> resolveMyFoundItem(@PathVariable Long id) {
        FoundItem item = foundItemService.getById(id);
        if (item != null) {
            item.setStatus(1); // 1表示已认领或已解决
            foundItemService.updateById(item);
        }
        return Result.success("状态已更新为：已完成");
    }

    // 3. 彻底删除该招领记录
    @DeleteMapping("/my/{id}")
    public Result<String> deleteMyFoundItem(@PathVariable Long id) {
        foundItemService.removeById(id);
        return Result.success("删除成功");
    }
}