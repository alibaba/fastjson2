package com.alibaba.fastjson2.example.spring6test.controller;

import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson2.example.spring6test.entity.User;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
