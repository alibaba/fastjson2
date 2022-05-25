package com.alibaba.fastjson2.support.money;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;

public class NumberValueWriter
        implements ObjectWriter {
    static Method METHOD_NUMBER_VALUE;

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (MoneySupport.CLASS_NUMBER_VALUE == null) {
            MoneySupport.CLASS_NUMBER_VALUE = TypeUtils.loadClass("javax.money.NumberValue");
        }

        if (METHOD_NUMBER_VALUE == null) {
            try {
                METHOD_NUMBER_VALUE = MoneySupport.CLASS_NUMBER_VALUE.getMethod("numberValue", Class.class);
            } catch (NoSuchMethodException e) {
                throw new JSONException("method not found : javax.money.NumberValue.numberValue", e);
            }
        }

        BigDecimal decimal;
        try {
            decimal = (BigDecimal) METHOD_NUMBER_VALUE.invoke(object, BigDecimal.class);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("numberValue error", e);
        }
        jsonWriter.writeDecimal(decimal);
    }
}
