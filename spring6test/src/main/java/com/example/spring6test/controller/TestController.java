package com.example.spring6test.controller;

import com.example.spring6test.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jiangqiang
 * date 2022-09-11
 */
@RestController
public class TestController {
    @GetMapping("/test")
    public User hello() {
        User user = new User("suixin", "123456");
        return user;
    }
}
