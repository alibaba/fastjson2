package com.alibaba.fastjson2.example.springtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
public class SpringTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringTestApplication.class, args);
    }
}
