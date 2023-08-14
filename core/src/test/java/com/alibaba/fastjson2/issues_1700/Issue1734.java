package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue1734 {
    final String str0 = "{\"aceess_token\": 1}";
    final String str1 = "{\"access_token\": 1}";

    @Test
    public void test() {
        {
            JSONObject object = JSON.parseObject(str0);
            assertTrue(object.containsKey("aceess_token"));
            assertEquals(1, object.keySet().size());
        }
        {
            JSONObject object = JSON.parseObject(str1);
            assertTrue(object.containsKey("access_token"));
            assertEquals(1, object.keySet().size());
        }
    }

    @Test
    public void testBytes() {
        {
            JSONObject object = JSON.parseObject(str0.getBytes());
            assertTrue(object.containsKey("aceess_token"));
            assertEquals(1, object.keySet().size());
        }
        {
            JSONObject object = JSON.parseObject(str1.getBytes());
            assertTrue(object.containsKey("access_token"));
            assertEquals(1, object.keySet().size());
        }
    }

    @Test
    public void testChars() {
        {
            JSONObject object = JSON.parseObject(str0.toCharArray());
            assertTrue(object.containsKey("aceess_token"));
            assertEquals(1, object.keySet().size());
        }
        {
            JSONObject object = JSON.parseObject(str1.toCharArray());
            assertTrue(object.containsKey("access_token"));
            assertEquals(1, object.keySet().size());
        }
    }
}
