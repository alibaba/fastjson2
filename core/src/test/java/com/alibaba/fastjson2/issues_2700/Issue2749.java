package com.alibaba.fastjson2.issues_2700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2_vo.Int1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Issue2749 {
    @Test
    public void test() {
        String str = "{\"v0000\":\"Alexander77\"}";
        {
            JSONException error = null;
            try {
                JSON.parseObject(str, Int1.class);
            } catch (JSONException e) {
                error = e;
            }
            assertNotNull(error);
            assertTrue(error.getMessage().contains("parseInt error"));
            assertTrue(error.getMessage().contains("Alexander77"));
        }
        {
            JSONException error = null;
            try {
                JSON.parseObject(str.toCharArray(), Int1.class);
            } catch (JSONException e) {
                error = e;
            }
            assertNotNull(error);
            assertTrue(error.getMessage().contains("parseInt error"));
            assertTrue(error.getMessage().contains("Alexander77"));
        }
        {
            JSONException error = null;
            try {
                JSON.parseObject(str.getBytes(), Int1.class);
            } catch (JSONException e) {
                error = e;
            }
            assertNotNull(error);
            assertTrue(error.getMessage().contains("parseInt error"));
            assertTrue(error.getMessage().contains("Alexander77"));
        }
    }
}
