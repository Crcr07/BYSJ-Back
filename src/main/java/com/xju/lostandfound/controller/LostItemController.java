package com.xju.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xju.lostandfound.common.result.Result;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.model.dto.PageQueryDto;
import com.xju.lostandfound.model.dto.PublishItemDto;
import com.xju.lostandfound.service.LostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item/lost")
public class LostItemController {

    // 🌟 解决报错的关键：必须把 Service 注入进来
    @Autowired
    private LostItemService lostItemService;

    /**
     * 发布失物
     * POST /item/lost/publish
     */
    @PostMapping("/publish")
    public Result<String> publish(PublishItemDto dto, @RequestHeader(value = "token", required = false) String token) {

        // 1. 恢复 Token 校验
        if (token == null || token.trim().isEmpty()) {
            return Result.error(401, "请先登录");
        }

        // 2. 解析 Token，获取里面存放的真实用户 ID (String格式)
        String userIdStr = JwtUtils.getClaimsByToken(token);
        if (userIdStr == null) {
            return Result.error(401, "Token无效或已过期");
        }

        // 3. 将 String 类型的 ID 转换为 Long 类型
        Long currentUserId = Long.parseLong(userIdStr);

        // 4. 使用真实的 ID 进行发布
        boolean success = lostItemService.publish(dto, currentUserId);

        if (success) {
            return Result.success("发布成功");
        } else {
            return Result.error("发布失败");
        }
    }

    /**
     * 分页查询失物列表
     * GET /item/lost/list
     */
    @GetMapping("/list")
    public Result<Page<LostItem>> list(PageQueryDto queryDto) {
        // 1. 构造分页对象
        Page<LostItem> page = new Page<>(queryDto.getCurrent(), queryDto.getSize());

        // 2. 构造查询条件
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();

        // 只查处于“寻找中(0)”状态的物品，按照时间倒序排
        wrapper.eq("status", 0).orderByDesc("create_time");

        // 关键词搜索
        if (queryDto.getKeyword() != null && !queryDto.getKeyword().trim().isEmpty()) {
            wrapper.and(w -> w.like("item_name", queryDto.getKeyword())
                    .or()
                    .like("description", queryDto.getKeyword()));
        }

        // 分类筛选
        if (queryDto.getCategoryId() != null) {
            wrapper.eq("category_id", queryDto.getCategoryId());
        }

        // 3. 执行查询 (这里就不会报错了，因为上面注入了 lostItemService)
        Page<LostItem> result = lostItemService.page(page, wrapper);

        // 4. 清除冗长且敏感的特征数据，不返回给前端
        if (result.getRecords() != null) {
            for (LostItem item : result.getRecords()) {
                item.setOcrText(null);
                item.setImageFeature(null);
            }
        }

        return Result.success(result);
    }

    // ================== 我的发布专属接口 ==================

    // 1. 查询当前用户发布的所有“失物”
    @GetMapping("/my")
    public Result<List<LostItem>> getMyLostItems(@RequestHeader(value = "token", required = false) String token) {
        if (token == null) return Result.error(401, "未登录");
        Long userId = Long.parseLong(JwtUtils.getClaimsByToken(token));
        QueryWrapper<LostItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        return Result.success(lostItemService.list(wrapper));
    }

    // 2. 标记失物为“已找回/下架”
    @PutMapping("/my/resolve/{id}")
    public Result<String> resolveMyLostItem(@PathVariable Long id) {
        LostItem item = lostItemService.getById(id);
        if (item != null) {
            item.setStatus(1); // 1表示已找回或已解决
            lostItemService.updateById(item);
        }
        return Result.success("状态已更新为：已找回");
    }

    // 3. 彻底删除该失物记录
    @DeleteMapping("/my/{id}")
    public Result<String> deleteMyLostItem(@PathVariable Long id) {
        lostItemService.removeById(id);
        // 注意：企业级开发中通常还会级联删除对应的 match_record 记录，这里为了简化先只删主表
        return Result.success("删除成功");
    }
}