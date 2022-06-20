package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    public void testNull() throws Exception {
        assertNull(JSON.parseObject((InputStream) null, Object.class));
    }

    @Test
    public void testInputStream() throws Exception {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        Bean bean = JSON.parseObject(new ByteArrayInputStream(utf8), Bean.class);
        assertEquals(123, bean.id);
        assertEquals("wenshao", bean.name);
    }

    @Test
    public void testCharArray() throws Exception {
        String str = "{\"id\":123,\"name\":\"wenshao\"}";
        Bean bean = JSON.parseObject(str.toCharArray(), Bean.class);
        assertEquals(123, bean.id);
        assertEquals("wenshao", bean.name);
    }

    public static class Bean {
        public int id;
        public String name;
    }
}
