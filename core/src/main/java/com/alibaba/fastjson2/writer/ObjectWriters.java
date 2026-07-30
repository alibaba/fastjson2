package com.alibaba.fastjson2.writer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.function.*;

public class ObjectWriters {
    static final ObjectWriterCreator INSTANCE = ObjectWriterCreator.INSTANCE;

    public static ObjectWriter ofReflect(Class objectType) {
        return ObjectWriterCreator.INSTANCE.createObjectWriter(objectType);
    }

    public static ObjectWriter objectWriter(Class objectType) {
        return INSTANCE.createObjectWriter(objectType);
    }

    public static ObjectWriter objectWriter(Class objectType, Object... fieldWriters) {
        return INSTANCE.createObjectWriter(objectType, fieldWriters);
    }

    public static <T> ObjectWriter<T> of(Class<T> objectType, Object... fieldWriters) {
        return INSTANCE.createObjectWriter(objectType, fieldWriters);
    }

    public static ObjectWriter objectWriter(Class objectType, long features, Object... fieldWriters) {
        return INSTANCE.createObjectWriter(objectType, features, fieldWriters);
    }

    public static ObjectWriter objectWriter(Object... fieldWriters) {
        return INSTANCE.createObjectWriter(fieldWriters);
    }

    public static <T> ObjectWriter ofToString(Function<T, String> function) {
        return INSTANCE.createObjectWriter(
                INSTANCE.createFieldWriter(
                        null, null, "toString", 0, 0, null, null,
                        String.class, String.class, null, function
                )
        );
    }

    public static <T> ObjectWriter ofToInt(ToIntFunction function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToLong(ToLongFunction function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToByteArray(Function<Object, byte[]> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToShortArray(Function<Object, short[]> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToIntArray(Function<Object, int[]> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToLongArray(Function<Object, long[]> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToCharArray(Function<Object, char[]> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToFloatArray(Function<Object, float[]> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToDoubleArray(Function<Object, double[]> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToBooleanArray(Function<Object, boolean[]> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToBooleanArray(
            ToIntFunction functionSize,
            BiFunction<Object, Integer, Boolean> functionGet
    ) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToBigDecimal(Function<Object, BigDecimal> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectWriter ofToBooleanArray(
            ToLongFunction functionSize,
            BiFunction<Object, Integer, Boolean> functionGet
    ) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, ToLongFunction function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, ToIntFunction function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, ToShortFunction function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, ToByteFunction function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, ToFloatFunction function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, ToDoubleFunction function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, ToCharFunction function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, Predicate function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, Function function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, Class fieldClass, Function function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, Field field) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, Method method) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(Class objectClass, String fieldName, Method method) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriter(String fieldName, Type fieldType, Class fieldClass, Function function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriterList(String fieldName, Class itemType, Function function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldWriterListString(String fieldName, Function function) {
        throw new UnsupportedOperationException();
    }
}
