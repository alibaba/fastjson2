package com.alibaba.fastjson.issue_2300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2341 {
    @Test
    public void test_for_issue() throws Exception {
        String ss = "{\"@type\":\"1234\"}";
        JSONObject object = JSON.parseObject(ss);
        assertEquals("1234", object.get("@type"));
    }
}
