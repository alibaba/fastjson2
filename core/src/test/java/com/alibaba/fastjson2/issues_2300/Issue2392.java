package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2392 {
    @Test
    public void test() throws Exception {
        Class<?> clazz = Class.forName("kotlin.collections.EmptyList");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object object = constructor.newInstance();
        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName);
        Object parsed = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed.getClass());
    }
}
