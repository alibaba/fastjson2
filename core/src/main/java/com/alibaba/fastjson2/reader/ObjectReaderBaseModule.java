package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.*;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.impl.*;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.support.money.MoneySupport;
import com.alibaba.fastjson2.util.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.alibaba.fastjson2.util.AnnotationUtils.getAnnotations;
import static com.alibaba.fastjson2.util.BeanUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE_SUPPORT;

public class ObjectReaderBaseModule
        implements ObjectReaderModule {
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
                switch (typeName) {
                    case "org.apache.commons.lang3.tuple.Triple":
                        provider.mixIn(objectClass, mixInSource = ApacheLang3Support.TripleMixIn.class);
                        break;
                    default:
                        break;
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
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonTypeInfo":
                        if (useJacksonAnnotation) {
                            processJacksonJsonTypeInfo(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.databind.annotation.JsonDeserialize":
                    case "com.alibaba.fastjson2.adapter.jackson.databind.annotation.JsonDeserialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonDeserializer(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonTypeName":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonTypeName":
                        if (useJacksonAnnotation) {
                            processJacksonJsonTypeName(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonFormat":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonFormat":
                        if (useJacksonAnnotation) {
                            processJacksonJsonFormat(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonSubTypes":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonSubTypes":
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

            BeanUtils.staticMethod(objectClass,
                    method -> getCreator(beanInfo, objectClass, method)
            );

            BeanUtils.constructor(objectClass, constructor ->
                    getCreator(beanInfo, objectClass, constructor)
            );

            if (beanInfo.creatorConstructor == null
                    && (beanInfo.readerFeatures & JSONReader.Feature.FieldBased.mask) == 0
                    && beanInfo.kotlin) {
                BeanUtils.getKotlinConstructor(objectClass, beanInfo);
                beanInfo.createParameterNames = BeanUtils.getKotlinConstructorParameters(objectClass);
            }
        }

        private void processJacksonJsonSubTypes(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "value": {
                            Object[] value = (Object[]) result;
                            if (value.length != 0) {
                                beanInfo.seeAlso = new Class[value.length];
                                beanInfo.seeAlsoNames = new String[value.length];
                                for (int i = 0; i < value.length; i++) {
                                    Annotation subTypeAnn = (Annotation) value[i];
                                    processJacksonJsonSubTypesType(beanInfo, i, subTypeAnn);
                                }
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

        private void processJacksonJsonDeserializer(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "using": {
                            Class using = processUsing((Class) result);
                            if (using != null) {
                                beanInfo.deserializer = using;
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

        private void processJacksonJsonTypeInfo(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "property": {
                            String value = (String) result;
                            if (!value.isEmpty()) {
                                beanInfo.typeKey = value;
                                beanInfo.readerFeatures |= JSONReader.Feature.SupportAutoType.mask;
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

        private void getBeanInfo(BeanInfo beanInfo, Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                JSONType jsonType = AnnotationUtils.findAnnotation(annotation, JSONType.class);
                if (jsonType != null) {
                    getBeanInfo1x(beanInfo, annotation);
                    if (jsonType == annotation) {
                        continue;
                    }
                }

                if (annotationType == JSONAutowired.class) {
                    beanInfo.readerFeatures |= FieldInfo.JSON_AUTO_WIRED_ANNOTATED;
                    JSONAutowired autowired = (JSONAutowired) annotation;
                    String fieldName = autowired.reader();
                    if (!fieldName.isEmpty()) {
                        beanInfo.objectReaderFieldName = fieldName;
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

                                    BeanInfo itemBeanInfo = new BeanInfo();
                                    getBeanInfo(itemBeanInfo, item);
                                    String typeName = itemBeanInfo.typeName;
                                    if (typeName == null || typeName.isEmpty()) {
                                        typeName = item.getSimpleName();
                                    }
                                    beanInfo.seeAlsoNames[i] = typeName;
                                }
                                beanInfo.readerFeatures |= JSONReader.Feature.SupportAutoType.mask;
                            }
                            break;
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
                                    case "SupportAutoType":
                                        beanInfo.readerFeatures |= JSONReader.Feature.SupportAutoType.mask;
                                        break;
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
                            Class<?> builderClass = (Class) result;
                            if (builderClass != void.class && builderClass != Void.class) {
                                beanInfo.builder = builderClass;

                                for (Annotation builderAnnotation : getAnnotations(builderClass)) {
                                    Class<? extends Annotation> builderAnnotationClass = builderAnnotation.annotationType();
                                    String builderAnnotationName = builderAnnotationClass.getName();

                                    switch (builderAnnotationName) {
                                        case "com.alibaba.fastjson.annotation.JSONPOJOBuilder":
                                            getBeanInfo1xJSONPOJOBuilder(beanInfo, builderClass, builderAnnotation, builderAnnotationClass);
                                            break;
                                        default:
                                            JSONBuilder jsonBuilder = AnnotationUtils.findAnnotation(builderClass, JSONBuilder.class);
                                            if (jsonBuilder != null) {
                                                String buildMethodName = jsonBuilder.buildMethod();
                                                beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, buildMethodName);
                                                String withPrefix = jsonBuilder.withPrefix();
                                                if (!withPrefix.isEmpty()) {
                                                    beanInfo.builderWithPrefix = withPrefix;
                                                }
                                            }
                                            break;
                                    }
                                }

                                if (beanInfo.buildMethod == null) {
                                    beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, "build");
                                }

                                if (beanInfo.buildMethod == null) {
                                    beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, "create");
                                }
                            }
                            break;
                        }
                        case "deserializeUsing": {
                            Class<?> deserializeUsing = (Class) result;
                            if (ObjectReader.class.isAssignableFrom(deserializeUsing)) {
                                beanInfo.deserializer = deserializeUsing;
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

        @Override
        public void getFieldInfo(
                FieldInfo fieldInfo,
                Class objectClass,
                Constructor constructor,
                int paramIndex,
                Parameter parameter
        ) {
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

            Annotation[] annotations = null;
            try {
                annotations = getAnnotations(parameter);
            } catch (ArrayIndexOutOfBoundsException ignored) {
                // ignored
            }

            if (annotations != null) {
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

            processAnnotation(fieldInfo, getAnnotations(parameter));
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
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

            Annotation[] annotations = getAnnotations(field);
            processAnnotation(fieldInfo, annotations);
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method) {
            Class mixInSource = provider.mixInCache.get(objectClass);
            String methodName = method.getName();
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

            String jsonFieldName = null;

            Annotation[] annotations = getAnnotations(method);
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                JSONField jsonField = AnnotationUtils.findAnnotation(annotation, JSONField.class);
                if (jsonField != null) {
                    getFieldInfo(fieldInfo, jsonField);
                    jsonFieldName = jsonField.name();
                    if (jsonField == annotation) {
                        continue;
                    }
                }

                boolean useJacksonAnnotation = JSONFactory.isUseJacksonAnnotation();
                String annotationTypeName = annotationType.getName();
                switch (annotationTypeName) {
                    case "com.fasterxml.jackson.annotation.JsonIgnore":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonIgnore":
                        if (useJacksonAnnotation) {
                            processJacksonJsonIgnore(fieldInfo, annotation);
                        }
                        break;
                    case "com.alibaba.fastjson2.adapter.jackson.databind.annotation.JsonDeserialize":
                    case "com.fasterxml.jackson.databind.annotation.JsonDeserialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonDeserialize(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonFormat":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonFormat":
                        if (useJacksonAnnotation) {
                            processJacksonJsonFormat(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAnySetter":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonAnySetter":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
                        }
                        break;
                    case "com.alibaba.fastjson.annotation.JSONField":
                        processJSONField1x(fieldInfo, annotation);
                        break;
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonProperty":
                    case "com.fasterxml.jackson.annotation.JsonProperty":
                        if (useJacksonAnnotation) {
                            processJacksonJsonProperty(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAlias":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonAlias":
                        if (useJacksonAnnotation) {
                            processJacksonJsonAlias(fieldInfo, annotation);
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
                if (field.getName().equals(fieldName)) {
                    int modifiers = field.getModifiers();
                    if ((!Modifier.isPublic(modifiers)) && !Modifier.isStatic(modifiers)) {
                        getFieldInfo(fieldInfo, objectClass, field);
                    }
                    fieldInfo.features |= FieldInfo.FIELD_MASK;
                } else if (fieldName1 != null && field.getName().equals(fieldName1)) {
                    int modifiers = field.getModifiers();
                    if ((!Modifier.isPublic(modifiers)) && !Modifier.isStatic(modifiers)) {
                        getFieldInfo(fieldInfo, objectClass, field);
                    }
                    fieldInfo.features |= FieldInfo.FIELD_MASK;
                } else if (fieldName2 != null && field.getName().equals(fieldName2)) {
                    int modifiers = field.getModifiers();
                    if ((!Modifier.isPublic(modifiers)) && !Modifier.isStatic(modifiers)) {
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
                JSONField jsonField = AnnotationUtils.findAnnotation(annotation, JSONField.class);
                if (jsonField != null) {
                    getFieldInfo(fieldInfo, jsonField);
                    if (jsonField == annotation) {
                        continue;
                    }
                }

                boolean useJacksonAnnotation = JSONFactory.isUseJacksonAnnotation();
                String annotationTypeName = annotationType.getName();
                switch (annotationTypeName) {
                    case "com.fasterxml.jackson.annotation.JsonIgnore":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonIgnore":
                        if (useJacksonAnnotation) {
                            processJacksonJsonIgnore(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAnyGetter":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonAnyGetter":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
                        }
                        break;
                    case "com.alibaba.fastjson.annotation.JSONField":
                        processJSONField1x(fieldInfo, annotation);
                        break;
                    case "com.fasterxml.jackson.annotation.JsonProperty":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonProperty":
                        if (useJacksonAnnotation) {
                            processJacksonJsonProperty(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonFormat":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonFormat":
                        if (useJacksonAnnotation) {
                            processJacksonJsonFormat(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.databind.annotation.JsonDeserialize":
                    case "com.alibaba.fastjson2.adapter.jackson.databind.annotation.JsonDeserialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonDeserialize(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAlias":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonAlias":
                        if (useJacksonAnnotation) {
                            processJacksonJsonAlias(fieldInfo, annotation);
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
            String noneClassName1 = "com.alibaba.fastjson2.adapter.jackson.databind.JsonDeserializer$None";
            if (!noneClassName0.equals(usingName)
                    && !noneClassName1.equals(usingName)
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
                            if (!value.isEmpty()) {
                                fieldInfo.fieldName = value;
                            }
                            break;
                        }
                        case "access": {
                            String access = ((Enum) result).name();
                            switch (access) {
                                case "READ_ONLY":
                                    fieldInfo.ignore = true;
                                    break;
                                default:
                                    fieldInfo.ignore = false;
                                    break;
                            }
                            break;
                        }
                        case "required":
                            boolean required = (Boolean) result;
                            if (required) {
                                fieldInfo.required = required;
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

        private void processJacksonJsonAlias(FieldInfo fieldInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "value": {
                            String[] values = (String[]) result;
                            if (values.length != 0) {
                                fieldInfo.alternateNames = values;
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
                                    format = format.replaceAll("T", "'T'");
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
                                    for (String alternateName : alternateNames) {
                                        nameSet.add(alternateName);
                                    }
                                    for (String alternateName : fieldInfo.alternateNames) {
                                        nameSet.add(alternateName);
                                    }
                                    fieldInfo.alternateNames = nameSet.toArray(new String[nameSet.size()]);
                                }
                            }
                            break;
                        }
                        case "ordinal": {
                            Integer ordinal = (Integer) result;
                            if (ordinal.intValue() != 0) {
                                fieldInfo.ordinal = ordinal;
                            }
                            break;
                        }
                        case "deserialize": {
                            Boolean serialize = (Boolean) result;
                            if (!serialize.booleanValue()) {
                                fieldInfo.ignore = true;
                            }
                            break;
                        }
                        case "parseFeatures": {
                            Enum[] features = (Enum[]) result;
                            for (Enum feature : features) {
                                switch (feature.name()) {
                                    case "SupportAutoType":
                                        fieldInfo.features |= JSONReader.Feature.SupportAutoType.mask;
                                        break;
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
                    jsonFieldFormat = jsonFieldFormat.replaceAll("T", "'T'");
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
                    Set<String> nameSet = new LinkedHashSet<>();
                    for (String alternateName : alternateNames) {
                        nameSet.add(alternateName);
                    }
                    for (String alternateName : fieldInfo.alternateNames) {
                        nameSet.add(alternateName);
                    }
                    fieldInfo.alternateNames = nameSet.toArray(new String[nameSet.size()]);
                }
            }

            if (!fieldInfo.ignore) {
                fieldInfo.ignore = !jsonField.deserialize();
            }

            for (JSONReader.Feature feature : jsonField.deserializeFeatures()) {
                fieldInfo.features |= feature.mask;
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
        }
    }

    private void getBeanInfo1xJSONPOJOBuilder(BeanInfo beanInfo,
                                              Class<?> builderClass,
                                              Annotation builderAnnatation,
                                              Class<? extends Annotation> builderAnnatationClass) {
        BeanUtils.annotationMethods(builderAnnatationClass, method -> {
            try {
                String methodName = method.getName();
                switch (methodName) {
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
        Annotation[] annotations = getAnnotations(constructor);

        boolean creatorMethod = false;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();

            JSONCreator jsonCreator = AnnotationUtils.findAnnotation(annotation, JSONCreator.class);
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
                    creatorMethod = true;
                    BeanUtils.annotationMethods(annotationType, m1 -> {
                        try {
                            switch (m1.getName()) {
                                case "parameterNames":
                                    String[] createParameterNames = (String[]) m1.invoke(annotation);
                                    if (createParameterNames.length != 0) {
                                        beanInfo.createParameterNames = createParameterNames;
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } catch (Throwable ignored) {
                        }
                    });
                    break;
                case "com.fasterxml.jackson.annotation.JsonCreator":
                case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonCreator":
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
        Annotation[] annotations = getAnnotations(method);

        boolean creatorMethod = false;
        JSONCreator jsonCreator = null;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            jsonCreator = AnnotationUtils.findAnnotation(annotation, JSONCreator.class);
            if (jsonCreator == annotation) {
                continue;
            }

            switch (annotationType.getName()) {
                case "com.alibaba.fastjson.annotation.JSONCreator":
                    creatorMethod = true;
                    BeanUtils.annotationMethods(annotationType, m1 -> {
                        try {
                            switch (m1.getName()) {
                                case "parameterNames":
                                    String[] createParameterNames = (String[]) m1.invoke(annotation);
                                    if (createParameterNames.length != 0) {
                                        beanInfo.createParameterNames = createParameterNames;
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } catch (Throwable ignored) {
                        }
                    });
                    break;
                case "com.fasterxml.jackson.annotation.JsonCreator":
                case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonCreator":
                    if (JSONFactory.isUseJacksonAnnotation()) {
                        creatorMethod = true;
                        BeanUtils.annotationMethods(annotationType, m1 -> {
                            try {
                                switch (m1.getName()) {
                                    case "parameterNames":
                                        String[] createParameterNames = (String[]) m1.invoke(annotation);
                                        if (createParameterNames.length != 0) {
                                            beanInfo.createParameterNames = createParameterNames;
                                        }
                                        break;
                                    default:
                                        break;
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
            targetMethod = objectClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
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

    @Override
    public ObjectReader getObjectReader(ObjectReaderProvider provider, Type type) {
        if (type == String.class || type == CharSequence.class) {
            return ObjectReaderImplString.INSTANCE;
        }

        if (type == char.class || type == Character.class) {
            return ObjectReaderImplCharacter.INSTANCE;
        }

        if (type == boolean.class || type == Boolean.class) {
            return ObjectReaderImplBoolean.INSTANCE;
        }

        if (type == byte.class || type == Byte.class) {
            return ObjectReaderImplByte.INSTANCE;
        }

        if (type == short.class || type == Short.class) {
            return ObjectReaderImplShort.INSTANCE;
        }

        if (type == int.class || type == Integer.class) {
            return ObjectReaderImplInteger.INSTANCE;
        }

        if (type == long.class || type == Long.class) {
            return ObjectReaderImplInt64.INSTANCE;
        }

        if (type == float.class || type == Float.class) {
            return ObjectReaderImplFloat.INSTANCE;
        }

        if (type == double.class || type == Double.class) {
            return ObjectReaderImplDouble.INSTANCE;
        }

        if (type == BigInteger.class) {
            return ObjectReaderImplBigInteger.INSTANCE;
        }

        if (type == BigDecimal.class) {
            return ObjectReaderImplBigDecimal.INSTANCE;
        }

        if (type == Number.class) {
            return ObjectReaderImplNumber.INSTANCE;
        }

        if (type == BitSet.class) {
            return ObjectReaderImplBitSet.INSTANCE;
        }

        if (type == OptionalInt.class) {
            return ObjectReaderImplOptionalInt.INSTANCE;
        }

        if (type == OptionalLong.class) {
            return ObjectReaderImplOptionalLong.INSTANCE;
        }

        if (type == OptionalDouble.class) {
            return ObjectReaderImplOptionalDouble.INSTANCE;
        }

        if (type == Optional.class) {
            return ObjectReaderImplOptional.INSTANCE;
        }

        if (type == UUID.class) {
            return ObjectReaderImplUUID.INSTANCE;
        }

        if (type == URI.class) {
            return new ObjectReaderImplFromString<URI>(URI.class, e -> URI.create(e));
        }

        if (type == Charset.class) {
            return new ObjectReaderImplFromString<Charset>(Charset.class, e -> Charset.forName(e));
        }

        if (type == File.class) {
            return new ObjectReaderImplFromString<File>(File.class, e -> new File(e));
        }

        if (type == URL.class) {
            return new ObjectReaderImplFromString<URL>(
                    URL.class,
                    e -> {
                        try {
                            return new URL(e);
                        } catch (MalformedURLException ex) {
                            throw new JSONException("read URL error", ex);
                        }
                    });
        }

        if (type == Pattern.class) {
            return new ObjectReaderImplFromString<Pattern>(Pattern.class, e -> Pattern.compile(e));
        }

        if (type == Class.class) {
            return ObjectReaderImplClass.INSTANCE;
        }

        if (type == Method.class) {
            return new ObjectReaderImplMethod();
        }

        if (type == Field.class) {
            return new ObjectReaderImplField();
        }

        if (type == Type.class) {
            return ObjectReaderImplClass.INSTANCE;
        }

        String internalMixin = null;
        String typeName = type.getTypeName();
        switch (typeName) {
            case "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList":
            case "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet":
                return null;
            case "org.springframework.util.LinkedMultiValueMap":
                return ObjectReaderImplMap.of(type, (Class) type, 0L);
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
            default:
                break;
        }

        if (internalMixin != null) {
            Class mixin = provider.mixInCache.get(type);
            if (mixin == null) {
                mixin = TypeUtils.loadClass(internalMixin);
                if (mixin == null) {
                    switch (internalMixin) {
                        case "org.springframework.security.jackson2.SimpleGrantedAuthorityMixin":
                            mixin = TypeUtils.loadClass("com.alibaba.fastjson2.internal.mixin.spring.SimpleGrantedAuthorityMixin");
                            break;
                        default:
                            break;
                    }
                }

                if (mixin != null) {
                    provider.mixInCache.putIfAbsent((Class) type, mixin);
                }
            }
        }

        if (type == Map.class || type == AbstractMap.class) {
            return ObjectReaderImplMap.of(null, (Class) type, 0);
        }

        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return typedMap((Class) type, ConcurrentHashMap.class, null, Object.class);
        }

        if (type == ConcurrentNavigableMap.class
                || type == ConcurrentSkipListMap.class
        ) {
            return typedMap((Class) type, ConcurrentSkipListMap.class, null, Object.class);
        }

        if (type == SortedMap.class
                || type == NavigableMap.class
                || type == TreeMap.class
        ) {
            return typedMap((Class) type, TreeMap.class, null, Object.class);
        }

        if (type == Calendar.class || "javax.xml.datatype.XMLGregorianCalendar".equals(typeName)) {
            return ObjectReaderImplCalendar.INSTANCE;
        }

        if (type == Date.class) {
            return ObjectReaderImplDate.INSTANCE;
        }

        if (type == LocalDate.class) {
            return ObjectReaderImplLocalDate.INSTANCE;
        }

        if (type == LocalTime.class) {
            return ObjectReaderImplLocalTime.INSTANCE;
        }

        if (type == LocalDateTime.class) {
            return ObjectReaderImplLocalDateTime.INSTANCE;
        }

        if (type == ZonedDateTime.class) {
            return ObjectReaderImplZonedDateTime.INSTANCE;
        }

        if (type == Instant.class) {
            return ObjectReaderImplInstant.INSTANCE;
        }

        if (type == Locale.class) {
            return ObjectReaderImplLocale.INSTANCE;
        }

        if (type == Currency.class) {
            return ObjectReaderImplCurrency.INSTANCE;
        }

        if (type == ZoneId.class) {
//            return ZoneIdImpl.INSTANCE;
            // ZoneId.of(strVal)
            return new ObjectReaderImplFromString<ZoneId>(ZoneId.class, e -> ZoneId.of(e));
        }

        if (type == TimeZone.class) {
            return new ObjectReaderImplFromString<TimeZone>(TimeZone.class, e -> TimeZone.getTimeZone(e));
        }

        if (type == char[].class) {
            return ObjectReaderImplCharValueArray.INSTANCE;
        }

        if (type == float[].class) {
            return ObjectReaderImplFloatValueArray.INSTANCE;
        }

        if (type == double[].class) {
            return ObjectReaderImplDoubleValueArray.INSTANCE;
        }

        if (type == boolean[].class) {
            return ObjectReaderImplBoolValueArray.INSTANCE;
        }

        if (type == byte[].class) {
            return ObjectReaderImplInt8ValueArray.INSTANCE;
        }

        if (type == short[].class) {
            return ObjectReaderImplInt16ValueArray.INSTANCE;
        }

        if (type == int[].class) {
            return ObjectReaderImplInt32ValueArray.INSTANCE;
        }

        if (type == long[].class) {
            return ObjectReaderImplInt64ValueArray.INSTANCE;
        }

        if (type == Byte[].class) {
            return ObjectReaderImplInt8Array.INSTANCE;
        }

        if (type == Short[].class) {
            return ObjectReaderImplInt16Array.INSTANCE;
        }

        if (type == Integer[].class) {
            return ObjectReaderImplInt32Array.INSTANCE;
        }

        if (type == Long[].class) {
            return ObjectReaderImplInt64Array.INSTANCE;
        }

        if (type == Float[].class) {
            return ObjectReaderImplFloatArray.INSTANCE;
        }

        if (type == Double[].class) {
            return ObjectReaderImplDoubleArray.INSTANCE;
        }

        if (type == Number[].class) {
            return ObjectReaderImplNumberArray.INSTANCE;
        }

        if (type == AtomicInteger.class) {
            return ObjectReaderImplAtomicInteger.INSTANCE;
        }

        if (type == AtomicLong.class) {
            return ObjectReaderImplAtomicLong.INSTANCE;
        }

        if (type == AtomicIntegerArray.class) {
            return ObjectReaderImplAtomicIntegerArray.INSTANCE;
        }

        if (type == AtomicLongArray.class) {
            return ObjectReaderImplAtomicLongArray.INSTANCE;
        }

        if (type == AtomicReference.class) {
            return ObjectReaderImplAtomicReference.INSTANCE;
        }

        if (type instanceof MultiType) {
            return new ObjectArrayReaderMultiType((MultiType) type);
        }

        if (type == StringBuffer.class || type == StringBuilder.class) {
            try {
                Class objectClass = (Class) type;
                return new ObjectReaderImplValue(
                        objectClass,
                        String.class,
                        String.class,
                        0,
                        null,
                        null,
                        null,
                        objectClass.getConstructor(String.class),
                        null,
                        null
                );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == Iterable.class
                || type == Collection.class
                || type == List.class
                || type == AbstractCollection.class
                || type == AbstractList.class
                || type == ArrayList.class
        ) {
            return ObjectReaderImplList.of(type, null, 0);
            // return new ObjectReaderImplList(type, (Class) type, ArrayList.class, Object.class, null);
        }

        if (type == Queue.class
                || type == Deque.class
                || type == AbstractSequentialList.class
                || type == LinkedList.class) {
//            return new ObjectReaderImplList(type, (Class) type, LinkedList.class, Object.class, null);
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == Set.class || type == AbstractSet.class || type == EnumSet.class) {
//            return new ObjectReaderImplList(type, (Class) type, HashSet.class, Object.class, null);
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == NavigableSet.class || type == SortedSet.class) {
//            return new ObjectReaderImplList(type, (Class) type, TreeSet.class, Object.class, null);
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == ConcurrentLinkedDeque.class
                || type == ConcurrentLinkedQueue.class
                || type == ConcurrentSkipListSet.class
                || type == LinkedHashSet.class
                || type == HashSet.class
                || type == TreeSet.class
                || type == CopyOnWriteArrayList.class
        ) {
//            return new ObjectReaderImplList(type, (Class) type, (Class) type, Object.class, null);
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == ObjectReaderImplList.CLASS_EMPTY_SET
                || type == ObjectReaderImplList.CLASS_EMPTY_LIST
                || type == ObjectReaderImplList.CLASS_SINGLETON
                || type == ObjectReaderImplList.CLASS_SINGLETON_LIST
                || type == ObjectReaderImplList.CLASS_ARRAYS_LIST
                || type == ObjectReaderImplList.CLASS_UNMODIFIABLE_COLLECTION
                || type == ObjectReaderImplList.CLASS_UNMODIFIABLE_LIST
                || type == ObjectReaderImplList.CLASS_UNMODIFIABLE_SET
                || type == ObjectReaderImplList.CLASS_UNMODIFIABLE_SORTED_SET
                || type == ObjectReaderImplList.CLASS_UNMODIFIABLE_NAVIGABLE_SET
        ) {
//            return new ObjectReaderImplList(type, (Class) type, (Class) type, Object.class, null);
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == TypeUtils.CLASS_SINGLE_SET) {
//            return SingletonSetImpl.INSTANCE;
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == Object.class
                || type == Cloneable.class
                || type == Closeable.class
                || type == Serializable.class
                || type == Comparable.class
        ) {
            return ObjectReaderImplObject.INSTANCE;
        }

        if (type == Map.Entry.class) {
            return new ObjectReaderImplMapEntry(null, null);
        }

        if (type instanceof Class) {
            Class objectClass = (Class) type;
            if (Map.class.isAssignableFrom(objectClass)) {
                return ObjectReaderImplMap.of(null, objectClass, 0);
            }

            if (List.class.isAssignableFrom(objectClass)) {
                return ObjectReaderImplList.of(objectClass, objectClass, 0);
            }

            if (objectClass.isArray()) {
                return new ObjectArrayTypedReader(objectClass);
            }

            ObjectReaderCreator creator = JSONFactory
                    .getDefaultObjectReaderProvider()
                    .getCreator();

            if (objectClass == StackTraceElement.class) {
                try {
                    Constructor constructor = objectClass.getConstructor(
                            String.class,
                            String.class,
                            String.class,
                            int.class);

                    return creator
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

            if (objectClass.isInterface()) {
                BeanInfo beanInfo = new BeanInfo();
                annotationProcessor.getBeanInfo(beanInfo, objectClass);
                if (beanInfo.seeAlso != null && beanInfo.seeAlso.length == 0) {
                    return new ObjectReaderInterfaceImpl(type);
                }
            }
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();

            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length == 2) {
                Type actualTypeParam0 = actualTypeArguments[0];
                Type actualTypeParam1 = actualTypeArguments[1];

                if (rawType == Map.class
                        || rawType == AbstractMap.class
                        || rawType == HashMap.class
                ) {
                    return typedMap((Class) rawType, HashMap.class, actualTypeParam0, actualTypeParam1);
                }

                if (rawType == ConcurrentMap.class
                        || rawType == ConcurrentHashMap.class
                ) {
                    return typedMap((Class) rawType, ConcurrentHashMap.class, actualTypeParam0, actualTypeParam1);
                }

                if (rawType == ConcurrentNavigableMap.class
                        || rawType == ConcurrentSkipListMap.class
                ) {
                    return typedMap((Class) rawType, ConcurrentSkipListMap.class, actualTypeParam0, actualTypeParam1);
                }

                if (rawType == LinkedHashMap.class || rawType == TreeMap.class) {
                    return typedMap((Class) rawType, (Class) rawType, actualTypeParam0, actualTypeParam1);
                }

                if (rawType == Map.Entry.class) {
                    return new ObjectReaderImplMapEntry(actualTypeArguments[0], actualTypeArguments[1]);
                }

                switch (rawType.getTypeName()) {
                    case "com.google.common.collect.ImmutableMap":
                    case "com.google.common.collect.RegularImmutableMap":
                        return new ObjectReaderImplMapTyped((Class) rawType, HashMap.class, actualTypeParam0, actualTypeParam1, 0, GuavaSupport.immutableMapConverter());
                    case "com.google.common.collect.SingletonImmutableBiMap":
                        return new ObjectReaderImplMapTyped((Class) rawType, HashMap.class, actualTypeParam0, actualTypeParam1, 0, GuavaSupport.singletonBiMapConverter());
                    case "org.springframework.util.LinkedMultiValueMap":
                        return ObjectReaderImplMap.of(type, (Class) rawType, 0L);
                    case "org.apache.commons.lang3.tuple.Pair":
                    case "org.apache.commons.lang3.tuple.ImmutablePair":
                        return new ApacheLang3Support.PairReader((Class) rawType, actualTypeParam0, actualTypeParam1);
                    default:
                        break;
                }
            }

            if (actualTypeArguments.length == 1) {
                Type itemType = actualTypeArguments[0];
                Class itemClass = TypeUtils.getMapping(itemType);

                if (rawType == Iterable.class
                        || rawType == Collection.class
                        || rawType == List.class
                        || rawType == AbstractCollection.class
                        || rawType == AbstractList.class
                        || rawType == ArrayList.class) {
                    if (itemClass == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, ArrayList.class);
                    } else if (itemClass == Long.class) {
                        return new ObjectReaderImplListInt64((Class) rawType, ArrayList.class);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == Queue.class
                        || rawType == Deque.class
                        || rawType == AbstractSequentialList.class
                        || rawType == LinkedList.class) {
                    if (itemClass == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, LinkedList.class);
                    } else if (itemClass == Long.class) {
                        return new ObjectReaderImplListInt64((Class) rawType, LinkedList.class);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == Set.class || rawType == AbstractSet.class || rawType == EnumSet.class) {
                    if (itemClass == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, HashSet.class);
                    } else if (itemClass == Long.class) {
                        return new ObjectReaderImplListInt64((Class) rawType, HashSet.class);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == NavigableSet.class || rawType == SortedSet.class) {
                    if (itemType == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, TreeSet.class);
                    } else if (itemClass == Long.class) {
                        return new ObjectReaderImplListInt64((Class) rawType, TreeSet.class);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == ConcurrentLinkedDeque.class
                        || rawType == ConcurrentLinkedQueue.class
                        || rawType == ConcurrentSkipListSet.class
                        || rawType == LinkedHashSet.class
                        || rawType == HashSet.class
                        || rawType == TreeSet.class
                        || rawType == CopyOnWriteArrayList.class
                ) {
                    if (itemType == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, (Class) rawType);
                    } else if (itemClass == Long.class) {
                        return new ObjectReaderImplListInt64((Class) rawType, (Class) rawType);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                switch (rawType.getTypeName()) {
                    case "com.google.common.collect.ImmutableList":
                        return ObjectReaderImplList.of(type, null, 0);
                    case "com.google.common.collect.ImmutableSet":
                        return ObjectReaderImplList.of(type, null, 0);
                    default:
                        break;
                }

                if (rawType == Optional.class) {
                    return ObjectReaderImplOptional.of(type, null, null);
                }

                if (rawType == AtomicReference.class) {
                    return new ObjectReaderImplAtomicReference(itemType);
                }

                if (itemType instanceof WildcardType) {
                    return getObjectReader(provider, rawType);
                }
            }

            return null;
        }

        if (type instanceof GenericArrayType) {
            return new ObjectReaderImplGenericArray((GenericArrayType) type);
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
            case "org.joda.time.Chronology":
                return JodaSupport.createChronologyReader((Class) type);
            case "org.joda.time.LocalDate":
                return JodaSupport.createLocalDateReader((Class) type);
            case "org.joda.time.LocalDateTime":
                return JodaSupport.createLocalDateTimeReader((Class) type);
            case "org.joda.time.Instant":
                return JodaSupport.createInstantReader((Class) type);
            case "javax.money.CurrencyUnit":
                return MoneySupport.createCurrencyUnitReader();
            case "javax.money.MonetaryAmount":
            case "javax.money.Money":
                return MoneySupport.createMonetaryAmountReader();
            case "javax.money.NumberValue":
                return MoneySupport.createNumberValueReader();
            case "java.net.InetSocketAddress":
                return new ObjectReaderMisc((Class) type);
            case "java.net.InetAddress":
                return ObjectReaderImplValue.of((Class<InetAddress>) type, String.class, address -> {
                    try {
                        return InetAddress.getByName(address);
                    } catch (UnknownHostException e) {
                        throw new JSONException("create address error", e);
                    }
                });
            case "java.text.SimpleDateFormat":
                return ObjectReaderImplValue.of((Class<SimpleDateFormat>) type, String.class, SimpleDateFormat::new);
            case "java.lang.Throwable":
            case "java.lang.Exception":
            case "java.lang.IllegalStateException":
            case "java.lang.RuntimeException":
            case "java.io.IOException":
            case "java.io.UncheckedIOException":
                if (!UNSAFE_SUPPORT) {
                    return new ObjectReaderException((Class) type);
                }
                break;
            case "org.apache.commons.lang3.tuple.Pair":
            case "org.apache.commons.lang3.tuple.ImmutablePair":
                return new ApacheLang3Support.PairReader((Class) type, Object.class, Object.class);
            default:
                break;
        }

        return null;
    }

    public static ObjectReader typedMap(Class mapType, Class instanceType, Type keyType, Type valueType) {
        if ((keyType == null || keyType == String.class) && valueType == String.class) {
            return new ObjectReaderImplMapString(mapType, instanceType, 0);
        }
        return new ObjectReaderImplMapTyped(mapType, instanceType, keyType, valueType, 0, null);
    }
}
