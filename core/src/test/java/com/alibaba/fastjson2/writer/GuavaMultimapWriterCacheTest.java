package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.TreeMultimap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@Tag("writer")
public class GuavaMultimapWriterCacheTest {
    @Test
    public void testLinkedListMultimapWriterCached() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        ObjectWriter w1 = provider.getObjectWriter(LinkedListMultimap.class);
        ObjectWriter w2 = provider.getObjectWriter(LinkedListMultimap.class);
        assertNotNull(w1);
        assertSame(w1, w2);
    }

    @Test
    public void testArrayListMultimapWriterCached() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        ObjectWriter w1 = provider.getObjectWriter(ArrayListMultimap.class);
        ObjectWriter w2 = provider.getObjectWriter(ArrayListMultimap.class);
        assertNotNull(w1);
        assertSame(w1, w2);
    }

    @Test
    public void testHashMultimapWriterCached() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        ObjectWriter w1 = provider.getObjectWriter(HashMultimap.class);
        ObjectWriter w2 = provider.getObjectWriter(HashMultimap.class);
        assertNotNull(w1);
        assertSame(w1, w2);
    }

    @Test
    public void testLinkedHashMultimapWriterCached() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        ObjectWriter w1 = provider.getObjectWriter(LinkedHashMultimap.class);
        ObjectWriter w2 = provider.getObjectWriter(LinkedHashMultimap.class);
        assertNotNull(w1);
        assertSame(w1, w2);
    }

    @Test
    public void testTreeMultimapWriterCached() {
        ObjectWriterProvider provider = new ObjectWriterProvider();
        ObjectWriter w1 = provider.getObjectWriter(TreeMultimap.class);
        ObjectWriter w2 = provider.getObjectWriter(TreeMultimap.class);
        assertNotNull(w1);
        assertSame(w1, w2);
    }

    @Test
    public void testLinkedListMultimapSerialize() {
        LinkedListMultimap<String, String> map = LinkedListMultimap.create();
        map.put("a", "1");
        map.put("a", "2");
        map.put("b", "3");
        String json = JSON.toJSONString(map);
        assertEquals("{\"a\":[\"1\",\"2\"],\"b\":[\"3\"]}", json);

        String json2 = JSON.toJSONString(map);
        assertEquals(json, json2);
    }

    @Test
    public void testArrayListMultimapSerialize() {
        ArrayListMultimap<String, Integer> map = ArrayListMultimap.create();
        map.put("k1", 1);
        map.put("k1", 2);
        String json = JSON.toJSONString(map);
        assertEquals("{\"k1\":[1,2]}", json);

        String json2 = JSON.toJSONString(map);
        assertEquals(json, json2);
    }

    @Test
    public void testHashMultimapSerialize() {
        HashMultimap<String, String> map = HashMultimap.create();
        map.put("k", "v");
        String json = JSON.toJSONString(map);
        assertEquals("{\"k\":[\"v\"]}", json);
    }

    @Test
    public void testLinkedHashMultimapSerialize() {
        LinkedHashMultimap<String, String> map = LinkedHashMultimap.create();
        map.put("a", "1");
        map.put("a", "2");
        map.put("b", "3");
        String json = JSON.toJSONString(map);
        assertEquals("{\"a\":[\"1\",\"2\"],\"b\":[\"3\"]}", json);
    }

    @Test
    public void testTreeMultimapSerialize() {
        TreeMultimap<String, String> map = TreeMultimap.create();
        map.put("b", "2");
        map.put("a", "1");
        String json = JSON.toJSONString(map);
        assertEquals("{\"a\":[\"1\"],\"b\":[\"2\"]}", json);
    }
}
