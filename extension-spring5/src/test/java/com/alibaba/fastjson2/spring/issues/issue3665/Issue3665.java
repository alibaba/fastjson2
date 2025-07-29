package com.alibaba.fastjson2.spring.issues.issue3665;

import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import lombok.AllArgsConstructor;
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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
public class Issue3665 {
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
        String requestJson = "{\"address\":\"中国\",\"kwhIncome\": \"\"}";
        mockMvc.perform(
                (post("/issue").characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestJson)
                )).andExpect(status().isOk());
    }

    @RestController
    public static class TestController {
        @PostMapping(value = "/issue", produces = "application/json")
        public void issue(@RequestBody DeserializeBugDTO dto) {
            assertEquals(dto, "{\"address\":\"中国\",\"kwhIncome\": \"\"}");
        }
    }

    @Data
    @AllArgsConstructor
    public static class DeserializeBugDTO {
        private String address;
        private BigDecimal kwhIncome;
    }

    @ComponentScan(basePackages = "com.alibaba.fastjson2.spring.issues.issue3665")
    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE + 1)
    @EnableWebMvc
    public static class WebMvcConfig
            implements WebMvcConfigurer {
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//            FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
//            FastJsonConfig fastJsonConfig = new FastJsonConfig();
//            fastConverter.setFastJsonConfig(fastJsonConfig);
//            fastConverter.setDefaultCharset(StandardCharsets.UTF_8);
//            converters.add(0, fastConverter);
            converters.add(new FastJsonHttpMessageConverter());
        }
    }
}
