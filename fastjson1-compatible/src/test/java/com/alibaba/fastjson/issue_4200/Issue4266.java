package com.alibaba.fastjson.issue_4200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class Issue4266 {
    @Data
    public static class SomeModel {
        private String id;
        @JSONField(name = "some_value")
        private int someValue;
    }

    @Test
    public void test1() {
        String json1 = "{\"id\":\"135951990146386429\",\"some_value\":1}";
        SomeModel dto1 = JSON.parseObject(json1, SomeModel.class);
        assertEquals(1, dto1.someValue);
    }

    @Test
    public void test2() {
        String json1 = "{\"id\":\"135951990146386429\",\"someValue\":1}";
        SomeModel dto1 = JSON.parseObject(json1, SomeModel.class);
        assertEquals(0, dto1.someValue);
    }
}
