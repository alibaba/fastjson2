package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue2350 {
    @Test
    public void test() {
        String json = "{\n" +
                "\t\"testField\": \"My Test\",\n" +
                "\t\"testField2\": \"My Test2\"\n" +
                "}";
        TestClass testClass = JSON.parseObject(json, TestClass.class);
        assertEquals("My Test", testClass.getTestField());
        assertNull(testClass.getTestField2());
    }

    public static class TestClass {
        @JSONField(deserializeFeatures = { JSONReader.Feature.FieldBased })
        private String testField;
        private String testField2;

        public String getTestField() {
            return testField;
        }
        public String getTestField2() {
            return testField2;
        }
    }
}
