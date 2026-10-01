package com.alibaba.fastjson.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrimitiveArrayTypeRegressionTest {
    @Test
    public void preservesEveryDimension() {
        for (Class<?> primitive : new Class<?>[]{boolean.class, byte.class, short.class, char.class,
                int.class, long.class, float.class, double.class}) {
            Type type = primitive;
            for (int dimension = 1; dimension <= 5; dimension++) {
                Type component = type;
                type = (GenericArrayType) () -> component;
                assertEquals(Array.newInstance(primitive, new int[dimension]).getClass(),
                        TypeUtils.checkPrimitiveArray((GenericArrayType) type));
            }
        }
    }
}
