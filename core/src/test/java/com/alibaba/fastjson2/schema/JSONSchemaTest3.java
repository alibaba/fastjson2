package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONSchemaValidException;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONSchemaTest3 {
    @Test
    public void test0() {
        Assertions.assertEquals(
                123,
                JSON.parseObject("{\"value\":123}", Bean.class).value
        );

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":0}", Bean.class)
        );
    }

    public static class Bean {
        @JSONField(schema = "{'minimum':1}")
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
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
        private byte value;

        public byte getValue() {
            return value;
        }

        public void setValue(byte value) {
            this.value = value;
        }
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
        private short value;

        public short getValue() {
            return value;
        }

        public void setValue(short value) {
            this.value = value;
        }
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
        private long value;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
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
        private Byte value;

        public Byte getValue() {
            return value;
        }

        public void setValue(Byte value) {
            this.value = value;
        }
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
        private Short value;

        public Short getValue() {
            return value;
        }

        public void setValue(Short value) {
            this.value = value;
        }
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
        private Integer value;

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
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
        private Long value;

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }
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
        private BigInteger value;

        public BigInteger getValue() {
            return value;
        }

        public void setValue(BigInteger value) {
            this.value = value;
        }
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
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
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
        private float value;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
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
        private double value;

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
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
        private Float value;

        public Float getValue() {
            return value;
        }

        public void setValue(Float value) {
            this.value = value;
        }
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
        private Double value;

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
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
        private AtomicInteger value;

        public AtomicInteger getValue() {
            return value;
        }

        public void setValue(AtomicInteger value) {
            this.value = value;
        }
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
        private AtomicInteger value;

        public AtomicInteger getValue() {
            return value;
        }

        public void setValue(AtomicInteger value) {
            this.value = value;
        }
    }

    @Test
    public void test16() {
        JSON.parseObject("{\"value\":[1,2,3]}", Bean16.class);

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":[]}", Bean16.class)
        );
    }

    public static class Bean16 {
        @JSONField(schema = "{'minItems':1}")
        private final List value = new ArrayList();

        public List getValue() {
            return value;
        }
    }

    @Test
    public void test17() {
        JSON.parseObject("{\"value\":[1,2,3]}", Bean17.class);

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":[]}", Bean17.class)
        );
    }

    public static class Bean17 {
        @JSONField(schema = "{'minItems':1}")
        private final List value = new ArrayList();

        public List getValue() {
            return value;
        }
    }

    @Test
    public void test23() {
        JSON.parseObject("{\"value\":\"123\"}", Bean23.class);

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":\"\"}", Bean23.class)
        );
    }

    public static class Bean23 {
        @JSONField(schema = "{'minLength':1}")
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Test
    public void test27() {
        JSON.parseObject("{}", Bean26.class);
        JSON.parseObject("{\"id\":123}", Bean26.class);
        JSON.parseObject("{\"name\":\"xxx\"}", Bean26.class);

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"id\":\"123\", \"name\":\"xx\"}", Bean26.class)
        );
    }

    @JSONType(schema = "{'maxProperties':1}")
    public static class Bean26 {
        private Integer id;
        private String name;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
