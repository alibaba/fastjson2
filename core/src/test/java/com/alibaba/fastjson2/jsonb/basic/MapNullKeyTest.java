package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A HashMap may legitimately hold a null key, and the text path handles it
 * (ObjectWriterImplMap.writeMapKey checks key == null first), but the JSONB path used to throw
 * NullPointerException from two unguarded dereferences of the entry key:
 *  - entryKey.getClass() on the value == null && WriteNulls path, and
 *  - entryKey.toString() on the main path, where the existing entryKey == null branch sat behind a
 *    catch-all condition that is always true while WriteClassName is off (the default), making it
 *    dead code.
 */
@Tag("jsonb")
public class MapNullKeyTest {
    public static class Bean {
        public Map<Long, String> map1;
    }

    static Map<Long, String> nullKeyMap(String value) {
        Map<Long, String> map = new HashMap<>();
        map.put(null, value);
        return map;
    }

    @Test
    public void nullKey_jsonb_default() {
        byte[] bytes = JSONB.toBytes(nullKeyMap("v"));

        JSONObject parsed = JSONB.parseObject(bytes);
        assertTrue(parsed.containsKey(null));
        assertEquals("v", parsed.get(null));
    }

    @Test
    public void nullKey_jsonb_writeClassName() {
        byte[] bytes = JSONB.toBytes(
                nullKeyMap("v"),
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName);

        JSONObject parsed = JSONB.parseObject(bytes);
        assertTrue(parsed.containsKey(null));
        assertEquals("v", parsed.get(null));
    }

    @Test
    public void nullKey_nullValue_jsonb_writeNulls() {
        byte[] bytes = JSONB.toBytes(nullKeyMap(null), JSONWriter.Feature.WriteNulls);

        JSONObject parsed = JSONB.parseObject(bytes);
        assertTrue(parsed.containsKey(null));
        assertNull(parsed.get(null));
    }

    @Test
    public void nullKey_nullValue_jsonb_writeNulls_writeClassName() {
        byte[] bytes = JSONB.toBytes(
                nullKeyMap(null),
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName);

        JSONObject parsed = JSONB.parseObject(bytes);
        assertTrue(parsed.containsKey(null));
        assertNull(parsed.get(null));
    }

    @Test
    public void nullKey_typedMapField_jsonb_default() {
        Bean bean = new Bean();
        bean.map1 = nullKeyMap("v");

        byte[] bytes = JSONB.toBytes(bean);

        Bean parsed = JSONB.parseObject(bytes, Bean.class);
        assertTrue(parsed.map1.containsKey(null));
        assertEquals("v", parsed.map1.get(null));
    }

    @Test
    public void nonNullKey_jsonb_default() {
        Map<Long, String> map = new HashMap<>();
        map.put(1L, "v");

        JSONObject parsed = JSONB.parseObject(JSONB.toBytes(map));
        assertEquals("v", parsed.get("1"));
    }
}
