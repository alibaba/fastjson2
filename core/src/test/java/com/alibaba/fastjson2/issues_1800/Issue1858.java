package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1858 {
    @Test
    public void test() {
        JSONObject object = new JSONObject();
        for (int i = 0; i < 100000; i++) {
            object.put("v" + i, 1694541292077L);
        }
        String str = JSON.toJSONString(object);
        String str1 = new String(JSON.toJSONBytes(object), StandardCharsets.ISO_8859_1);
        assertEquals(str, str1);
    }
}
