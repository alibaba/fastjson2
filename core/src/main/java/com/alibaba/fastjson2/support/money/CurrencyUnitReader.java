package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import javax.money.Monetary;

final class CurrencyUnitReader implements ObjectReader {
    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        String str = jsonReader.readString();
        return Monetary.getCurrency(str);
    }
}
