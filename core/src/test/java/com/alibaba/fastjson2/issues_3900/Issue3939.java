package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3939 {
    @Test
    public void test_1() {
        String json = "{\"a\":14900000000000002396241300002543983538316751946695148944854736328125e-7}";
        A a = JSON.parseObject(json, A.class);
        JSONObject obj = JSON.parseObject(json, JSONReader.Feature.UseBigDecimalForDoubles);

        BigDecimal expected = new BigDecimal("14900000000000002396241300002543983538316751946695148944854736328125e-7");
        assertEquals(expected, a.a);
        assertEquals(obj.get("a"), a.a);
    }

    @Test
    public void test_2() {
        String json = "{\"a\":12345678901234567890123456789012345678e-10}";
        A a = JSON.parseObject(json, A.class);
        JSONObject obj = JSON.parseObject(json, JSONReader.Feature.UseBigDecimalForDoubles);

        BigDecimal expected = new BigDecimal("12345678901234567890123456789012345678e-10");
        assertEquals(expected, a.a);
        assertEquals(obj.get("a"), a.a);
    }

    @Test
    public void test_3() {
        String json = "{\"a\":9223372036854776000e-10}";
        A a = JSON.parseObject(json, A.class);
        JSONObject obj = JSON.parseObject(json, JSONReader.Feature.UseBigDecimalForDoubles);

        BigDecimal expected = new BigDecimal("9223372036854776000e-10");
        assertEquals(expected, a.a);
        assertEquals(obj.get("a"), a.a);
    }

    public static class A {
        public BigDecimal a;
    }
}
