package com.alibaba.fastjson2.spring.issues.issue471;

import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
public class Issue471 {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac) //
                .addFilter(new CharacterEncodingFilter("UTF-8", true)) // 设置服务器端返回的字符集为：UTF-8
                .build();
    }

    @Test
    public void test() throws Exception {
        mockMvc.perform(
                (post("/test").characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"id\":10000,\"name\":\"name\"}")
                )).andExpect(status().isOk()).andDo(print());
    }

    @RestController
    @RequestMapping
    public static class BeanController {
        @PostMapping(path = "/test", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseData test(@RequestBody RequestData request) {
            ResponseData<Map<String, List>> response = new ResponseData();
            Map<String, List> map = new HashMap<>();
            request.setId(10L + request.getId());
            request.setName("test" + request.getName());
            map.put("test", Arrays.asList(request));
            response.setCode(200);
            response.setMessage("请求成功");
            response.setSuccess(true);
            response.setData(map);
            return response;
        }
    }

    @ComponentScan(basePackages = "com.alibaba.fastjson2.spring.issues.issue471")
    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE + 1)
    @EnableWebMvc
    public static class WebMvcConfig
            implements WebMvcConfigurer {
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            FastJsonConfig config = new FastJsonConfig();
            config.setDateFormat("yyyy-MM-dd HH:mm:ss");
            config.setCharset(StandardCharsets.UTF_8);
            config.setWriterFilters(new CustomizePropertyFilter());
            FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
            converter.setFastJsonConfig(config);
            converter.setDefaultCharset(StandardCharsets.UTF_8);
            converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
            converters.add(converter);
        }
    }

    static class CustomizePropertyFilter
            implements PropertyFilter {
        @Override
        public boolean apply(Object object, String name, Object value) {
            return true;
        }
    }

    @Data
    static class RequestData {
        private Long id;
        private String name;
    }

    @Data
    static class ResponseData<T> {
        private int code;
        private String message;
        private boolean success;
        private T data;
    }
}
