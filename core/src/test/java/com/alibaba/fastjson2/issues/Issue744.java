package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue744 {
    @Test
    public void test() {
        URL url = this.getClass().getClassLoader().getResource("issue744.json");
        JSONObject map = JSON.parseObject(url);
        byte[] jsonbBytes = JSONB.toBytes(map);
        JSONObject map2 = JSONB.parseObject(jsonbBytes);
        assertEquals(map, map2);
    }
}
