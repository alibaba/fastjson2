package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class JSONFieldTest_defaultValue {
    @Test
    public void test_0() {
        assertEquals(Boolean.TRUE,
                JSON.parseObject("{}", Bean0.class).value
        );
    }

    @Test
    public void test_0_jsonb() {
        assertEquals(Boolean.TRUE,
                JSONB.parseObject(JSONB.toBytes(new HashMap<>()), Bean0.class).value
        );
    }

    public static class Bean0 {
        @JSONField(defaultValue = "true")
        public Boolean value;
    }

    @Test
    public void test_1() {
        assertEquals(Byte.valueOf((byte) 1),
                JSON.parseObject("{}", Bean1.class).value
        );
    }

    public static class Bean1 {
        @JSONField(defaultValue = "1")
        public Byte value;
    }

    @Test
    public void test_2() {
        assertEquals(Short.valueOf((short) 2),
                JSON.parseObject("{}", Bean2.class).value
        );
    }

    public static class Bean2 {
        @JSONField(defaultValue = "2")
        public Short value;
    }

    @Test
    public void test_3() {
        Bean3 bean = JSON.parseObject("{}", Bean3.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
    }

    @Test
    public void test_3_jsonb() {
        Bean3 bean = JSONB.parseObject(JSONB.toBytes(Collections.emptyMap()), Bean3.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
    }

    public static class Bean3 {
        @JSONField(defaultValue = "1")
        public Integer value0;

        @JSONField(defaultValue = "2")
        public Long value1;
    }

    @Test
    public void test_4() {
        Bean4 bean = JSON.parseObject("{}", Bean4.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
        assertEquals(3.1f, bean.value2);
    }

    @Test
    public void test_4_jsonb() {
        Bean4 bean = JSONB.parseObject(JSONB.toBytes(Collections.emptyMap()), Bean4.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
        assertEquals(3.1f, bean.value2);
    }

    public static class Bean4 {
        @JSONField(defaultValue = "1")
        public int value0;

        @JSONField(defaultValue = "2")
        public long value1;

        @JSONField(defaultValue = "3.1")
        public float value2;
    }

    @Test
    public void test_5() {
        Bean5 bean = JSON.parseObject("{}", Bean5.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
        assertEquals(3.1f, bean.value2);
        assertEquals(4.2D, bean.value3);
    }

    @Test
    public void test_5_jsonb() {
        Bean5 bean = JSONB.parseObject(JSONB.toBytes(Collections.emptyMap()), Bean5.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
        assertEquals(3.1f, bean.value2);
        assertEquals(4.2D, bean.value3);
    }

    public static class Bean5 {
        @JSONField(defaultValue = "1")
        public int value0;

        @JSONField(defaultValue = "2")
        public long value1;

        @JSONField(defaultValue = "3.1")
        public Float value2;

        @JSONField(defaultValue = "4.2")
        public Double value3;
    }

    @Test
    public void test_6() {
        Bean6 bean = JSON.parseObject("{}", Bean6.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
        assertEquals(3.1f, bean.value2);
        assertEquals(4.2D, bean.value3);
        assertEquals(6.3D, bean.value4);
    }

    @Test
    public void test_6_jsonb() {
        Bean6 bean = JSONB.parseObject(JSONB.toBytes(Collections.emptyMap()), Bean6.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
        assertEquals(3.1f, bean.value2);
        assertEquals(4.2D, bean.value3);
        assertEquals(6.3D, bean.value4);
    }

    public static class Bean6 {
        @JSONField(defaultValue = "1")
        public int value0;

        @JSONField(defaultValue = "2")
        public long value1;

        @JSONField(defaultValue = "3.1")
        public Float value2;

        @JSONField(defaultValue = "4.2")
        public Double value3;

        @JSONField(defaultValue = "6.3")
        public double value4;
    }

    @Test
    public void test_7() {
        Bean7 bean = JSON.parseObject("{}", Bean7.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
        assertEquals(3.1f, bean.value2);
        assertEquals(4.2D, bean.value3);
        assertEquals(6.3D, bean.value4);
        assertEquals("xx", bean.value5);
    }

    @Test
    public void test_7_jsonb() {
        Bean7 bean = JSONB.parseObject(JSONB.toBytes(Collections.emptyMap()), Bean7.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
        assertEquals(3.1f, bean.value2);
        assertEquals(4.2D, bean.value3);
        assertEquals(6.3D, bean.value4);
        assertEquals("xx", bean.value5);
    }

    public static class Bean7 {
        @JSONField(defaultValue = "1")
        public int value0;

        @JSONField(defaultValue = "2")
        public long value1;

        @JSONField(defaultValue = "3.1")
        public Float value2;

        @JSONField(defaultValue = "4.2")
        public Double value3;

        @JSONField(defaultValue = "6.3")
        public double value4;

        @JSONField(defaultValue = "xx")
        public String value5;
    }

    @Test
    public void test_8() {
        Bean8 bean = JSON.parseObject("{}", Bean8.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
        assertEquals(3.1f, bean.value2);
        assertEquals(4.2D, bean.value3);
        assertEquals(6.3D, bean.value4);
        assertEquals("xx", bean.value5);
        assertEquals(new BigDecimal(123.5), bean.value6);
    }

    @Test
    public void test_8_jsonb() {
        Bean8 bean = JSONB.parseObject(JSONB.toBytes(Collections.emptyMap()), Bean8.class);
        assertEquals(1, bean.value0);
        assertEquals(2, bean.value1);
        assertEquals(3.1f, bean.value2);
        assertEquals(4.2D, bean.value3);
        assertEquals(6.3D, bean.value4);
        assertEquals("xx", bean.value5);
        assertEquals(new BigDecimal(123.5), bean.value6);
    }

    public static class Bean8 {
        @JSONField(defaultValue = "1")
        public int value0;

        @JSONField(defaultValue = "2")
        public long value1;

        @JSONField(defaultValue = "3.1")
        public Float value2;

        @JSONField(defaultValue = "4.2")
        public Double value3;

        @JSONField(defaultValue = "6.3")
        public double value4;

        @JSONField(defaultValue = "xx")
        public String value5;

        @JSONField(defaultValue = "123.5")
        private BigDecimal value6;

        public BigDecimal getValue6() {
            return value6;
        }

        public void setValue6(BigDecimal value6) {
            this.value6 = value6;
        }
    }

    @Test
    public void test_9() {
        Bean9 bean = JSON.parseObject("{}", Bean9.class);
        assertNotNull(bean.list);
        assertTrue(bean.list.isEmpty());
    }

    public static class Bean9 {
        @JSONField(defaultValue = "[]")
        public JSONArray list;

        public Bean9() {
        }
    }
}
