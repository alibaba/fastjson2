package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.ToByteFunction;
import com.alibaba.fastjson2.function.ToFloatFunction;
import com.alibaba.fastjson2.function.ToShortFunction;
import com.alibaba.fastjson2.modules.ObjectWriterAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.*;

public class ObjectWriterCreator {
    public static final ObjectWriterCreator INSTANCE = new ObjectWriterCreator();

    public ObjectWriterCreator() {

    }

    public ObjectWriter createObjectWriter(List<FieldWriter> fieldWriters) {
        return new ObjectWriterAdapter(null, fieldWriters);
    }

    public ObjectWriter createObjectWriter(FieldWriter... fieldWriters) {
        return new ObjectWriterAdapter(null, 0, fieldWriters);
    }

    public ObjectWriter createObjectWriter(Class objectType) {
        return createObjectWriter(
                objectType
                , 0
                , JSONFactory
                        .getDefaultObjectWriterProvider()
                        .getModules()
        );
    }

    public ObjectWriter createObjectWriter(Class objectType
            , FieldWriter... fieldWriters) {
        return createObjectWriter(objectType, 0, fieldWriters);
    }

    public ObjectWriter createObjectWriter(
            Class objectType
            , long features
            , FieldWriter... fieldWriters
    ) {
        if (fieldWriters.length == 0) {
            return createObjectWriter(objectType, features, Collections.emptyList());
        } else {
            return new ObjectWriterAdapter(objectType, features, fieldWriters);
        }
    }

    protected FieldWriter creteFieldWriter(
            Class objectClass
            , long writerFeatures
            , List<ObjectWriterModule> modules
            , BeanInfo beanInfo
            , FieldInfo fieldInfo
            , Field field
    ) {
        fieldInfo.features = writerFeatures;
        for (ObjectWriterModule module : modules) {
            ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor == null) {
                continue;
            }
            annotationProcessor.getFieldInfo(fieldInfo, objectClass, field);
        }

        if (fieldInfo.ignore) {
            return null;
        }

        String fieldName;
        if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
            fieldName = field.getName();
        } else {
            fieldName = fieldInfo.fieldName;
        }

        if (beanInfo.orders != null) {
            boolean match = false;
            for (int i = 0; i < beanInfo.orders.length; i++) {
                if (fieldName.equals(beanInfo.orders[i])) {
                    fieldInfo.ordinal = i;
                    match = true;
                }
            }
            if (!match) {
                if (fieldInfo.ordinal == 0) {
                    fieldInfo.ordinal = beanInfo.orders.length;
                }
            }
        }

        ObjectWriter writeUsingWriter = null;
        if (fieldInfo.writeUsing != null) {
            try {
                writeUsingWriter = (ObjectWriter) fieldInfo.writeUsing.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create writeUsing Writer error", e);
            }
        }

        field.setAccessible(true);

        if (writeUsingWriter == null && fieldInfo.fieldClassMixIn) {
            writeUsingWriter = ObjectWriterBaseModule.VoidObjectWriter.INSTANCE;
        }

        return createFieldWriter(fieldName, fieldInfo.ordinal, fieldInfo.features, fieldInfo.format, field, writeUsingWriter);
    }

    public ObjectWriter createObjectWriter(
            Class objectClass
            , long features
            , final List<ObjectWriterModule> modules
    ) {
        BeanInfo beanInfo = new BeanInfo();
        for (ObjectWriterModule module : modules) {
            ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor == null) {
                continue;
            }
            annotationProcessor.getBeanInfo(beanInfo, objectClass);
        }

        long writerFeatures = features | beanInfo.writerFeatures;

        boolean fieldBased = (writerFeatures & JSONWriter.Feature.FieldBased.mask) != 0;

        if (fieldBased && (objectClass.isInterface() || objectClass.isInterface())) {
            fieldBased = false;
        }

        if (fieldBased && JDKUtils.JVM_VERSION >= 11
                && !JDKUtils.LANG_UNNAMED
                && Throwable.class.isAssignableFrom(objectClass)) {
            fieldBased = false;
        }


        List<FieldWriter> fieldWriters;
        final FieldInfo fieldInfo = new FieldInfo();


        if (fieldBased) {
            Map<String, FieldWriter> fieldWriterMap = new TreeMap<>();
            BeanUtils.declaredFields(objectClass, field -> {
                if (Modifier.isTransient(field.getModifiers())) {
                    return;
                }

                FieldWriter fieldWriter = creteFieldWriter(objectClass, writerFeatures, Collections.emptyList(), beanInfo, fieldInfo, field);
                if (fieldWriter != null) {
                    fieldWriterMap.put(fieldWriter.getFieldName(), fieldWriter);
                }
            });
            fieldWriters = new ArrayList<>(fieldWriterMap.values());
        } else {
            boolean fieldWritersCreated = false;
            fieldWriters = new ArrayList<>();
            for (ObjectWriterModule module : modules) {
                if (module.createFieldWriters(this, objectClass, fieldWriters)) {
                    fieldWritersCreated = true;
                    break;
                }
            }

            if (!fieldWritersCreated) {
                Map<String, FieldWriter> fieldWriterMap = new TreeMap<>();

                BeanUtils.fields(objectClass, field -> {
                    if (!Modifier.isPublic(field.getModifiers())) {
                        return;
                    }

                    fieldInfo.init();
                    FieldWriter fieldWriter = creteFieldWriter(objectClass, writerFeatures, modules, beanInfo, fieldInfo, field);
                    if (fieldWriter != null) {
                        fieldWriterMap.putIfAbsent(fieldWriter.getFieldName(), fieldWriter);
                    }
                });

                BeanUtils.getters(objectClass, method -> {
                    fieldInfo.init();
                    fieldInfo.features = writerFeatures;
                    for (ObjectWriterModule module : modules) {
                        ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                        if (annotationProcessor == null) {
                            continue;
                        }
                        annotationProcessor.getFieldInfo(fieldInfo, objectClass, method);
                    }
                    if (fieldInfo.ignore) {
                        return;
                    }

                    String fieldName;
                    if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
                        fieldName = BeanUtils.getterName(method.getName(), beanInfo.namingStrategy);
                    } else {
                        fieldName = fieldInfo.fieldName;
                    }

                    if (beanInfo.includes != null && beanInfo.includes.length > 0) {
                        boolean match = false;
                        for (String include : beanInfo.includes) {
                            if (include.equals(fieldName)) {
                                match = true;
                                break;
                            }
                        }
                        if (!match) {
                            return;
                        }
                    }

                    if (beanInfo.orders != null) {
                        boolean match = false;
                        for (int i = 0; i < beanInfo.orders.length; i++) {
                            if (fieldName.equals(beanInfo.orders[i])) {
                                fieldInfo.ordinal = i;
                                match = true;
                            }
                        }
                        if (!match) {
                            if (fieldInfo.ordinal == 0) {
                                fieldInfo.ordinal = beanInfo.orders.length;
                            }
                        }
                    }

                    ObjectWriter writeUsingWriter = null;
                    if (fieldInfo.writeUsing != null) {
                        try {
                            writeUsingWriter = (ObjectWriter) fieldInfo.writeUsing.newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new JSONException("create writeUsing Writer error", e);
                        }
                    }

                    if (writeUsingWriter == null && fieldInfo.fieldClassMixIn) {
                        writeUsingWriter = ObjectWriterBaseModule.VoidObjectWriter.INSTANCE;
                    }

                    FieldWriter fieldWriter
                            = createFieldWriter(objectClass
                            , fieldName
                            , fieldInfo.ordinal
                            , fieldInfo.features
                            , fieldInfo.format
                            , method
                            , writeUsingWriter
                    );

                    FieldWriter origin = fieldWriterMap.putIfAbsent(fieldWriter.getFieldName(), fieldWriter);

                    if (origin != null && origin.compareTo(fieldWriter) > 0) {
                        fieldWriterMap.put(fieldName, fieldWriter);
                    }
                });

                fieldWriters = new ArrayList<>(fieldWriterMap.values());
            }
        }

        if ((!fieldBased) && Throwable.class.isAssignableFrom(objectClass)) {
            return new ObjectWriterException(objectClass, writerFeatures, fieldWriters);
        }

        handleIgnores(beanInfo, fieldWriters);

        Collections.sort(fieldWriters);

        return new ObjectWriterAdapter(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
    }

    protected void handleIgnores(BeanInfo beanInfo, List<FieldWriter> fieldWriters) {
        if (beanInfo.ignores != null && beanInfo.ignores.length > 0) {
            for (int i = fieldWriters.size() - 1; i >= 0; i--) {
                FieldWriter fieldWriter = fieldWriters.get(i);
                for (String ignore : beanInfo.ignores) {
                    if (ignore.equals(fieldWriter.getFieldName())) {
                        fieldWriters.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public <T> FieldWriter<T> createFieldWriter(String fieldName, String format, Field field) {
        return createFieldWriter(fieldName, 0, 0L, format, field, null);
    }

    public <T> FieldWriter<T> createFieldWriter(
            String fieldName
            , int ordinal
            , long features
            , String format
            , Field field
    ) {
        return createFieldWriter(fieldName, ordinal, features, format, field, null);
    }

    public <T> FieldWriter<T> createFieldWriter(
            String fieldName
            , int ordinal
            , long features
            , String format
            , Field field
            , ObjectWriter initObjectWriter
    ) {
        field.setAccessible(true);

        Class<?> fieldClass = field.getType();

        if (initObjectWriter != null) {
            FieldWriterObjectField objImp = new FieldWriterObjectField(fieldName, ordinal, features, format, field.getGenericType(), fieldClass, field);
            objImp.initValueClass = fieldClass;
            if (initObjectWriter != ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
                objImp.initObjectWriter = initObjectWriter;
            }
            return objImp;
        }

        if (fieldClass == boolean.class) {
            return new FieldWriterBoolValField(fieldName, ordinal, features, field, fieldClass);
        }

        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            return new FieldWriterBooleanField(fieldName, ordinal, features, field, fieldClass);
        }

        if (fieldClass == byte.class) {
            return new FieldWriterInt8ValField(fieldName, ordinal, field);
        }

        if (fieldClass == short.class) {
            return new FieldWriterInt16ValField(fieldName, ordinal, field);
        }

        if (fieldClass == int.class) {
            return new FieldWriterInt32Val(fieldName, ordinal, features, format, field);
        }

        if (fieldClass == long.class) {
            if (format == null || format.isEmpty()) {
                return new FieldWriterInt64ValField(fieldName, ordinal, features, format, field);
            }
            return new FieldWriterMillisField(fieldName, ordinal, features, format, field);
        }

        if (fieldClass == float.class) {
            return new FieldWriterFloatValField(fieldName, ordinal, field);
        }

        if (fieldClass == double.class) {
            return new FieldWriterDoubleValField(fieldName, ordinal, field);
        }

        if (fieldClass == char.class) {
            return new FieldWriterCharValField(fieldName, ordinal, field);
        }

        if (fieldClass == Integer.class) {
            return new FieldWriterInt32Field(fieldName, ordinal, features, format, field);
        }

        if (fieldClass == Long.class) {
            return new FieldWriterInt64Field(fieldName, ordinal, field);
        }

        if (fieldClass == Short.class) {
            return new FieldWriterInt16Field(fieldName, ordinal, field, fieldClass);
        }

        if (fieldClass == Byte.class) {
            return new FieldWriterInt8Field(fieldName, ordinal, field);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldWriterBigIntField(fieldName, ordinal, features, format, field);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldWriterBigDecimalField(fieldName, ordinal, features, field);
        }

        if (fieldClass == java.util.Date.class) {
            return new FieldWriterDateField(fieldName, ordinal, features, format, field);
        }

        if (fieldClass == String.class) {
            return new FieldWriterStringField(fieldName, ordinal, format, features, field);
        }

        if (fieldClass.isEnum() && BeanUtils.getEnumValueField(fieldClass) == null) {
            return new FIeldWriterEnumField(fieldName, ordinal, format, features, fieldClass, field);
        }

        if (fieldClass == List.class || fieldClass == ArrayList.class) {
            Type itemType = null;
            Type fieldType = field.getGenericType();
            if (fieldType instanceof ParameterizedType) {
                itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            }
            return new FieldWriterListField(fieldName, itemType, 0, 0, format, fieldType, fieldClass, field);
        }

        if (fieldClass.isArray() && !fieldClass.getComponentType().isPrimitive()) {
            Class<?> itemClass = fieldClass.getComponentType();

            if (field.getDeclaringClass() == Throwable.class && "stackTrace".equals(fieldName)) {
                try {
                    Method method = Throwable.class.getMethod("getStackTrace");
                    return new FieldWriterObjectArrayMethod(fieldName, itemClass, ordinal, features, format, field.getGenericType(), fieldClass, method);
                } catch (NoSuchMethodException ignored) {
                }
            }

            return new FieldWriterObjectArrayField(fieldName, itemClass, ordinal, features, format, itemClass, fieldClass, field);
        }

        return new FieldWriterObjectField(fieldName, ordinal, features, format, field.getGenericType(), fieldClass, field);
    }

    public <T> FieldWriter<T> createFieldWriter(Class<T> objectType
            , String fieldName
            , String dateFormat
            , Method method) {
        return createFieldWriter(objectType, fieldName, 0, 0, dateFormat, method);
    }

    public <T> FieldWriter<T> createFieldWriter(
            Class<T> objectType
            , String fieldName
            , int ordinal
            , long features
            , String format
            , Method method) {
        return createFieldWriter(objectType, fieldName, ordinal, features, format, method, null);
    }

    public <T> FieldWriter<T> createFieldWriter(
            Class<T> objectType
            , String fieldName
            , int ordinal
            , long features
            , String format
            , Method method
            , ObjectWriter initObjectWriter
    ) {
        method.setAccessible(true);
        Class<?> fieldClass = method.getReturnType();
        Type fieldType = method.getGenericReturnType();

        if (initObjectWriter != null) {
            FieldWriterObjectMethod objMethod = new FieldWriterObjectMethod(fieldName, ordinal, features, format, fieldType, fieldClass, method);
            objMethod.initValueClass = fieldClass;
            if (initObjectWriter != ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
                objMethod.initObjectWriter = initObjectWriter;
            }
            return objMethod;
        }

        if (fieldName == null) {
            fieldName = BeanUtils.getterName(method.getName(), null);
        }

        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            return new FieldWriterBoolMethod(fieldName, ordinal, features, method, fieldClass);
        }

        if (fieldClass == int.class || fieldClass == Integer.class) {
            return new FieldWriterInt32Method(fieldName, ordinal, features, format, method, fieldClass);
        }

        if (fieldClass == long.class || fieldClass == Long.class) {
            if (format == null || format.isEmpty()) {
                return new FieldWriterInt64Method(fieldName, ordinal, features, format, method, fieldClass);
            }
            return new FieldWriterMillisMethod(fieldName, ordinal, features, format, method, fieldClass);
        }

        if (fieldClass == short.class || fieldClass == Short.class) {
            return new FieldWriterInt16Method(fieldName, ordinal, method, fieldClass);
        }

        if (fieldClass == byte.class || fieldClass == Byte.class) {
            return new FieldWriterInt8Method(fieldName, ordinal, method, fieldClass);
        }

        if (fieldClass == char.class || fieldClass == Character.class) {
            return new FieldWriterCharMethod(fieldName, ordinal, method, fieldClass);
        }

        if (fieldClass.isEnum() && (BeanUtils.getEnumValueField(fieldClass) == null && initObjectWriter == null)) {
            return new FieldWriterEnumMethod(fieldName, ordinal, features, fieldClass, method);
        }

        if (fieldClass == Date.class) {
            if (format != null) {
                format = format.trim();

                if (format.isEmpty()) {
                    format = null;
                }
            }

            if ("unwrapped".equals(format)) {
                format = null;
            }
            return new FieldWriterDateMethod(fieldName, ordinal, features, format, fieldClass, method);
        }

        if (fieldClass == String.class) {
            return new FieldWriterStringMethod(fieldName, ordinal, format, features, method);
        }

        if (fieldClass == List.class) {
            Type itemType = null;
            if (fieldType instanceof ParameterizedType) {
                itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            } else {
                itemType = Object.class;
            }
            return new FieldWriterListMethod(fieldName, itemType, ordinal, 0, format, method, fieldType, fieldClass);
        }

        return new FieldWriterObjectMethod(fieldName, ordinal, features, format, fieldType, fieldClass, method);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToLongFunction<T> function) {
        return new FieldWriterInt64ValFunc(fieldName, 0, 0, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToIntFunction<T> function) {
        return new FieldWriterInt32ValFunc(fieldName, 0, 0, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToShortFunction<T> function) {
        return new FieldWriterInt16ValFunc(fieldName, 0, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToByteFunction<T> function) {
        return new FieldWriterInt8ValFunc(fieldName, 0, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToFloatFunction<T> function) {
        return new FieldWriterFloatValueFunc(fieldName, 0, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToDoubleFunction<T> function) {
        return new FieldWriterDoubleValueFunc(fieldName, 0, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, Predicate<T> function) {
        return new FieldWriterBoolValFunc(fieldName, 0, 0, null, function);
    }

    public <T, V> FieldWriter createFieldWriter(
            String fieldName
            , Class fieldClass
            , Function<T, V> function
    ) {
        return createFieldWriter(null, fieldName, 0, 0, null, fieldClass, fieldClass, null, function);
    }

    public <T, V> FieldWriter createFieldWriter(
            String fieldName
            , long features
            , String format
            , Class fieldClass
            , Function<T, V> function
    ) {
        return createFieldWriter(null, fieldName, 0, features, format, fieldClass, fieldClass, null, function);
    }

    public <T, V> FieldWriter<T> createFieldWriter(
            Class<T> objectType
            , String fieldName
            , int ordinal
            , long features
            , String format
            , Type fieldType
            , Class<V> fieldClass
            , Method method
            , Function<T, V> function
    ) {
        if (fieldClass == Byte.class) {
            return new FieldWriterInt8Func(fieldName, ordinal, method, function);
        }

        if (fieldClass == Short.class) {
            return new FieldWriterInt16Func(fieldName, ordinal, method, function);
        }

        if (fieldClass == Integer.class) {
            return new FieldWriterInt32Func(fieldName, ordinal, features, format, method, function);
        }

        if (fieldClass == Long.class) {
            return new FieldWriterInt64Func(fieldName, ordinal, method, function);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldWriterBigIntFunc(fieldName, ordinal, method, function);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldWriterBigDecimalFunc(fieldName, ordinal, features, method, function);
        }

        if (fieldClass == String.class) {
            return new FieldWriterStringFunc(fieldName, ordinal, features, format, method, function);
        }

        if (fieldClass == Date.class) {
            return new FieldWriterDateFunc(fieldName, ordinal, features, format, method, function);
        }

        if (Calendar.class.isAssignableFrom(fieldClass)) {
            return new FieldWriterCalendarFunc(fieldName, ordinal, features, format, method, function);
        }

        if (fieldClass.isEnum() && BeanUtils.getEnumValueField(fieldClass) == null) {
            return new FieldWriterEnumFunc(fieldName, ordinal, features, fieldType, fieldClass, method, function);
        }

        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (rawType == List.class || rawType == ArrayList.class) {
                if (actualTypeArguments.length == 1) {
                    Type itemType = actualTypeArguments[0];
                    if (itemType == String.class) {
                        return new FieldWriterListStrFunc(fieldName, ordinal, features, format, method, function, fieldType, fieldClass);
                    }
                    return new FieldWriterListFunc(fieldName, ordinal, features, format, itemType, method, function, fieldType, fieldClass);
                }
            }
        }

        if (Modifier.isFinal(fieldClass.getModifiers())) {
            return new FieldWriterObjectFuncFinal(fieldName, ordinal, features, format, fieldType, fieldClass, null, function);
        }

        return new FieldWriterObjectFunc(fieldName, ordinal, features, format, fieldType, fieldClass, null, function);
    }

}
