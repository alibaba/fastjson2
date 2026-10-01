package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ParameterizedTypeEqualityTest {
    public List<String> strings;

    @Test
    public void reflectionEqualityAndHashCode() throws Exception {
        ParameterizedType reflected = (ParameterizedType) getClass().getField("strings").getGenericType();
        ParameterizedType created = new ParameterizedTypeImpl(List.class, String.class);
        assertEquals(reflected, created);
        assertEquals(created, reflected);
        assertEquals(reflected.hashCode(), created.hashCode());
        Map<Type, String> cache = new HashMap<>();
        cache.put(reflected, "found");
        assertEquals("found", cache.get(created));
        cache.clear();
        cache.put(created, "found");
        assertEquals("found", cache.get(reflected));
        assertNotEquals(created, new ParameterizedTypeImpl(List.class, Integer.class));
        assertNotEquals(created, List.class);
    }
}
