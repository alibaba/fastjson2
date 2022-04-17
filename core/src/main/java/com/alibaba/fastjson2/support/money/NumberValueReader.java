package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import org.javamoney.moneta.spi.DefaultNumberValue;

import java.math.BigDecimal;

public class NumberValueReader implements ObjectReader {
    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        BigDecimal decimal = jsonReader.readBigDecimal();
        return DefaultNumberValue.of(decimal);
    }
}
