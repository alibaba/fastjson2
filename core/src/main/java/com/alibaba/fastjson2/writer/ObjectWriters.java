package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.ToByteFunction;
import com.alibaba.fastjson2.function.ToFloatFunction;
import com.alibaba.fastjson2.function.ToShortFunction;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.*;

public class ObjectWriters {
    static final ObjectWriterCreator INSTANCE = ObjectWriterCreator.INSTANCE;

    public static ObjectWriter ofReflect(Class objectType) {
        return ObjectWriterCreator.INSTANCE.createObjectWriter(objectType);
    }

    public static ObjectWriter objectWriter(Class objectType) {
        return INSTANCE.createObjectWriter(objectType);
    }

    public static ObjectWriter objectWriter(Class objectType, FieldWriter... fieldWriters) {
        return INSTANCE.createObjectWriter(objectType, fieldWriters);
    }

    public static <T> ObjectWriter<T> of(Class<T> objectType, FieldWriter... fieldWriters) {
        return INSTANCE.createObjectWriter(objectType, fieldWriters);
    }

    public static ObjectWriter objectWriter(Class objectType, long features, FieldWriter... fieldWriters) {
        return INSTANCE.createObjectWriter(objectType, features, fieldWriters);
    }

    public static ObjectWriter objectWriter(FieldWriter... fieldWriters) {
        return INSTANCE.createObjectWriter(fieldWriters);
    }

    public static <T> ObjectWriter ofToString(Function<T, String> function) {
        return INSTANCE.createObjectWriter(
                INSTANCE.createFieldWriter(
                        null,
                        null,
                        "toString",
                        0,
                        FieldInfo.VALUE_MASK,
                        null,
                        null,
                        String.class,
                        String.class,
                        null,
                        function
                )
        );
    }

    public static <T> ObjectWriter ofToInt(ToIntFunction function) {
        return INSTANCE.createObjectWriter(
                new FieldWriterInt32ValFunc(
                        "toInt",
                        0,
                        FieldInfo.VALUE_MASK,
                        null,
                        null,
                        null,
                        function
                )
        );
    }

    public static <T> ObjectWriter ofToLong(ToLongFunction function) {
        return INSTANCE.createObjectWriter(
                new FieldWriterInt64ValFunc(
                        "toLong",
                        0,
                        FieldInfo.VALUE_MASK,
                        null,
                        null,
                        null,
                        function
                )
        );
    }

    public static <T> ObjectWriter ofToByteArray(Function<Object, byte[]> function) {
        return new ObjectWriterImplInt8ValueArray(function);
    }

    public static <T> ObjectWriter ofToShortArray(Function<Object, short[]> function) {
        return new ObjectWriterImplInt16ValueArray(function);
    }

    public static <T> ObjectWriter ofToIntArray(Function<Object, int[]> function) {
        return new ObjectWriterImplInt32ValueArray(function);
    }

    public static <T> ObjectWriter ofToLongArray(Function<Object, long[]> function) {
        return new ObjectWriterImplInt64ValueArray(function);
    }

    public static <T> ObjectWriter ofToCharArray(Function<Object, char[]> function) {
        return new ObjectWriterImplCharValueArray(function);
    }

    public static <T> ObjectWriter ofToFloatArray(Function<Object, float[]> function) {
        return new ObjectWriterImplFloatValueArray(function, null);
    }

    public static <T> ObjectWriter ofToDoubleArray(Function<Object, double[]> function) {
        return new ObjectWriterImplDoubleValueArray(function, null);
    }

    public static <T> ObjectWriter ofToBooleanArray(Function<Object, boolean[]> function) {
        return new ObjectWriterImplBoolValueArray(function);
    }

    public static <T> ObjectWriter ofToBooleanArray(
            ToIntFunction functionSize,
            BiFunction<Object, Integer, Boolean> functionGet
    ) {
        return new ObjectWriterImplBoolValueArrayLambda(functionSize, functionGet);
    }

    public static <T> ObjectWriter ofToBigDecimal(Function<Object, BigDecimal> function) {
        return new ObjectWriterImplBigDecimal(null, function);
    }

    public static <T> ObjectWriter ofToBooleanArray(
            ToLongFunction functionSize,
            BiFunction<Object, Integer, Boolean> functionGet
    ) {
        ToIntFunction functionSizeInt = o -> (int) functionSize.applyAsLong(o);
        return new ObjectWriterImplBoolValueArrayLambda(functionSizeInt, functionGet);
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

    public static <T, V> FieldWriter fieldWriter(String fieldName,
                                                 Type fieldType,
                                                 Class<V> fieldClass,
                                                 Function<T, V> function) {
        return INSTANCE.createFieldWriter(fieldName, fieldType, fieldClass, function);
    }

    public static <T, V> FieldWriter fieldWriterList(String fieldName,
                                                     Class<V> itemType,
                                                     Function<T, List<V>> function) {
        ParameterizedType listType;
        if (itemType == String.class) {
            listType = TypeUtils.PARAM_TYPE_LIST_STR;
        } else {
            listType = new ParameterizedTypeImpl(List.class, itemType);
        }
        return INSTANCE.createFieldWriter(fieldName, listType, List.class, function);
    }

    public static <T> FieldWriter fieldWriterListString(String fieldName, Function<T, List<String>> function) {
        return INSTANCE.createFieldWriter(fieldName, TypeUtils.PARAM_TYPE_LIST_STR, List.class, function);
    }
}
