package com.alibaba.fastjson2.issues_3700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue3772 {
    @Test
    public void testJSONObject() {
        JSONObject obj = JSON.parseObject("{\"key\":\" \"}");

        Double D = obj.getDouble("key");
        assertNull(D);

        double d = obj.getDoubleValue("key");
        assertEquals(0.0, d);

        Float F = obj.getFloat("key");
        assertNull(F);

        float f = obj.getFloatValue("key");
        assertEquals(0.0, f);

        Long L = obj.getLong("key");
        assertNull(L);

        long l = obj.getLongValue("key");
        assertEquals(0, l);

        Integer I = obj.getInteger("key");
        assertNull(I);

        int i = obj.getIntValue("key");
        assertEquals(0, i);

        Short S = obj.getShort("key");
        assertNull(S);

        Byte B = obj.getByte("key");
        assertNull(B);

        BigInteger bigInteger = obj.getBigInteger("key");
        assertNull(bigInteger);

        BigDecimal bigDecimal = obj.getBigDecimal("key");
        assertNull(bigDecimal);
    }

    @Test
    public void testJSONArray() {
        JSONArray arr = JSON.parseArray("[\" \"]");

        Double D = arr.getDouble(0);
        assertNull(D);

        double d = arr.getDoubleValue(0);
        assertEquals(0.0, d);

        Float F = arr.getFloat(0);
        assertNull(F);

        float f = arr.getFloatValue(0);
        assertEquals(0.0, f);

        Long L = arr.getLong(0);
        assertNull(L);

        long l = arr.getLongValue(0);
        assertEquals(0, l);

        Integer I = arr.getInteger(0);
        assertNull(I);

        int i = arr.getIntValue(0);
        assertEquals(0, i);

        Short S = arr.getShort(0);
        assertNull(S);

        short s = arr.getShortValue(0);
        assertEquals(0, s);

        Byte B = arr.getByte(0);
        assertNull(B);

        BigInteger bigInteger = arr.getBigInteger(0);
        assertNull(bigInteger);

        BigDecimal bigDecimal = arr.getBigDecimal(0);
        assertNull(bigDecimal);
    }
}
