package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.*;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.impl.*;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.util.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.Function;

import static com.alibaba.fastjson2.util.BeanUtils.*;

public class ObjectReaderBaseModule
        implements ObjectReaderModule {
    static Method METHOD_getPermittedSubclasses;
    final ObjectReaderProvider provider;
    final ReaderAnnotationProcessor annotationProcessor;

    public ObjectReaderBaseModule(ObjectReaderProvider provider) {
        this.provider = provider;
        this.annotationProcessor = new ReaderAnnotationProcessor();
    }

    @Override
    public ObjectReaderProvider getProvider() {
        return provider;
    }

    @Override
    public void init(ObjectReaderProvider provider) {
        provider.registerTypeConvert(Character.class, char.class, o -> o);

        Class[] numberTypes = new Class[]{
                Boolean.class,
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Number.class,
                Float.class,
                Double.class,
                BigInteger.class,
                BigDecimal.class,
                AtomicInteger.class,
                AtomicLong.class,
        };

        Function<Object, Boolean> TO_BOOLEAN = new ToBoolean(null);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, Boolean.class, TO_BOOLEAN);
        }

        Function<Object, Boolean> TO_BOOLEAN_VALUE = new ToBoolean(Boolean.FALSE);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, boolean.class, TO_BOOLEAN_VALUE);
        }

        Function<Object, String> TO_STRING = new ToString();
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, String.class, TO_STRING);
        }

        Function<Object, BigDecimal> TO_DECIMAL = new ToBigDecimal();
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, BigDecimal.class, TO_DECIMAL);
        }

        Function<Object, BigInteger> TO_BIGINT = new ToBigInteger();
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, BigInteger.class, TO_BIGINT);
        }

        Function<Object, Byte> TO_BYTE = new ToByte(null);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, Byte.class, TO_BYTE);
        }

        Function<Object, Byte> TO_BYTE_VALUE = new ToByte((byte) 0);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, byte.class, TO_BYTE_VALUE);
        }

        Function<Object, Short> TO_SHORT = new ToShort(null);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, Short.class, TO_SHORT);
        }

        Function<Object, Short> TO_SHORT_VALUE = new ToShort((short) 0);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, short.class, TO_SHORT_VALUE);
        }

        Function<Object, Integer> TO_INTEGER = new ToInteger(null);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, Integer.class, TO_INTEGER);
        }

        Function<Object, Integer> TO_INT = new ToInteger(0);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, int.class, TO_INT);
        }

        Function<Object, Long> TO_LONG = new ToLong(null);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, Long.class, TO_LONG);
        }

        Function<Object, Long> TO_LONG_VALUE = new ToLong(0L);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, long.class, TO_LONG_VALUE);
        }

        Function<Object, Float> TO_FLOAT = new ToFloat(null);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, Float.class, TO_FLOAT);
        }

        Function<Object, Float> TO_FLOAT_VALUE = new ToFloat(0F);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, float.class, TO_FLOAT_VALUE);
        }

        Function<Object, Double> TO_DOUBLE = new ToDouble(null);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, Double.class, TO_DOUBLE);
        }

        Function<Object, Double> TO_DOUBLE_VALUE = new ToDouble(0D);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, double.class, TO_DOUBLE_VALUE);
        }

        Function<Object, Number> TO_NUMBER = new ToNumber(0D);
        for (Class type : numberTypes) {
            provider.registerTypeConvert(type, Number.class, TO_NUMBER);
        }

        {
            // String to Any
            provider.registerTypeConvert(String.class, char.class, new StringToAny(char.class, '0'));
            provider.registerTypeConvert(String.class, boolean.class, new StringToAny(boolean.class, false));
            provider.registerTypeConvert(String.class, float.class, new StringToAny(float.class, (float) 0));
            provider.registerTypeConvert(String.class, double.class, new StringToAny(double.class, (double) 0));
            provider.registerTypeConvert(String.class, byte.class, new StringToAny(byte.class, (byte) 0));
            provider.registerTypeConvert(String.class, short.class, new StringToAny(short.class, (short) 0));
            provider.registerTypeConvert(String.class, int.class, new StringToAny(int.class, 0));
            provider.registerTypeConvert(String.class, long.class, new StringToAny(long.class, 0L));

            provider.registerTypeConvert(String.class, Character.class, new StringToAny(Character.class, null));
            provider.registerTypeConvert(String.class, Boolean.class, new StringToAny(Boolean.class, null));
            provider.registerTypeConvert(String.class, Double.class, new StringToAny(Double.class, null));
            provider.registerTypeConvert(String.class, Float.class, new StringToAny(Float.class, null));
            provider.registerTypeConvert(String.class, Byte.class, new StringToAny(Byte.class, null));
            provider.registerTypeConvert(String.class, Short.class, new StringToAny(Short.class, null));
            provider.registerTypeConvert(String.class, Integer.class, new StringToAny(Integer.class, null));
            provider.registerTypeConvert(String.class, Long.class, new StringToAny(Long.class, null));
            provider.registerTypeConvert(String.class, BigDecimal.class, new StringToAny(BigDecimal.class, null));
            provider.registerTypeConvert(String.class, BigInteger.class, new StringToAny(BigInteger.class, null));
            provider.registerTypeConvert(String.class, Number.class, new StringToAny(BigDecimal.class, null));
            provider.registerTypeConvert(String.class, Collection.class, new StringToAny(Collection.class, null));
            provider.registerTypeConvert(String.class, List.class, new StringToAny(List.class, null));
            provider.registerTypeConvert(String.class, JSONArray.class, new StringToAny(JSONArray.class, null));
        }

        {
            provider.registerTypeConvert(Boolean.class, boolean.class, o -> o);
        }
        {
            Function function = o -> o == null || "null".equals(o) || o.equals(0L)
                    ? null
                    : LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) o), ZoneId.systemDefault());
            provider.registerTypeConvert(Long.class, LocalDateTime.class, function);
        }
        {
            Function function = o -> o == null || "null".equals(o) || "".equals(o)
                    ? null
                    : UUID.fromString((String) o);
            provider.registerTypeConvert(String.class, UUID.class, function);
        }
    }

    public class ReaderAnnotationProcessor
            implements ObjectReaderAnnotationProcessor {
        @Override
        public void getBeanInfo(BeanInfo beanInfo, Class<?> objectClass) {
            Class mixInSource = provider.mixInCache.get(objectClass);
            if (mixInSource == null) {
                String typeName = objectClass.getName();
                if ("org.apache.commons.lang3.tuple.Triple".equals(typeName)) {
                    // TripleMixIn support removed in slim build
                }
            }

            if (mixInSource != null && mixInSource != objectClass) {
                beanInfo.mixIn = true;
                getBeanInfo(beanInfo, getAnnotations(mixInSource));

                BeanUtils.staticMethod(mixInSource,
                        method -> getCreator(beanInfo, objectClass, method)
                );

                BeanUtils.constructor(mixInSource, constructor ->
                        getCreator(beanInfo, objectClass, constructor)
                );
            }

            Class seeAlsoClass = null;
            for (Class superClass = objectClass.getSuperclass(); ; superClass = superClass.getSuperclass()) {
                if (superClass == null || superClass == Object.class || superClass == Enum.class) {
                    break;
                }

                BeanInfo superBeanInfo = new BeanInfo(JSONFactory.getDefaultObjectReaderProvider());
                getBeanInfo(superBeanInfo, superClass);
                if (superBeanInfo.seeAlso != null) {
                    boolean inSeeAlso = false;
                    for (Class seeAlsoItem : superBeanInfo.seeAlso) {
                        if (seeAlsoItem == objectClass) {
                            inSeeAlso = true;
                            break;
                        }
                    }
                    if (!inSeeAlso) {
                        seeAlsoClass = superClass;
                    }
                }
            }

            if (seeAlsoClass != null) {
                getBeanInfo(beanInfo, seeAlsoClass);
            }

            Annotation[] annotations = getAnnotations(objectClass);
            getBeanInfo(beanInfo, annotations);

            for (Annotation annotation : annotations) {
                boolean useJacksonAnnotation = JSONFactory.isUseJacksonAnnotation();
                Class<? extends Annotation> annotationType = annotation.annotationType();
                String annotationTypeName = annotationType.getName();
                switch (annotationTypeName) {
                    case "com.alibaba.fastjson.annotation.JSONType":
                        getBeanInfo1x(beanInfo, annotation);
                        break;
                    case "com.fasterxml.jackson.annotation.JsonTypeInfo":
                        if (useJacksonAnnotation) {
                            processJacksonJsonTypeInfo(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.databind.annotation.JsonDeserialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonDeserializer(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonTypeName":
                        if (useJacksonAnnotation) {
                            processJacksonJsonTypeName(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonFormat":
                        if (useJacksonAnnotation) {
                            processJacksonJsonFormat(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonSubTypes":
                        if (useJacksonAnnotation) {
                            processJacksonJsonSubTypes(beanInfo, annotation);
                        }
                        break;
                    case "kotlin.Metadata":
                        beanInfo.kotlin = true;
                        break;
                    default:
                        break;
                }
            }

            if (JDKUtils.JVM_VERSION >= 17
                    && beanInfo.seeAlso == null
                    && objectClass.isAnnotationPresent(JSONType.class)
            ) {
                try {
                    Method method = METHOD_getPermittedSubclasses;
                    if (method == null) {
                        method = Class.class.getMethod("getPermittedSubclasses");
                        METHOD_getPermittedSubclasses = method;
                    }
                    Class[] classes = (Class[]) method.invoke(objectClass);
                    beanInfo.seeAlso = classes;
                    beanInfo.seeAlsoNames = new String[classes.length];
                    for (int i = 0; i < classes.length; i++) {
                        Class<?> item = classes[i];

                        BeanInfo itemBeanInfo = new BeanInfo(provider);
                        processSeeAlsoAnnotation(itemBeanInfo, item);
                        String typeName = itemBeanInfo.typeName;
                        if (typeName == null || typeName.isEmpty()) {
                            typeName = item.getSimpleName();
                        }
                        beanInfo.seeAlsoNames[i] = typeName;
                    }
                } catch (Throwable ignored) {
                    // ignore
                }
            }

            BeanUtils.staticMethod(objectClass,
                    method -> getCreator(beanInfo, objectClass, method)
            );

            BeanUtils.constructor(objectClass, constructor ->
                    getCreator(beanInfo, objectClass, constructor)
            );

            if (beanInfo.creatorConstructor == null
                    && (beanInfo.readerFeatures & JSONReader.Feature.FieldBased.mask) == 0
                    && beanInfo.kotlin) {
                // Kotlin support removed
            }
        }

        private void processJacksonJsonSubTypes(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    if ("value".equals(name)) {
                        Object[] value = (Object[]) result;
                        if (value.length != 0) {
                            beanInfo.seeAlso = new Class[value.length];
                            beanInfo.seeAlsoNames = new String[value.length];
                            for (int i = 0; i < value.length; i++) {
                                Annotation subTypeAnn = (Annotation) value[i];
                                processJacksonJsonSubTypesType(beanInfo, i, subTypeAnn);
                            }
                        }
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
        }

        private void processJacksonJsonDeserializer(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    if ("using".equals(name) || "contentUsing".equals(name)) {
                        Class using = processUsing((Class) result);
                        if (using != null) {
                            beanInfo.deserializer = using;
                        }
                    } else if ("builder".equals(name)) {
                        processBuilder(beanInfo, (Class) result);
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
        }

        private void processJacksonJsonTypeInfo(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    if ("property".equals(name)) {
                        String value = (String) result;
                        if (!value.isEmpty()) {
                            beanInfo.typeKey = value;
                        }
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
        }

        private void getBeanInfo(BeanInfo beanInfo, Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                JSONType jsonType = findAnnotation(annotation, JSONType.class);
                if (jsonType != null) {
                    getBeanInfo1x(beanInfo, annotation);
                    if (jsonType == annotation) {
                        continue;
                    }
                }

                if (annotationType == JSONCompiler.class) {
                    JSONCompiler compiler = (JSONCompiler) annotation;
                    if (compiler.value() == JSONCompiler.CompilerOption.LAMBDA) {
                        beanInfo.readerFeatures |= FieldInfo.JIT;
                    }
                }
            }
        }

        void getBeanInfo1x(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);

                    switch (name) {
                        case "seeAlso": {
                            Class<?>[] classes = (Class<?>[]) result;
                            if (classes.length != 0) {
                                beanInfo.seeAlso = classes;
                                beanInfo.seeAlsoNames = new String[classes.length];
                                for (int i = 0; i < classes.length; i++) {
                                    Class<?> item = classes[i];

                                    BeanInfo itemBeanInfo = new BeanInfo(JSONFactory.getDefaultObjectReaderProvider());
                                    processSeeAlsoAnnotation(itemBeanInfo, item);
                                    String typeName = itemBeanInfo.typeName;
                                    if (typeName == null || typeName.isEmpty()) {
                                        typeName = item.getSimpleName();
                                    }
                                    beanInfo.seeAlsoNames[i] = typeName;
                                }
                            }
                            break;
                        }
                        case "seeAlsoDefault": {
                            Class<?> seeAlsoDefault = (Class<?>) result;
                            if (seeAlsoDefault != Void.class) {
                                beanInfo.seeAlsoDefault = seeAlsoDefault;
                            }
                        }
                        case "typeKey": {
                            String jsonTypeKey = (String) result;
                            if (!jsonTypeKey.isEmpty()) {
                                beanInfo.typeKey = jsonTypeKey;
                            }
                            break;
                        }
                        case "typeName": {
                            String typeName = (String) result;
                            if (!typeName.isEmpty()) {
                                beanInfo.typeName = typeName;
                            }
                            break;
                        }
                        case "rootName": {
                            String rootName = (String) result;
                            if (!rootName.isEmpty()) {
                                beanInfo.rootName = rootName;
                            }
                            break;
                        }
                        case "naming": {
                            Enum naming = (Enum) result;
                            beanInfo.namingStrategy = naming.name();
                            break;
                        }
                        case "ignores": {
                            String[] ignores = (String[]) result;
                            if (ignores.length > 0) {
                                beanInfo.ignores = ignores;
                            }
                            break;
                        }
                        case "orders": {
                            String[] fields = (String[]) result;
                            if (fields.length != 0) {
                                beanInfo.orders = fields;
                            }
                            break;
                        }
                        case "schema": {
                            String schema = (String) result;
                            schema = schema.trim();
                            if (!schema.isEmpty()) {
                                beanInfo.schema = schema;
                            }
                            break;
                        }
                        case "deserializer": {
                            Class<?> deserializer = (Class) result;
                            if (ObjectReader.class.isAssignableFrom(deserializer)) {
                                beanInfo.deserializer = deserializer;
                            }
                            break;
                        }
                        case "parseFeatures": {
                            Enum[] features = (Enum[]) result;
                            for (Enum feature : features) {
                                switch (feature.name()) {
                                    case "SupportArrayToBean":
                                        beanInfo.readerFeatures |= JSONReader.Feature.SupportArrayToBean.mask;
                                        break;
                                    case "InitStringFieldAsEmpty":
                                        beanInfo.readerFeatures |= JSONReader.Feature.InitStringFieldAsEmpty.mask;
                                        break;
                                    case "TrimStringFieldValue":
//                                        beanInfo.readerFeatures |= JSONReader.Feature.TrimStringFieldValue.mask;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            break;
                        }
                        case "deserializeFeatures": {
                            JSONReader.Feature[] features = (JSONReader.Feature[]) result;
                            for (JSONReader.Feature feature : features) {
                                beanInfo.readerFeatures |= feature.mask;
                            }
                            break;
                        }
                        case "builder": {
                            processBuilder(beanInfo, (Class) result);
                            break;
                        }
                        case "deserializeUsing": {
                            Class<?> deserializeUsing = (Class) result;
                            if (ObjectReader.class.isAssignableFrom(deserializeUsing)) {
                                beanInfo.deserializer = deserializeUsing;
                            }
                            break;
                        }
                        case "disableReferenceDetect":
                            if (Boolean.TRUE.equals(result)) {
                                beanInfo.readerFeatures |= FieldInfo.DISABLE_REFERENCE_DETECT;
                            }
                            break;
                        case "disableArrayMapping":
                            if (Boolean.TRUE.equals(result)) {
                                beanInfo.readerFeatures |= FieldInfo.DISABLE_ARRAY_MAPPING;
                            }
                            break;
                        case "disableJSONB":
                            if (Boolean.TRUE.equals(result)) {
                                beanInfo.readerFeatures |= FieldInfo.DISABLE_JSONB;
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Throwable ignored) {
                }
            });
        }

        private void processBuilder(BeanInfo beanInfo, Class builderClass) {
            if (builderClass != void.class && builderClass != Void.class) {
                beanInfo.builder = builderClass;

                for (Annotation builderAnnotation : getAnnotations(builderClass)) {
                    Class<? extends Annotation> builderAnnotationClass = builderAnnotation.annotationType();
                    String builderAnnotationName = builderAnnotationClass.getName();

                    if ("com.alibaba.fastjson.annotation.JSONPOJOBuilder".equals(builderAnnotationName)
                            || "com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder".equals(builderAnnotationName)) {
                        getBeanInfo1xJSONPOJOBuilder(beanInfo, builderClass, builderAnnotation, builderAnnotationClass);
                    } else {
                        JSONBuilder jsonBuilder = findAnnotation(builderClass, JSONBuilder.class);
                        if (jsonBuilder != null) {
                            String buildMethodName = jsonBuilder.buildMethod();
                            beanInfo.buildMethod = buildMethod(builderClass, buildMethodName);
                            String withPrefix = jsonBuilder.withPrefix();
                            if (!withPrefix.isEmpty()) {
                                beanInfo.builderWithPrefix = withPrefix;
                            }
                        }
                    }
                }

                if (beanInfo.buildMethod == null) {
                    beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, "build");
                }

                if (beanInfo.buildMethod == null) {
                    beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, "create");
                }
            }
        }

        private void processSeeAlsoAnnotation(BeanInfo beanInfo, Class<?> objectClass) {
            Class mixInSource = provider.mixInCache.get(objectClass);
            if (mixInSource == null) {
                String typeName = objectClass.getName();
                if ("org.apache.commons.lang3.tuple.Triple".equals(typeName)) {
                    // TripleMixIn support removed in slim build
                }
            }

            if (mixInSource != null && mixInSource != objectClass) {
                beanInfo.mixIn = true;
                processSeeAlsoAnnotation(beanInfo, getAnnotations(mixInSource));
            }

            processSeeAlsoAnnotation(beanInfo, getAnnotations(objectClass));
        }

        private void processSeeAlsoAnnotation(BeanInfo beanInfo, Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> itemAnnotationType = annotation.annotationType();
                BeanUtils.annotationMethods(itemAnnotationType, m -> {
                    String name = m.getName();
                    try {
                        Object result = m.invoke(annotation);
                        if ("typeName".equals(name)) {
                            String typeName = (String) result;
                            if (!typeName.isEmpty()) {
                                beanInfo.typeName = typeName;
                            }
                        }
                    } catch (Throwable ignored) {
                        // ignored
                    }
                });
            }
        }

        @Override
        public void getFieldInfo(
                FieldInfo fieldInfo,
                Class objectClass,
                Constructor constructor,
                int paramIndex,
                Parameter parameter
        ) {
            if (objectClass != null) {
                Class mixInSource = provider.mixInCache.get(objectClass);
                if (mixInSource != null && mixInSource != objectClass) {
                    Constructor mixInConstructor = null;
                    try {
                        mixInConstructor = mixInSource.getDeclaredConstructor(constructor.getParameterTypes());
                    } catch (NoSuchMethodException ignored) {
                    }
                    if (mixInConstructor != null) {
                        Parameter mixInParam = mixInConstructor.getParameters()[paramIndex];
                        processAnnotation(fieldInfo, getAnnotations(mixInParam));
                    }
                }
            }

            boolean staticClass = Modifier.isStatic(constructor.getDeclaringClass().getModifiers());
            Annotation[] annotations = null;
            if (staticClass) {
                try {
                    annotations = getAnnotations(parameter);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    // ignored
                }
            } else {
                Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
                int paIndex;
                if (parameterAnnotations.length == constructor.getParameterCount()) {
                    paIndex = paramIndex;
                } else {
                    paIndex = paramIndex - 1;
                }
                if (paIndex >= 0 && paIndex < parameterAnnotations.length) {
                    annotations = parameterAnnotations[paIndex];
                }
            }

            if (annotations != null && annotations.length > 0) {
                processAnnotation(fieldInfo, annotations);
            }
        }

        @Override
        public void getFieldInfo(
                FieldInfo fieldInfo,
                Class objectClass,
                Method method,
                int paramIndex,
                Parameter parameter
        ) {
            if (objectClass != null) {
                Class mixInSource = provider.mixInCache.get(objectClass);
                if (mixInSource != null && mixInSource != objectClass) {
                    Method mixInMethod = null;
                    try {
                        mixInMethod = mixInSource.getMethod(method.getName(), method.getParameterTypes());
                    } catch (NoSuchMethodException ignored) {
                    }
                    if (mixInMethod != null) {
                        Parameter mixInParam = mixInMethod.getParameters()[paramIndex];
                        processAnnotation(fieldInfo, getAnnotations(mixInParam));
                    }
                }
            }

            processAnnotation(fieldInfo, getAnnotations(parameter));
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
            if (objectClass != null) {
                Class mixInSource = provider.mixInCache.get(objectClass);
                if (mixInSource != null && mixInSource != objectClass) {
                    Field mixInField = null;
                    try {
                        mixInField = mixInSource.getDeclaredField(field.getName());
                    } catch (Exception ignored) {
                    }

                    if (mixInField != null) {
                        getFieldInfo(fieldInfo, mixInSource, mixInField);
                    }
                }
            }

            Annotation[] annotations = getAnnotations(field);
            processAnnotation(fieldInfo, annotations);
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method) {
            String methodName = method.getName();

            if (objectClass != null) {
                Class superclass = objectClass.getSuperclass();
                Method supperMethod = BeanUtils.getMethod(superclass, method);
                if (supperMethod != null) {
                    getFieldInfo(fieldInfo, superclass, supperMethod);
                }

                Class[] interfaces = objectClass.getInterfaces();
                for (Class i : interfaces) {
                    if (i == Serializable.class) {
                        continue;
                    }

                    Method interfaceMethod = BeanUtils.getMethod(i, method);
                    if (interfaceMethod != null) {
                        getFieldInfo(fieldInfo, superclass, interfaceMethod);
                    }
                }

                Class mixInSource = provider.mixInCache.get(objectClass);
                if (mixInSource != null && mixInSource != objectClass) {
                    Method mixInMethod = null;
                    try {
                        mixInMethod = mixInSource.getDeclaredMethod(methodName, method.getParameterTypes());
                    } catch (Exception ignored) {
                    }

                    if (mixInMethod != null) {
                        getFieldInfo(fieldInfo, mixInSource, mixInMethod);
                    }
                }
            }

            String jsonFieldName = null;

            Annotation[] annotations = getAnnotations(method);
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                JSONField jsonField = findAnnotation(annotation, JSONField.class);
                if (jsonField != null) {
                    getFieldInfo(fieldInfo, jsonField);
                    jsonFieldName = jsonField.name();
                    if (jsonField == annotation) {
                        continue;
                    }
                }

                if (annotationType == JSONCompiler.class) {
                    JSONCompiler compiler = (JSONCompiler) annotation;
                    if (compiler.value() == JSONCompiler.CompilerOption.LAMBDA) {
                        fieldInfo.features |= FieldInfo.JIT;
                    }
                }

                boolean useJacksonAnnotation = JSONFactory.isUseJacksonAnnotation();
                String annotationTypeName = annotationType.getName();
                switch (annotationTypeName) {
                    case "com.fasterxml.jackson.annotation.JsonIgnore":
                        if (useJacksonAnnotation) {
                            processJacksonJsonIgnore(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.databind.annotation.JsonDeserialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonDeserialize(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonFormat":
                        if (useJacksonAnnotation) {
                            processJacksonJsonFormat(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAnySetter":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
                        }
                        break;
                    case "com.alibaba.fastjson.annotation.JSONField":
                        processJSONField1x(fieldInfo, annotation);
                        break;
                    case "com.fasterxml.jackson.annotation.JsonProperty":
                        if (useJacksonAnnotation) {
                            processJacksonJsonProperty(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAlias":
                        if (useJacksonAnnotation) {
                            processJacksonJsonAlias(fieldInfo, annotation);
                        }
                        break;
                    case "com.google.gson.annotations.SerializedName":
                        if (JSONFactory.isUseGsonAnnotation()) {
                            processGsonSerializedName(fieldInfo, annotation);
                        }
                        break;
                    default:
                        break;
                }
            }

            String fieldName;
            if (methodName.startsWith("set")) {
                fieldName = BeanUtils.setterName(methodName, null);
            } else {
                fieldName = BeanUtils.getterName(methodName, null); // readOnlyProperty
            }

            String fieldName1, fieldName2;
            char c0, c1;
            if (fieldName.length() > 1
                    && (c0 = fieldName.charAt(0)) >= 'A' && c0 <= 'Z'
                    && (c1 = fieldName.charAt(1)) >= 'A' && c1 <= 'Z'
                    && (jsonFieldName == null || jsonFieldName.isEmpty())) {
                char[] chars = fieldName.toCharArray();
                chars[0] = (char) (chars[0] + 32);
                fieldName1 = new String(chars);

                chars[1] = (char) (chars[1] + 32);
                fieldName2 = new String(chars);
            } else {
                fieldName1 = null;
                fieldName2 = null;
            }

            BeanUtils.declaredFields(objectClass, field -> {
                String name = "";
                if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    if (field.getName().startsWith("is")) {
                        name = field.getName().substring(2);
                        if (!name.isEmpty()) {
                            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                        }
                    }
                }

                if (field.getName().equals(fieldName) || name.equals(fieldName)) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers)) {
                        getFieldInfo(fieldInfo, objectClass, field);
                    }
                    fieldInfo.features |= FieldInfo.FIELD_MASK;
                } else if (field.getName().equals(fieldName1)) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers)) {
                        getFieldInfo(fieldInfo, objectClass, field);
                    }
                    fieldInfo.features |= FieldInfo.FIELD_MASK;
                } else if (field.getName().equals(fieldName2)) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers)) {
                        getFieldInfo(fieldInfo, objectClass, field);
                    }
                    fieldInfo.features |= FieldInfo.FIELD_MASK;
                }
            });

            if (fieldName1 != null && fieldInfo.fieldName == null && fieldInfo.alternateNames == null) {
                fieldInfo.alternateNames = new String[]{fieldName1, fieldName2};
            }
        }

        private void processAnnotation(FieldInfo fieldInfo, Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                JSONField jsonField = findAnnotation(annotation, JSONField.class);
                if (jsonField != null) {
                    getFieldInfo(fieldInfo, jsonField);
                    if (jsonField == annotation) {
                        continue;
                    }
                }

                if (annotationType == JSONCompiler.class) {
                    JSONCompiler compiler = (JSONCompiler) annotation;
                    if (compiler.value() == JSONCompiler.CompilerOption.LAMBDA) {
                        fieldInfo.features |= FieldInfo.JIT;
                    }
                }

                boolean useJacksonAnnotation = JSONFactory.isUseJacksonAnnotation();
                String annotationTypeName = annotationType.getName();
                switch (annotationTypeName) {
                    case "com.fasterxml.jackson.annotation.JsonIgnore":
                        if (useJacksonAnnotation) {
                            processJacksonJsonIgnore(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAnyGetter":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
                        }
                        break;
                    case "com.alibaba.fastjson.annotation.JSONField":
                        processJSONField1x(fieldInfo, annotation);
                        break;
                    case "com.fasterxml.jackson.annotation.JsonProperty":
                        if (useJacksonAnnotation) {
                            processJacksonJsonProperty(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonFormat":
                        if (useJacksonAnnotation) {
                            processJacksonJsonFormat(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.databind.annotation.JsonDeserialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonDeserialize(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAlias":
                        if (useJacksonAnnotation) {
                            processJacksonJsonAlias(fieldInfo, annotation);
                        }
                        break;
                    case "com.google.gson.annotations.SerializedName":
                        if (JSONFactory.isUseGsonAnnotation()) {
                            processGsonSerializedName(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonSetter":
                        if (useJacksonAnnotation) {
                            processJacksonJsonSetter(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonManagedReference":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= JSONWriter.Feature.ReferenceDetection.mask;
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonBackReference":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.BACKR_REFERENCE;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        private void processJacksonJsonDeserialize(FieldInfo fieldInfo, Annotation annotation) {
            if (!JSONFactory.isUseJacksonAnnotation()) {
                return;
            }

            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "using": {
                            Class using = processUsing((Class) result);
                            if (using != null) {
                                fieldInfo.readUsing = using;
                            }
                            break;
                        }
                        case "keyUsing": {
                            Class keyUsing = processUsing((Class) result);
                            if (keyUsing != null) {
                                fieldInfo.keyUsing = keyUsing;
                            }
                            break;
                        }
                        case "valueUsing": {
                            Class valueUsing = processUsing((Class) result);
                            if (valueUsing != null) {
                                fieldInfo.keyUsing = valueUsing;
                            }
                            break;
                        }
                        default:
                            break;
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
        }

        private Class processUsing(Class using) {
            String usingName = using.getName();
            String noneClassName0 = "com.fasterxml.jackson.databind.JsonDeserializer$None";
            if (!noneClassName0.equals(usingName)
                    && ObjectReader.class.isAssignableFrom(using)
            ) {
                return using;
            }
            return null;
        }

        private void processJacksonJsonProperty(FieldInfo fieldInfo, Annotation annotation) {
            if (!JSONFactory.isUseJacksonAnnotation()) {
                return;
            }

            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "value": {
                            String value = (String) result;
                            if (!value.isEmpty()
                                    && (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty())
                            ) {
                                fieldInfo.fieldName = value;
                            }
                            break;
                        }
                        case "access": {
                            String access = ((Enum) result).name();
                            fieldInfo.ignore = "READ_ONLY".equals(access);
                            break;
                        }
                        case "required":
                            boolean required = (Boolean) result;
                            if (required) {
                                fieldInfo.required = true;
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
        }

        private void processJacksonJsonSetter(FieldInfo fieldInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "value": {
                            String value = (String) result;
                            if (!value.isEmpty()) {
                                fieldInfo.fieldName = value;
                            }
                            break;
                        }
                        default:
                            break;
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
        }

        private void processJacksonJsonAlias(FieldInfo fieldInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    if ("value".equals(name)) {
                        String[] values = (String[]) result;
                        if (values.length != 0) {
                            fieldInfo.alternateNames = values;
                        }
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
        }

        private void processJSONField1x(FieldInfo fieldInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "name": {
                            String value = (String) result;
                            if (!value.isEmpty()) {
                                fieldInfo.fieldName = value;
                            }
                            break;
                        }
                        case "format": {
                            String format = (String) result;
                            if (!format.isEmpty()) {
                                format = format.trim();

                                if (format.indexOf('T') != -1 && !format.contains("'T'")) {
                                    format = format.replace("T", "'T'");
                                }

                                fieldInfo.format = format;
                            }
                            break;
                        }
                        case "label": {
                            String label = (String) result;
                            if (!label.isEmpty()) {
                                fieldInfo.label = label;
                            }
                            break;
                        }
                        case "defaultValue": {
                            String value = (String) result;
                            if (!value.isEmpty()) {
                                fieldInfo.defaultValue = value;
                            }
                            break;
                        }
                        case "alternateNames": {
                            String[] alternateNames = (String[]) result;
                            if (alternateNames.length != 0) {
                                if (fieldInfo.alternateNames == null) {
                                    fieldInfo.alternateNames = alternateNames;
                                } else {
                                    Set<String> nameSet = new LinkedHashSet<>();
                                    nameSet.addAll(Arrays.asList(alternateNames));
                                    nameSet.addAll(Arrays.asList(fieldInfo.alternateNames));
                                    fieldInfo.alternateNames = nameSet.toArray(new String[nameSet.size()]);
                                }
                            }
                            break;
                        }
                        case "ordinal": {
                            int ordinal = (Integer) result;
                            if (ordinal != 0) {
                                fieldInfo.ordinal = ordinal;
                            }
                            break;
                        }
                        case "deserialize": {
                            boolean serialize = (Boolean) result;
                            if (!serialize) {
                                fieldInfo.ignore = true;
                            }
                            break;
                        }
                        case "parseFeatures": {
                            Enum[] features = (Enum[]) result;
                            for (Enum feature : features) {
                                switch (feature.name()) {
                                    case "SupportArrayToBean":
                                        fieldInfo.features |= JSONReader.Feature.SupportArrayToBean.mask;
                                        break;
                                    case "InitStringFieldAsEmpty":
                                        fieldInfo.features |= JSONReader.Feature.InitStringFieldAsEmpty.mask;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            break;
                        }
                        case "deserializeUsing": {
                            Class<?> deserializeUsing = (Class) result;
                            if (ObjectReader.class.isAssignableFrom(deserializeUsing)) {
                                fieldInfo.readUsing = deserializeUsing;
                            }
                            break;
                        }
                        case "unwrapped": {
                            boolean unwrapped = (Boolean) result;
                            if (unwrapped) {
                                fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
                            }
                            break;
                        }
                        default:
                            break;
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
        }

        private void getFieldInfo(FieldInfo fieldInfo, JSONField jsonField) {
            if (jsonField == null) {
                return;
            }

            String jsonFieldName = jsonField.name();
            if (!jsonFieldName.isEmpty()) {
                fieldInfo.fieldName = jsonFieldName;
            }

            String jsonFieldFormat = jsonField.format();
            if (!jsonFieldFormat.isEmpty()) {
                jsonFieldFormat = jsonFieldFormat.trim();
                if (jsonFieldFormat.indexOf('T') != -1 && !jsonFieldFormat.contains("'T'")) {
                    jsonFieldFormat = jsonFieldFormat.replace("T", "'T'");
                }

                fieldInfo.format = jsonFieldFormat;
            }

            String label = jsonField.label();
            if (!label.isEmpty()) {
                label = label.trim();
                fieldInfo.label = label;
            }

            String defaultValue = jsonField.defaultValue();
            if (!defaultValue.isEmpty()) {
                fieldInfo.defaultValue = defaultValue;
            }

            String locale = jsonField.locale();
            if (!locale.isEmpty()) {
                String[] parts = locale.split("_");
                if (parts.length == 2) {
                    fieldInfo.locale = new Locale(parts[0], parts[1]);
                }
            }

            String[] alternateNames = jsonField.alternateNames();
            if (alternateNames.length != 0) {
                if (fieldInfo.alternateNames == null) {
                    fieldInfo.alternateNames = alternateNames;
                } else {
                    Set<String> nameSet = new LinkedHashSet<>(alternateNames.length + fieldInfo.alternateNames.length, 1F);
                    Collections.addAll(nameSet, alternateNames);
                    Collections.addAll(nameSet, fieldInfo.alternateNames);
                    fieldInfo.alternateNames = nameSet.toArray(new String[nameSet.size()]);
                }
            }

            boolean ignore = !jsonField.deserialize();
            if (!fieldInfo.ignore) {
                fieldInfo.ignore = ignore;
            }

            for (JSONReader.Feature feature : jsonField.deserializeFeatures()) {
                fieldInfo.features |= feature.mask;
                if (fieldInfo.ignore && !ignore && feature == JSONReader.Feature.FieldBased) {
                    fieldInfo.ignore = false;
                }
            }

            int ordinal = jsonField.ordinal();
            if (ordinal != 0) {
                fieldInfo.ordinal = ordinal;
            }

            boolean value = jsonField.value();
            if (value) {
                fieldInfo.features |= FieldInfo.VALUE_MASK;
            }

            if (jsonField.unwrapped()) {
                fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
            }

            if (jsonField.required()) {
                fieldInfo.required = true;
            }

            String schema = jsonField.schema().trim();
            if (!schema.isEmpty()) {
                fieldInfo.schema = schema;
            }

            Class deserializeUsing = jsonField.deserializeUsing();
            if (ObjectReader.class.isAssignableFrom(deserializeUsing)) {
                fieldInfo.readUsing = deserializeUsing;
            }

            String keyName = jsonField.arrayToMapKey().trim();
            if (!keyName.isEmpty()) {
                fieldInfo.arrayToMapKey = keyName;
            }

            Class<?> arrayToMapDuplicateHandler = jsonField.arrayToMapDuplicateHandler();
            if (arrayToMapDuplicateHandler != Void.class) {
                fieldInfo.arrayToMapDuplicateHandler = arrayToMapDuplicateHandler;
            }

            Class<?> contentAs = jsonField.contentAs();
            if (contentAs != Void.class) {
                fieldInfo.contentAs = contentAs;
            }
        }
    }

    private void getBeanInfo1xJSONPOJOBuilder(
            BeanInfo beanInfo,
            Class<?> builderClass,
            Annotation builderAnnatation,
            Class<? extends Annotation> builderAnnatationClass
    ) {
        BeanUtils.annotationMethods(builderAnnatationClass, method -> {
            try {
                String methodName = method.getName();
                switch (methodName) {
                    case "buildMethodName":
                    case "buildMethod": {
                        String buildMethodName = (String) method.invoke(builderAnnatation);
                        beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, buildMethodName);
                        break;
                    }
                    case "withPrefix": {
                        String withPrefix = (String) method.invoke(builderAnnatation);
                        if (!withPrefix.isEmpty()) {
                            beanInfo.builderWithPrefix = withPrefix;
                        }
                        break;
                    }
                    default:
                        break;
                }
            } catch (Throwable ignored) {
            }
        });
    }

    private void getCreator(BeanInfo beanInfo, Class<?> objectClass, Constructor constructor) {
        if (objectClass.isEnum()) {
            return;
        }

        Annotation[] annotations = getAnnotations(constructor);

        boolean creatorMethod = false;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();

            JSONCreator jsonCreator = findAnnotation(annotation, JSONCreator.class);
            if (jsonCreator != null) {
                String[] createParameterNames = jsonCreator.parameterNames();
                if (createParameterNames.length != 0) {
                    beanInfo.createParameterNames = createParameterNames;
                }

                creatorMethod = true;
                if (jsonCreator == annotation) {
                    continue;
                }
            }

            switch (annotationType.getName()) {
                case "com.alibaba.fastjson.annotation.JSONCreator":
                case "com.alibaba.fastjson2.annotation.JSONCreator":
                    creatorMethod = true;
                    BeanUtils.annotationMethods(annotationType, m1 -> {
                        try {
                            if ("parameterNames".equals(m1.getName())) {
                                String[] createParameterNames = (String[]) m1.invoke(annotation);
                                if (createParameterNames.length != 0) {
                                    beanInfo.createParameterNames = createParameterNames;
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    });
                    break;
                case "com.fasterxml.jackson.annotation.JsonCreator":
                    if (JSONFactory.isUseJacksonAnnotation()) {
                        creatorMethod = true;
                    }
                    break;
                default:
                    break;
            }
        }

        if (!creatorMethod) {
            return;
        }

        Constructor<?> targetConstructor = null;
        try {
            targetConstructor = objectClass.getDeclaredConstructor(constructor.getParameterTypes());
        } catch (NoSuchMethodException ignored) {
        }
        if (targetConstructor != null) {
            beanInfo.creatorConstructor = targetConstructor;
        }
    }

    private void getCreator(BeanInfo beanInfo, Class<?> objectClass, Method method) {
        if (method.getDeclaringClass() == Enum.class) {
            return;
        }

        String methodName = method.getName();
        if (objectClass.isEnum()) {
            if ("values".equals(methodName)) {
                return;
            }
        }

        Annotation[] annotations = getAnnotations(method);

        boolean creatorMethod = false;
        JSONCreator jsonCreator = null;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            jsonCreator = findAnnotation(annotation, JSONCreator.class);
            if (jsonCreator == annotation) {
                continue;
            }

            switch (annotationType.getName()) {
                case "com.alibaba.fastjson.annotation.JSONCreator":
                    creatorMethod = true;
                    BeanUtils.annotationMethods(annotationType, m1 -> {
                        try {
                            if ("parameterNames".equals(m1.getName())) {
                                String[] createParameterNames = (String[]) m1.invoke(annotation);
                                if (createParameterNames.length != 0) {
                                    beanInfo.createParameterNames = createParameterNames;
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    });
                    break;
                case "com.fasterxml.jackson.annotation.JsonCreator":
                    if (JSONFactory.isUseJacksonAnnotation()) {
                        creatorMethod = true;
                        BeanUtils.annotationMethods(annotationType, m1 -> {
                            try {
                                if ("parameterNames".equals(m1.getName())) {
                                    String[] createParameterNames = (String[]) m1.invoke(annotation);
                                    if (createParameterNames.length != 0) {
                                        beanInfo.createParameterNames = createParameterNames;
                                    }
                                }
                            } catch (Throwable ignored) {
                            }
                        });
                    }
                    break;
                default:
                    break;
            }
        }

        if (jsonCreator != null) {
            String[] createParameterNames = jsonCreator.parameterNames();
            if (createParameterNames.length != 0) {
                beanInfo.createParameterNames = createParameterNames;
            }

            creatorMethod = true;
        }

        if (!creatorMethod) {
            return;
        }

        Method targetMethod = null;
        try {
            targetMethod = objectClass.getDeclaredMethod(methodName, method.getParameterTypes());
        } catch (NoSuchMethodException ignored) {
        }

        if (targetMethod != null) {
            beanInfo.createMethod = targetMethod;
        }
    }

    @Override
    public ReaderAnnotationProcessor getAnnotationProcessor() {
        return annotationProcessor;
    }

    public void getBeanInfo(BeanInfo beanInfo, Class<?> objectClass) {
        if (annotationProcessor != null) {
            annotationProcessor.getBeanInfo(beanInfo, objectClass);
        }
    }

    public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
        if (annotationProcessor != null) {
            annotationProcessor.getFieldInfo(fieldInfo, objectClass, field);
        }
    }

    @Override
    public ObjectReader getObjectReader(ObjectReaderProvider provider, Type type) {
        String internalMixin = null;
        String typeName = type.getTypeName();
        switch (typeName) {
            case "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet":
                return null;
            case "org.springframework.util.LinkedMultiValueMap":
                return null;
            case "org.springframework.security.core.authority.RememberMeAuthenticationToken":
                internalMixin = "org.springframework.security.jackson2.AnonymousAuthenticationTokenMixin";
                break;
            case "org.springframework.security.core.authority.AnonymousAuthenticationToken":
                internalMixin = "org.springframework.security.jackson2.RememberMeAuthenticationTokenMixin";
                break;
            case "org.springframework.security.core.authority.SimpleGrantedAuthority":
                internalMixin = "org.springframework.security.jackson2.SimpleGrantedAuthorityMixin";
                break;
            case "org.springframework.security.core.userdetails.User":
                internalMixin = "org.springframework.security.jackson2.UserMixin";
                break;
            case "org.springframework.security.authentication.UsernamePasswordAuthenticationToken":
                internalMixin = "org.springframework.security.jackson2.UsernamePasswordAuthenticationTokenMixin";
                break;
            case "org.springframework.security.authentication.BadCredentialsException":
                internalMixin = "org.springframework.security.jackson2.BadCredentialsExceptionMixin";
                break;
            case "org.springframework.security.web.csrf.DefaultCsrfToken":
                internalMixin = "org.springframework.security.web.jackson2.DefaultCsrfTokenMixin";
                break;
            case "org.springframework.security.web.savedrequest.SavedCookie":
                internalMixin = "org.springframework.security.web.jackson2.SavedCookieMixin";
                break;
            case "org.springframework.security.web.authentication.WebAuthenticationDetails":
                internalMixin = "org.springframework.security.web.jackson2.WebAuthenticationDetailsMixin";
                break;
        }

        if (internalMixin != null) {
            Class mixin = provider.mixInCache.get(type);
            if (mixin == null) {
                mixin = TypeUtils.loadClass(internalMixin);
                if (mixin == null) {
                    // mixin not available in slim build
                }

                if (mixin != null) {
                    provider.mixInCache.putIfAbsent((Class) type, mixin);
                }
            }
        }

        if (type == Object.class
                || type == Cloneable.class
                || type == Closeable.class
                || type == Serializable.class
                || type == Comparable.class
        ) {
            return null;
        }

        if (type instanceof Class) {
            Class objectClass = (Class) type;

            if (isExtendedMap(objectClass)) {
                return null;
            }

            if (Map.class.isAssignableFrom(objectClass)) {
                return null;
            }

            if (Collection.class.isAssignableFrom(objectClass)) {
                return null;
            }

            if (objectClass.isArray()) {
                return null;
            }

            if (objectClass == StackTraceElement.class) {
                try {
                    Constructor constructor = objectClass.getConstructor(
                            String.class,
                            String.class,
                            String.class,
                            int.class);

                    return JSONFactory
                            .getDefaultObjectReaderProvider()
                            .getCreator()
                            .createObjectReaderNoneDefaultConstructor(
                                    constructor,
                                    "className",
                                    "methodName",
                                    "fileName",
                                    "lineNumber");
                } catch (Throwable ignored) {
                    //
                }
            }
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();

            if (rawType instanceof Class) {
                Class rawClass = (Class) rawType;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (actualTypeArguments.length == 2 && Map.class.isAssignableFrom(rawClass)) {
                    return null;
                }

                if (actualTypeArguments.length == 1 && Collection.class.isAssignableFrom(rawClass)) {
                    return null;
                }

                if (actualTypeArguments.length == 1 && rawType == Optional.class) {
                    return null;
                }

                if (actualTypeArguments.length == 1 && rawType == AtomicReference.class) {
                    return null;
                }

                if (actualTypeArguments.length == 1 && rawType == Map.Entry.class) {
                    return null;
                }
            }

            return null;
        }

        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getObjectReader(provider, upperBounds[0]);
            }
        }

        if (type == ParameterizedType.class) {
            return ObjectReaders.ofReflect(ParameterizedTypeImpl.class);
        }

        switch (typeName) {
            case "java.sql.Time":
                return JdbcSupport.createTimeReader((Class) type, null, null);
            case "java.sql.Timestamp":
                return JdbcSupport.createTimestampReader((Class) type, null, null);
            case "java.sql.Date":
                return JdbcSupport.createDateReader((Class) type, null, null);
            case "java.lang.Throwable":
            case "java.lang.Exception":
            case "java.lang.IllegalStateException":
            case "java.lang.RuntimeException":
            case "java.io.IOException":
            case "java.io.UncheckedIOException":
                return new ObjectReaderException((Class) type);
            case "com.carrotsearch.hppc.ByteArrayList":
            case "com.carrotsearch.hppc.ShortArrayList":
            case "com.carrotsearch.hppc.IntArrayList":
            case "com.carrotsearch.hppc.IntHashSet":
            case "com.carrotsearch.hppc.LongArrayList":
            case "com.carrotsearch.hppc.LongHashSet":
            case "com.carrotsearch.hppc.CharArrayList":
            case "com.carrotsearch.hppc.CharHashSet":
            case "com.carrotsearch.hppc.FloatArrayList":
            case "com.carrotsearch.hppc.DoubleArrayList":
            case "gnu.trove.list.array.TByteArrayList":
            case "gnu.trove.list.array.TCharArrayList":
            case "gnu.trove.list.array.TShortArrayList":
            case "gnu.trove.list.array.TIntArrayList":
            case "java.awt.Color":
                try {
                    Constructor constructor = ((Class) type).getConstructor(int.class, int.class, int.class, int.class);
                    return ObjectReaderCreator.INSTANCE.createObjectReaderNoneDefaultConstructor(constructor, "r", "g", "b", "alpha");
                } catch (Throwable ignored) {
                    // ignored
                }
            default:
                break;
        }

        return null;
    }

    public static ObjectReader typedMap(Class mapType, Class instanceType, Type keyType, Type valueType) {
        return null;
    }
}
