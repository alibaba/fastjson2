package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.*;

public class ObjectReaders {
    public static <T> ObjectReader<T> of(
            Supplier<T> defaultCreator,
            FieldReader... fieldReaders
    ) {
        return ObjectReaderCreator.INSTANCE.createObjectReader(null, defaultCreator, fieldReaders);
    }

    public static <T> ObjectReader<T> of(
            Class<T> objectClass,
            Supplier<T> defaultCreator,
            FieldReader... fieldReaders
    ) {
        return ObjectReaderCreator.INSTANCE.createObjectReader(objectClass, defaultCreator, fieldReaders);
    }

    public static <T> ObjectReader<T> ofString(Function<String, T> function) {
        return new ObjectReaderImplFromString<>(null, function);
    }

    public static <T> ObjectReader<T> ofInt(IntFunction<T> function) {
        return new ObjectReaderImplFromInt<>(null, function);
    }

    public static <T> ObjectReader<T> ofLong(LongFunction<T> function) {
        return new ObjectReaderImplFromLong<>(null, function);
    }

    public static <T> ObjectReader<T> fromCharArray(Function<char[], Object> function) {
        return (ObjectReader<T>) new ObjectReaderImplCharValueArray(function);
    }

    public static <T> ObjectReader<T> fromByteArray(Function<byte[], Object> function) {
        return (ObjectReader<T>) new ObjectReaderImplInt8ValueArray(function, "base64");
    }

    public static <T> ObjectReader<T> fromShortArray(Function<short[], Object> function) {
        return (ObjectReader<T>) new ObjectReaderImplInt16ValueArray(function);
    }

    public static <T> ObjectReader<T> fromIntArray(Function<int[], Object> function) {
        return (ObjectReader<T>) new ObjectReaderImplInt32ValueArray(null, function);
    }

    public static <T> ObjectReader<T> fromLongArray(Function<long[], Object> function) {
        return (ObjectReader<T>) new ObjectReaderImplInt64ValueArray(null, function);
    }

    public static <T> ObjectReader<T> fromFloatArray(Function<float[], Object> function) {
        return (ObjectReader<T>) new ObjectReaderImplFloatValueArray(function);
    }

    public static <T> ObjectReader<T> fromDoubleArray(Function<double[], Object> function) {
        return (ObjectReader<T>) new ObjectReaderImplDoubleValueArray(function);
    }

    public static <T> ObjectReader<T> fromBigDecimal(Function<BigDecimal, Object> function) {
        return (ObjectReader<T>) new ObjectReaderImplBigDecimal(function);
    }

    public static <T> ObjectReader<T> objectReader(
            Class<T> objectClass,
            Supplier<T> defaultCreator,
            FieldReader... fieldReaders
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
            FieldReader... fieldReaders) {
        return ObjectReaderCreator.INSTANCE.createObjectReaderNoneDefaultConstructor(
                null,
                creator,
                fieldReaders
        );
    }

    public static FieldReader fieldReader(String fieldName, Class fieldClass) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(null, fieldName, fieldClass, fieldClass, null);
    }

    public static FieldReader fieldReader(String fieldName, Type fieldType, Class fieldClass) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(null, fieldName, fieldType, fieldClass, null);
    }

    public static <T> FieldReader fieldReaderBool(String fieldName, ObjBoolConsumer<T> function) {
        return new FieldReaderBoolValFunc<>(fieldName, 0, null, null, function);
    }

    public static <T> FieldReader fieldReaderByte(String fieldName, ObjByteConsumer<T> function) {
        return new FieldReaderInt8ValueFunc<>(fieldName, 0, null, null, function);
    }

    public static <T> FieldReader fieldReaderShort(String fieldName, ObjShortConsumer<T> function) {
        return new FieldReaderInt16ValueFunc<>(fieldName, 0, 0L, null, null, null, null, null, function);
    }

    public static <T> FieldReader fieldReaderInt(String fieldName, ObjIntConsumer<T> function) {
        return new FieldReaderInt32ValueFunc<>(fieldName, 0, null, null, null, function);
    }

    public static <T> FieldReader fieldReaderLong(String fieldName, ObjLongConsumer<T> function) {
        return new FieldReaderInt64ValueFunc<>(fieldName, 0, null, null, null, function);
    }

    public static <T> FieldReader fieldReaderChar(String fieldName, ObjCharConsumer<T> function) {
        return new FieldReaderCharValueFunc<>(fieldName, 0, null, null, null, null, function);
    }

    public static <T> FieldReader fieldReaderFloat(String fieldName, ObjFloatConsumer<T> function) {
        return new FieldReaderFloatValueFunc<>(fieldName, 0, null, null, null, function);
    }

    public static <T> FieldReader fieldReaderDouble(String fieldName, ObjDoubleConsumer<T> function) {
        return new FieldReaderDoubleValueFunc<>(fieldName, 0, null, null, null, function);
    }

    public static <T> FieldReader fieldReaderString(
            String fieldName,
            BiConsumer<T, String> function
    ) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(fieldName, String.class, String.class, null, function);
    }

    public static <T, V> FieldReader fieldReader(
            String fieldName,
            Class<V> fieldClass,
            BiConsumer<T, V> function
    ) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(fieldName, fieldClass, fieldClass, null, function);
    }

    public static <T, V> FieldReader fieldReader(
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            BiConsumer<T, V> function
    ) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(fieldName, fieldType, fieldClass, null, function);
    }

    public static <T, V> FieldReader fieldReader(
            String fieldName,
            Type fieldType,
            BiConsumer<T, V> consumer,
            ObjectReader<V> fieldObjectReader
    ) {
//        return new FieldReaderObjectFunc2<>(fieldObjectReader, consumer, fieldType, fieldName);
        return new FieldReaderObjectFunc<>(
                fieldName,
                fieldType,
                (Class<V>) TypeUtils.getClass(fieldType),
                0,
                0,
                null,
                null,
                null,
                null,
                null,
                consumer,
                fieldObjectReader
        );
    }

    public static <T, V> FieldReader fieldReaderList(
            String fieldName,
            Type itemType,
            Supplier<List<V>> listCreator,
            BiConsumer<T, List<V>> function,
            ObjectReader<V> itemObjectReader
    ) {
        return new FieldReaderListFuncImpl<>(listCreator, itemObjectReader, function, itemType, fieldName);
    }

    public static <T, V> FieldReader fieldReaderList(
            String fieldName,
            Type itemType,
            Supplier<List<V>> listCreator,
            BiConsumer<T, List<V>> function
    ) {
        return new FieldReaderListFuncImpl<>(listCreator, null, function, itemType, fieldName);
    }

    public static <T> FieldReader fieldReaderListStr(
            String fieldName,
            BiConsumer<T, List<String>> function
    ) {
        return new FieldReaderListFuncImpl<>(ArrayList::new, null, function, String.class, fieldName);
    }

    public static <T, V> FieldReader fieldReaderList(
            String fieldName,
            Type itemType,
            BiConsumer<T, List<V>> function
    ) {
        return fieldReaderList(fieldName, itemType, ArrayList::new, function);
    }
}
