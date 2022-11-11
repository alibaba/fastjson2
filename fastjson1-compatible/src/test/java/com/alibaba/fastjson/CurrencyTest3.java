package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurrencyTest3 {
    public static class Money {
        public Currency currency;
        public BigDecimal amount;

        @Override
        public String toString() {
            return "Money{currency=" + currency + ", amount=" + amount + '}';
        }
    }

    @Test
    public void testJson() throws Exception {
        Money money = new Money();
        money.currency = Currency.getInstance("CNY");
        money.amount = new BigDecimal("10.03");

        String json = JSON.toJSONString(money);
        assertEquals("{\"amount\":10.03,\"currency\":\"CNY\"}", json);

        Money moneyBack = JSON.parseObject(json, Money.class);
        assertEquals(money.amount, moneyBack.amount);
        assertEquals(money.currency, moneyBack.currency);

        JSONObject jsonObject = JSON.parseObject(json);
        Money moneyCast = JSON.toJavaObject(jsonObject, Money.class);
        assertEquals(money.amount, moneyCast.amount);
        assertEquals(money.currency, moneyCast.currency);
    }
}
