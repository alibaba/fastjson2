package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1459 {
    @Test
    public void test() {
        String json = null;
        List<String> strings = JSON.parseArray(json, String.class);
        assertNull(strings);
    }
}
