package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue983 {
    @Test
    public void test2() {
        String s = "{\"OneDemo\":\"1\",\"TDemo\":\"2\",\"ThDemo\":\"3\"}";
        TestModel testModel = JSON.parseObject(s, TestModel.class);
        assertEquals("1", testModel.OneDemo);
        assertEquals("2", testModel.TDemo);
        assertEquals("3", testModel.ThDemo);
    }

    @Setter
    class TestModel {
        private String OneDemo;
        private String TDemo;
        private String ThDemo;
    }
}
