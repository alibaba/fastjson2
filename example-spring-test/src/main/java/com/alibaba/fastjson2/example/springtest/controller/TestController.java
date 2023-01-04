package com.alibaba.fastjson2.example.springtest.controller;

import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson2.example.springtest.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author jiangqiang
 * date 2022-09-11
 */
@RestController
public class TestController {
    @GetMapping("/test")
    public User test(User user) {
        return user;
    }

    @GetMapping(value = "/hello", produces = "text/html")
    public void hello(HttpServletResponse response) {
        //返回一个html测试页面
        try (ServletOutputStream servletOutputStream = response.getOutputStream();
        ) {
            ClassPathResource classPathResource = new ClassPathResource("hello.html");
            servletOutputStream.write(classPathResource.readBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
