package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriters;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.NumberValue;
import java.util.Arrays;

final class MonetaryAmountWriter extends ObjectWriterAdapter {
    public MonetaryAmountWriter() {
        super(javax.money.MonetaryAmount.class, Arrays.asList(
                ObjectWriters.fieldWriter("currency", CurrencyUnit.class, MonetaryAmount::getCurrency),
                ObjectWriters.fieldWriter("number", NumberValue.class, MonetaryAmount::getNumber)
        ));
    }
}
