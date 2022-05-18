package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue273 {
    @Test
    public void test() {
        Map<Integer, Long> map = new HashMap<>();
        map.put(1, 111L);
        map.put(2, 222L);
        String json = JSON.toJSONString(map, JSONWriter.Feature.WriteNonStringValueAsString);

        Type type = new TypeReference<HashMap<Integer, Long>>() {}.getType();
        Map<Integer, Long> parseObject = JSON.parseObject(json, type);

        assertEquals(111L, parseObject.get(1));
        assertEquals(222L, parseObject.get(2));
    }
}
