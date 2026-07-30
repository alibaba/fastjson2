package com.alibaba.fastjson2.writer;

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

    public static <T> ObjectWriter ofToString(Function<T, String> function) {
        throw new UnsupportedOperationException();
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
}
