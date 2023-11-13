package com.alibaba.fastjson2.issues_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1995 {
    @Test
    public void test() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        map.put(3, 3);

        String json = JSON.toJSONString(map, JSONWriter.Feature.WriteNonStringKeyAsString);
        Map<Integer, Integer> resMap = jsonToMap(json, Integer.class, Integer.class);
        resMap.put(4, 4);
        assertEquals(4, resMap.size());
    }

    private static <K, V> Map<K, V> jsonToMap(String json, Class<K> keyType, Class<V> valueType) {
        return JSON.parseObject(json, new TypeReference<Map<K, V>>(keyType, valueType) { });
    }
}
