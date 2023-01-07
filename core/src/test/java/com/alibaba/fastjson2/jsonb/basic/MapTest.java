package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapTest {
    /**
     * 0xa6 0xa5
     */
    @Test
    public void testEmpty() {
        HashMap map = new HashMap();
        byte[] bytes = JSONB.toBytes(map);
        assertEquals(2, bytes.length);
        assertEquals(BC_OBJECT, bytes[0]);
        assertEquals(BC_OBJECT_END, bytes[1]);

        Map parsed = (Map) JSONB.parse(bytes);
        assertEquals(0, parsed.size());
    }

    /**
     * 0xa6 key value 0xa5
     */
    @Test
    public void testSingle() {
        HashMap map = new HashMap();

        String key = "id";
        int value = 1;

        map.put(key, value);

        byte[] bytes = JSONB.toBytes(map);
        assertEquals(6, bytes.length);
        assertEquals(BC_OBJECT, bytes[0]);
        assertEquals(BC_OBJECT_END, bytes[bytes.length - 1]);

        byte[] keyBytes = JSONB.toBytes(key);
        byte[] valueBytes = JSONB.toBytes(value);

        assertEquals(bytes.length, 2 + keyBytes.length, valueBytes.length);
        for (int i = 0; i < keyBytes.length; i++) {
            assertEquals(bytes[i + 1], keyBytes[i]);
        }

        for (int i = 0; i < valueBytes.length; i++) {
            assertEquals(bytes[i + 1 + keyBytes.length], valueBytes[i]);
        }

        Map parsed = (Map) JSONB.parse(bytes);
        assertEquals(1, parsed.size());
        assertEquals(value, parsed.get(key));
    }

    @Test
    public void testTwo() {
        LinkedHashMap map = new LinkedHashMap();

        String k1 = "id";
        int v1 = 1;

        String k2 = "name";
        String v2 = "DataWorks";

        map.put(k1, v1);
        map.put(k2, v2);

        byte[] bytes = JSONB.toBytes(map);
        assertEquals(21, bytes.length);
        assertEquals(BC_OBJECT, bytes[0]);
        assertEquals(BC_OBJECT_END, bytes[bytes.length - 1]);

        byte[] k1Bytes = JSONB.toBytes(k1);
        byte[] v1Bytes = JSONB.toBytes(v1);

        byte[] k2Bytes = JSONB.toBytes(k2);
        byte[] v2Bytes = JSONB.toBytes(v2);

        assertEquals(bytes.length, 2 + k1Bytes.length + v1Bytes.length + k2Bytes.length + v2Bytes.length);

        for (int i = 0; i < k1Bytes.length; i++) {
            assertEquals(bytes[i + 1], k1Bytes[i]);
        }

        for (int i = 0; i < v1Bytes.length; i++) {
            assertEquals(bytes[i + 1 + k1Bytes.length], v1Bytes[i]);
        }

        for (int i = 0; i < k2Bytes.length; i++) {
            assertEquals(bytes[i + 1 + k1Bytes.length + v1Bytes.length], k2Bytes[i]);
        }

        for (int i = 0; i < v2Bytes.length; i++) {
            assertEquals(bytes[i + 1 + k1Bytes.length + v1Bytes.length + k2Bytes.length], v2Bytes[i]);
        }

        Map parsed = (Map) JSONB.parse(bytes);
        assertEquals(2, parsed.size());
        assertEquals(v1, parsed.get(k1));
        assertEquals(v2, parsed.get(k2));
    }
}
