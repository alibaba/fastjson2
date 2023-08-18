package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1763 {
    @Test
    public void test() {
        String str = "\"\"";
        assertNull(JSON.parseObject(str, UUID.class));
        assertNull(JSON.parseObject(str.toCharArray(), UUID.class));
        assertNull(JSON.parseObject(str.getBytes(), UUID.class));
    }
}
