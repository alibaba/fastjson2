package com.alibaba.fastjson2.aliyun;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapGhostTest {
    @Test
    public void test() {
//        MapGhost<String, Object> map = new MapGhost<String, Object>("abc");
//        map.put("name", 123);
//        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
//        byte[] bytes1 = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
//        assertArrayEquals(bytes, bytes1);
//
//        System.out.println(JSONB.toJSONString(bytes));
//
//        MapGhost mapGhost = (MapGhost) JSONB.parseObject(
//                bytes, Object.class,
//                JSONReader.Feature.FieldBased,
//                JSONReader.Feature.SupportAutoType
//        );
//        assertEquals(map.name, mapGhost.name);
//        assertEquals(map.get("name"), mapGhost.get("name"));
    }

    public static class MapGhost<K, V>
            extends HashMap<K, V> {
        private String name;

        public MapGhost(String name) {
            this.name = name;
        }
    }

    @Test
    public void test1() {
        MapGhost1<String, Object> map = new MapGhost1<String, Object>("abc");
        map.put("name", 123);
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);

        MapGhost1 mapGhost = JSONB.parseObject(bytes, MapGhost1.class, JSONReader.Feature.FieldBased, JSONReader.Feature.SupportAutoType);
        assertEquals(map.name, mapGhost.name);
        assertEquals(map.get("name"), mapGhost.get("name"));
    }

    public static class MapGhost1<K, V>
            extends LinkedHashMap<K, V> {
        private String name;

        public MapGhost1(String name) {
            this.name = name;
        }
    }

    @Test
    public void test2() {
        MapGhost2<String, Object> map = new MapGhost2<String, Object>("abc");
        map.put("name", 123);
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);

        MapGhost2 mapGhost = JSONB.parseObject(bytes, MapGhost2.class, JSONReader.Feature.FieldBased, JSONReader.Feature.SupportAutoType);
        assertEquals(map.name, mapGhost.name);
        assertEquals(map.get("name"), mapGhost.get("name"));
    }

    public static class MapGhost2<K, V>
            extends TreeMap<K, V> {
        private String name;

        public MapGhost2(String name) {
            this.name = name;
        }
    }
}
