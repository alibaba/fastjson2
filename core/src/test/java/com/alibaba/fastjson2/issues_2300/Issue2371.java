package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2371 {
    @JSONType(deserializeFeatures = JSONReader.Feature.FieldBased)
    private static class Bean {
        private String testField;

        public String getTestField() {
            return testField;
        }
    }

    @Test
    public void test() {
        Bean test = JSON.parseObject("{\"testField\": \"My Test\"}", Bean.class);
        assertEquals("My Test", test.getTestField());
    }
}
