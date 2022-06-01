package com.alibaba.fastjson2.springfox;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import springfox.documentation.spring.web.json.Json;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SwaggerJsonWriterTest {
    String jsonStr = "{\"abc\":\"cde中文\"}";

    @Test
    public void test() {
        assertEquals(jsonStr, JSON.toJSONString(new Json(jsonStr)));
    }

    @Test
    public void test1() {
        assertEquals(jsonStr, new String(JSON.toJSONBytes(new Json(jsonStr))));
    }
}
