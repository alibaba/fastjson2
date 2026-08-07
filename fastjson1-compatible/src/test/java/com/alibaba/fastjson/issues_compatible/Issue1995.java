package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
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

        String json = JSON.toJSONString(map, SerializerFeature.WriteNonStringKeyAsString);
        Map<Integer, Integer> resMap = jsonToMap(json, Integer.class, Integer.class);
        resMap.put(4, 4);
        assertEquals(4, resMap.size());
    }

    private static <K, V> Map<K, V> jsonToMap(String json, Class<K> keyType, Class<V> valueType) {
        TypeReference<Map<K, V>> typeReference = new TypeReference<Map<K, V>>(keyType, valueType) {};
        return JSON.parseObject(json, typeReference);
    }
}
