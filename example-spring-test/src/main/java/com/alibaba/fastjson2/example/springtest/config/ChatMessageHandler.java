package com.alibaba.fastjson2.example.springtest.config;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jackiechan on 2018/2/5/下午4:11
 * 文本消息的处理器
 */
public class ChatMessageHandler
        extends TextWebSocketHandler {
    //用于缓存所有的用户和连接之间的关系
    private static final Map<String, WebSocketSession> allClients;

    static {
        //初始化连接
        allClients = new ConcurrentHashMap();
    }

    /**
     * 当和用户成功建立连接的时候会调用此方法,在此方法内部应该保存连接
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    }

    /**
     * 给某个用户发送消息
     *
     * @param userName
     * @param message
     */
    public void sendMessageToUser(String userName, TextMessage message) {
        //根据接收方的名字找到对应的连接
        WebSocketSession webSocketSession = allClients.get(userName);

        //如果没有离线,如果离线,请根据实际业务需求来处理,可能会需要保存离线消息
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                //发送消息
                webSocketSession.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给所有在线用户发送消息,此处以文本消息为例子
     *
     * @param message
     */
    public void sendMessageToUsers(TextMessage message) {
        //获取所有的连接
        for (Map.Entry<String, WebSocketSession> webSocketSessionEntry : allClients.entrySet()) {
            //找到每个连接
            WebSocketSession session = webSocketSessionEntry.getValue();
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
