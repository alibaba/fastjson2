package com.alibaba.fastjson2.springdoc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.springdoc.OpenApiJsonWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpenApiJsonWriterTest {
    String jsonStr = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"OpenAPI definition\",\"version\":\"v0\"},\"servers\":[{\"url\":\"http://localhost:8099\",\"description\":\"Generated server url\"}],\"paths\":{},\"components\":{}}";

    @Test
    public void test() {
        OpenApiJsonWriter writer = OpenApiJsonWriter.INSTANCE;
        writer.write(JSONWriter.of(), null);
        writer.write(JSONWriter.of(), jsonStr);
    }

    @Test
    public void test1() {
        JSON.register(String.class, OpenApiJsonWriter.INSTANCE);
        assertEquals(jsonStr, JSON.toJSONString(jsonStr));
    }
}
