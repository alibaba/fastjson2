package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SkipTest {
    @Test
    public void test_0() {
        byte[] bytes = JSONB.toBytes(
                JSONObject.of("id", "01234567890ABCDE01234567890ABCDE01234567890ABCDE01234567890ABCDE01234567890ABCDE")
                        .fluentPut("v0", 1)
                        .fluentPut("v1", this)
                        .fluentPut("v2", new JSONObject())
                        .fluentPut("v3", JSONArray.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, "A", "B", "C", "D", "E", "F")),
                JSONWriter.Feature.ReferenceDetection
        );

        JSONB.dump(bytes);

        A a = JSONB.parseObject(bytes, A.class);
        assertNotNull(a);
    }

    @Test
    public void test_1() {
        for (int i = 0; i < 100; i += 10) {
            byte[] jsonbBytes = JSONObject.of("v", (byte) i).toJSONBBytes();
            A a = JSONB.parseObject(jsonbBytes, A.class);
            assertNotNull(a);
        }

        for (int i = 0; i < 1000; i += 10) {
            byte[] jsonbBytes = JSONObject.of("v", (short) i).toJSONBBytes();
            A a = JSONB.parseObject(jsonbBytes, A.class);
            assertNotNull(a);
        }

        for (int i = 0; i < 100000; i += 10) {
            byte[] jsonbBytes = JSONObject.of("v", i).toJSONBBytes();
            A a = JSONB.parseObject(jsonbBytes, A.class);
            assertNotNull(a);
        }

        for (int i = 0; i < 100000; i += 10) {
            byte[] jsonbBytes = JSONObject.of("v", (long) i).toJSONBBytes();
            A a = JSONB.parseObject(jsonbBytes, A.class);
            assertNotNull(a);
        }
    }

    public static class A {
    }
}
