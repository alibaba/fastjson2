package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertSame;

public class CurrencyTest4 {
    @Test
    public void test_0() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("currency", "CNY");

        String text = JSON.toJSONString(jsonObject);

        Currency currency = JSON.parseObject(text, Currency.class);

        assertSame(Currency.getInstance("CNY"), currency);
    }

    @Test
    public void test_1() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("currencyCode", "CNY");

        String text = JSON.toJSONString(jsonObject);

        Currency currency = JSON.parseObject(text, Currency.class);

        assertSame(Currency.getInstance("CNY"), currency);
    }

    public static class VO {
        public Currency value;
    }
}
