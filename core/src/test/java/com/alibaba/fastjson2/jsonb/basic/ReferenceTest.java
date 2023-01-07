package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ReferenceTest {
    @Test
    public void testRefRoot() {
        Map map = new LinkedHashMap();
        map.put("value", Collections.singletonMap("root", map));

        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.ReferenceDetection);
        assertEquals(18, bytes.length);

        assertEquals(BC_OBJECT, bytes[0]);

        byte[] k1Bytes = JSONB.toBytes("value");
        for (int i = 0; i < k1Bytes.length; i++) {
            assertEquals(bytes[i + 1], k1Bytes[i]);
        }

        assertEquals(BC_OBJECT, bytes[1 + k1Bytes.length]);

        byte[] k2Bytes = JSONB.toBytes("root");
        for (int i = 0; i < k2Bytes.length; i++) {
            assertEquals(bytes[i + 2 + k1Bytes.length], k2Bytes[i]);
        }

        assertEquals(BC_REFERENCE, bytes[2 + k1Bytes.length + k2Bytes.length]);

        byte[] refBytes = JSONB.toBytes("$");
        for (int i = 0; i < refBytes.length; i++) {
            assertEquals(bytes[i + 3 + k1Bytes.length + k2Bytes.length], refBytes[i]);
        }
        assertEquals(BC_OBJECT_END, bytes[3 + k1Bytes.length + k2Bytes.length + refBytes.length]); // 16
        assertEquals(BC_OBJECT_END, bytes[4 + k1Bytes.length + k2Bytes.length + refBytes.length]); // 17

        Map parsed = (Map) JSONB.parse(bytes);
        assertSame(parsed, ((Map) parsed.get("value")).get("root"));
    }

    /**
     * 0x93 reference '..'
     */
    @Test
    public void testRefParent() {
        Map map = new LinkedHashMap();
        Map value = new LinkedHashMap();
        value.put("parent", value);
        map.put("value", value);

        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.ReferenceDetection);

        assertEquals(21, bytes.length);

        assertEquals(BC_OBJECT, bytes[0]);

        byte[] k1Bytes = JSONB.toBytes("value");
        for (int i = 0; i < k1Bytes.length; i++) {
            assertEquals(bytes[i + 1], k1Bytes[i]);
        }

        assertEquals(BC_OBJECT, bytes[1 + k1Bytes.length]);

        byte[] k2Bytes = JSONB.toBytes("parent");
        for (int i = 0; i < k2Bytes.length; i++) {
            assertEquals(bytes[i + 2 + k1Bytes.length], k2Bytes[i]);
        }

        assertEquals(BC_REFERENCE, bytes[2 + k1Bytes.length + k2Bytes.length]);

        byte[] refBytes = JSONB.toBytes("..");
        for (int i = 0; i < refBytes.length; i++) {
            assertEquals(bytes[i + 3 + k1Bytes.length + k2Bytes.length], refBytes[i]);
        }
        assertEquals(BC_OBJECT_END, bytes[3 + k1Bytes.length + k2Bytes.length + refBytes.length]); // 19
        assertEquals(BC_OBJECT_END, bytes[4 + k1Bytes.length + k2Bytes.length + refBytes.length]); // 20

        Map parsed = (Map) JSONB.parse(bytes);
        assertSame(parsed.get("value"), ((Map) parsed.get("value")).get("parent"));
    }

    /**
     * 0x93 reference '$.value0'
     */
    @Test
    public void testRefPath() {
        Map map = new LinkedHashMap();
        Map value0 = new LinkedHashMap();
        Map value1 = new LinkedHashMap();
        value1.put("ref", value0);
        map.put("value0", value0);
        map.put("value1", value1);

        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.ReferenceDetection);

        assertEquals(34, bytes.length);

        assertEquals(BC_OBJECT, bytes[0]);

        byte[] k1Bytes = JSONB.toBytes("value0");
        for (int i = 0; i < k1Bytes.length; i++) {
            assertEquals(bytes[i + 1], k1Bytes[i]);
        }

        assertEquals(BC_OBJECT, bytes[1 + k1Bytes.length]);
        assertEquals(BC_OBJECT_END, bytes[2 + k1Bytes.length]);

        byte[] k2Bytes = JSONB.toBytes("value1");
        for (int i = 0; i < k2Bytes.length; i++) {
            assertEquals(bytes[i + 3 + k1Bytes.length], k2Bytes[i]);
        }

        assertEquals(BC_OBJECT, bytes[3 + k1Bytes.length + k2Bytes.length]);

        byte[] k3Bytes = JSONB.toBytes("ref");
        for (int i = 0; i < k3Bytes.length; i++) {
            assertEquals(bytes[i + 4 + k1Bytes.length + k2Bytes.length], k3Bytes[i]);
        }

        byte[] refBytes = JSONB.toBytes("$.value0");
        for (int i = 0; i < refBytes.length; i++) {
            assertEquals(bytes[i + 5 + k1Bytes.length + k2Bytes.length + k3Bytes.length], refBytes[i]);
        }

        assertEquals(BC_OBJECT_END, bytes[5 + k1Bytes.length + k2Bytes.length + k3Bytes.length + refBytes.length]); // 32
        assertEquals(BC_OBJECT_END, bytes[6 + k1Bytes.length + k2Bytes.length + k3Bytes.length + refBytes.length]); // 33

        Map parsed = (Map) JSONB.parse(bytes);
        assertSame(parsed.get("value0"), ((Map) parsed.get("value1")).get("ref"));
    }
}
