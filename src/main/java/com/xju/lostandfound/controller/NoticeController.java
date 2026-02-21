package com.xju.lostandfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xju.lostandfound.common.result.Result;
import com.xju.lostandfound.common.utils.JwtUtils;
import com.xju.lostandfound.entity.Notice;
import com.xju.lostandfound.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    // 1. 获取当前用户的未读消息数量 (供右上角小红点使用)
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestHeader(value = "token", required = false) String token) {
        if (token == null) return Result.error(401, "未登录");
        Long userId = Long.parseLong(JwtUtils.getClaimsByToken(token));

        QueryWrapper<Notice> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("is_read", 0);
        long count = noticeService.count(wrapper);
        return Result.success(count);
    }

    // 2. 获取当前用户的所有消息列表
    @GetMapping("/my-notices")
    public Result<List<Notice>> getMyNotices(@RequestHeader(value = "token", required = false) String token) {
        if (token == null) return Result.error(401, "未登录");
        Long userId = Long.parseLong(JwtUtils.getClaimsByToken(token));

        QueryWrapper<Notice> wrapper = new QueryWrapper<>();
        // 按时间倒序，最新的在最上面
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        return Result.success(noticeService.list(wrapper));
    }

    // 3. 将指定消息标记为已读
    @PutMapping("/read/{id}")
    public Result<String> readNotice(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice != null && notice.getIsRead() == 0) {
            notice.setIsRead(1);
            noticeService.updateById(notice);
        }
        return Result.success("已读成功");
    }

    // 4. 一键全部已读
    @PutMapping("/read-all")
    public Result<String> readAllNotices(@RequestHeader(value = "token", required = false) String token) {
        if (token == null) return Result.error(401, "未登录");
        Long userId = Long.parseLong(JwtUtils.getClaimsByToken(token));

        UpdateWrapper<Notice> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId).eq("is_read", 0).set("is_read", 1);
        noticeService.update(updateWrapper);
        return Result.success("全部已读成功");
    }
}