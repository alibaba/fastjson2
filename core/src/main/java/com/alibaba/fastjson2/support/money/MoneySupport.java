package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.writer.ObjectWriter;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmountFactory;
import javax.money.NumberValue;

public class MoneySupport {
    public static ObjectReader createCurrencyUnitReader() {
        return new CurrencyUnitReader();
    }

    public static ObjectReader createMonetaryAmountReader() {
        try {
            return ObjectReaderCreator.INSTANCE.createObjectReaderFactoryMethod(
                    MoneySupport.class.getMethod("createMonetaryAmount", CurrencyUnit.class, NumberValue.class), "currency", "number"
            );
        } catch (NoSuchMethodException e) {
            throw new JSONException("createMonetaryAmountReader error", e);
        }
    }

    public static ObjectReader createNumberValueReader() {
        return new NumberValueReader();
    }

    public static ObjectWriter createMonetaryAmountWriter() {
        return new MonetaryAmountWriter();
    }

    public static ObjectWriter createNumberValueWriter() {
        return new NumberValueWriter();
    }

    public static Object createMonetaryAmount(CurrencyUnit currency, NumberValue number) {
        MonetaryAmountFactory<?> factory = Monetary.getDefaultAmountFactory();
        if (currency != null) {
            factory.setCurrency(currency);
        }
        if (number != null) {
            factory.setNumber(number);
        }
        return factory.create();
    }
}
