package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public static class OneFieldBean {
        public int id;
    }

    public static class SideEffect {
        static volatile boolean created;

        public SideEffect() {
            created = true;
        }
    }

    @Test
    public void testHashCacheDoesNotBypassSafeMode() {
        String typeName = Bean.class.getName();
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.registerIfAbsent(
                Fnv.hashCode64(typeName),
                provider.getObjectReader(Bean.class)
        );

        Object object = JSON.parseObject(
                "{\"@type\":\"" + typeName + "\"}",
                Object.class,
                JSONFactory.createReadContext(provider, JSONReader.Feature.SupportAutoType)
        );
        assertInstanceOf(Map.class, object);
    }

    @Test
    public void testConcreteBeanHashCacheDoesNotBypassSafeMode() {
        String unauthorized = "com.example.Unauthorized";
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.registerIfAbsent(
                Fnv.hashCode64(unauthorized),
                provider.getObjectReader(SideEffect.class)
        );

        SideEffect.created = false;
        try {
            JSON.parseObject(
                    "{\"@type\":\"" + unauthorized + "\",\"id\":1}",
                    OneFieldBean.class,
                    JSONFactory.createReadContext(provider, JSONReader.Feature.SupportAutoType)
            );
        } catch (JSONException ignored) {
        }
        assertFalse(SideEffect.created);
    }

    @Test
    public void test1() {
        IOException ex = new IOException();
        String jsonString = JSON.toJSONString(ex, JSONWriter.Feature.WriteClassName);
        Throwable e1 = JSON.parseObject(jsonString, Throwable.class);
        assertEquals(Throwable.class, e1.getClass());
        JSONObject object = JSON.parseObject(jsonString);
        assertEquals(Throwable.class, object.toJavaObject(Throwable.class).getClass());
    }
}
