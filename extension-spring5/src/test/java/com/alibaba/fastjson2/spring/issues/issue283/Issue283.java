package com.alibaba.fastjson2.spring.issues.issue283;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import com.alibaba.fastjson2.writer.ObjectWriter;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
public class Issue283 {
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
        String requestJson = "{\"captcha\":\"test_captcha\",\"password\":\"test_password\",\"rememberMe\":true,\"username\":\"test_username\",\"uuid\":\"test_uuid\"}";
        mockMvc.perform(
                (post("/xx/xx").characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson)
                )).andExpect(status().isOk()).andDo(print());
    }

    @RestController
    @RequestMapping
    public static class BeanController {
        @PostMapping("/xx/xx")
        public @ResponseBody
        Map<String, Object> login(@RequestBody SysLoginForm form) throws IOException {
            String json = JSON.toJSONString(form);
            Map<String, Object> r = new HashMap<>();
            r.put("code", 0);
            r.put("expire", 43200);
            r.put("msg", "success");
            r.put("token", "05600918b21afba2298729d2b24c4af9");
            r.put("form", json);
            return r;
        }
    }

    @ComponentScan(basePackages = "com.alibaba.fastjson2.spring.issues.issue283")
    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE + 1)
    @EnableWebMvc
    public static class WebMvcConfig
            implements WebMvcConfigurer {
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
            FastJsonConfig fastJsonConfig = new FastJsonConfig();
            fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
            fastJsonConfig.setCharset(StandardCharsets.UTF_8);
            fastJsonConfig.setWriterFeatures(JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.PrettyFormat);
            fastConverter.setFastJsonConfig(fastJsonConfig);
            fastConverter.setDefaultCharset(StandardCharsets.UTF_8);
            fastConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
            JSON.register(BigInteger.class, ToStringObjectWriter.INSTANCE);
            JSON.register(Long.class, ToStringObjectWriter.INSTANCE);
            converters.add(0, fastConverter);
        }
    }

    public static class ToStringObjectWriter
            implements ObjectWriter<Object> {
        public static final ToStringObjectWriter INSTANCE = new ToStringObjectWriter();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
            } else {
                String strVal = object.toString();
                jsonWriter.writeString(strVal);
            }
        }
    }

    @Data
    public class SysLoginForm {
        private String username;

        private String password;

        private String captcha;

        private String uuid;

        private boolean rememberMe;
    }
}
