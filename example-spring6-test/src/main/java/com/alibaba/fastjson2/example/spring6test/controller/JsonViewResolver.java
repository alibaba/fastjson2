package com.alibaba.fastjson2.example.spring6test.controller;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.support.spring6.webservlet.view.FastJsonJsonView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class JsonViewResolver
        implements ErrorViewResolver {
    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        FastJsonJsonView fastJsonJsonView = new FastJsonJsonView();
        Properties properties = new Properties();
        properties.setProperty("username", "suixin");
        properties.setProperty("password", "123456");
        fastJsonJsonView.setAttributes(properties);
        Map<String, Object> map = new HashMap<>();
        map.put("age", 26);
        fastJsonJsonView.setRenderedAttributes(CollUtil.set(false, "username", "age"));
        fastJsonJsonView.setAttributesMap(map);
        return new ModelAndView(fastJsonJsonView);
    }
}
