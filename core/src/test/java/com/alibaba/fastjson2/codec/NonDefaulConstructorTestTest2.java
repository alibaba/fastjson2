package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
