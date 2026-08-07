package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapDeserializerTest {
    @Test
    public void test() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        HashMap map = new HashMap();
        MapDeserializer.parseMap(parser, map, String.class, Long.class, null);
        assertEquals(Long.valueOf(123), map.get("id"));
    }

    @Test
    public void test1() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        HashMap map = new HashMap();
        MapDeserializer.parseMap(parser, map, Long.class, null);
        assertEquals(Long.valueOf(123), map.get("id"));
    }

    @Test
    public void test2() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        Map map = (Map) MapDeserializer.instance.deserialze(parser, null, null);
        assertEquals(123, map.get("id"));
    }
}
