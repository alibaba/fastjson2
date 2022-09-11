package com.example.spring6test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
public class Spring6testApplication {

    public static void main(String[] args) {
        SpringApplication.run(Spring6testApplication.class, args);
    }

}
