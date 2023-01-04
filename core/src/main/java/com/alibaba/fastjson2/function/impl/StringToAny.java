package com.alibaba.fastjson2.function.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class StringToAny
        implements Function {
    final Object defaultValue;
    final Class targetClass;

    public StringToAny(Class targetClass, Object defaultValue) {
        this.targetClass = targetClass;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(Object from) {
        String str = (String) from;
        if (str == null || "null".equals(str) || "".equals(str)) {
            return defaultValue;
        }

        if (targetClass == byte.class || targetClass == Byte.class) {
            return Byte.parseByte(str);
        }

        if (targetClass == short.class || targetClass == Short.class) {
            return Short.parseShort(str);
        }

        if (targetClass == int.class || targetClass == Integer.class) {
            return Integer.parseInt(str);
        }

        if (targetClass == long.class || targetClass == Long.class) {
            return Long.parseLong(str);
        }

        if (targetClass == float.class || targetClass == Float.class) {
            return Float.parseFloat(str);
        }

        if (targetClass == double.class || targetClass == Double.class) {
            return Double.parseDouble(str);
        }

        if (targetClass == char.class || targetClass == Character.class) {
            return str.charAt(0);
        }

        if (targetClass == boolean.class || targetClass == Boolean.class) {
            return "true".equals(str);
        }

        if (targetClass == BigDecimal.class) {
            return new BigDecimal(str);
        }

        if (targetClass == BigInteger.class) {
            return new BigInteger(str);
        }

        if (targetClass == Collections.class || targetClass == List.class || targetClass == JSONArray.class) {
            if ("[]".equals(str)) {
                return new JSONArray();
            }
        }

        throw new JSONException("can not convert to " + targetClass + ", value : " + str);
    }
}
