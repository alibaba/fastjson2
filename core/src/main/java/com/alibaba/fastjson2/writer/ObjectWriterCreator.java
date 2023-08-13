package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteClassName;
import static com.alibaba.fastjson2.util.BeanUtils.SUPER;
import static com.alibaba.fastjson2.util.TypeUtils.*;
import static com.alibaba.fastjson2.writer.ObjectWriterProvider.NAME_COMPATIBLE_WITH_FILED;

public class ObjectWriterCreator {
    public static final ObjectWriterCreator INSTANCE = new ObjectWriterCreator();

    static final Map<Class, LambdaInfo> lambdaMapping = new HashMap<>();

    static {
        lambdaMapping.put(boolean.class, new LambdaInfo(boolean.class, Predicate.class, "test"));
        lambdaMapping.put(char.class, new LambdaInfo(char.class, ToCharFunction.class, "applyAsChar"));
        lambdaMapping.put(byte.class, new LambdaInfo(byte.class, ToByteFunction.class, "applyAsByte"));
        lambdaMapping.put(short.class, new LambdaInfo(short.class, ToShortFunction.class, "applyAsShort"));
        lambdaMapping.put(int.class, new LambdaInfo(int.class, ToIntFunction.class, "applyAsInt"));
        lambdaMapping.put(long.class, new LambdaInfo(long.class, ToLongFunction.class, "applyAsLong"));
        lambdaMapping.put(float.class, new LambdaInfo(float.class, ToFloatFunction.class, "applyAsFloat"));
        lambdaMapping.put(double.class, new LambdaInfo(double.class, ToDoubleFunction.class, "applyAsDouble"));
    }

    protected final AtomicInteger jitErrorCount = new AtomicInteger();
    protected volatile Throwable jitErrorLast;

    public ObjectWriterCreator() {
    }

    public ObjectWriter createObjectWriter(List<FieldWriter> fieldWriters) {
        return new ObjectWriterAdapter(null, null, null, 0, fieldWriters);
    }

    public ObjectWriter createObjectWriter(FieldWriter... fieldWriters) {
        return createObjectWriter(Arrays.asList(fieldWriters));
    }

    public <T> ObjectWriter<T> createObjectWriter(String[] names, Type[] types, FieldSupplier<T> supplier) {
        FieldWriter[] fieldWriters = new FieldWriter[names.length];
        for (int i = 0; i < names.length; i++) {
            String fieldName = names[i];
            Type fieldType = types[i];
            int fieldIndex = i;
            Function<T, Object> function = new FieldSupplierFunction<T>(supplier, fieldIndex);
            fieldWriters[i] = createFieldWriter(fieldName, fieldType, TypeUtils.getClass(fieldType), function);
        }
        return createObjectWriter(fieldWriters);
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
                        return new ObjectWriter1(objectClass, null, null, features, Arrays.asList(fieldWriters));
                    }
                    return new ObjectWriterAdapter(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 2:
                    return new ObjectWriter2(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 3:
                    return new ObjectWriter3(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 4:
                    return new ObjectWriter4(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 5:
                    return new ObjectWriter5(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 6:
                    return new ObjectWriter6(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 7:
                    return new ObjectWriter7(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 8:
                    return new ObjectWriter8(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 9:
                    return new ObjectWriter9(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 10:
                    return new ObjectWriter10(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 11:
                    return new ObjectWriter11(objectClass, null, null, features, Arrays.asList(fieldWriters));
                case 12:
                    return new ObjectWriter12(objectClass, null, null, features, Arrays.asList(fieldWriters));
                default:
                    return new ObjectWriterAdapter(objectClass, null, null, features, Arrays.asList(fieldWriters));
            }
        }

        return new ObjectWriterAdapter(objectClass, null, null, features, Arrays.asList(fieldWriters));
    }

    protected FieldWriter creteFieldWriter(
            Class objectClass,
            long writerFeatures,
            ObjectWriterProvider provider,
            BeanInfo beanInfo,
            FieldInfo fieldInfo,
            Field field
    ) {
        fieldInfo.features = writerFeatures;
        provider.getFieldInfo(beanInfo, fieldInfo, objectClass, field);

        if (fieldInfo.ignore || isFunction(field.getType())) {
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
                Constructor<?> constructor = fieldInfo.writeUsing.getDeclaredConstructor();
                constructor.setAccessible(true);
                writeUsingWriter = (ObjectWriter) constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
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

        if (writeUsingWriter == null) {
            Class<?> fieldClass = field.getType();
            if (fieldClass == Date.class) {
                ObjectWriter objectWriter = provider.cache.get(fieldClass);
                if (objectWriter != ObjectWriterImplDate.INSTANCE) {
                    writeUsingWriter = objectWriter;
                }
            } else if (Map.class.isAssignableFrom(fieldClass)
                    && (fieldInfo.keyUsing != null || fieldInfo.valueUsing != null)) {
                ObjectWriter keyWriter = null;
                ObjectWriter valueWriter = null;
                if (fieldInfo.keyUsing != null) {
                    try {
                        Constructor<?> constructor = fieldInfo.keyUsing.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        keyWriter = (ObjectWriter) constructor.newInstance();
                    } catch (Exception ignored) {
                        // ignored
                    }
                }
                if (fieldInfo.valueUsing != null) {
                    try {
                        Constructor<?> constructor = fieldInfo.valueUsing.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        valueWriter = (ObjectWriter) constructor.newInstance();
                    } catch (Exception ignored) {
                        // ignored
                    }
                }

                if (keyWriter != null || valueWriter != null) {
                    ObjectWriterImplMap mapWriter = ObjectWriterImplMap.of(field.getType(), fieldClass);
                    mapWriter.keyWriter = keyWriter;
                    mapWriter.valueWriter = valueWriter;
                    writeUsingWriter = mapWriter;
                }
            }
        }

        String format = fieldInfo.format;
        if (format == null && beanInfo.format != null) {
            format = beanInfo.format;
        }

        return createFieldWriter(provider, fieldName, fieldInfo.ordinal, fieldInfo.features, format, fieldInfo.label, field, writeUsingWriter);
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

    protected void setDefaultValue(List<FieldWriter> fieldWriters, Class objectClass) {
        Constructor constructor = BeanUtils.getDefaultConstructor(objectClass, true);
        if (constructor == null) {
            return;
        }

        int parameterCount = constructor.getParameterCount();
        Object object;
        try {
            constructor.setAccessible(true);
            if (parameterCount == 0) {
                object = constructor.newInstance();
            } else if (parameterCount == 1) {
                object = constructor.newInstance(true);
            } else {
                return;
            }
        } catch (Exception ignored) {
            // ignored
            return;
        }

        for (FieldWriter fieldWriter : fieldWriters) {
            fieldWriter.setDefaultValue(object);
        }
    }

    public ObjectWriter createObjectWriter(
            final Class objectClass,
            final long features,
            final ObjectWriterProvider provider
    ) {
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.readerFeatures |= FieldInfo.JIT;

        provider.getBeanInfo(beanInfo, objectClass);

        if (beanInfo.serializer != null && ObjectWriter.class.isAssignableFrom(beanInfo.serializer)) {
            try {
                return (ObjectWriter) beanInfo.serializer.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create serializer error", e);
            }
        }

        boolean record = BeanUtils.isRecord(objectClass);

        long beanFeatures = beanInfo.writerFeatures;
        if (beanInfo.seeAlso != null) {
            beanFeatures &= ~JSONWriter.Feature.WriteClassName.mask;
        }

        long writerFieldFeatures = features | beanFeatures;
        boolean fieldBased = (writerFieldFeatures & JSONWriter.Feature.FieldBased.mask) != 0;

        if (fieldBased && (record || objectClass.isInterface())) {
            fieldBased = false;
        }

        List<FieldWriter> fieldWriters;
        final FieldInfo fieldInfo = new FieldInfo();

        if (fieldBased) {
            Map<String, FieldWriter> fieldWriterMap = new TreeMap<>();
            BeanUtils.declaredFields(objectClass, field -> {
                fieldInfo.init();
                FieldWriter fieldWriter = creteFieldWriter(objectClass, writerFieldFeatures, provider, beanInfo, fieldInfo, field);
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

                if (!record) {
                    BeanUtils.declaredFields(objectClass, field -> {
                        fieldInfo.init();
                        fieldInfo.ignore = (field.getModifiers() & Modifier.PUBLIC) == 0;
                        FieldWriter fieldWriter = creteFieldWriter(objectClass, writerFieldFeatures, provider, beanInfo, fieldInfo, field);
                        if (fieldWriter != null) {
                            FieldWriter origin = fieldWriterMap.putIfAbsent(fieldWriter.fieldName, fieldWriter);

                            if (origin != null && origin.compareTo(fieldWriter) > 0) {
                                fieldWriterMap.put(fieldWriter.fieldName, fieldWriter);
                            }
                        }
                    });
                }

                Class mixIn = provider.getMixIn(objectClass);

                BeanUtils.getters(objectClass, mixIn, method -> {
                    fieldInfo.init();
                    fieldInfo.features = writerFieldFeatures;
                    fieldInfo.format = beanInfo.format;
                    provider.getFieldInfo(beanInfo, fieldInfo, objectClass, method);
                    if (fieldInfo.ignore) {
                        return;
                    }

                    String fieldName = getFieldName(objectClass, provider, beanInfo, record, fieldInfo, method);

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

                    // skip typeKey field
                    if ((beanInfo.writerFeatures & WriteClassName.mask) != 0
                            && fieldName.equals(beanInfo.typeKey)) {
                        return;
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

                    Class<?> returnType = method.getReturnType();
                    // skip function
                    if (isFunction(returnType)) {
                        return;
                    }

                    ObjectWriter writeUsingWriter = null;
                    if (fieldInfo.writeUsing != null) {
                        try {
                            Constructor<?> constructor = fieldInfo.writeUsing.getDeclaredConstructor();
                            constructor.setAccessible(true);
                            writeUsingWriter = (ObjectWriter) constructor.newInstance();
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException e) {
                            throw new JSONException("create writeUsing Writer error", e);
                        }
                    }

                    if (writeUsingWriter == null && fieldInfo.fieldClassMixIn) {
                        writeUsingWriter = ObjectWriterBaseModule.VoidObjectWriter.INSTANCE;
                    }

                    FieldWriter fieldWriter = null;
                    if ((beanInfo.readerFeatures & FieldInfo.JIT) != 0) {
                        try {
                            fieldWriter = createFieldWriterLambda(
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
                        } catch (Throwable ignored) {
                            jitErrorCount.incrementAndGet();
                            jitErrorLast = ignored;
                        }
                    }
                    if (fieldWriter == null) {
                        fieldWriter = createFieldWriter(
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
                    }

                    FieldWriter origin = fieldWriterMap.putIfAbsent(fieldWriter.fieldName, fieldWriter);

                    if (origin != null && origin.compareTo(fieldWriter) > 0) {
                        fieldWriterMap.put(fieldName, fieldWriter);
                    }
                });

                fieldWriters = new ArrayList<>(fieldWriterMap.values());
            }
        }

        long writerFeatures = features | beanInfo.writerFeatures;
        if ((!fieldBased) && Throwable.class.isAssignableFrom(objectClass)) {
            return new ObjectWriterException(objectClass, writerFeatures, fieldWriters);
        }

        handleIgnores(beanInfo, fieldWriters);

        if (beanInfo.alphabetic) {
            Collections.sort(fieldWriters);
        }

        if (BeanUtils.isExtendedMap(objectClass)) {
            Type superType = objectClass.getGenericSuperclass();
            FieldWriter superWriter = ObjectWriters.fieldWriter(
                    SUPER,
                    superType,
                    objectClass.getSuperclass(),
                    o -> o
            );
            fieldWriters.add(superWriter);
        }

        setDefaultValue(fieldWriters, objectClass);

        ObjectWriterAdapter writerAdapter = null;

        boolean googleCollection;
        String typeName = objectClass.getName();
        googleCollection =
                "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList".equals(typeName)
                        || "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet".equals(typeName);
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
            configSerializeFilters(beanInfo, writerAdapter);
        }

        return writerAdapter;
    }

    protected static String getFieldName(
            Class objectClass,
            ObjectWriterProvider provider,
            BeanInfo beanInfo,
            boolean record,
            FieldInfo fieldInfo,
            Method method
    ) {
        String fieldName;
        if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
            if (record) {
                fieldName = method.getName();
            } else {
                fieldName = BeanUtils.getterName(method, beanInfo.namingStrategy);

                Field field = null;
                if ((provider.userDefineMask & NAME_COMPATIBLE_WITH_FILED) != 0
                        && (field = BeanUtils.getField(objectClass, method)) != null) {
                    fieldName = field.getName();
                } else {
                    char c0 = '\0', c1;
                    int len = fieldName.length();
                    if (len > 0) {
                        c0 = fieldName.charAt(0);
                    }

                    if ((len == 1 && c0 >= 'a' && c0 <= 'z')
                            || (len > 2 && c0 >= 'A' && c0 <= 'Z' && (c1 = fieldName.charAt(1)) >= 'A' && c1 <= 'Z')
                    ) {
                        char[] chars = fieldName.toCharArray();
                        if (c0 >= 'a') {
                            chars[0] = (char) (chars[0] - 32);
                        } else {
                            chars[0] = (char) (chars[0] + 32);
                        }
                        String fieldName1 = new String(chars);
                        field = BeanUtils.getDeclaredField(objectClass, fieldName1);

                        if (field != null && (len == 1 || Modifier.isPublic(field.getModifiers()))) {
                            fieldName = field.getName();
                        }
                    }
                }
            }
        } else {
            fieldName = fieldInfo.fieldName;
        }
        return fieldName;
    }

    protected static void configSerializeFilters(BeanInfo beanInfo, ObjectWriterAdapter writerAdapter) {
        for (Class<? extends Filter> filterClass : beanInfo.serializeFilters) {
            if (!Filter.class.isAssignableFrom(filterClass)) {
                continue;
            }

            try {
                Filter filter = filterClass.newInstance();
                writerAdapter.setFilter(filter);
            } catch (InstantiationException | IllegalAccessException ignored) {
                //ignored
            }
        }
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
            if (field.getName().equals("stackTrace")) {
                method = BeanUtils.getMethod(Throwable.class, "getStackTrace");
            }
        }

        if (method != null) {
            return createFieldWriter(provider, (Class<T>) Throwable.class, fieldName, ordinal, features, format, label, method, initObjectWriter);
        }

        Class<?> fieldClass = field.getType();
        Type fieldType = field.getGenericType();

        if (initObjectWriter != null) {
//            if (fieldClass == byte.class) {
//                fieldType = fieldClass = Byte.class;
//            } else if (fieldClass == short.class) {
//                fieldType = fieldClass = Short.class;
//            } else if (fieldClass == int.class) {
//                fieldType = fieldClass = Integer.class;
//            } else if (fieldClass == long.class) {
//                fieldType = fieldClass = Long.class;
//            } else if (fieldClass == float.class) {
//                fieldType = fieldClass = Float.class;
//            } else if (fieldClass == double.class) {
//                fieldType = fieldClass = Double.class;
//            } else if (fieldClass == boolean.class) {
//                fieldType = fieldClass = Boolean.class;
//            }

            FieldWriterObject objImp = new FieldWriterObject(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, null);
            objImp.initValueClass = fieldClass;
            if (initObjectWriter != ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
                objImp.initObjectWriter = initObjectWriter;
            }
            return objImp;
        }

        if (fieldClass == boolean.class) {
            return new FieldWriterBoolValField(fieldName, ordinal, features, format, label, field, fieldClass);
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
            if (format == null || format.isEmpty() || "string".equals(format)) {
                return new FieldWriterInt64ValField(fieldName, ordinal, features, format, label, field);
            }
            return new FieldWriterMillisField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == float.class) {
            return new FieldWriterFloatValField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == Float.class) {
            return new FieldWriterFloatField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == double.class) {
            return new FieldWriterDoubleValField(fieldName, ordinal, format, label, field);
        }

        if (fieldClass == Double.class) {
            return new FieldWriterDoubleField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == char.class) {
            return new FieldWriterCharValField(fieldName, ordinal, features, format, label, field);
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

        if (fieldClass.isEnum()) {
            BeanInfo beanInfo = new BeanInfo();
            provider.getBeanInfo(beanInfo, fieldClass);

            boolean writeEnumAsJavaBean = beanInfo.writeEnumAsJavaBean;
            if (!writeEnumAsJavaBean) {
                ObjectWriter objectWriter = provider.cache.get(fieldClass);
                if (objectWriter != null && !(objectWriter instanceof ObjectWriterImplEnum)) {
                    writeEnumAsJavaBean = true;
                }
            }

            Member enumValueField = BeanUtils.getEnumValueField(fieldClass, provider);
            if (enumValueField == null && !writeEnumAsJavaBean) {
                String[] enumAnnotationNames = BeanUtils.getEnumAnnotationNames(fieldClass);
                if (enumAnnotationNames == null) {
                    return new FieldWriterEnum(fieldName, ordinal, features, format, label, fieldType, (Class<? extends Enum>) fieldClass, field, null);
                }
            }
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

        return new FieldWriterObject(fieldName, ordinal, features, format, label, field.getGenericType(), fieldClass, field, null);
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
            initObjectWriter = getInitWriter(provider, fieldClass);
        }

        if (initObjectWriter != null) {
            FieldWriterObjectMethod objMethod = new FieldWriterObjectMethod(fieldName, ordinal, features, format, label, fieldType, fieldClass, null, method);
            objMethod.initValueClass = fieldClass;
            if (initObjectWriter != ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
                objMethod.initObjectWriter = initObjectWriter;
            }
            return objMethod;
        }

        if (fieldName == null) {
            fieldName = BeanUtils.getterName(method, null);
        }

        Field field = BeanUtils.getField(objectType, method);

        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            return new FieldWriterBoolMethod(fieldName, ordinal, features, format, label, field, method, fieldClass);
        }

        if (fieldClass == int.class || fieldClass == Integer.class) {
            return new FieldWriterInt32Method(fieldName, ordinal, features, format, label, field, method, fieldClass);
        }

        if (fieldClass == float.class || fieldClass == Float.class) {
            return new FieldWriterFloatMethod<>(fieldName, ordinal, features, format, label, fieldClass, fieldClass, field, method);
        }

        if (fieldClass == double.class || fieldClass == Double.class) {
            return new FieldWriterDoubleMethod<>(fieldName, ordinal, features, format, label, fieldClass, fieldClass, field, method);
        }

        if (fieldClass == long.class || fieldClass == Long.class) {
            if (format == null || format.isEmpty() || "string".equals(format)) {
                return new FieldWriterInt64Method(fieldName, ordinal, features, format, label, field, method, fieldClass);
            }

            return new FieldWriterMillisMethod(fieldName, ordinal, features, format, label, fieldClass, field, method);
        }

        if (fieldClass == short.class || fieldClass == Short.class) {
            return new FieldWriterInt16Method(fieldName, ordinal, features, format, label, field, method, fieldClass);
        }

        if (fieldClass == byte.class || fieldClass == Byte.class) {
            return new FieldWriterInt8Method(fieldName, ordinal, features, format, label, field, method, fieldClass);
        }

        if (fieldClass == char.class || fieldClass == Character.class) {
            return new FieldWriterCharMethod(fieldName, ordinal, features, format, label, field, method, fieldClass);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldWriterBigDecimalMethod<>(fieldName, ordinal, features, format, label, field, method);
        }

        if (fieldClass.isEnum()
                && BeanUtils.getEnumValueField(fieldClass, provider) == null
                && !BeanUtils.isWriteEnumAsJavaBean(fieldClass)
        ) {
            String[] enumAnnotationNames = BeanUtils.getEnumAnnotationNames(fieldClass);
            if (enumAnnotationNames == null) {
                return new FieldWriterEnumMethod(fieldName, ordinal, features, format, label, fieldClass, field, method);
            }
        }

        if (fieldClass == Date.class) {
            if (format != null) {
                format = format.trim();

                if (format.isEmpty()) {
                    format = null;
                }
            }

            return new FieldWriterDateMethod(fieldName, ordinal, features, format, label, fieldClass, field, method);
        }

        if (fieldClass == String.class) {
            return new FieldWriterStringMethod(fieldName, ordinal, format, label, features, field, method);
        }

        if (fieldClass == List.class || fieldClass == Iterable.class) {
            Type itemType;
            if (fieldType instanceof ParameterizedType) {
                itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            } else {
                itemType = Object.class;
            }
            return new FieldWriterListMethod(fieldName, itemType, ordinal, features, format, label, null, method, fieldType, fieldClass);
        }

        if (fieldClass == Float[].class || fieldClass == Double[].class || fieldClass == BigDecimal[].class) {
            return new FieldWriterObjectArrayMethod(fieldName, fieldClass.getComponentType(), ordinal, features, format, label, fieldType, fieldClass, field, method);
        }

        return new FieldWriterObjectMethod(fieldName, ordinal, features, format, label, fieldType, fieldClass, null, method);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToLongFunction<T> function) {
        return new FieldWriterInt64ValFunc(fieldName, 0, 0, null, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToIntFunction<T> function) {
        return new FieldWriterInt32ValFunc(fieldName, 0, 0, null, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, Field field, Method method, ToIntFunction<T> function) {
        return new FieldWriterInt32ValFunc(fieldName, 0, 0, null, null, field, method, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToShortFunction<T> function) {
        return new FieldWriterInt16ValFunc(fieldName, 0, 0, null, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToByteFunction<T> function) {
        return new FieldWriterInt8ValFunc(fieldName, 0, 0, null, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToFloatFunction<T> function) {
        return new FieldWriterFloatValueFunc(fieldName, 0, 0L, null, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, ToDoubleFunction<T> function) {
        return new FieldWriterDoubleValueFunc(fieldName, 0, 0, null, null, null, null, function);
    }

    public <T> FieldWriter createFieldWriter(String fieldName, Predicate<T> function) {
        return new FieldWriterBoolValFunc(fieldName, 0, 0, null, null, null, null, function);
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
            Class fieldClass,
            Field field,
            Method method,
            Function<T, V> function
    ) {
        return createFieldWriter(null, null, fieldName, 0, 0, null, null, fieldClass, fieldClass, field, method, function);
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
            Class<T> objectClass,
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
        return createFieldWriter(provider, objectClass, fieldName, ordinal, features, format, label, fieldType, fieldClass, null, method, function);
    }

    public <T, V> FieldWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectClass,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class<V> fieldClass,
            Field field,
            Method method,
            Function<T, V> function
    ) {
        if (fieldClass == Byte.class) {
            return new FieldWriterInt8Func(fieldName, ordinal, features, format, label, field, method, function);
        }

        if (fieldClass == Short.class) {
            return new FieldWriterInt16Func(fieldName, ordinal, features, format, label, field, method, function);
        }

        if (fieldClass == Integer.class) {
            return new FieldWriterInt32Func(fieldName, ordinal, features, format, label, field, method, function);
        }

        if (fieldClass == Long.class) {
            return new FieldWriterInt64Func(fieldName, ordinal, features, format, label, field, method, function);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldWriterBigIntFunc(fieldName, ordinal, features, format, label, field, method, function);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldWriterBigDecimalFunc(fieldName, ordinal, features, format, label, field, method, function);
        }

        if (fieldClass == String.class) {
            return new FieldWriterStringFunc(fieldName, ordinal, features, format, label, field, method, function);
        }

        if (fieldClass == Date.class) {
            return new FieldWriterDateFunc(fieldName, ordinal, features, format, label, field, method, function);
        }

        if (fieldClass == LocalDate.class) {
            return new FieldWriterLocalDateFunc(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, method, function);
        }

        if (fieldClass == OffsetDateTime.class) {
            return new FieldWriterOffsetDateTimeFunc(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, method, function);
        }

        if (fieldClass == UUID.class) {
            return new FieldWriterUUIDFunc(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, method, function);
        }

        if (Calendar.class.isAssignableFrom(fieldClass)) {
            return new FieldWriterCalendarFunc(fieldName, ordinal, features, format, label, field, method, function);
        }

        if (fieldClass.isEnum()) {
            BeanInfo beanInfo = new BeanInfo();
            if (provider == null) {
                provider = JSONFactory.getDefaultObjectWriterProvider();
            }
            provider.getBeanInfo(beanInfo, fieldClass);

            boolean writeEnumAsJavaBean = beanInfo.writeEnumAsJavaBean;
            if (!writeEnumAsJavaBean) {
                ObjectWriter objectWriter = provider.cache.get(fieldClass);
                if (objectWriter != null && !(objectWriter instanceof ObjectWriterImplEnum)) {
                    writeEnumAsJavaBean = true;
                }
            }

            if (!writeEnumAsJavaBean && BeanUtils.getEnumValueField(fieldClass, provider) == null) {
                String[] enumAnnotationNames = BeanUtils.getEnumAnnotationNames(fieldClass);
                if (enumAnnotationNames == null) {
                    return new FieldWriterEnumFunc(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, method, function);
                }
            }
        }

        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (rawType == List.class || rawType == ArrayList.class) {
                if (actualTypeArguments.length == 1) {
                    Type itemType = actualTypeArguments[0];
                    if (itemType == String.class) {
                        return new FieldWriterListStrFunc(fieldName, ordinal, features, format, label, field, method, function, fieldType, fieldClass);
                    }
                    return new FieldWriterListFunc(fieldName, ordinal, features, format, label, itemType, field, method, function, fieldType, fieldClass);
                }
            }
        }

        if (Modifier.isFinal(fieldClass.getModifiers())) {
            return new FieldWriterObjectFuncFinal(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, method, function);
        }

        return new FieldWriterObjectFunc(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, method, function);
    }

    static class LambdaInfo {
        final Class fieldClass;
        final Class supplierClass;
        final String methodName;
        final MethodType methodType;
        final MethodType invokedType;
        final MethodType samMethodType;

        LambdaInfo(Class fieldClass, Class supplierClass, String methodName) {
            this.fieldClass = fieldClass;
            this.supplierClass = supplierClass;
            this.methodName = methodName;
            this.methodType = MethodType.methodType(fieldClass);
            this.invokedType = MethodType.methodType(supplierClass);
            this.samMethodType = MethodType.methodType(fieldClass, Object.class);
        }
    }

    Object lambdaGetter(Class objectClass, Class fieldClass, Method method) {
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);

        LambdaInfo buildInfo = lambdaMapping.get(fieldClass);

        MethodType methodType;
        MethodType invokedType;
        String methodName;
        MethodType samMethodType;
        if (buildInfo != null) {
            methodType = buildInfo.methodType;
            invokedType = buildInfo.invokedType;
            methodName = buildInfo.methodName;
            samMethodType = buildInfo.samMethodType;
        } else {
            methodType = MethodType.methodType(fieldClass);
            invokedType = METHOD_TYPE_FUNCTION;
            methodName = "apply";
            samMethodType = METHOD_TYPE_OBJECT_OBJECT;
        }

        try {
            MethodHandle target = lookup.findVirtual(objectClass, method.getName(), methodType);
            MethodType instantiatedMethodType = target.type();

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    methodName,
                    invokedType,
                    samMethodType,
                    target,
                    instantiatedMethodType
            );

            return callSite
                    .getTarget()
                    .invoke();
        } catch (Throwable e) {
            throw new JSONException("create fieldLambdaGetter error, method : " + method, e);
        }
    }

    protected ObjectWriter getInitWriter(ObjectWriterProvider provider, Class fieldClass) {
        if (fieldClass == Date.class) {
            if ((provider.userDefineMask & ObjectWriterProvider.TYPE_DATE_MASK) != 0) {
                ObjectWriter objectWriter = provider.cache.get(fieldClass);
                if (objectWriter != ObjectWriterImplDate.INSTANCE) {
                    return objectWriter;
                }
            }
        } else if (fieldClass == long.class || fieldClass == Long.class) {
            if ((provider.userDefineMask & ObjectWriterProvider.TYPE_INT64_MASK) != 0) {
                ObjectWriter objectWriter = provider.cache.get(Long.class);
                if (objectWriter != ObjectWriterImplInt64.INSTANCE) {
                    return objectWriter;
                }
            }
        } else if (fieldClass == BigDecimal.class) {
            if ((provider.userDefineMask & ObjectWriterProvider.TYPE_DECIMAL_MASK) != 0) {
                ObjectWriter objectWriter = provider.cache.get(fieldClass);
                if (objectWriter != ObjectWriterImplBigDecimal.INSTANCE) {
                    return objectWriter;
                }
            }
        } else if (Enum.class.isAssignableFrom(fieldClass)) {
            ObjectWriter objectWriter = provider.cache.get(fieldClass);
            if (!(objectWriter instanceof ObjectWriterImplEnum)) {
                return objectWriter;
            }
        }
        return null;
    }

    <T> FieldWriter<T> createFieldWriterLambda(
            ObjectWriterProvider provider,
            Class<T> objectClass,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            ObjectWriter initObjectWriter
    ) {
        Class<?> fieldClass = method.getReturnType();
        Type fieldType = method.getGenericReturnType();

        if (initObjectWriter == null && provider != null) {
            initObjectWriter = getInitWriter(provider, fieldClass);
        }

        if (initObjectWriter != null) {
            return null;
        }

        String objectClassName = objectClass.getName();
        if (objectClassName.indexOf('$') != -1 && objectClassName.contains("$$")) {
            return null;
        }

        Object lambda = lambdaGetter(objectClass, fieldClass, method);

        Field field = BeanUtils.getField(objectClass, method);

        if (fieldClass == int.class) {
            return new FieldWriterInt32ValFunc(fieldName, ordinal, features, format, label, null, method, (ToIntFunction<T>) lambda);
        }

        if (fieldClass == long.class) {
            if (format == null || format.isEmpty() || "string".equals(format)) {
                return new FieldWriterInt64ValFunc(fieldName, ordinal, features, format, label, field, method, (ToLongFunction) lambda);
            }

            return new FieldWriterMillisFunc(fieldName, ordinal, features, format, label, field, method, (ToLongFunction) lambda);
        }

        if (fieldClass == boolean.class) {
            return new FieldWriterBoolValFunc(fieldName, ordinal, features, format, label, field, method, (Predicate<T>) lambda);
        }

        if (fieldClass == Boolean.class) {
            return new FieldWriterBooleanFunc(fieldName, ordinal, features, format, label, field, method, (Function) lambda);
        }

        if (fieldClass == short.class) {
            return new FieldWriterInt16ValFunc(fieldName, ordinal, features, format, label, field, method, (ToShortFunction) lambda);
        }

        if (fieldClass == byte.class) {
            return new FieldWriterInt8ValFunc(fieldName, ordinal, features, format, label, field, method, (ToByteFunction) lambda);
        }

        if (fieldClass == float.class) {
            return new FieldWriterFloatValueFunc(fieldName, ordinal, features, format, label, field, method, (ToFloatFunction) lambda);
        }

        if (fieldClass == Float.class) {
            return new FieldWriterFloatFunc(fieldName, ordinal, features, format, label, field, method, (Function) lambda);
        }

        if (fieldClass == double.class) {
            return new FieldWriterDoubleValueFunc(fieldName, ordinal, features, format, label, field, method, (ToDoubleFunction) lambda);
        }

        if (fieldClass == Double.class) {
            return new FieldWriterDoubleFunc(fieldName, ordinal, features, format, label, field, method, (Function) lambda);
        }

        if (fieldClass == char.class) {
            return new FieldWriterCharValFunc(fieldName, ordinal, features, format, label, field, method, (ToCharFunction) lambda);
        }

        Function function = (Function) lambda;
        return createFieldWriter(provider, objectClass, fieldName, ordinal, features, format, label, fieldType, fieldClass, field, method, function);
    }
}
