package com.alibaba.fastjson2.example.spring6test.controller;

import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson2.example.spring6test.entity.User;
import com.alibaba.fastjson2.example.spring6test.entity.WebFluxMockBean;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Objects;

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

    @GetMapping("/webFluxGet")
    public WebFluxMockBean webFluxGet() {
        return WebFluxMockBean.builder()
                .name("name")
                .upperName("NAME")
                .innerBean(WebFluxMockBean.InnerBean.builder()
                        .id("id1")
                        .upperId("ID1")
                        .build())
                .upperInnerBean(WebFluxMockBean.InnerBean.builder()
                        .id("id2")
                        .upperId("ID2")
                        .build())
                .build();
    }

    @PostMapping("/webFluxPost")
    public WebFluxMockBean webFluxPost(@RequestBody WebFluxMockBean request) {
        if (Objects.isNull(request)) {
            return null;
        }
        request.setName("res=" + request.getName());
        request.setUpperName("RES=" + request.getUpperName());
        request.getInnerBean().setId("res=" + request.getInnerBean().getId());
        request.getInnerBean().setUpperId("RES=" + request.getInnerBean().getUpperId());
        request.getUpperInnerBean().setId("res=" + request.getUpperInnerBean().getId());
        request.getUpperInnerBean().setUpperId("RES=" + request.getUpperInnerBean().getUpperId());
        return request;
    }
}
