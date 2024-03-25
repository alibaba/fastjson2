package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue2305 {
    BigInteger bigInt = new BigInteger("18446744073709550616");
    String s = "{\"value\": " + bigInt + "}";

    @Test
    public void test() {
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(s, Bean.class));

        Bean bean = JSON.parseObject(s, Bean.class, JSONReader.Feature.NonErrorOnNumberOverflow);
        assertEquals(bean.value, bigInt.longValue());
    }

    public static class Bean {
        public long value;
    }

    @Test
    public void testInt() {
        assertThrows(
                JSONException.class,
                () -> JSON.parseObject(s, Bean1.class));

        Bean1 bean = JSON.parseObject(s, Bean1.class, JSONReader.Feature.NonErrorOnNumberOverflow);
        assertEquals(bean.value, bigInt.intValue());
    }

    public static class Bean1 {
        public int value;
    }
}
