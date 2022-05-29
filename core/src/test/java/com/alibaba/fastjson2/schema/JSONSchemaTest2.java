package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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

public class JSONSchemaTest2 {
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

    @Test
    public void test16() {
        JSON.parseObject("{\"value\":[1,2,3]}", Bean16.class);

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":[]}", Bean16.class)
        );
    }

    public static class Bean16 {
        @JSONField(schema = "{'minItems':1}")
        public List value = new ArrayList();
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
        public final List value = new ArrayList();
    }

    @Test
    public void test18() {
        JSON.parseObject("{\"value\":[1,2,3]}", Bean18.class);

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":[]}", Bean18.class)
        );
    }

    private static class Bean18 {
        @JSONField(schema = "{'minItems':1}")
        public final List value = new ArrayList();
    }

    @Test
    public void test19() {
        JSON.parseObject("{\"value\":[1,2,3]}", Bean19.class);

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":[]}", Bean19.class)
        );
    }

    public static class Bean19 {
        @JSONField(schema = "{'minItems':1}")
        public int[] value;
    }

    @Test
    public void test20() {
        JSON.parseObject("{\"value\":true}", Bean20.class);
    }

    public static class Bean20 {
        @JSONField(schema = "{'minItems':1}")
        public Boolean value;
    }

    @Test
    public void test21() {
        JSON.parseObject("{\"value\":true}", Bean21.class);
    }

    public static class Bean21 {
        @JSONField(schema = "{'minItems':1}")
        public boolean value;
    }

    @Test
    public void test22() {
        JSON.parseObject("{\"value\":{'a':1,'b':2}}", Bean22.class);

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":{}}", Bean22.class)
        );
    }

    public static class Bean22 {
        @JSONField(schema = "{'minProperties':1}")
        public JSONObject value;
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
        public String value;
    }

    @Test
    public void test24() {
        JSON.parseObject("{\"value\":{\"id\":\"123\", \"name\":\"xx\"}}", Bean24.class);

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":{}}", Bean24.class)
        );
    }

    public static class Bean24 {
        @JSONField(schema = "{'minProperties':1}")
        public Item value;
    }

    @Test
    public void test25() {
        JSON.parseObject("{\"value\":{}}", Bean25.class);
        JSON.parseObject("{\"value\":{\"id\":123}}", Bean25.class);
        JSON.parseObject("{\"value\":{\"name\":\"xxx\"}}", Bean25.class);

        assertThrows(JSONSchemaValidException.class,
                () -> JSON.parseObject("{\"value\":{\"id\":\"123\", \"name\":\"xx\"}}", Bean25.class)
        );
    }

    public static class Bean25 {
        @JSONField(schema = "{'maxProperties':1}")
        public Item value;
    }

    public static class Item {
        public Integer id;
        public String name;
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
        public Integer id;
        public String name;
    }
}
