package com.alibaba.fastjson.parser;

import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultJSONParserTest {
    @Test
    public void test() {
        DefaultJSONParser parser = new DefaultJSONParser("[1,2]", ParserConfig.global);
        assertNotNull(parser.getLexer());
        assertNotNull(parser.getConfig());
        List<Long> array = parser.parseArray(Long.class);
        assertEquals(1L, array.get(0));
        assertEquals(2L, array.get(1));
    }

    @Test
    public void parseObject() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        LinkedHashMap map = parser.parseObject(LinkedHashMap.class);
        assertEquals(123, map.get("id"));
    }

    @Test
    public void parseObject1() {
        DefaultJSONParser parser = new DefaultJSONParser("{\"id\":123}");
        LinkedHashMap map = parser.parseObject(new TypeReference<LinkedHashMap<String, Long>>(){}.getType());
        assertEquals(123L, map.get("id"));
        parser.close();
    }
}
