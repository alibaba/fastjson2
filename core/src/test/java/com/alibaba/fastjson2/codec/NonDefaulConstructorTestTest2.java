package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NonDefaulConstructorTestTest2 {
    @Test
    public void test_a() {
        String str = JSONObject.of("unit", 3).toString();
        A a = JSON.parseObject(str, A.class);
        assertEquals(3, a.unit);
    }

    @Test
    public void test_b() {
        JSONObject obj = JSONObject.of("id", 3);
        String str = obj.toString();
        assertEquals(3, JSON.parseObject(str, B.class).id);

        byte[] bytes = JSONB.toBytes(obj);
        assertEquals(3, JSONB.parseObject(bytes, B.class).id);
    }

    @Test
    public void test_ab() {
        String str = JSONObject.of("id", 3).fluentPut("name", "DataWorks").toString();
        B a = JSON.parseObject(str, B.class);
        assertEquals(3, a.id);
    }

    @Test
    public void test_ab_1() {
        String str = JSONObject.of("id", 3).fluentPut("name", "DataWorks").toString(JSONWriter.Feature.UseSingleQuotes);
        B a = JSON.parseObject(str, B.class);
        assertEquals(3, a.id);
    }

    public static class A {
        private BigDecimal amount;
        private Currency currency;
        private final int unit;

        public A(BigDecimal amount, Currency currency, int unit) {
            if (amount == null) {
                throw new IllegalArgumentException();
            }
            this.amount = amount;
            this.unit = unit;
        }

        public A(BigDecimal decimal, int unit) {
            if (decimal == null) {
                throw new IllegalArgumentException();
            }
            this.unit = unit;
        }

        public A(int unit) {
            this.unit = unit;
        }

        public int getUnit() {
            return unit;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public Currency getCurrency() {
            return currency;
        }
    }

    public static class B {
        public final long id;
        public final String name;

        public B(long id) {
            this.id = id;
            this.name = null;
        }

        public B(long id, String name) {
            this.id = id;
            if (name == null) {
                throw new IllegalArgumentException("name is null");
            }
            this.name = name;
        }
    }

    @Test
    public void test_bean1() {
        JSONObject jsonObject = JSONObject.of("v0", 3);

        {
            String str = jsonObject.toString();
            Bean1 a = JSON.parseObject(str, Bean1.class);
            assertEquals(3, a.v0);
        }

        {
            byte[] jsonb = JSONB.toBytes(jsonObject);
            Bean1 a = JSONB.parseObject(jsonb, Bean1.class);
            assertEquals(3, a.v0);
        }
    }

    @Test
    public void test_bean1_eror() {
        byte[] jsonb = JSONObject.of("v0", null).toJSONBBytes(JSONWriter.Feature.WriteNulls);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(jsonb, Bean1.class, JSONReader.Feature.ErrorOnNullForPrimitives));
    }

    public static class Bean1 {
        final byte v0;
        public Bean1(byte v0) {
            this.v0 = v0;
        }
    }

    @Test
    public void test_ben6() {
        JSONObject jsonObject = JSONObject.of("v0", 3);

        String str = jsonObject.toString();
        Bean6 a = JSON.parseObject(str, Bean6.class);
        assertEquals(3, a.v0);

        byte[] jsonb = JSONB.toBytes(jsonObject);
        a = JSONB.parseObject(jsonb, Bean6.class);
        assertEquals(3, a.v0);
    }

    @Test
    public void test_ben6_error() {
        byte[] jsonb = JSONObject.of("v0", null).toJSONBBytes(JSONWriter.Feature.WriteNulls);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(jsonb, Bean6.class, JSONReader.Feature.ErrorOnNullForPrimitives));
    }

    public static class Bean6 {
        final int v0;
        final int v1;
        final int v2;
        final int v3;
        final int v4;
        final int v5;
        final int v6;
        public Bean6(int v0, int v1, int v2, int v3, int v4, int v5, int v6) {
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
        }
    }
}
