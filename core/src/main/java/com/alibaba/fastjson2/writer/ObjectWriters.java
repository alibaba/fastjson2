package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.function.ToByteFunction;
import com.alibaba.fastjson2.function.ToFloatFunction;
import com.alibaba.fastjson2.function.ToShortFunction;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.*;

public class ObjectWriters {
    static ObjectWriterCreator INSTANCE = ObjectWriterCreatorLambda.INSTANCE;

    public static ObjectWriter ofReflect(Class objectType) {
        return ObjectWriterCreator.INSTANCE.createObjectWriter(objectType);
    }

    public static ObjectWriter objectWriter(Class objectType) {
        return INSTANCE.createObjectWriter(objectType);
    }

    public static ObjectWriter objectWriter(Class objectType, FieldWriter... fieldWriters) {
        return INSTANCE.createObjectWriter(objectType, fieldWriters);
    }

    public static ObjectWriter of(Class objectType, FieldWriter... fieldWriters) {
        return INSTANCE.createObjectWriter(objectType, fieldWriters);
    }

    public static ObjectWriter objectWriter(Class objectType, long features, FieldWriter... fieldWriters) {
        return INSTANCE.createObjectWriter(objectType, features, fieldWriters);
    }

    public static ObjectWriter objectWriter(FieldWriter... fieldWriters) {
        return INSTANCE.createObjectWriter(fieldWriters);
    }

    public static <T> FieldWriter fieldWriter(String fieldName, ToLongFunction<T> function) {
        return INSTANCE.createFieldWriter(fieldName, function);
    }

    public static <T> FieldWriter fieldWriter(String fieldName, ToIntFunction<T> function) {
        return INSTANCE.createFieldWriter(fieldName, function);
    }

    public static <T> FieldWriter fieldWriter(String fieldName, ToShortFunction<T> function) {
        return INSTANCE.createFieldWriter(fieldName, function);
    }

    public static <T> FieldWriter fieldWriter(String fieldName, ToByteFunction<T> function) {
        return INSTANCE.createFieldWriter(fieldName, function);
    }

    public static <T> FieldWriter fieldWriter(String fieldName, ToFloatFunction<T> function) {
        return INSTANCE.createFieldWriter(fieldName, function);
    }

    public static <T> FieldWriter fieldWriter(String fieldName, ToDoubleFunction<T> function) {
        return INSTANCE.createFieldWriter(fieldName, function);
    }

    public static <T> FieldWriter fieldWriter(String fieldName, Predicate<T> function) {
        return INSTANCE.createFieldWriter(fieldName, function);
    }

    public static <T> FieldWriter fieldWriter(String fieldName, Function<T, String> function) {
        return INSTANCE.createFieldWriter(fieldName, String.class, function);
    }

    public static <T, V> FieldWriter fieldWriter(String fieldName, Class<V> fieldClass, Function<T, V> function) {
        return INSTANCE.createFieldWriter(fieldName, fieldClass, function);
    }

    public static <T, V> FieldWriter fieldWriter(String fieldName, Type fieldType, Class<V> fieldClass, Function<T, V> function) {
        return INSTANCE.createFieldWriter(fieldName, fieldType, fieldClass, function);
    }

    public static <T, V> FieldWriter fieldWriterList(String fieldName, Class<V> itemType, Function<T, List<V>> function) {
        return INSTANCE.createFieldWriter(fieldName, new ParameterizedTypeImpl(List.class, itemType), List.class, function);
    }
}
