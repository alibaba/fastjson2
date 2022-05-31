package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue389 {
    @Test
    public void test() {
        byte[] utf8Bytes = "".getBytes(StandardCharsets.UTF_8);
        assertNull(JSON.parseObject(""));
        assertNull(JSON.parseObject(utf8Bytes));

        assertNull(JSON.parseObject(new ByteArrayInputStream(utf8Bytes)));
    }
}
