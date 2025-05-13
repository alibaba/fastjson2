package com.alibaba.fastjson2.spring.issues.issue3539;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
public class Issue3539 {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    public void test() throws Exception {
        String requestJson = "{\"date\":\"\",\"abc\":false,\"def\":\"def\"}";
        mockMvc.perform(
                (post("/issue3539").characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson)
                )).andExpect(status().isOk());
    }

    @RestController
    public static class TestController {
        @PostMapping(value = "/issue3539", produces = {"application/json"})
        public void issue3539(@RequestBody TestData testData) {
            assertEquals(testData.abc, false);
        }
    }

    @ComponentScan(basePackages = "com.alibaba.fastjson2.spring.issues.issue3539")
    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE + 1)
    @EnableWebMvc
    public static class WebMvcConfig implements WebMvcConfigurer {
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
            FastJsonConfig fastJsonConfig = new FastJsonConfig();
            fastConverter.setFastJsonConfig(fastJsonConfig);
            fastConverter.setDefaultCharset(StandardCharsets.UTF_8);
            converters.add(0, fastConverter);
        }
    }

    @Data
    public class TestData {
        private LocalDateTime date;

        private Boolean abc;

        private String def;
    }
}
