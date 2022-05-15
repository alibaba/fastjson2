package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONSchemaTest2 {
    @Test
    public void test0() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean.class).value
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean.class)
        );
    }

    public static class Bean {
        @JSONField(schema = "{'minimum':1}")
        public int value;
    }

    @Test
    public void test1() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean1.class).value
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean1.class)
        );
    }

    public static class Bean1 {
        @JSONField(schema = "{'minimum':1}")
        public byte value;
    }

    @Test
    public void test2() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean2.class).value
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean2.class)
        );
    }

    public static class Bean2 {
        @JSONField(schema = "{'minimum':1}")
        public short value;
    }

    @Test
    public void test3() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean3.class).value
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean3.class)
        );
    }

    public static class Bean3 {
        @JSONField(schema = "{'minimum':1}")
        public long value;
    }

    @Test
    public void test4() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean4.class).value.intValue()
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean4.class)
        );
    }

    public static class Bean4 {
        @JSONField(schema = "{'minimum':1}")
        public Byte value;
    }

    @Test
    public void test5() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean5.class).value.intValue()
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean5.class)
        );
    }

    public static class Bean5 {
        @JSONField(schema = "{'minimum':1}")
        public Short value;
    }

    @Test
    public void test6() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean6.class).value.intValue()
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean6.class)
        );
    }

    public static class Bean6 {
        @JSONField(schema = "{'minimum':1}")
        public Integer value;
    }

    @Test
    public void test7() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean7.class).value.intValue()
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean7.class)
        );
    }

    public static class Bean7 {
        @JSONField(schema = "{'minimum':1}")
        public Long value;
    }

    @Test
    public void test8() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean8.class).value.intValue()
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean8.class)
        );
    }

    public static class Bean8 {
        @JSONField(schema = "{'minimum':1}")
        public BigInteger value;
    }

    @Test
    public void test9() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean9.class).value.intValue()
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean9.class)
        );
    }

    public static class Bean9 {
        @JSONField(schema = "{'minimum':1}")
        public BigDecimal value;
    }

    @Test
    public void test10() {
        assertEquals(
                123F,
                JSON.parseObject("{\"value\":123}", Bean10.class).value
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean10.class)
        );
    }

    public static class Bean10 {
        @JSONField(schema = "{'minimum':1}")
        public float value;
    }

    @Test
    public void test11() {
        assertEquals(
                123F,
                JSON.parseObject("{\"value\":123}", Bean11.class).value
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean11.class)
        );
    }

    public static class Bean11 {
        @JSONField(schema = "{'minimum':1}")
        public double value;
    }

    @Test
    public void test12() {
        assertEquals(
                123F,
                JSON.parseObject("{\"value\":123}", Bean12.class).value
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean12.class)
        );
    }

    public static class Bean12 {
        @JSONField(schema = "{'minimum':1}")
        public Float value;
    }

    @Test
    public void test13() {
        assertEquals(
                123D,
                JSON.parseObject("{\"value\":123}", Bean13.class).value
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean13.class)
        );
    }

    public static class Bean13 {
        @JSONField(schema = "{'minimum':1}")
        public Double value;
    }

    @Test
    public void test14() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean14.class).value.intValue()
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean14.class)
        );
    }

    public static class Bean14 {
        @JSONField(schema = "{'minimum':1}")
        public AtomicInteger value;
    }

    @Test
    public void test15() {
        assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean15.class).value.intValue()
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean15.class)
        );
    }

    public static class Bean15 {
        @JSONField(schema = "{'minimum':1}")
        public AtomicInteger value;
    }
}
