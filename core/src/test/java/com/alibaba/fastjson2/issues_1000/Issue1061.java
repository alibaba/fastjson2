package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1061 {
    @BeforeEach
    public void setUp() throws Exception {
        JSON.config(JSONWriter.Feature.ReferenceDetection);
    }

    @AfterEach
    public void tearDown() throws Exception {
        JSON.config(JSONWriter.Feature.ReferenceDetection, false);
    }

    @Test
    public void test() {
        String json = "{\n" +
                "    \"success\": false,\n" +
                "    \"requestId\": \"requestId\",\n" +
                "    \"error\": [\n" +
                "        {\n" +
                "            \"code\": \"111\",\n" +
                "            \"message\": \"message\",\n" +
                "            \"longMessage\": \"--\",\n" +
                "            \"source\": \"666\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"data\": {\n" +
                "        \"requestString\": \"{\\\"scope\\\":\\\"1\\\",\\\"action\\\":\\\"1\\\",\\\"OrderNumber\\\":\\\"1\\\"}\"\n" +
                "    }\n" +
                "}";

        Bean bean = JSON.parseObject(json, Bean.class);
        assertNotNull(bean);
        assertEquals("requestId", bean.requestId);
    }

    public static class Bean {
        public boolean success;
        public String requestId;
        public String data;
        public String error;
    }
}
