package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapEntryTest {
    static Map map = Collections.singletonMap("id", 101);
    static Map.Entry entry = (Map.Entry) map.entrySet().stream().findFirst().get();

    @Test
    public void test_str() {
        String str = JSON.toJSONString(entry);
        assertEquals("{\"id\":101}", str);
        Map.Entry entry = JSON.parseObject(str, Map.Entry.class);
        assertEquals("id", entry.getKey());
        assertEquals(101, entry.getValue());
        entry.setValue(102);
        assertEquals(102, entry.getValue());
    }

    @Test
    public void test_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(entry);
        Map.Entry entry = JSONB.parseObject(jsonbBytes, Map.Entry.class);
        assertEquals("id", entry.getKey());
        assertEquals(101, ((Number) entry.getValue()).intValue());
        entry.setValue(102);
        assertEquals(102, entry.getValue());
    }
}
