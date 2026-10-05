package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypedMapReviewTest {
    @Test
    public void objectKeysAndNullKeysSurviveConversion() {
        Map<Object, Integer> input = new LinkedHashMap<>();
        input.put(123, 1);
        input.put(null, 2);
        Type type = new TypeReference<Map<Object, Integer>>() { }.getType();
        ObjectReader<Map<Object, Integer>> reader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(type);
        assertEquals(input, reader.createInstance(input));
    }

    @Test
    public void stringKeyConversionRetainsNullKey() {
        Map<Object, Integer> input = new LinkedHashMap<>();
        input.put(123, 1);
        input.put(null, 2);
        Type type = new TypeReference<Map<String, Integer>>() { }.getType();
        Map<?, ?> converted = (Map<?, ?>) JSONFactory.getDefaultObjectReaderProvider().getObjectReader(type).createInstance(input);
        assertEquals(1, converted.get("123"));
        assertEquals(2, converted.get(null));
    }

    @Test
    public void jsonbIgnoresNullBeforeInsertingIntoConcurrentMap() {
        Map<String, Integer> input = new LinkedHashMap<>();
        input.put("value", null);
        Type type = new TypeReference<ConcurrentHashMap<String, Integer>>() { }.getType();
        Map<?, ?> converted = JSONB.parseObject(JSONB.toBytes(input), type, JSONReader.Feature.IgnoreNullPropertyValue);
        assertTrue(converted.isEmpty());
    }
}
