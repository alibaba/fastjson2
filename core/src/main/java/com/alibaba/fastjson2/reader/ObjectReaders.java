package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.*;

public class ObjectReaders {
    public static <T> ObjectReader<T> of(
            Supplier<T> defaultCreator,
            Object... fieldReaders
    ) {
        return ObjectReaderCreator.INSTANCE.createObjectReader(null, defaultCreator, fieldReaders);
    }

    public static <T> ObjectReader<T> of(
            Class<T> objectClass,
            Supplier<T> defaultCreator,
            Object... fieldReaders
    ) {
        return ObjectReaderCreator.INSTANCE.createObjectReader(objectClass, defaultCreator, fieldReaders);
    }

    public static <T> ObjectReader<T> ofString(Function<String, T> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> ofInt(IntFunction<T> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> ofLong(LongFunction<T> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> fromCharArray(Function<char[], Object> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> fromByteArray(Function<byte[], Object> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> fromShortArray(Function<short[], Object> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> fromIntArray(Function<int[], Object> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> fromLongArray(Function<long[], Object> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> fromFloatArray(Function<float[], Object> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> fromDoubleArray(Function<double[], Object> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> fromBigDecimal(Function<BigDecimal, Object> function) {
        throw new UnsupportedOperationException();
    }

    public static <T> ObjectReader<T> objectReader(
            Class<T> objectClass,
            Supplier<T> defaultCreator,
            Object... fieldReaders
    ) {
        return ObjectReaderCreator.INSTANCE.createObjectReader(objectClass, defaultCreator, fieldReaders);
    }

    public static <T> ObjectReader<T> ofReflect(Class<T> objectType) {
        return ObjectReaderCreator.INSTANCE.createObjectReader(objectType);
    }

    public static <T> ObjectReader<T> of(Class<T> objectType) {
        return ObjectReaderCreator.INSTANCE.createObjectReader(objectType);
    }

    public static <T> ObjectReader<T> objectReader(
            Function<Map<Long, Object>, T> creator,
            Object... fieldReaders) {
        return ObjectReaderCreator.INSTANCE.createObjectReaderNoneDefaultConstructor(
                null,
                creator,
                fieldReaders
        );
    }

    public static Object fieldReader(String fieldName, Class fieldClass) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(null, fieldName, fieldClass, fieldClass, null);
    }

    public static Object fieldReader(String fieldName, Type fieldType, Class fieldClass) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(null, fieldName, fieldType, fieldClass, null);
    }

    public static Object fieldReaderBool(String fieldName, ObjBoolConsumer function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderByte(String fieldName, ObjByteConsumer function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderShort(String fieldName, ObjShortConsumer function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderInt(String fieldName, ObjIntConsumer function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderLong(String fieldName, ObjLongConsumer function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderChar(String fieldName, ObjCharConsumer function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderFloat(String fieldName, ObjFloatConsumer function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderDouble(String fieldName, ObjDoubleConsumer function) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderString(
            String fieldName,
            BiConsumer function
    ) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(fieldName, String.class, String.class, null, function);
    }

    public static Object fieldReader(
            String fieldName,
            Class fieldClass,
            BiConsumer function
    ) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(fieldName, fieldClass, fieldClass, null, function);
    }

    public static Object fieldReader(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            BiConsumer function
    ) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(fieldName, fieldType, fieldClass, null, function);
    }

    public static Object fieldReader(
            String fieldName,
            Type fieldType,
            BiConsumer consumer,
            ObjectReader fieldObjectReader
    ) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderList(
            String fieldName,
            Type itemType,
            Supplier<List> listCreator,
            BiConsumer function,
            ObjectReader itemObjectReader
    ) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderList(
            String fieldName,
            Type itemType,
            Supplier<List> listCreator,
            BiConsumer function
    ) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderListStr(
            String fieldName,
            BiConsumer function
    ) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderList(
            String fieldName,
            Type itemType,
            BiConsumer function
    ) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderMap(
            String fieldName,
            Class mapClass,
            Type keyType,
            Type valueType,
            BiConsumer function
    ) {
        throw new UnsupportedOperationException();
    }

    public static Object fieldReaderWithField(String fieldName, Class objectClass) {
        Field field = BeanUtils.getDeclaredField(objectClass, fieldName);
        return ObjectReaderCreator.INSTANCE.createFieldReader(
                fieldName,
                field
        );
    }

    public static Object fieldReaderWithField(String name, Class objectClass, String fieldName) {
        Field field = BeanUtils.getDeclaredField(objectClass, fieldName);
        return ObjectReaderCreator.INSTANCE.createFieldReader(
                name,
                field
        );
    }

    public static Object fieldReaderWithMethod(String name, Class objectClass, String methodName) {
        Method method = BeanUtils.getSetter(objectClass, methodName);
        return ObjectReaderCreator.INSTANCE.createFieldReader(
                name,
                method
        );
    }
}
