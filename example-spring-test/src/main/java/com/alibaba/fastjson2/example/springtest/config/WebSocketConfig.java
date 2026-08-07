package com.alibaba.fastjson2.example.springtest.config;

import com.alibaba.fastjson2.support.spring.websocket.sockjs.FastjsonSockJsMessageCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig
        implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        System.out.println("初始化路径拦截");
        webSocketHandlerRegistry.addHandler(new ChatMessageHandler(), "/websocket/*").withSockJS().setMessageCodec(new FastjsonSockJsMessageCodec());
    }

    /**
     * ServerEndpointExporter 作用
     * <p>
     * 这个Bean会自动注册使用@ServerEndpoint注解声明的websocket endpoint
     *
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
