package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue614 {
    @Test
    public void test() {
        final Map<String, Object> paraMap = new HashMap<>();
        String[] value = new String[] {"x", "y"};
        paraMap.put("s", value);
        final byte[] bytes = JSONB.toBytes(paraMap, JSONWriter.Feature.WriteClassName);

        final Map<String, Object> map = JSONB.parseObject(bytes, Map.class, JSONReader.Feature.SupportAutoType);
        String[] strings = ((String[]) map.getOrDefault("s", ""));
        assertEquals(2, strings.length);
        assertEquals("x", strings[0]);
        assertEquals("y", strings[1]);
    }
}
