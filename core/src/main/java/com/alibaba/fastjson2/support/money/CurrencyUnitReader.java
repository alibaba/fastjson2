package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;

final class CurrencyUnitReader
        implements ObjectReader {
    static Method METHOD_GET_CURRENCY;

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        String str = jsonReader.readString();

        if (MoneySupport.CLASS_MONETARY == null) {
            MoneySupport.CLASS_MONETARY = TypeUtils.loadClass("javax.money.Monetary");
        }

        if (METHOD_GET_CURRENCY == null) {
            try {
                METHOD_GET_CURRENCY = MoneySupport.CLASS_MONETARY.getMethod("getCurrency", String.class, String[].class);
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : javax.money.Monetary.getCurrency", e);
            }
        }

        try {
            return METHOD_GET_CURRENCY.invoke(null, str, new String[0]);
        } catch (Exception e) {
            throw new JSONException("getCurrency error", e);
        }
    }
}
