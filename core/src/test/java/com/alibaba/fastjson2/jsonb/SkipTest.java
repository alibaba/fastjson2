package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
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

        JSONBDump.dump(bytes);

        A a = JSONB.parseObject(bytes, A.class);
        assertNotNull(a);
    }

    public static class A {
    }
}
