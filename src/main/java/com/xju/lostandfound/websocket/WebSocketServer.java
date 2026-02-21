package com.xju.lostandfound.websocket;
//核心枢纽：记录谁在线，负责发弹窗
import org.springframework.stereotype.Component;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{userId}")
@Component
public class WebSocketServer {

    // 静态变量，用来存放目前所有在线的客户端 (Key是用户ID，Value是对应的连接会话)
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessionMap.put(userId, session);
        System.out.println("【WebSocket】有新连接加入！用户ID：" + userId + "，当前在线人数：" + sessionMap.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        sessionMap.remove(userId);
        System.out.println("【WebSocket】连接断开！用户ID：" + userId + "，当前在线人数：" + sessionMap.size());
    }

    /**
     * 收到客户端消息后调用的方法 (本系统暂时用不到前端发消息给后端，但必须保留)
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("【WebSocket】收到客户端消息：" + message);
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("【WebSocket】发生错误");
        error.printStackTrace();
    }

    /**
     * 🌟 核心方法：后端向指定的用户发送实时弹窗消息！
     */
    public static void sendMessage(String userId, String message) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                // 如果用户在线，直接用光速把消息推过去！
                session.getBasicRemote().sendText(message);
                System.out.println("【WebSocket】成功向用户 " + userId + " 推送了一条实时消息！");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 如果用户不在线，就安静地放弃推送，反正我们已经存进数据库了（第二道防线）
            System.out.println("【WebSocket】用户 " + userId + " 不在线，放弃实时推送，转为离线消息。");
        }
    }
}