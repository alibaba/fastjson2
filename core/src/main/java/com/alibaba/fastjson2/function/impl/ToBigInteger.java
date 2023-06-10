package com.alibaba.fastjson2.function.impl;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.Function;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ToBigInteger
        implements Function {
    @Override
    public Object apply(Object o) {
        if (o == null || o instanceof BigInteger) {
            return o;
        }

        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue() ? BigInteger.ONE : BigInteger.ZERO;
        }

        if (o instanceof Byte
                || o instanceof Short
                || o instanceof Integer
                || o instanceof Long
                || o instanceof AtomicInteger
                || o instanceof AtomicLong
                || o instanceof Float
                || o instanceof Double
        ) {
            return BigInteger.valueOf(((Number) o).longValue());
        }

        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).toBigInteger();
        }

        throw new JSONException("can not cast to BigInteger " + o.getClass());
    }
}
