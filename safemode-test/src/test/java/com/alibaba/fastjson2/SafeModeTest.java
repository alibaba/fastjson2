package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SafeModeTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        String str = JSON.toJSONString(bean, JSONWriter.Feature.WriteClassName);
        Object object = JSON.parse(str);
        assertTrue(object instanceof Map);
        assertTrue(JSON.parse(str, JSONReader.Feature.SupportAutoType) instanceof Map);
    }
    public static class Bean {
    }
    @Test
    public void test1() {
        IOException ex = new IOException();
        String jsonString = JSON.toJSONString(ex, JSONWriter.Feature.WriteClassName);
        Throwable e1 = JSON.parseObject(jsonString, Throwable.class);
        assertEquals(Throwable.class, e1.getClass());
        JSONObject object = JSON.parseObject(jsonString);
        Throwable e2 = object.toJavaObject(Throwable.class);
        assertEquals(Throwable.class, e2.getClass());
    }
}
