package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SkipTest {
    @Test
    public void test_0() {
        assertEquals(0,
                JSON.parseObject("{\"value\":123}".getBytes(StandardCharsets.UTF_8),
                        A.class).id);

        assertEquals(0,
                JSON.parseObject("{\"value\":123,\"name\":\"DataWorks\"}".getBytes(StandardCharsets.UTF_8),
                        A.class).id);

        assertEquals(0,
                JSON.parseObject("{\"value\":123,\"name\":\"DataWorks\"}".getBytes(StandardCharsets.UTF_8),
                        A1.class).id);

        assertEquals("DataWorks",
                JSONPath
                        .of("$.name")
                        .extract(
                                JSONReader
                                        .of("{\"id\":123,\"name\":\"DataWorks\"}".getBytes(StandardCharsets.UTF_8))
                        )
        );

        assertEquals("DataWorks",
                JSONPath
                        .of("$[1]")
                        .extract(
                                JSONReader
                                        .of("[123,\"DataWorks\"]".getBytes(StandardCharsets.UTF_8))
                        )
        );
    }

    public static class A {
        public int id;
    }

    private static class A1 {
        public int id;
    }
}
