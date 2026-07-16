package com.alibaba.fastjson2.spring.issues.issue3912;

import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
public class Issue3912 {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    /**
     * 测试场景 1: 数据合法，应该返回 200 OK
     */
    @Test
    public void testSuccess() throws Exception {
        String requestJson = "{\"username\":\"admin_user\",\"age\":20}";
        mockMvc.perform(
                        (post("/issue3912")
                                .characterEncoding("UTF-8")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(requestJson)
                        ))
                .andExpect(status().isOk())
                .andExpect(content().string("\"success\""));
    }

    /**
     * 测试场景 2: 数据包含多个错误，应该返回 400 Bad Request，并包含所有错误信息
     * 错误 1: username 长度不够 (min 5)
     * 错误 2: age 未满 18 (min 18)
     */
    @Test
    public void testValidationFail() throws Exception {
        String requestJson = "{\"username\":\"a\",\"age\":10}";
        mockMvc.perform(
                        (post("/issue3912")
                                .characterEncoding("UTF-8")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(requestJson)
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("用户名长度不能小于5")))
                .andExpect(content().string(containsString("必须年满18岁")));
    }

    @RestController
    public static class TestController {
        @PostMapping(value = "/issue3912", produces = "application/json")
        public String issue3912(@RequestBody UserBean user) {
            return "success";
        }
    }

    @Data
    public static class UserBean {
        private String username;
        private Integer age;
    }

    /**
     * 自定义异常，用于携带校验错误信息
     */
    public static class SchemaValidationException extends RuntimeException {
        private final List<String> errors;

        public SchemaValidationException(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    /**
     * 在 JSON 反序列化成对象后，Controller 执行前，进行 Schema 校验
     */
    @RestControllerAdvice
    public static class SchemaValidationAdvice extends RequestBodyAdviceAdapter {
        private static final JSONSchema USER_SCHEMA = JSONSchema.parseSchema("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"username\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"minLength\": 5,\n" +
                "      \"error\": \"用户名长度不能小于5\"\n" + // 自定义错误消息
                "    },\n" +
                "    \"age\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"minimum\": 18,\n" +
                "      \"error\": \"必须年满18岁\"\n" + // 自定义错误消息
                "    }\n" +
                "  }\n" +
                "}");

        @Override
        public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
            return targetType == UserBean.class; // 仅拦截 UserBean
        }

        @Override
        public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
            if (body instanceof UserBean) {
                List<String> errors = new ArrayList<>();

                USER_SCHEMA.validate(body, (schema, value, message, path) -> {
                    errors.add(path + ": " + message);
                    return true;
                });

                if (!errors.isEmpty()) {
                    throw new SchemaValidationException(errors);
                }
            }
            return body;
        }
    }

    @RestControllerAdvice
    public static class GlobalExceptionHandler {
        @ExceptionHandler(SchemaValidationException.class)
        public ResponseEntity<Map<String, Object>> handleException(SchemaValidationException ex) {
            Map<String, Object> body = new HashMap<>();
            body.put("errors", ex.getErrors());
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
    }

    @ComponentScan(basePackages = "com.alibaba.fastjson2.spring.issues.issue3912")
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
            List<MediaType> supportedMediaTypes = new ArrayList<>();
            supportedMediaTypes.add(MediaType.APPLICATION_JSON);
            fastConverter.setSupportedMediaTypes(supportedMediaTypes);
            converters.add(0, fastConverter);
        }
    }
}
