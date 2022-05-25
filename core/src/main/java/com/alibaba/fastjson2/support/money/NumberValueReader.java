package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;

public class NumberValueReader
        implements ObjectReader {
    static Class CLASS_NUMBER_VALUE;
    static Method METHOD_OF;

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        BigDecimal decimal = jsonReader.readBigDecimal();

        if (CLASS_NUMBER_VALUE == null) {
            CLASS_NUMBER_VALUE = TypeUtils.loadClass("org.javamoney.moneta.spi.DefaultNumberValue");
        }

        if (METHOD_OF == null) {
            try {
                METHOD_OF = CLASS_NUMBER_VALUE.getMethod("of", Number.class);
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : org.javamoney.moneta.spi.DefaultNumberValue.of", e);
            }
        }

        try {
            return METHOD_OF.invoke(null, decimal);
        } catch (Exception e) {
            throw new JSONException("create NumberValue error", e);
        }
    }
}
