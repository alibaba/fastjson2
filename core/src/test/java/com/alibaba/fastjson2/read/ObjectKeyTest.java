package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectKeyTest {
    @Test
    public void test() {
        assertEquals(1, JSON.parseObject("{\"items\":{{}:{\"id\":123}}}", Bean.class).items.size());
        assertEquals(1, JSON.parseObject("{\"items\":{1:{\"id\":123}}}", Bean.class).items.size());
        assertEquals(1, JSON.parseObject("{\"items\":{true:{\"id\":123}}}", Bean.class).items.size());
        assertEquals(1, JSON.parseObject("{\"items\":{false:{\"id\":123}}}", Bean.class).items.size());
        assertEquals(1, JSON.parseObject("{\"items\":{null:{\"id\":123}}}", Bean.class).items.size());
    }

    @Test
    public void test1() {
        String s = "{items:{k1:{id:123}}}";
        assertEquals(1, JSON.parseObject(s, Bean.class, JSONReader.Feature.AllowUnQuotedFieldNames).items.size());
        byte[] bytes = s.getBytes();
        assertEquals(1, JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.UTF_8, Bean.class, JSONReader.Feature.AllowUnQuotedFieldNames).items.size());
        assertEquals(1, JSON.parseObject(bytes, Bean.class, JSONReader.Feature.AllowUnQuotedFieldNames).items.size());
        assertEquals(1, JSON.parseObject(s.toCharArray(), Bean.class, JSONReader.Feature.AllowUnQuotedFieldNames).items.size());
        assertEquals(1, ((Bean) JSON.parseObject(s.toCharArray(), (Type) Bean.class, JSONReader.Feature.AllowUnQuotedFieldNames)).items.size());
    }

    public static class Bean {
        public Map<String, Item> items;
    }

    public static class Item {
        public int id;
    }
}
