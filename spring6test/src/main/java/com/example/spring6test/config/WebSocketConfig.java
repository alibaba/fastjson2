package com.example.spring6test.config;

import com.alibaba.fastjson2.support.spring.websocket.sockjs.FastjsonSockJsMessageCodec;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        System.out.println("初始化路径拦截");
        webSocketHandlerRegistry.addHandler(new ChatMessageHandler(), "/websocket/*").withSockJS().setMessageCodec(new FastjsonSockJsMessageCodec());
    }

}

