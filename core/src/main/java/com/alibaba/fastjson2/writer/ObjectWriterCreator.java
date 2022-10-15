package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.function.ToByteFunction;
import com.alibaba.fastjson2.function.ToFloatFunction;
import com.alibaba.fastjson2.function.ToShortFunction;
import com.alibaba.fastjson2.modules.ObjectWriterAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.*;

import static com.alibaba.fastjson2.codec.FieldInfo.JSON_AUTO_WIRED_ANNOTATED;
import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;

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
                objectType,
                0,
                JSONFactory.getDefaultObjectWriterProvider()
        );
    }

    public ObjectWriter createObjectWriter(Class objectType,
            FieldWriter... fieldWriters) {
        return createObjectWriter(objectType, 0, fieldWriters);
    }

    public ObjectWriter createObjectWriter(
            Class objectClass,
            long features,
            FieldWriter... fieldWriters
    ) {
        if (fieldWriters.length == 0) {
            return createObjectWriter(objectClass, features, JSONFactory.getDefaultObjectWriterProvider());
        }

        boolean googleCollection = false;
        if (objectClass != null) {
            String typeName = objectClass.getName();
            googleCollection =
                    "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList".equals(typeName)
                            || "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet".equals(typeName);
        }

        if (!googleCollection) {
            switch (fieldWriters.length) {
                case 1:
                    if ((fieldWriters[0].features & FieldInfo.VALUE_MASK) == 0) {
                        return new ObjectWriter1(objectClass, features, fieldWriters);
                    }
                    return new ObjectWriterAdapter(objectClass, features, fieldWriters);
                case 2:
                    return new ObjectWriter2(objectClass, features, fieldWriters);
                case 3:
                    return new ObjectWriter3(objectClass, features, fieldWriters);
                case 4:
                    return new ObjectWriter4(objectClass, features, fieldWriters);
                case 5:
                    return new ObjectWriter5(objectClass, features, fieldWriters);
                case 6:
                    return new ObjectWriter6(objectClass, features, fieldWriters);
                case 7:
                    return new ObjectWriter7(objectClass, features, fieldWriters);
                case 8:
                    return new ObjectWriter8(objectClass, features, fieldWriters);
                case 9:
                    return new ObjectWriter9(objectClass, features, fieldWriters);
                case 10:
                    return new ObjectWriter10(objectClass, features, fieldWriters);
                case 11:
                    return new ObjectWriter11(objectClass, features, fieldWriters);
                case 12:
                    return new ObjectWriter12(objectClass, features, fieldWriters);
                default:
                    return new ObjectWriterAdapter(objectClass, features, fieldWriters);
            }
        }

        return new ObjectWriterAdapter(objectClass, features, fieldWriters);
    }

    protected FieldWriter creteFieldWriter(
            Class objectClass,
            long writerFeatures,
            List<ObjectWriterModule> modules,
            BeanInfo beanInfo,
            FieldInfo fieldInfo,
            Field field
    ) {
        fieldInfo.features = writerFeatures;
        for (ObjectWriterModule module : modules) {
            ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor == null) {
                continue;
            }
            annotationProcessor.getFieldInfo(beanInfo, fieldInfo, objectClass, field);
        }

        if (fieldInfo.ignore) {
            return null;
        }

        String fieldName;
        if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
            fieldName = field.getName();

            if (beanInfo.namingStrategy != null) {
                fieldName = BeanUtils.fieldName(fieldName, beanInfo.namingStrategy);
            }
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

        if (beanInfo.includes != null && beanInfo.includes.length > 0) {
            boolean match = false;
            for (String include : beanInfo.includes) {
                if (include.equals(fieldName)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return null;
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

        try {
            field.setAccessible(true);
        } catch (Throwable ignored) {
            // ignored
        }

        if (writeUsingWriter == null && fieldInfo.fieldClassMixIn) {
            writeUsingWriter = ObjectWriterBaseModule.VoidObjectWriter.INSTANCE;
        }

        ObjectWriterProvider provider = null;
        for (ObjectWriterModule module : modules) {
            if (provider == null) {
                provider = module.getProvider();
            }
        }

        if (writeUsingWriter == null) {
            Class<?> fieldClass = field.getType();
            if (fieldClass == Date.class && provider != null) {
                ObjectWriter objectWriter = provider.cache.get(fieldClass);
                if (objectWriter != ObjectWriterImplDate.INSTANCE) {
                    writeUsingWriter = objectWriter;
                }
            }
        }

        return createFieldWriter(provider, fieldName, fieldInfo.ordinal, fieldInfo.features, fieldInfo.format, fieldInfo.label, field, writeUsingWriter);
    }

    protected ObjectWriter getAnnotatedObjectWriter(ObjectWriterProvider provider, Class objectClass, BeanInfo beanInfo) {
        if ((beanInfo.writerFeatures & JSON_AUTO_WIRED_ANNOTATED) == 0) {
            return null;
        }

        String fieldName = beanInfo.objectWriterFieldName;
        if (fieldName == null) {
            fieldName = "objectWriter";
        }
        try {
            Field field = null;
            if (beanInfo.mixIn) {
                Class mixinClass = provider.mixInCache.get(objectClass);
                if (mixinClass != null) {
                    try {
                        field = mixinClass.getDeclaredField(fieldName);
                    } catch (NoSuchFieldException | SecurityException igored) {
                        // ignored
                    }
                }
            }

            if (field == null) {
                field = objectClass.getDeclaredField(fieldName);
            }

            if (field != null
                    && ObjectWriter.class.isAssignableFrom(field.getType())
                    && Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                return (ObjectWriter) field.get(null);
            }
        } catch (Throwable ignored) {
            // ignored
        }
        return null;
    }

    public ObjectWriter createObjectWriter(
            Class objectClass,
            long features,
            final List<ObjectWriterModule> modules
    ) {
        ObjectWriterProvider provider = null;
        for (ObjectWriterModule module : modules) {
            if (provider == null) {
                provider = module.getProvider();
            }
        }
        return createObjectWriter(objectClass, features, provider);
    }

    public ObjectWriter createObjectWriter(
            final Class objectClass,
            final long features,
            final ObjectWriterProvider provider
    ) {
        BeanInfo beanInfo = new BeanInfo();
        for (ObjectWriterModule module : provider.modules) {
            ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor == null) {
                continue;
            }
            annotationProcessor.getBeanInfo(beanInfo, objectClass);
        }

        if (beanInfo.serializer != null && ObjectWriter.class.isAssignableFrom(beanInfo.serializer)) {
            try {
                return (ObjectWriter) beanInfo.serializer.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create serializer error", e);
            }
        }

        ObjectWriter annotatedObjectWriter = getAnnotatedObjectWriter(provider, objectClass, beanInfo);
        if (annotatedObjectWriter != null) {
            return annotatedObjectWriter;
        }

        long writerFeatures = features | beanInfo.writerFeatures;

        boolean fieldBased = (writerFeatures & JSONWriter.Feature.FieldBased.mask) != 0;

        if (fieldBased && (objectClass.isInterface() || objectClass.isInterface())) {
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

                fieldInfo.init();
                FieldWriter fieldWriter = creteFieldWriter(objectClass, writerFeatures, provider.modules, beanInfo, fieldInfo, field);
                if (fieldWriter != null) {
                    fieldWriterMap.put(fieldWriter.fieldName, fieldWriter);
                }
            });
            fieldWriters = new ArrayList<>(fieldWriterMap.values());
        } else {
            boolean fieldWritersCreated = false;
            fieldWriters = new ArrayList<>();
            for (ObjectWriterModule module : provider.modules) {
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
                    FieldWriter fieldWriter = creteFieldWriter(objectClass, writerFeatures, provider.modules, beanInfo, fieldInfo, field);
                    if (fieldWriter != null) {
                        fieldWriterMap.putIfAbsent(fieldWriter.fieldName, fieldWriter);
                    }
                });

                BeanUtils.getters(objectClass, method -> {
                    fieldInfo.init();
                    fieldInfo.features = writerFeatures;
                    for (ObjectWriterModule module : provider.modules) {
                        ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                        if (annotationProcessor == null) {
                            continue;
                        }
                        annotationProcessor.getFieldInfo(beanInfo, fieldInfo, objectClass, method);
                    }
                    if (fieldInfo.ignore) {
                        return;
                    }

                    String fieldName;
                    if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
                        fieldName = BeanUtils.getterName(method, beanInfo.namingStrategy);
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
                            = createFieldWriter(
                            provider,
                            objectClass,
                            fieldName,
                            fieldInfo.ordinal,
                            fieldInfo.features,
                            fieldInfo.format,
                            fieldInfo.label,
                            method,
                            writeUsingWriter
                    );

                    FieldWriter origin = fieldWriterMap.putIfAbsent(fieldWriter.fieldName, fieldWriter);

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

        if (beanInfo.alphabetic) {
            Collections.sort(fieldWriters);
        }

        ObjectWriterAdapter writerAdapter = null;

        boolean googleCollection = false;
        if (objectClass != null) {
            String typeName = objectClass.getName();
            googleCollection =
                    "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList".equals(typeName)
                            || "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet".equals(typeName);
        }
        if (!googleCollection) {
            switch (fieldWriters.size()) {
                case 1:
                    if ((fieldWriters.get(0).features & FieldInfo.VALUE_MASK) == 0) {
                        writerAdapter = new ObjectWriter1(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    }
                    break;
                case 2:
                    writerAdapter = new ObjectWriter2(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                case 3:
                    writerAdapter = new ObjectWriter3(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                case 4:
                    writerAdapter = new ObjectWriter4(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                case 5:
                    writerAdapter = new ObjectWriter5(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                case 6:
                    writerAdapter = new ObjectWriter6(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                case 7:
                    writerAdapter = new ObjectWriter7(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                case 8:
                    writerAdapter = new ObjectWriter8(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                case 9:
                    writerAdapter = new ObjectWriter9(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                case 10:
                    writerAdapter = new ObjectWriter10(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                case 11:
                    writerAdapter = new ObjectWriter11(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                case 12:
                    writerAdapter = new ObjectWriter12(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
                    break;
                default:
                    break;
            }
        }
        if (writerAdapter == null) {
            writerAdapter = new ObjectWriterAdapter(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
        }

        if (beanInfo.serializeFilters != null) {
            for (Class<? extends Filter> filterClass : beanInfo.serializeFilters) {
                if (!Filter.class.isAssignableFrom(filterClass)) {
                    continue;
                }

                try {
                    Filter filter = filterClass.newInstance();
                    if (filter instanceof PropertyFilter) {
                        writerAdapter.setPropertyFilter((PropertyFilter) filter);
                    }

                    if (filter instanceof ValueFilter) {
                        writerAdapter.setValueFilter((ValueFilter) filter);
                    }

                    if (filter instanceof NameFilter) {
                        writerAdapter.setNameFilter((NameFilter) filter);
                    }
                    if (filter instanceof PropertyPreFilter) {
                        writerAdapter.setPropertyPreFilter((PropertyPreFilter) filter);
                    }
                } catch (InstantiationException | IllegalAccessException ignored) {
                }
            }
            // return super.createObjectWriter(objectClass, features, modules);
        }

        return writerAdapter;
    }

    protected void handleIgnores(BeanInfo beanInfo, List<FieldWriter> fieldWriters) {
        if (beanInfo.ignores == null || beanInfo.ignores.length == 0) {
            return;
        }

        for (int i = fieldWriters.size() - 1; i >= 0; i--) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            for (String ignore : beanInfo.ignores) {
                if (ignore.equals(fieldWriter.fieldName)) {
                    fieldWriters.remove(i);
                    break;
                }
            }
        }
    }

    public <T> FieldWriter<T> createFieldWriter(String fieldName, String format, Field field) {
        return createFieldWriter(JSONFactory.getDefaultObjectWriterProvider(), fieldName, 0, 0L, format, null, field, null);
    }

    public <T> FieldWriter<T> createFieldWriter(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Field field
    ) {
        return createFieldWriter(JSONFactory.getDefaultObjectWriterProvider(), fieldName, ordinal, features, format, null, field, null);
    }

    public <T> FieldWriter<T> createFieldWriter(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            ObjectWriter initObjectWriter
    ) {
        return createFieldWriter(JSONFactory.getDefaultObjectWriterProvider(), fieldName, ordinal, features, format, label, field, initObjectWriter);
    }

    public <T> FieldWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            ObjectWriter initObjectWriter
    ) {
        Class<?> declaringClass = field.getDeclaringClass();
        Method method = null;

        if (declaringClass == Throwable.class) {
            switch (field.getName()) {
                case "detailMessage":
                    method = BeanUtils.getMethod(Throwable.class, "getMessage");
                    fieldName = "message";
                    break;
                case "cause":
                    method = BeanUtils.getMethod(Throwable.class, "getCause");
                    break;
                case "stackTrace": {
                    if (JVM_VERSION > 11) {
                        method = BeanUtils.getMethod(Throwable.class, "getStackTrace");
                    }
                    break;
                }
                case "suppressedExceptions": {
                    method = BeanUtils.getMethod(Throwable.class, "getSuppressed");
                    fieldName = "suppressed";
                }
                default:
                    break;
            }
        } else if (declaringClass == DateTimeParseException.class) {
            switch (field.getName()) {
                case "errorIndex":
                    method = BeanUtils.getMethod(DateTimeParseException.class, "getErrorIndex");
                    break;
                case "parsedString":
                    method = BeanUtils.getMethod(DateTimeParseException.class, "getParsedString");
                    break;
                default:
                    break;
            }
        }

        if (method != null) {
            return createFieldWriter(provider, (Class<T>) Throwable.class, fieldName, ordinal, features, format, label, method, initObjectWriter);
        }

        field.setAccessible(true);

        Class<?> fieldClass = field.getType();
        Type fieldType = field.getGenericType();

        if (initObjectWriter != null) {
            if (fieldClass == byte.class) {
                fieldType = fieldClass = Byte.class;
            } else if (fieldClass == short.class) {
                fieldType = fieldClass = Short.class;
            } else if (fieldClass == int.class) {
                fieldType = fieldClass = Integer.class;
            } else if (fieldClass == long.class) {
                fieldType = fieldClass = Long.class;
            } else if (fieldClass == float.class) {
                fieldType = fieldClass = Float.class;
            } else if (fieldClass == double.class) {
                fieldType = fieldClass = Double.class;
            } else if (fieldClass == boolean.class) {
                fieldType = fieldClass = Boolean.class;
            }

            FieldWriterObjectField objImp = new FieldWriterObjectField(fieldName, ordinal, features, format, label, fieldType, fieldClass, field);
            objImp.initValueClass = fieldClass;
            if (initObjectWriter != ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
                objImp.initObjectWriter = initObjectWriter;
            }
            return objImp;
        }

        if (fieldClass == boolean.class) {
            return new FieldWriterBoolValField(fieldName, ordinal, features, format, label, field, fieldClass);
        }

        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            return new FieldWriterBooleanField(fieldName, ordinal, features, format, label, field, fieldClass);
        }

        if (fieldClass == byte.class) {
            return new FieldWriterInt8ValField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == short.class) {
            return new FieldWriterInt16ValField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == int.class) {
            return new FieldWriterInt32Val(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == long.class) {
            if (format == null || format.isEmpty()) {
                return new FieldWriterInt64ValField(fieldName, ordinal, features, format, label, field);
            }
            return new FieldWriterMillisField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == float.class) {
            return new FieldWriterFloatValField(fieldName, ordinal, format, label, field);
        }

        if (fieldClass == double.class) {
            return new FieldWriterDoubleValField(fieldName, ordinal, format, label, field);
        }

        if (fieldClass == char.class) {
            return new FieldWriterCharValField(fieldName, ordinal, format, label, field);
        }

        if (fieldClass == Integer.class) {
            return new FieldWriterInt32Field(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == Long.class) {
            return new FieldWriterInt64Field(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == Short.class) {
            return new FieldWriterInt16Field(fieldName, ordinal, features, format, label, field, fieldClass);
        }

        if (fieldClass == Byte.class) {
            return new FieldWriterInt8Field(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldWriterBigIntField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldWriterBigDecimalField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == java.util.Date.class) {
            return new FieldWriterDateField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == String.class) {
            return new FieldWriterStringField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass.isEnum() && BeanUtils.getEnumValueField(fieldClass, provider) == null) {
            return new FIeldWriterEnumField(fieldName, ordinal, features, format, label, fieldClass, field);
        }

        if (fieldClass == List.class || fieldClass == ArrayList.class) {
            Type itemType = null;
            if (fieldType instanceof ParameterizedType) {
                itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            }
            return new FieldWriterListField(fieldName, itemType, ordinal, features, format, label, fieldType, fieldClass, field);
        }

        if (fieldClass.isArray() && !fieldClass.getComponentType().isPrimitive()) {
            Class<?> itemClass = fieldClass.getComponentType();
            return new FieldWriterObjectArrayField(fieldName, itemClass, ordinal, features, format, label, itemClass, fieldClass, field);
        }

        return new FieldWriterObjectField(fieldName, ordinal, features, format, label, field.getGenericType(), fieldClass, field);
    }

    public <T> FieldWriter<T> createFieldWriter(Class<T> objectType,
            String fieldName,
            String dateFormat,
            Method method) {
        return createFieldWriter(objectType, fieldName, 0, 0, dateFormat, method);
    }

    public <T> FieldWriter<T> createFieldWriter(
            Class<T> objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Method method) {
        return createFieldWriter(null, objectType, fieldName, ordinal, features, format, null, method, null);
    }

    public <T> FieldWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            ObjectWriter initObjectWriter
    ) {
        method.setAccessible(true);
        Class<?> fieldClass = method.getReturnType();
        Type fieldType = method.getGenericReturnType();

        if (initObjectWriter == null && provider != null) {
            if (fieldClass == Date.class) {
                ObjectWriter objectWriter = provider.cache.get(fieldClass);
                if (objectWriter != ObjectWriterImplDate.INSTANCE) {
                    initObjectWriter = objectWriter;
                }
            } else if (fieldClass == Long.class) {
                ObjectWriter objectWriter = provider.cache.get(fieldClass);
                if (objectWriter != ObjectWriterImplInt64.INSTANCE) {
                    initObjectWriter = objectWriter;
                }
            } else if (fieldClass == BigDecimal.class) {
                ObjectWriter objectWriter = provider.cache.get(fieldClass);
                if (objectWriter != ObjectWriterImplBigDecimal.INSTANCE) {
                    initObjectWriter = objectWriter;
                }
            }
        }

        if (initObjectWriter != null) {
            FieldWriterObjectMethod objMethod = new FieldWriterObjectMethod(fieldName, ordinal, features, format, label, fieldType, fieldClass, method);
            objMethod.initValueClass = fieldClass;
            if (initObjectWriter != ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
                objMethod.initObjectWriter = initObjectWriter;
            }
            return objMethod;
        }

        if (fieldName == null) {
            fieldName = BeanUtils.getterName(method, null);
        }

        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            return new FieldWriterBoolMethod(fieldName, ordinal, features, format, label, method, fieldClass);
        }

        if (fieldClass == int.class || fieldClass == Integer.class) {
            return new FieldWriterInt32Method(fieldName, ordinal, features, format, label, method, fieldClass);
        }

        if (fieldClass == long.class || fieldClass == Long.class) {
            if (format == null || format.isEmpty()) {
                return new FieldWriterInt64Method(fieldName, ordinal, features, format, label, method, fieldClass);
            }
            return new FieldWriterMillisMethod(fieldName, ordinal, features, format, label, fieldClass, method);
        }

        if (fieldClass == short.class || fieldClass == Short.class) {
            return new FieldWriterInt16Method(fieldName, ordinal, features, format, label, method, fieldClass);
        }

        if (fieldClass == byte.class || fieldClass == Byte.class) {
            return new FieldWriterInt8Method(fieldName, ordinal, features, format, label, method, fieldClass);
        }

        if (fieldClass == char.class || fieldClass == Character.class) {
            return new FieldWriterCharMethod(fieldName, ordinal, features, format, label, method, fieldClass);
        }

        if (fieldClass.isEnum()
                && (BeanUtils.getEnumValueField(fieldClass, provider) == null && initObjectWriter == null)
                && !BeanUtils.isWriteEnumAsJavaBean(fieldClass)
        ) {
            return new FieldWriterEnumMethod(fieldName, ordinal, features, format, label, fieldClass, method);
        }

        if (fieldClass == Date.class) {
            if (format != null) {
                format = format.trim();

                if (format.isEmpty()) {
                    format = null;
                }
            }

            return new FieldWriterDateMethod(fieldName, ordinal, features, format, label, fieldClass, method);
        }

        if (fieldClass == String.class) {
            return new FieldWriterStringMethod(fieldName, ordinal, format, label, features, method);
        }

        if (fieldClass == List.class) {
            Type itemType = null;
            if (fieldType instanceof ParameterizedType) {
                itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            } else {
                itemType = Object.class;
            }
            return new FieldWriterListMethod(fieldName, itemType, ordinal, features, format, label, method, fieldType, fieldClass);
        }

        return new FieldWriterObjectMethod(fieldName, ordinal, features, format, label, fieldType, fieldClass, method);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToLongFunction<T> function) {
        return new FieldWriterInt64ValFunc(fieldName, 0, 0, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToIntFunction<T> function) {
        return new FieldWriterInt32ValFunc(fieldName, 0, 0, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToShortFunction<T> function) {
        return new FieldWriterInt16ValFunc(fieldName, 0, 0, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToByteFunction<T> function) {
        return new FieldWriterInt8ValFunc(fieldName, 0, 0, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToFloatFunction<T> function) {
        return new FieldWriterFloatValueFunc(fieldName, 0, 0L, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToDoubleFunction<T> function) {
        return new FieldWriterDoubleValueFunc(fieldName, 0, 0, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, Predicate<T> function) {
        return new FieldWriterBoolValFunc(fieldName, 0, 0, null, null, null, function);
    }

    public <T, V> FieldWriter createFieldWriter(
            String fieldName,
            Class fieldClass,
            Function<T, V> function
    ) {
        return createFieldWriter(null, null, fieldName, 0, 0, null, null, fieldClass, fieldClass, null, function);
    }

    public <T, V> FieldWriter createFieldWriter(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            Function<T, V> function
    ) {
        return createFieldWriter(null, null, fieldName, 0, 0, null, null, fieldType, fieldClass, null, function);
    }

    public <T, V> FieldWriter createFieldWriter(
            String fieldName,
            long features,
            String format,
            Class fieldClass,
            Function<T, V> function
    ) {
        return createFieldWriter(null, null, fieldName, 0, features, format, null, fieldClass, fieldClass, null, function);
    }

    public <T, V> FieldWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class<V> fieldClass,
            Method method,
            Function<T, V> function
    ) {
        if (fieldClass == Byte.class) {
            return new FieldWriterInt8Func(fieldName, ordinal, features, format, label, method, function);
        }

        if (fieldClass == Short.class) {
            return new FieldWriterInt16Func(fieldName, ordinal, features, format, label, method, function);
        }

        if (fieldClass == Integer.class) {
            return new FieldWriterInt32Func(fieldName, ordinal, features, format, label, method, function);
        }

        if (fieldClass == Long.class) {
            return new FieldWriterInt64Func(fieldName, ordinal, features, format, label, method, function);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldWriterBigIntFunc(fieldName, ordinal, features, format, label, method, function);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldWriterBigDecimalFunc(fieldName, ordinal, features, format, label, method, function);
        }

        if (fieldClass == String.class) {
            return new FieldWriterStringFunc(fieldName, ordinal, features, format, label, method, function);
        }

        if (fieldClass == Date.class) {
            return new FieldWriterDateFunc(fieldName, ordinal, features, format, label, method, function);
        }

        if (Calendar.class.isAssignableFrom(fieldClass)) {
            return new FieldWriterCalendarFunc(fieldName, ordinal, features, format, label, method, function);
        }

        if (fieldClass.isEnum() && BeanUtils.getEnumValueField(fieldClass, provider) == null) {
            return new FieldWriterEnumFunc(fieldName, ordinal, features, format, label, fieldType, fieldClass, method, function);
        }

        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (rawType == List.class || rawType == ArrayList.class) {
                if (actualTypeArguments.length == 1) {
                    Type itemType = actualTypeArguments[0];
                    if (itemType == String.class) {
                        return new FieldWriterListStrFunc(fieldName, ordinal, features, format, label, method, function, fieldType, fieldClass);
                    }
                    return new FieldWriterListFunc(fieldName, ordinal, features, format, label, itemType, method, function, fieldType, fieldClass);
                }
            }
        }

        if (Modifier.isFinal(fieldClass.getModifiers())) {
            return new FieldWriterObjectFuncFinal(fieldName, ordinal, features, format, label, fieldType, fieldClass, null, function);
        }

        return new FieldWriterObjectFunc(fieldName, ordinal, features, format, label, fieldType, fieldClass, null, function);
    }
}
