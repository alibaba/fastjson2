package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3381 {
    @Test
    public void test() {
        String jsonStr2 = "{    'user_Name': \"xxx\",    \"age\": \"xx\"}";
        assertNotNull(JSON.parseObject(jsonStr2, JSONField2VO.class, JSONReader.Feature.SupportSmartMatch).getName());
        assertNotNull(JSON.parseObject(jsonStr2.toCharArray(), JSONField2VO.class, JSONReader.Feature.SupportSmartMatch).getName());
        assertNotNull(JSON.parseObject(jsonStr2.getBytes(StandardCharsets.UTF_8), JSONField2VO.class, JSONReader.Feature.SupportSmartMatch).getName());
    }

    @Test
    public void test2() {
        String jsonStr2 = "{    'user_Name': \"xxx\",    \"age\": \"中文\"}";
        assertNotNull(JSON.parseObject(jsonStr2, JSONField2VO.class, JSONReader.Feature.SupportSmartMatch).getName());
        assertNotNull(JSON.parseObject(jsonStr2.toCharArray(), JSONField2VO.class, JSONReader.Feature.SupportSmartMatch).getName());
        assertNotNull(JSON.parseObject(jsonStr2.getBytes(StandardCharsets.UTF_8), JSONField2VO.class, JSONReader.Feature.SupportSmartMatch).getName());
    }

    @Data
    public static class JSONField1VO {
        @com.alibaba.fastjson.annotation.JSONField(name = "user_name")
        private String name;
        private String age;
    }

    public static class JSONField2VO {
        @JSONField(name = "user_name")
        private String name;
        private String age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }
    }
}
