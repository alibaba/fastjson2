package com.alibaba.fastjson2.example.solontest.controller;

import com.alibaba.fastjson2.example.solontest.entity.User;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;

/**
 * @author noear
 * @since 2024-10-01
 */
@Controller
public class DemoController {
    @Mapping("/demo")
    public User demo(User user) {
        return user;
    }
}
