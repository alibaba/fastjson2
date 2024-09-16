package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserCompatible;
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteBigDecimalAsPlain;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2854 {
    @Test
    public void test() {
        BigDecimal decimal = new BigDecimal("12345678901234567890");
        String expected = "\"12345678901234567890\"";
        assertEquals(expected, JSON.toJSONString(decimal, WriteBigDecimalAsPlain, BrowserCompatible));

        assertEquals(expected,
                new String(JSON.toJSONBytes(decimal, WriteBigDecimalAsPlain, BrowserCompatible)));

        Bean bean = new Bean();
        bean.decimal = decimal;

        assertEquals("{\"decimal\":\"12345678901234567890\"}",
                JSON.toJSONString(bean, WriteBigDecimalAsPlain, BrowserCompatible));

        assertEquals("{\"decimal\":\"12345678901234567890\"}",
                new String(JSON.toJSONBytes(bean, WriteBigDecimalAsPlain, BrowserCompatible)));
    }

    public static class Bean {
        public BigDecimal decimal;
    }
}
