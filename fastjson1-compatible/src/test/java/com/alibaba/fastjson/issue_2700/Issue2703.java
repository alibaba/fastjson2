package com.alibaba.fastjson.issue_2700;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue2703 {
    @Test
    public void test_for_issue() {
        Object a = JSONObject.toJavaObject(new JSONObject(), JSON.class);
        assertTrue(a instanceof JSONObject);

        Object b = new JSONObject().toJavaObject(JSON.class);
        assertTrue(b instanceof JSONObject);

        Object c = JSONObject.toJavaObject(new JSONArray(), JSON.class);
        assertTrue(c instanceof JSONArray);
    }
}
