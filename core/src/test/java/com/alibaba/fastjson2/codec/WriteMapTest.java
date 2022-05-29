package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteMapTest {
    @Test
    public void test_HashMap() {
        assertEquals("{}", JSON.toJSONString(new HashMap<>()));
    }

    @Test
    public void test_emptyMap() {
        assertEquals("{}", JSON.toJSONString(Collections.emptyMap()));
    }

    @Test
    public void test_singletonMap() {
        assertEquals("{\"id\":101}", JSON.toJSONString(Collections.singletonMap("id", 101)));
    }

    @Test
    public void test_unmodifiableMap() {
        assertEquals("{}", JSON.toJSONString(Collections.unmodifiableMap(new HashMap<>())));
    }
}
