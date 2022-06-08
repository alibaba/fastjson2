package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONTest {
    @Test
    public void test() {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        assertEquals(HashMap.class, JSON.parseObject(bytes).getInnerMap().getClass());
        assertEquals(LinkedHashMap.class, JSON.parseObject(bytes, Feature.OrderedField).getInnerMap().getClass());
    }

    @Test
    public void test1() {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        assertEquals(HashMap.class, JSON.parseObject(str).getInnerMap().getClass());
        assertEquals(LinkedHashMap.class, JSON.parseObject(str, Feature.OrderedField).getInnerMap().getClass());
    }
}
