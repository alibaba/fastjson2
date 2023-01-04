package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapTest {
    @Test
    public void test() {
        JSONObject object = JSONObject
                .of("", 0)
                .fluentPut("1", 1)
                .fluentPut("12", 12)
                .fluentPut("123", 123)
                .fluentPut("1234", 1234)
                .fluentPut("12345", 12345)
                .fluentPut("123456", 123456)
                .fluentPut("1234567", 1234567)
                .fluentPut("12345678", 12345678)
                .fluentPut("123456789", 123456789)
                .fluentPut("1234567890", 1234567890)
                .fluentPut("lastName", 1001)
                .fluentPut("UserName", 102);

        for (int i = 0; i < 3; ++i) {
            byte[] bytes = JSONB.toBytes(object);
            JSONObject object1 = JSONB.parseObject(bytes);
            assertEquals(object, object1);
        }
    }

    @Test
    public void test1() {
        JSONObject object = JSONObject
                .of("", 0)
                .fluentPut("1", 1)
                .fluentPut("12", 12)
                .fluentPut("123", 123)
                .fluentPut("1234", 1234)
                .fluentPut("12345", 12345)
                .fluentPut("123456", 123456)
                .fluentPut("1234567", 1234567)
                .fluentPut("12345678", 12345678)
                .fluentPut("123456789", 123456789)
                .fluentPut("1234567890", 1234567890)
                .fluentPut("lastName", 1001)
                .fluentPut("UserName", 102);
        for (int i = 0; i < 3; ++i) {
            Bean bean = new Bean();
            bean.values = object;
            byte[] bytes = JSONB.toBytes(bean);

            Bean bean1 = JSONB.parseObject(bytes, Bean.class);
            assertEquals(bean.values, bean1.values);
        }
    }

    @Test
    public void test2() {
        long[] starts = new long[]{
                10_000,
                100_000,
                1_000_000,
                10_000_000,
                100_000_000,
                1_000_000_000,
                10_000_000_000L,
                100_000_000_000L,
                1_000_000_000_000L,
                10_000_000_000_000L,
                100_000_000_000_000L,
                1_000_000_000_000_000L,
                10_000_000_000_000_000L,
                100_000_000_000_000_000L,
                1_000_000_000_000_000_000L,
        };

        JSONObject object = new JSONObject();
        for (int i = 0; i < starts.length; i++) {
            long start = starts[i];
            for (long j = start; j < start + 10000; j++) {
                String key = Long.toString(j);
                if (j >= Integer.MIN_VALUE && j <= Integer.MAX_VALUE) {
                    object.put(key, (int) j);
                } else {
                    object.put(key, j);
                }
            }
        }

        String str = JSON.toJSONString(object);
        JSONReader[] jsonReaders4 = TestUtils.createJSONReaders4(str);
        for (int i = 0; i < jsonReaders4.length; i++) {
            JSONReader jsonReader = jsonReaders4[i];
            Map<String, Object> map1 = jsonReader.readObject();
            assertEquals(object.size(), map1.size());
            assertEquals(object, map1);
        }

        {
            byte[] jsonbBytes = JSONB.toBytes(object);
            JSONObject map1 = JSONB.parseObject(jsonbBytes, JSONObject.class);
            assertEquals(object.size(), map1.size());
            assertEquals(object, map1);
        }
    }

    public static class Bean {
        public Map values;
    }

    @Test
    public void testNullKey() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("1", "1");
        map.put("2", "2");
        map.put(null, "3");
        byte[] bytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName);
        HashMap map1 = JSONB.parseObject(bytes, HashMap.class, JSONReader.Feature.SupportAutoType);
        assertEquals(map.size(), map1.size());
        assertTrue(map1.containsKey(null));
        assertEquals(map.get(null), map1.get(null));

        String str = JSON.toJSONString(map);
        HashMap map2 = JSON.parseObject(str, HashMap.class, JSONReader.Feature.SupportAutoType);
        assertEquals(map.size(), map2.size());
    }
}
