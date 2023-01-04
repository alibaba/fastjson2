package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.javamoney.moneta.spi.DefaultNumberValue;
import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;
import javax.money.NumberValue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoneySupportTest {
    @Test
    public void test() {
        MoneySupport.CLASS_NUMBER_VALUE = null;
        ObjectWriter writer = MoneySupport.createNumberValueWriter();
        NumberValue numberValue = DefaultNumberValue.of(123);
        JSONWriter jsonWriter = JSONWriter.of();
        writer.write(jsonWriter, numberValue);
        assertEquals("123", jsonWriter.toString());
    }

    @Test
    public void test1() {
        MoneySupport.CLASS_NUMBER_VALUE = null;

        String str = "{\"currency\":\"USD\",\"number\":200}";
        MonetaryAmount amount = JSON.parseObject(str, MonetaryAmount.class);
        assertEquals("USD", amount.getCurrency().getCurrencyCode());

        MoneySupport.CLASS_NUMBER_VALUE = null;
        MoneySupport.CLASS_CURRENCY_UNIT = null;
        MoneySupport.CLASS_MONETARY_AMOUNT_FACTORY = null;

        Object amount1 = MoneySupport.createMonetaryAmount(amount.getCurrency(), amount.getNumber());
        assertEquals(amount, amount1);
    }

    @Test
    public void test2() {
        MoneySupport.CLASS_DEFAULT_NUMBER_VALUE = null;
        MoneySupport.METHOD_NUMBER_VALUE_OF = null;
        MoneySupport.CLASS_NUMBER_VALUE = null;

        NumberValue numberValue = JSON.parseObject("2000", NumberValue.class);
        assertEquals(2000, numberValue.intValue());

        NumberValue numberValue1 = JSON.parseObject("1000", NumberValue.class);
        assertEquals(1000, numberValue1.intValue());
    }
}
