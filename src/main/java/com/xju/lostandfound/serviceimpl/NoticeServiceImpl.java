package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xju.lostandfound.entity.Notice;
import com.xju.lostandfound.mapper.NoticeMapper;
import com.xju.lostandfound.service.NoticeService;
import com.xju.lostandfound.websocket.WebSocketServer;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public void sendNotice(Long userId, String title, String content, Integer type) {
        // 🛡️ 第一道防线：存入数据库 (不管在不在线，先留底)
        Notice notice = new Notice();
        notice.setUserId(userId);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setType(type);
        notice.setIsRead(0); // 默认未读
        notice.setCreateTime(LocalDateTime.now());
        this.save(notice);

        // 🛡️ 第二道防线：WebSocket 强推 (判断如果在线，立刻弹窗)
        try {
            // 将 Notice 对象转成 JSON 字符串发给前端
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // 解决时间格式化问题
            String jsonMessage = mapper.writeValueAsString(notice);

            // 调用 WebSocket 引擎发射！
            WebSocketServer.sendMessage(userId.toString(), jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("消息推送失败，但已存入数据库！");
        }
    }
}