package com.alibaba.fastjson2.money;

import com.alibaba.fastjson2.JSON;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MonetaryTest {
    @Test
    public void test_unit() {
        CurrencyUnit usd = Monetary.getCurrency("USD");
        String str = JSON.toJSONString(usd);
        assertEquals("\"USD\"", str);
        CurrencyUnit usd2 = JSON.parseObject(str, CurrencyUnit.class);
        assertEquals(usd, usd2);
    }

    @Test
    public void test_amount() {
        CurrencyUnit usd = Monetary.getCurrency("USD");
        MonetaryAmount amount = Monetary.getDefaultAmountFactory()
                .setCurrency(usd).setNumber(200).create();

        String str = JSON.toJSONString(amount);
        assertEquals("{\"currency\":\"USD\",\"number\":200}", str);

        MonetaryAmount amount2 = JSON.parseObject(str, MonetaryAmount.class);
        assertEquals(amount, amount2);
    }

    @Test
    public void test_money() {
        Money oneEuro = Money.of(1, "EUR");
        String str = JSON.toJSONString(oneEuro);
        //System.out.println(str);
        Money money = JSON.parseObject(str, Money.class);
        assertEquals(oneEuro, money);
    }
}
