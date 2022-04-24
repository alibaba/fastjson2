package com.alibaba.fastjson.issue_1600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1633 {
    @Test
    public void test_for_issue_int() throws Exception {
        String text = "{123:\"abc\"}";
        JSONObject obj = JSON.parseObject(text, Feature.NonStringKeyAsString);
        assertEquals("abc", obj.getString("123"));
    }

    @Test
    public void test_for_issue_bool() throws Exception {
        String text = "{false:\"abc\"}";
        JSONObject obj = JSON.parseObject(text, Feature.NonStringKeyAsString);
        assertEquals("abc", obj.getString("false"));
    }
}
