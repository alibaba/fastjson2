package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathTest6 {
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

    @Test
    public void test_set_int_0_private() {
        B vo = new B();
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
    public void test_set_1_private() {
        B vo = new B();
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
    public void test_set_long_0_private() {
        B vo = new B();
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

    public static class A {
        private byte v0;
        private short v1;
        private Byte v2;
        private Short v3;

        public byte getV0() {
            return v0;
        }

        public void setV0(byte v0) {
            this.v0 = v0;
        }

        public short getV1() {
            return v1;
        }

        public void setV1(short v1) {
            this.v1 = v1;
        }

        public Byte getV2() {
            return v2;
        }

        public void setV2(Byte v2) {
            this.v2 = v2;
        }

        public Short getV3() {
            return v3;
        }

        public void setV3(Short v3) {
            this.v3 = v3;
        }
    }

    private static class B {
        private byte v0;
        private short v1;
        private Byte v2;
        private Short v3;

        public byte getV0() {
            return v0;
        }

        public void setV0(byte v0) {
            this.v0 = v0;
        }

        public short getV1() {
            return v1;
        }

        public void setV1(short v1) {
            this.v1 = v1;
        }

        public Byte getV2() {
            return v2;
        }

        public void setV2(Byte v2) {
            this.v2 = v2;
        }

        public Short getV3() {
            return v3;
        }

        public void setV3(Short v3) {
            this.v3 = v3;
        }
    }
}
