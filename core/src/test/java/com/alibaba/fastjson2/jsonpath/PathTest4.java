package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathTest4 {
    @Test
    public void test_set_int_0() {
        A vo = new A();
        JSONPath.of("$.v0")
                .setInt(vo, 10);
        assertEquals(10, vo.v0);

        JSONPath.of("$.v1")
                .setInt(vo, 11);
        assertEquals(11, vo.v1);

        JSONPath.of("$.v2")
                .setInt(vo, 12);
        assertEquals(12, vo.v2.byteValue());

        JSONPath.of("$.v3")
                .setInt(vo, 13);
        assertEquals(13, vo.v3.shortValue());
    }

    @Test
    public void test_set_1() {
        A vo = new A();
        JSONPath.of("$.v0")
                .set(vo, 10);
        assertEquals(10, vo.v0);

        JSONPath.of("$.v1")
                .set(vo, 11);
        assertEquals(11, vo.v1);

        JSONPath.of("$.v2")
                .set(vo, 12);
        assertEquals(12, vo.v2.byteValue());

        JSONPath.of("$.v3")
                .set(vo, 13);
        assertEquals(13, vo.v3.shortValue());
    }

    @Test
    public void test_set_long_0() {
        A vo = new A();
        JSONPath.of("$.v0")
                .setLong(vo, 10);
        assertEquals(10, vo.v0);

        JSONPath.of("$.v1")
                .setLong(vo, 11);
        assertEquals(11, vo.v1);

        JSONPath.of("$.v2")
                .setLong(vo, 12);
        assertEquals(12, vo.v2.byteValue());

        JSONPath.of("$.v3")
                .setLong(vo, 13);
        assertEquals(13, vo.v3.shortValue());
    }

    private static class A {
        public byte v0;
        public short v1;
        public Byte v2;
        public Short v3;
    }
}
