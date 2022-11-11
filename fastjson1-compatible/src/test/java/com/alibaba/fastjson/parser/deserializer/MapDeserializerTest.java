package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapDeserializerTest {
    @Test
    public void test() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        HashMap map = new HashMap();
        MapDeserializer.parseMap(parser, map, String.class, Long.class, null);
        assertEquals(Long.valueOf(123), map.get("id"));
    }
}
