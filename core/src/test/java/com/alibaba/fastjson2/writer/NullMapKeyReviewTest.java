package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NullMapKeyReviewTest {
    @Test
    public void jsonbSupportsNullKeysWithNullAndNonNullValues() {
        for (Object value : new Object[] {null, 123, new LinkedHashMap<>()}) {
            Map<Object, Object> map = new LinkedHashMap<>();
            map.put(null, value);
            byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.ReferenceDetection);
            assertEquals(map, JSONB.parseObject(bytes, Map.class));
        }
    }
}
