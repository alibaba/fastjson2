package com.alibaba.fastjson2.support.springfox;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import springfox.documentation.spring.web.json.Json;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonTest {
    @Test
    public void test() {
        JSONObject jsonObject = JSONObject.of("id", 123);
        String jsonStr = jsonObject.toJSONString();

        Json json = new Json(jsonStr);
        assertEquals(jsonStr, JSON.toJSONString(json));
    }
}
