package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1867 {
    @Test
    public void test() {
        assertNull(JSON.parseObject("null", Map.class));
    }
}
