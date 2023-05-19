package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1487 {
    @Test
    public void test() {
        String str = "{'P1Q1': {1.0: '选项 1', 2.0: '选项 2', 3.0: '选项 3'}}";
        JSONObject jsonObject = JSON.parseObject(str);
        String expected = "{\"P1Q1\":{1.0:\"选项 1\",2.0:\"选项 2\",3.0:\"选项 3\"}}";
        assertEquals(expected, jsonObject.toJSONString());
        assertEquals(expected, new String(JSON.toJSONBytes(jsonObject), StandardCharsets.UTF_8));
    }
}
