package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.*;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
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

    public static <T> ObjectReader<T> ofReflect(Class<T> objectType) {
        return ObjectReaderCreator.INSTANCE.createObjectReader(objectType);
    }

    public static <T> ObjectReader<T> of(Class<T> objectType) {
        return ObjectReaderCreatorLambda.INSTANCE.createObjectReader(objectType);
    }

    public static <T> ObjectReader<T> createObjectReader(
            Function<Map<Long, Object>, T> creator,
            FieldReader... fieldReaders) {
        return ObjectReaderCreator.INSTANCE.createObjectReaderNoneDefaultConstrutor(null, creator, fieldReaders);
    }

    public static <T, U, R> ObjectReader<T> createObjectReader(BiFunction<T, U, R> function, FieldReader first, FieldReader second) {
        throw new JSONException("TODO");
    }

    public static FieldReader fieldReader(String fieldName, Class fieldType) {
        return ObjectReaderCreator.INSTANCE.createFieldReader(null, fieldName, fieldType, fieldType, (Method) null);
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

    public static <T, U> FieldReader fieldReader(
            String fieldName,
            Type fieldType,
            BiConsumer<T, U> consumer,
            ObjectReader<U> fieldObjectReader
    ) {
        return new FieldReaderObjectFunc2<>(fieldObjectReader, consumer, fieldType, fieldName);
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
}
