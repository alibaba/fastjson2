package com.alibaba.fastjson2.issues_3500;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3569 {
    @Test
    public void test() {
        TestClass<String> testClass1 = new TestClass<>("hello");
        JSONObject jsonObject = JSONObject.from(testClass1);
        assertEquals(jsonObject.toString(), "{\"generic\":\"hello\"}");

        TestClass<TestData> testClass2 = new TestClass<>(new TestData("hello"));
        jsonObject = JSONObject.from(testClass2);
        assertEquals(jsonObject.toString(), "{\"generic\":{\"testData\":\"hello\"}}");
    }

    @Data
    @AllArgsConstructor
    public static class TestClass<T> {
        private T generic;
    }

    @Data
    @AllArgsConstructor
    public static class TestData {
        private String testData;
    }
}
