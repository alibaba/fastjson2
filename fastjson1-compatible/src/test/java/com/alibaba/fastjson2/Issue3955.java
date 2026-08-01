package com.alibaba.fastjson2;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3955 {
    @Data
    private static class DoubleBean {
        private Double test;
    }

    @Test
    public void test() {
        String json = "{\"test\":1.2E-4D}";
        DoubleBean doubleBean = JSON.parseObject(json, DoubleBean.class);
        assertEquals((Double) 1.2E-4, doubleBean.getTest());
    }
}
