package com.alibaba.fastjson2.spring.issues.issue3563;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
public class Issue3563 {
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
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        provider.register(Boolean.class, new MyObjectWriter());
        provider.register(boolean.class, new MyObjectWriter());

        mockMvc.perform((get("/issue3563").characterEncoding("UTF-8")))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"testBoolean\":\"true\",\"testboolean\":\"true\"}"));
    }

    @RestController
    public static class TestController {
        @GetMapping(value = "/issue3563")
        public OrderDTO issue3563() {
            return new OrderDTO(true, true);
        }
    }

    public static class MyObjectWriter
            implements ObjectWriter {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            String str = convertToString(object);
            jsonWriter.writeString(str);
        }

        private String convertToString(Object value) {
            if (value == null) {
                return null;
            }
            if (value.getClass().isPrimitive() || value instanceof Boolean) {
                return value.toString();
            }
            return null;
        }
    }

    @Data
    @AllArgsConstructor
    public static class OrderDTO{
        private boolean testboolean;
        private Boolean testBoolean;
    }

    @ComponentScan(basePackages = "com.alibaba.fastjson2.spring.issues.issue3563")
    @Configuration
    @Order(Ordered.LOWEST_PRECEDENCE + 1)
    @EnableWebMvc
    public static class WebMvcConfig
            implements WebMvcConfigurer {
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
            FastJsonConfig fastJsonConfig = new FastJsonConfig();
            fastConverter.setFastJsonConfig(fastJsonConfig);
            fastConverter.setDefaultCharset(StandardCharsets.UTF_8);

            //Resolve content-Type cannot contain wildcard type '*
            List<MediaType> supportedMediaTypes = new ArrayList<>();
            supportedMediaTypes.add(MediaType.APPLICATION_JSON);
            fastConverter.setSupportedMediaTypes(supportedMediaTypes);

            converters.add(0, fastConverter);
        }
    }
}
