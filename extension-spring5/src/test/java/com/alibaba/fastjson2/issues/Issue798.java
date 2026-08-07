package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class Issue798 {
    @Autowired
    Config configs;

    @Test
    public void test() {
        assertEquals("{}", JSON.toJSONString(configs));
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "config")
    public static class Config {
        public String[] deviceCodes;
        public String barkPushUrl;
        public String barkPushToken;
        public String location;
        public List<String> storeNameWhiteList;
    }
}
