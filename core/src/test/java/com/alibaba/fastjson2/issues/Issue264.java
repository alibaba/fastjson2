package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue264 {
    @Test
    public void test() {
        String str = "// 首行的注释\n" +
                "// 首行的注释\n" +
                "{\n" +
                "  // 此处的注释没有问题\n" +
                "  \"type_number\": 1,\n" +
                "  \"type_boolean\": true,\n" +
                "  \"type_string\": \"abcd\",\n" +
                "  \"type_object\": {\n" +
                "    \"o1_1\": \"555\",\n" +
                "    \"o1_2\": \"abcd\"\n" +
                "  }\n" +
                "}";

        assertEquals(
                1,
                JSON.parseObject(str)
                        .get("type_number")
        );

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        assertEquals(
                1,
                JSON.parseObject(bytes)
                        .get("type_number")
        );

        assertEquals(
                1,
                JSON.parseObject(bytes, 0, bytes.length)
                        .get("type_number")
        );

        assertEquals(
                1,
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.US_ASCII)
                        .get("type_number")
        );

        byte[] utf16Bytes = str.getBytes(StandardCharsets.UTF_16);
        assertEquals(
                1,
                JSON.parseObject(utf16Bytes, 0, utf16Bytes.length, StandardCharsets.UTF_16)
                        .get("type_number")
        );

        JSONReader jsonReaderStr = TestUtils.createJSONReaderStr(str);
        assertEquals(
                1, jsonReaderStr.read(JSONObject.class).get("type_number"));
    }
}
