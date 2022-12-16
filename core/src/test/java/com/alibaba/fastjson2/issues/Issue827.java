package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue827 {
    @Test
    public void test() {
        String json2 = "{\n" +
                "  \"content\": \"123456\",\n" +
                "  \"type\": \"\"\n" +
                "}";
        A a = JSON.parseObject(json2, A.class);
        assertNull(a.type);

        byte[] bytes = json2.getBytes();
        A a1 = JSON.parseObject(bytes, 0, bytes.length, A.class);
        assertNull(a1.type);

        A a2 = JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.UTF_8, A.class);
        assertNull(a2.type);

        A a3 = JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.US_ASCII, A.class);
        assertNull(a3.type);
    }

    @Data
    public static class A {
        private Integer type;
        private String content;
    }
}
