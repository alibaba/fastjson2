package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1073 {
    @Test
    public void testJsonOffsetFail() {
        String s = "123456789123456789123456789{//comm\n\"name\": \"www\"}";
        assertNotNull(JSON.parseObject(s, 27, 22));
        assertNotNull(JSON.parseObject(s.toCharArray(), 27, 22));
        assertNotNull(JSON.parseObject(s.getBytes(StandardCharsets.UTF_8), 27, 22));
    }

    @Test
    public void testJsonOffsetSucccess() {
        String s = "{//comm\n\"name\": \"www\"}";
        assertNotNull(JSON.parseObject(s, 0, 22));
        assertNotNull(JSON.parseObject(s.toCharArray(), 0, 22));
        assertNotNull(JSON.parseObject(s.getBytes(StandardCharsets.UTF_8), 0, 22));
    }
}
