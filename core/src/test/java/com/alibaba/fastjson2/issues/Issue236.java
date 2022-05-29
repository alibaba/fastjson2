package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue236 {
    @Test
    public void test() {
        String testJsonStr = "{\"result\":{\"puid\":\"21025318\"},\"state\":0}";
        JSONObject testJson = JSON.parseObject(testJsonStr);
        assertNotNull(testJson.getJSONObject("result"));
        assertNull(testJson.getJSONObject("result2"));
    }
}
