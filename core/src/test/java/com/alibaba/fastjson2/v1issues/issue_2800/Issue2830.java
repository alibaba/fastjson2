package com.alibaba.fastjson2.v1issues.issue_2800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2830 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject jsonObject = JSON.parseObject("{\"qty\":\"10\",\"qty1\":\"10.0\",\"qty2\":\"10.000\"}");

        assertEquals(10, jsonObject.getIntValue("qty"));
        assertEquals(10, jsonObject.getIntValue("qty1"));
        assertEquals(10, jsonObject.getIntValue("qty2"));

        assertEquals(10, jsonObject.getInteger("qty"));
        assertEquals(10, jsonObject.getInteger("qty1"));
        assertEquals(10, jsonObject.getInteger("qty2"));

        assertEquals(10, jsonObject.getLongValue("qty"));
        assertEquals(10, jsonObject.getLongValue("qty1"));
        assertEquals(10, jsonObject.getLongValue("qty2"));

        assertEquals(10, jsonObject.getLong("qty"));
        assertEquals(10, jsonObject.getLong("qty1"));
        assertEquals(10, jsonObject.getLong("qty2"));
    }
}
