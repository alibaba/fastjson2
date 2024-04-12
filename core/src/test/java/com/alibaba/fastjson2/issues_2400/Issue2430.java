package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Issue2430 {
    @Test
    void test() {
        Map.Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<>(1, 2);
        String jsonString = JSON.toJSONString(entry, JSONWriter.Feature.WriteNonStringKeyAsString);
        assertEquals("{\"1\":2}",jsonString);
        jsonString = JSON.toJSONString(entry, JSONWriter.Feature.BrowserCompatible);
        assertEquals("{\"1\":2}",jsonString);
        jsonString = JSONB.toJSONString(JSONB.toBytes(entry, JSONWriter.Feature.WriteNonStringKeyAsString));
        assertEquals("[\n" +
                "\t\"1\",\n" +
                "\t2\n" +
                "]",jsonString);
        jsonString = JSONB.toJSONString(JSONB.toBytes(entry, JSONWriter.Feature.BrowserCompatible));
        assertEquals("[\n" +
                "\t\"1\",\n" +
                "\t2\n" +
                "]",jsonString);
    }
}
