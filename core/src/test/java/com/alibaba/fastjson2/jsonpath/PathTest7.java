package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathTest7 {
    @Test
    public void test_set_int_0() {
        A vo = new A();
        JSONPath.of("$.v0")
                .setInt(vo, 10);
        assertEquals(10F, vo.v0);

        JSONPath.of("$.v1")
                .setInt(vo, 11);
        assertEquals(11D, vo.v1);

        JSONPath.of("$.v2")
                .setInt(vo, 12);
        assertEquals(12F, vo.v2.floatValue());

        JSONPath.of("$.v3")
                .setInt(vo, 13);
        assertEquals(13D, vo.v3.doubleValue());
    }

    @Test
    public void test_set_1() {
        A vo = new A();
        JSONPath.of("$.v0")
                .set(vo, 10);
        assertEquals(10F, vo.v0);

        JSONPath.of("$.v1")
                .set(vo, 11);
        assertEquals(11D, vo.v1);

        JSONPath.of("$.v2")
                .set(vo, 12);
        assertEquals(12F, vo.v2.floatValue());

        JSONPath.of("$.v3")
                .set(vo, 13);
        assertEquals(13D, vo.v3.doubleValue());
    }

    @Test
    public void test_set_long_0() {
        A vo = new A();
        JSONPath.of("$.v0")
                .setLong(vo, 10);
        assertEquals(10F, vo.v0);

        JSONPath.of("$.v1")
                .setLong(vo, 11);
        assertEquals(11D, vo.v1);

        JSONPath.of("$.v2")
                .setLong(vo, 12);
        assertEquals(12F, vo.v2.floatValue());

        JSONPath.of("$.v3")
                .setLong(vo, 13);
        assertEquals(13D, vo.v3.doubleValue());
    }

    @Test
    public void test_set_int_0_private() {
        B vo = new B();
        JSONPath.of("$.v0")
                .setInt(vo, 10);
        assertEquals(10F, vo.v0);

        JSONPath.of("$.v1")
                .setInt(vo, 11);
        assertEquals(11D, vo.v1);

        JSONPath.of("$.v2")
                .setInt(vo, 12);
        assertEquals(12F, vo.v2.floatValue());

        JSONPath.of("$.v3")
                .setInt(vo, 13);
        assertEquals(13D, vo.v3.doubleValue());
    }

    @Test
    public void test_set_1_private() {
        B vo = new B();
        JSONPath.of("$.v0")
                .set(vo, 10);
        assertEquals(10F, vo.v0);

        JSONPath.of("$.v1")
                .set(vo, 11);
        assertEquals(11D, vo.v1);

        JSONPath.of("$.v2")
                .set(vo, 12);
        assertEquals(12F, vo.v2.floatValue());

        JSONPath.of("$.v3")
                .set(vo, 13);
        assertEquals(13D, vo.v3.doubleValue());
    }

    @Test
    public void test_set_long_0_private() {
        B vo = new B();
        JSONPath.of("$.v0")
                .setLong(vo, 10);
        assertEquals(10F, vo.v0);

        JSONPath.of("$.v1")
                .setLong(vo, 11);
        assertEquals(11D, vo.v1);

        JSONPath.of("$.v2")
                .setLong(vo, 12);
        assertEquals(12F, vo.v2.floatValue());

        JSONPath.of("$.v3")
                .setLong(vo, 13);
        assertEquals(13D, vo.v3.doubleValue());
    }

    public static class A {
        private float v0;
        private double v1;
        private Float v2;
        private Double v3;

        public float getV0() {
            return v0;
        }

        public void setV0(float v0) {
            this.v0 = v0;
        }

        public double getV1() {
            return v1;
        }

        public void setV1(double v1) {
            this.v1 = v1;
        }

        public Float getV2() {
            return v2;
        }

        public void setV2(Float v2) {
            this.v2 = v2;
        }

        public Double getV3() {
            return v3;
        }

        public void setV3(Double v3) {
            this.v3 = v3;
        }
    }

    private static class B {
        private float v0;
        private double v1;
        private Float v2;
        private Double v3;

        public float getV0() {
            return v0;
        }

        public void setV0(float v0) {
            this.v0 = v0;
        }

        public double getV1() {
            return v1;
        }

        public void setV1(double v1) {
            this.v1 = v1;
        }

        public Float getV2() {
            return v2;
        }

        public void setV2(Float v2) {
            this.v2 = v2;
        }

        public Double getV3() {
            return v3;
        }

        public void setV3(Double v3) {
            this.v3 = v3;
        }
    }
}
