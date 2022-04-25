package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue80 {
    @Test
    public void test_issue() {
        String str = "{\"id\":123,\"姓名\":{\"location\":{\"top\":277,\"left\":618,\"width\":372,\"height\":164},\"words\":\"张三\"},\"log_id\":1453528763137919643}";

        JSONPath jsonPath = JSONPath.of("log_id");
        JSONReader parser = JSONReader.of(str);
        assertEquals(1453528763137919643L, jsonPath.extract(parser));
    }
}
