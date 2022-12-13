package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectArrayReaderTest {
    @Test
    public void test() {
        Object[] objects = JSON.parseObject("[1,\"abc\",{},[],null,.3,-1,true,false,2147483649]", Object[].class);

        assertEquals(10, objects.length);
        assertEquals(1, objects[0]);
        assertEquals("abc", objects[1]);
        assertEquals(new JSONObject(), objects[2]);
        assertEquals(new JSONArray(), objects[3]);
        assertNull(objects[4]);
        assertEquals(new BigDecimal("0.3"), objects[5]);
        assertEquals(-1, objects[6]);
        assertEquals(Boolean.TRUE, objects[7]);
        assertEquals(Boolean.FALSE, objects[8]);
        assertEquals(2147483649L, objects[9]);

        byte[] jsonbBytes = JSONB.toBytes(objects);
        Object[] values = JSONB.parseObject(jsonbBytes, Object[].class);
        assertArrayEquals(objects, values);
    }

    @Test
    public void testNull() {
        Bean bean = JSON.parseObject("{\"value\":null}", Bean.class);
        assertNull(bean.value);

        Bean bean1 = JSONObject.of("value", null).to(Bean.class);
        assertNull(bean1.value);

        Bean bean2 = JSONObject.of("value", "").to(Bean.class);
        assertNull(bean2.value);

        Bean bean3 = JSON.parseObject("{\"value\":\"\"}", Bean.class);
        assertNull(bean3.value);
    }

    @Test
    public void testBig() {
        int[] ints = new int[256];
        Arrays.fill(ints, 1);
        String json = JSON.toJSONString(ints);
        Object[] objects = JSON.parseObject(json, Object[].class);
        assertEquals(ints.length, objects.length);
        for (int i = 0; i < objects.length; i++) {
            Integer item = (Integer) objects[i];
            assertEquals(ints[i], item.intValue());
        }
    }

    @Test
    public void testError() {
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"value\":[a}", Bean.class));
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"value\":{}}", Bean.class));
    }

    public static class Bean {
        public Object[] value;
    }
}
