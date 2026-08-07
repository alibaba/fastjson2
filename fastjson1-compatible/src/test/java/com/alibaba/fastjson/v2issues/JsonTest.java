package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import springfox.documentation.spring.web.json.Json;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonTest {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject().fluentPut("id", 123);
        String jsonStr = jsonObject.toJSONString();

        Json json = new Json(jsonStr);
        assertEquals(jsonStr, JSON.toJSONString(json));
    }
}
