package com.xju.lostandfound.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xju.lostandfound.entity.Notice;

public interface NoticeService extends IService<Notice> {
    // 🌟 核心封装方法：发消息只需调这一个接口，自动存库+自动推送！
    void sendNotice(Long userId, String title, String content, Integer type);
}