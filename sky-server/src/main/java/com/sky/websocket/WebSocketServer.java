package com.sky.websocket;

import org.springframework.stereotype.Component;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务端实现类
 * 该类是一个WebSocket服务器端，使用Spring注解方式进行配置。
 * 该服务器端能够支持客户端的连接、消息接收和消息发送等功能。
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    // 用来存储所有已连接的会话对象（客户端的WebSocket连接）
    private static Map<String, Session> sessionMap = new HashMap<>();

    /**
     * 连接建立成功时调用的方法
     * 当客户端成功连接到WebSocket服务器时，系统自动调用此方法
     *
     * @param session 当前客户端的WebSocket会话对象
     * @param sid 客户端的会话ID（通过URL中的参数获取）
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + "建立连接");

        // 将当前客户端的会话对象存入 sessionMap，以便后续使用
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端发送的消息时调用的方法
     * 每当WebSocket服务器收到客户端发送的消息时，都会调用该方法
     *
     * @param message 客户端发送的消息
     * @param sid 客户端的会话ID
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + "的信息: " + message);

        // 这里可以进一步处理客户端发送的消息
        // 比如：解析消息、做某些操作、或者给客户端发送响应
    }

    /**
     * 连接关闭时调用的方法
     * 当客户端断开连接时，WebSocket服务器会自动调用此方法
     *
     * @param sid 客户端的会话ID
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开：" + sid);

        // 移除断开连接的客户端会话对象
        sessionMap.remove(sid);
    }

    /**
     * 群发消息给所有已连接的客户端
     *
     * @param message 要发送的消息内容
     */
    public void sendToAllClient(String message) {
        // 获取所有已连接的客户端会话对象
        Collection<Session> sessions = sessionMap.values();

        // 遍历所有会话对象并发送消息
        for (Session session : sessions) {
            try {
                // 通过WebSocket会话对象向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                // 如果发生错误，打印堆栈信息
                e.printStackTrace();
            }
        }
    }
}
