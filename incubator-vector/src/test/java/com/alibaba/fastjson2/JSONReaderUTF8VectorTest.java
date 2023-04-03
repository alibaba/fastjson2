package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReaderUTF8VectorTest {
    @Test
    public void test() {
        JSONReader.Context ctx = JSONFactory.createReadContext();
        String str = "abcdef1234567890中国";
        String json = JSON.toJSONString(str);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        JSONReaderUTF8Vector jsonReader = new JSONReaderUTF8Vector(ctx, json, bytes, 0, bytes.length);
        String parsed = jsonReader.readString();
        assertEquals(str, parsed);
    }
}
