package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue485 {
    @Test
    public void test() {
        String testData = "{\"Status\": \"200\"}";
        TestMO responseMO = JSON.parseObject(testData, TestMO.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals("200", responseMO.Status);
    }

    @Getter
    @Setter
    @ToString
    public class TestMO {
        private String Status;
    }
}
