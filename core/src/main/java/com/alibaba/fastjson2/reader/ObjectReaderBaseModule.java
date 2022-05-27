package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONBuilder;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.DateTimeCodec;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.support.money.MoneySupport;
import com.alibaba.fastjson2.util.*;

import java.io.Closeable;
import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.Function;

import static com.alibaba.fastjson2.reader.TypeConverts.*;

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

    class ReaderAnnotationProcessor
            implements ObjectReaderAnnotationProcessor {
        @Override
        public void getBeanInfo(BeanInfo beanInfo, Class<?> objectClass) {
            Class mixInSource = provider.mixInCache.get(objectClass);
            if (mixInSource == null) {
                String typeName = objectClass.getName();
                switch (typeName) {
                    case "org.apache.commons.lang3.tuple.Pair":
                    case "org.apache.commons.lang3.tuple.ImmutablePair":
                        provider.mixIn(objectClass, mixInSource = ApacheLang3Support.PairMixIn.class);
                        break;
                    case "org.apache.commons.lang3.tuple.Triple":
                        provider.mixIn(objectClass, mixInSource = ApacheLang3Support.TripleMixIn.class);
                        break;
                    default:
                        break;
                }
            }

            if (mixInSource != null && mixInSource != objectClass) {
                getBeanInfo(beanInfo, mixInSource.getAnnotations());

                BeanUtils.staticMethod(mixInSource,
                        method -> getCreator(beanInfo, objectClass, method)
                );

                BeanUtils.constructor(mixInSource, constructor ->
                        getCreator(beanInfo, objectClass, constructor)
                );
            }

            Annotation[] annotations = objectClass.getAnnotations();
            getBeanInfo(beanInfo, annotations);

            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                switch (annotationType.getName()) {
                    case "com.alibaba.fastjson.annotation.JSONType":
                        getBeanInfo1x(beanInfo, annotation);
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

            if (beanInfo.creatorConstructor == null && beanInfo.kotlin) {
                beanInfo.creatorConstructor = BeanUtils.getKotlinConstructor(objectClass, beanInfo.createParameterNames);
            }
        }

        private void getBeanInfo(BeanInfo beanInfo, Annotation[] annotations) {
            JSONType jsonType = null;
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType == JSONType.class) {
                    jsonType = (JSONType) annotation;
                }
                String annotationTypeName = annotationType.getName();
                switch (annotationTypeName) {
                    case "com.alibaba.fastjson2.annotation.JSONType":
                        getBeanInfo1x(beanInfo, annotation);
                        break;
                    default:
                        break;
                }
            }

            getBeanInfo(beanInfo, jsonType);
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
                            beanInfo.ignores = (String[]) result;
                            break;
                        }
                        case "orders": {
                            String[] fields = (String[]) result;
                            if (fields.length != 0) {
                                beanInfo.orders = fields;
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
                        case "builder": {
                            Class<?> builderClass = (Class) result;
                            if (builderClass != void.class && builderClass != Void.class) {
                                beanInfo.builder = builderClass;

                                for (Annotation builderAnnation : builderClass.getAnnotations()) {
                                    Class<? extends Annotation> builderAnnationClass = builderAnnation.annotationType();
                                    String builderAnnationName = builderAnnationClass.getName();

                                    switch (builderAnnationName) {
                                        case "com.alibaba.fastjson.annotation.JSONPOJOBuilder":
                                            getBeanInfo1xJSONPOJOBuilder(beanInfo, builderClass, builderAnnation, builderAnnationClass);
                                            break;
                                        default:
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
                        default:
                            break;
                    }
                } catch (Throwable ignored) {
                }
            });
        }

        void getBeanInfo(BeanInfo beanInfo, JSONType jsonType) {
            if (jsonType == null) {
                return;
            }

            Class<?>[] classes = jsonType.seeAlso();
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

            String jsonTypeKey = jsonType.typeKey();
            if (!jsonTypeKey.isEmpty()) {
                beanInfo.typeKey = jsonTypeKey;
            }

            String typeName = jsonType.typeName();
            if (!typeName.isEmpty()) {
                beanInfo.typeName = typeName;
            }

            beanInfo.namingStrategy =
                    jsonType.naming().name();

            for (JSONReader.Feature feature : jsonType.deserializeFeatures()) {
                beanInfo.readerFeatures |= feature.mask;
            }

            Class<?> builderClass = jsonType.builder();
            if (builderClass != void.class && builderClass != Void.class) {
                beanInfo.builder = builderClass;

                JSONBuilder jsonBuilder = builderClass.getAnnotation(JSONBuilder.class);
                if (jsonBuilder != null) {
                    String buildMethodName = jsonBuilder.buildMethod();
                    beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, buildMethodName);
                    String withPrefix = jsonBuilder.withPrefix();
                    if (!withPrefix.isEmpty()) {
                        beanInfo.builderWithPrefix = withPrefix;
                    }
                }
            }

            Class<?> deserializer = jsonType.deserializer();
            if (ObjectReader.class.isAssignableFrom(deserializer)) {
                beanInfo.deserializer = deserializer;
            }

            String[] ignores = jsonType.ignores();
            if (ignores.length > 0) {
                beanInfo.ignores = ignores;
            }

            String schema = jsonType.schema().trim();
            if (!schema.isEmpty()) {
                beanInfo.schema = schema;
            }
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Constructor constructor, int paramIndex, Parameter parameter) {
            Class mixInSource = provider.mixInCache.get(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                Constructor mixInConstructor = null;
                try {
                    mixInConstructor = mixInSource.getDeclaredConstructor(constructor.getParameterTypes());
                } catch (NoSuchMethodException ignored) {
                }
                if (mixInConstructor != null) {
                    Parameter mixInParam = mixInConstructor.getParameters()[paramIndex];
                    processAnnotation(fieldInfo, mixInParam.getAnnotations());
                }
            }

            Annotation[] annotations = null;
            try {
                annotations = parameter.getAnnotations();
            } catch (ArrayIndexOutOfBoundsException ignored) {
                // ignored
            }

            if (annotations != null) {
                processAnnotation(fieldInfo, annotations);
            }
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method, int paramIndex, Parameter parameter) {
            Class mixInSource = provider.mixInCache.get(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                Method mixInMethod = null;
                try {
                    mixInMethod = mixInSource.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException ignored) {
                }
                if (mixInMethod != null) {
                    Parameter mixInParam = mixInMethod.getParameters()[paramIndex];
                    processAnnotation(fieldInfo, mixInParam.getAnnotations());
                }
            }

            processAnnotation(fieldInfo, parameter.getAnnotations());
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

            processAnnotation(fieldInfo, field.getAnnotations());
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

            JSONField jsonField = null;
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType == JSONField.class) {
                    jsonField = (JSONField) annotation;
                }
                String annotationTypeName = annotationType.getName();
                switch (annotationTypeName) {
                    case "com.fasterxml.jackson.annotation.JsonIgnore":
                        fieldInfo.ignore = true;
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAnySetter":
                        fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
                        break;
                    case "com.alibaba.fastjson.annotation.JSONField":
                        processJSONField1x(fieldInfo, annotation);
                        break;
                    case "com.fasterxml.jackson.annotation.JsonProperty":
                        processJacksonJsonProperty(fieldInfo, annotation);
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAlias":
                        processJacksonJsonAlias(fieldInfo, annotation);
                        break;
                    case "com.taobao.api.internal.mapping.ApiField":
                        processTaobaoApiField(fieldInfo, annotation);
                        break;
                    default:
                        break;
                }
            }

            getFieldInfo(fieldInfo, jsonField);

            String fieldName;
            if (methodName.startsWith("set")) {
                fieldName = BeanUtils.setterName(methodName, null);
            } else {
                fieldName = BeanUtils.getterName(methodName, null); // readOnlyProperty
            }

            BeanUtils.declaredFields(objectClass, field -> {
                if (field.getName().equals(fieldName)) {
                    int modifiers = field.getModifiers();
                    if ((!Modifier.isPublic(modifiers)) && !Modifier.isStatic(modifiers)) {
                        getFieldInfo(fieldInfo, objectClass, field);
                    }
                }
            });
        }

        private void processAnnotation(FieldInfo fieldInfo, Annotation[] annotations) {
            JSONField jsonField = null;
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType == JSONField.class) {
                    jsonField = (JSONField) annotation;
                }
                String annotationTypeName = annotationType.getName();
                switch (annotationTypeName) {
                    case "com.fasterxml.jackson.annotation.JsonIgnore":
                        fieldInfo.ignore = true;
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAnyGetter":
                        fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
                        break;
                    case "com.alibaba.fastjson.annotation.JSONField":
                        processJSONField1x(fieldInfo, annotation);
                        break;
                    case "com.fasterxml.jackson.annotation.JsonProperty":
                        processJacksonJsonProperty(fieldInfo, annotation);
                        break;
                    case "com.fasterxml.jackson.annotation.JsonAlias":
                        processJacksonJsonAlias(fieldInfo, annotation);
                        break;
                    case "com.taobao.api.internal.mapping.ApiField":
                        processTaobaoApiField(fieldInfo, annotation);
                        break;
                    default:
                        break;
                }
            }

            getFieldInfo(fieldInfo, jsonField);
        }

        private void processJacksonJsonProperty(FieldInfo fieldInfo, Annotation annotation) {
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
                                case "WRITE_ONLY":
                                    fieldInfo.ignore = true;
                                    break;
                                default:
                                    break;
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

        private void processTaobaoApiField(FieldInfo fieldInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "value": {
                            String value = (String) result;
                            if (!value.isEmpty()) {
                                // fieldInfo.alternateNames = new String[]{value};
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

            if (jsonField.unwrapped()) {
                fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
            }

            String schema = jsonField.schema().trim();
            if (!schema.isEmpty()) {
                fieldInfo.schema = schema;
            }
        }
    }

    private void getBeanInfo1xJSONPOJOBuilder(BeanInfo beanInfo, Class<?> builderClass, Annotation builderAnnatation, Class<? extends Annotation> builderAnnatationClass) {
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
        Annotation[] annotations = constructor.getAnnotations();

        boolean creatorMethod = false;
        JSONCreator jsonCreator = null;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType == JSONCreator.class) {
                jsonCreator = (JSONCreator) annotation;
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
                    creatorMethod = true;
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
        Annotation[] annotations = method.getAnnotations();

        boolean creatorMethod = false;
        JSONCreator jsonCreator = null;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType == JSONCreator.class) {
                jsonCreator = (JSONCreator) annotation;
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
            return CharacterImpl.INSTANCE;
        }

        if (type == boolean.class || type == Boolean.class) {
            return BooleanImpl.INSTANCE;
        }

        if (type == byte.class || type == Byte.class) {
            return ObjectReaderImplByte.INSTANCE;
        }

        if (type == short.class || type == Short.class) {
            return ObjectReaderImplShort.INSTANCE;
        }

        if (type == int.class || type == Integer.class) {
            return IntegerImpl.INSTANCE;
        }

        if (type == long.class || type == Long.class) {
            return LongImpl.INSTANCE;
        }

        if (type == float.class || type == Float.class) {
            return FloatImpl.INSTANCE;
        }

        if (type == double.class || type == Double.class) {
            return DoubleImpl.INSTANCE;
        }

        if (type == BigInteger.class) {
            return ObjectReaderImplBigInteger.INSTANCE;
        }

        if (type == BigDecimal.class) {
            return ObjectReaderImplBigDecimal.INSTANCE;
        }

        if (type == Number.class) {
            return NumberImpl.INSTANCE;
        }

        if (type == BitSet.class) {
            return ObjectReaderImplBitSet.INSTANCE;
        }

        if (type == OptionalInt.class) {
            return ObjectReaderImplOptionalInt.INSTANCE;
        }

        if (type == OptionalLong.class) {
            return OptionalLongImpl.INSTANCE;
        }

        if (type == OptionalDouble.class) {
            return OptionalDoubleImpl.INSTANCE;
        }

        if (type == Optional.class) {
            return ObjectReaderImplOptional.INSTANCE;
        }

        if (type == UUID.class) {
            return UUIDImpl.INSTANCE;
        }

        if (type == URI.class) {
            return new ObjectReaderImplFromString<URI>(e -> URI.create(e));
        }

        if (type == File.class) {
            return new ObjectReaderImplFromString<File>(e -> new File(e));
        }

        if (type == URL.class) {
            return new ObjectReaderImplFromString<URL>(e -> {
                try {
                    return new URL(e);
                } catch (MalformedURLException ex) {
                    throw new JSONException("read URL error", ex);
                }
            });
        }

        if (type == Class.class) {
            return ObjectReaderImplClass.INSTANCE;
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
            return LocalDateImpl.INSTANCE;
        }

        if (type == LocalTime.class) {
            return LocalTimeImpl.INSTANCE;
        }

        if (type == LocalDateTime.class) {
            return LocalDateTimeImpl.INSTANCE;
        }

        if (type == ZonedDateTime.class) {
            return ObjectReaderImplZonedDateTime.INSTANCE;
        }

        if (type == Instant.class) {
            return ObjectReaderImplInstant.INSTANCE;
        }

        if (type == Locale.class) {
            return LocaleImpl.INSTANCE;
        }

        if (type == Currency.class) {
            return ObjectReaderImplCurrency.INSTANCE;
        }

        if (type == ZoneId.class) {
//            return ZoneIdImpl.INSTANCE;
            // ZoneId.of(strVal)
            return new ObjectReaderImplFromString<ZoneId>(e -> ZoneId.of(e));
        }

        if (type == TimeZone.class) {
            return new ObjectReaderImplFromString<TimeZone>(e -> TimeZone.getTimeZone(e));
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
            return BoolValueArrayImpl.INSTANCE;
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
            return AtomicIntegerArrayImpl.INSTANCE;
        }

        if (type == AtomicLongArray.class) {
            return ObjectReaderImplAtomicLongArray.INSTANCE;
        }

        if (type == AtomicReference.class) {
            return ObjectReaderImplAtomicReference.INSTANCE;
        }

        if (type == Object[].class) {
            return ObjectArrayReader.INSTANCE;
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

        if (type == Set.class || type == AbstractSet.class) {
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

        if (type == SingletonSetImpl.TYPE) {
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
            return new MapEntryImpl(null, null);
        }

        if (type instanceof Class) {
            Class objectClass = (Class) type;
            if (Map.class.isAssignableFrom(objectClass)) {
                return ObjectReaderImplMap.of(null, objectClass, 0);
            }

            if (List.class.isAssignableFrom(objectClass)) {
                return ObjectReaderImplList.of(objectClass, null, 0);
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
                            .createObjectReaderNoneDefaultConstrutor(
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
                    return new InterfaceImpl(type);
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
                    return new MapEntryImpl(actualTypeArguments[0], actualTypeArguments[1]);
                }

                switch (rawType.getTypeName()) {
                    case "com.google.common.collect.ImmutableMap":
                    case "com.google.common.collect.RegularImmutableMap":
                        return new ObjectReaderImplMapTyped((Class) rawType, HashMap.class, actualTypeParam0, actualTypeParam1, 0, GuavaSupport.immutableMapConverter());
                    case "com.google.common.collect.SingletonImmutableBiMap":
                        return new ObjectReaderImplMapTyped((Class) rawType, HashMap.class, actualTypeParam0, actualTypeParam1, 0, GuavaSupport.singletonBiMapConverter());
                    case "org.springframework.util.LinkedMultiValueMap":
                        return ObjectReaderImplMap.of(type, (Class) rawType, 0L);
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
                        return new FieldReaderListInt64((Class) rawType, ArrayList.class);
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
                        return new FieldReaderListInt64((Class) rawType, LinkedList.class);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == Set.class || rawType == AbstractSet.class) {
                    if (itemClass == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, HashSet.class);
                    } else if (itemClass == Long.class) {
                        return new FieldReaderListInt64((Class) rawType, HashSet.class);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == NavigableSet.class || rawType == SortedSet.class) {
                    if (itemType == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, TreeSet.class);
                    } else if (itemClass == Long.class) {
                        return new FieldReaderListInt64((Class) rawType, TreeSet.class);
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
                        return new FieldReaderListInt64((Class) rawType, (Class) rawType);
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
            }

            return null;
        }

        if (type instanceof GenericArrayType) {
            return new GenericArrayImpl(((GenericArrayType) type).getGenericComponentType());
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
                return JdbcSupport.createTimeReader(null, null);
            case "java.sql.Timestamp":
                return JdbcSupport.createTimestampReader(null, null);
            case "java.sql.Date":
                return JdbcSupport.createDateReader(null, null);
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
            case "java.net.InetAddress":
            case "java.net.InetSocketAddress":
                return new ObjectReaderMisc((Class) type);
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

    abstract static class PrimitiveImpl<T>
            implements ObjectReader<T> {
        @Override
        public T createInstance(long features) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FieldReader getFieldReader(long hashCode) {
            return null;
        }

        @Override
        public abstract T readJSONBObject(JSONReader jsonReader, long features);
    }

    static class CharacterImpl
            extends PrimitiveImpl {
        static final CharacterImpl INSTANCE = new CharacterImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            String str = jsonReader.readString();
            if (str == null) {
                return null;
            }
            return str.charAt(0);
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            String str = jsonReader.readString();
            if (str == null) {
                return null;
            }
            return str.charAt(0);
        }
    }

    static class BooleanImpl
            extends PrimitiveImpl {
        static final BooleanImpl INSTANCE = new BooleanImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readBool();
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            return jsonReader.readBool();
        }
    }

    static class IntegerImpl
            extends PrimitiveImpl {
        static final IntegerImpl INSTANCE = new IntegerImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readInt32();
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            return jsonReader.readInt32();
        }
    }

    static class OptionalLongImpl
            extends PrimitiveImpl {
        static final OptionalLongImpl INSTANCE = new OptionalLongImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            Long integer = jsonReader.readInt64();
            if (integer == null) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(integer.longValue());
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            Long integer = jsonReader.readInt64();
            if (integer == null) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(integer.longValue());
        }
    }

    static class OptionalDoubleImpl
            extends PrimitiveImpl {
        static final OptionalDoubleImpl INSTANCE = new OptionalDoubleImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            Double value = jsonReader.readDouble();
            if (value == null) {
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(value.doubleValue());
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            Double value = jsonReader.readDouble();
            if (value == null) {
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(value.doubleValue());
        }
    }

    static class LongImpl
            extends PrimitiveImpl<Long> {
        static final LongImpl INSTANCE = new LongImpl();

        @Override
        public Long readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readInt64();
        }

        @Override
        public Long readObject(JSONReader jsonReader, long features) {
            return jsonReader.readInt64();
        }
    }

    static class FloatImpl
            extends PrimitiveImpl {
        static final FloatImpl INSTANCE = new FloatImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readFloat();
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            return jsonReader.readFloat();
        }
    }

    static class DoubleImpl
            extends PrimitiveImpl {
        static final DoubleImpl INSTANCE = new DoubleImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readDouble();
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            return jsonReader.readDouble();
        }
    }

    static class NumberImpl
            extends PrimitiveImpl {
        static final NumberImpl INSTANCE = new NumberImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readNumber();
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            return jsonReader.readNumber();
        }
    }

    static class UUIDImpl
            extends PrimitiveImpl {
        static final UUIDImpl INSTANCE = new UUIDImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readUUID();
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            return jsonReader.readUUID();
        }
    }

    static class LocalDateImpl
            extends PrimitiveImpl {
        static final LocalDateImpl INSTANCE = new LocalDateImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readLocalDate();
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            return jsonReader.readLocalDate();
        }
    }

    static class LocalTimeImpl
            extends PrimitiveImpl {
        static final LocalTimeImpl INSTANCE = new LocalTimeImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readLocalTime();
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            return jsonReader.readLocalTime();
        }
    }

    static class LocaleImpl
            extends PrimitiveImpl {
        static final LocaleImpl INSTANCE = new LocaleImpl();

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            String strVal = jsonReader.readString();
            if (strVal == null || strVal.isEmpty()) {
                return null;
            }
            String[] items = strVal.split("_");
            if (items.length == 1) {
                return new Locale(items[0]);
            }
            if (items.length == 2) {
                return new Locale(items[0], items[1]);
            }
            return new Locale(items[0], items[1], items[2]);
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            String strVal = jsonReader.readString();
            if (strVal == null || strVal.isEmpty()) {
                return null;
            }
            String[] items = strVal.split("_");
            if (items.length == 1) {
                return new Locale(items[0]);
            }
            if (items.length == 2) {
                return new Locale(items[0], items[1]);
            }
            return new Locale(items[0], items[1], items[2]);
        }
    }

    static class LocalDateTimeImpl
            extends DateTimeCodec
            implements ObjectReader {
        static final LocalDateTimeImpl INSTANCE = new LocalDateTimeImpl(null);
        static final LocalDateTimeImpl INSTANCE_UNIXTIME = new LocalDateTimeImpl("unixtime");

        public LocalDateTimeImpl(String format) {
            super(format);
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readLocalDateTime();
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            if (jsonReader.isInt()) {
                DateTimeFormatter formatter = getDateFormatter();
                if (formatter != null) {
                    String str = jsonReader.readString();
                    return LocalDateTime.parse(str, formatter);
                }

                long millis = jsonReader.readInt64Value();

                if (formatUnixTime) {
                    millis *= 1000;
                }

                Instant instant = Instant.ofEpochMilli(millis);
                ZoneId zoneId = jsonReader.getContext().getZoneId();
                return LocalDateTime.ofInstant(instant, zoneId);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            return jsonReader.readLocalDateTime();
        }
    }

    static class AtomicIntegerArrayImpl
            extends PrimitiveImpl {
        static final AtomicIntegerArrayImpl INSTANCE = new AtomicIntegerArrayImpl();

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            if (jsonReader.readIfNull()) {
                return null;
            }

            if (jsonReader.nextIfMatch('[')) {
                List<Integer> values = new ArrayList<>();
                for (; ; ) {
                    if (jsonReader.nextIfMatch(']')) {
                        break;
                    }
                    values.add(jsonReader.readInt32());
                }
                jsonReader.nextIfMatch(',');

                AtomicIntegerArray array = new AtomicIntegerArray(values.size());
                for (int i = 0; i < values.size(); i++) {
                    Integer value = values.get(i);
                    if (value == null) {
                        continue;
                    }
                    array.set(i, value);
                }
                return array;
            }

            throw new JSONException("TODO");
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            int entryCnt = jsonReader.startArray();
            if (entryCnt == -1) {
                return null;
            }
            AtomicIntegerArray array = new AtomicIntegerArray(entryCnt);
            for (int i = 0; i < entryCnt; i++) {
                Integer value = jsonReader.readInt32();
                if (value == null) {
                    continue;
                }
                array.set(i, value);
            }
            return array;
        }
    }

    static class BoolValueArrayImpl
            extends PrimitiveImpl {
        static final BoolValueArrayImpl INSTANCE = new BoolValueArrayImpl();

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            if (jsonReader.readIfNull()) {
                return null;
            }

            if (jsonReader.nextIfMatch('[')) {
                boolean[] values = new boolean[16];
                int size = 0;
                for (; ; ) {
                    if (jsonReader.nextIfMatch(']')) {
                        break;
                    }

                    int minCapacity = size + 1;
                    if (minCapacity - values.length > 0) {
                        int oldCapacity = values.length;
                        int newCapacity = oldCapacity + (oldCapacity >> 1);
                        if (newCapacity - minCapacity < 0) {
                            newCapacity = minCapacity;
                        }

                        values = Arrays.copyOf(values, newCapacity);
                    }

                    values[size++] = jsonReader.readBoolValue();
                }
                jsonReader.nextIfMatch(',');

                return Arrays.copyOf(values, size);
            }

            throw new JSONException("TODO");
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            int entryCnt = jsonReader.startArray();
            if (entryCnt == -1) {
                return null;
            }
            boolean[] array = new boolean[entryCnt];
            for (int i = 0; i < entryCnt; i++) {
                array[i] = jsonReader.readBoolValue();
            }
            return array;
        }
    }

    static class InterfaceImpl
            extends PrimitiveImpl {
        final Type interfaceType;

        public InterfaceImpl(Type interfaceType) {
            this.interfaceType = interfaceType;
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            if (jsonReader.nextIfMatch('{')) {
                long hash = jsonReader.readFieldNameHashCode();
                JSONReader.Context context = jsonReader.getContext();

                if (hash == HASH_TYPE && context.isEnabled(JSONReader.Feature.SupportAutoType)) {
                    long typeHash = jsonReader.readTypeHashCode();
                    ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
                    if (autoTypeObjectReader == null) {
                        String typeName = jsonReader.getString();
                        autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);

                        if (autoTypeObjectReader == null) {
                            throw new JSONException("auotype not support : " + typeName);
                        }
                    }

                    return autoTypeObjectReader.readObject(jsonReader, 0);
                }

                return ObjectReaderImplMap.INSTANCE.readObject(jsonReader, 0);
            }

            Object value;
            switch (jsonReader.current()) {
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '.':
                    value = jsonReader.readNumber();
                    break;
                case '[':
                    value = jsonReader.readArray();
                    break;
                case '"':
                case '\'':
                    value = jsonReader.readString();
                    break;
                case 't':
                case 'f':
                    value = jsonReader.readBoolValue();
                    break;
                case 'n':
                    jsonReader.readNull();
                    value = null;
                    break;
                default:
                    throw new JSONException("TODO : " + jsonReader.current());
            }

            return value;
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            return jsonReader.readAny();
        }
    }

    static class MapEntryImpl
            extends PrimitiveImpl {
        final Type keyType;
        final Type valueType;

        volatile ObjectReader keyReader;
        volatile ObjectReader valueReader;

        public MapEntryImpl(Type keyType, Type valueType) {
            this.keyType = keyType;
            this.valueType = valueType;
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            int entryCnt = jsonReader.startArray();
            if (entryCnt != 2) {
                throw new JSONException("entryCnt must be 2, but " + entryCnt);
            }
            Object key;
            if (keyType == null) {
                key = jsonReader.readAny();
            } else {
                if (keyReader == null) {
                    keyReader = jsonReader.getObjectReader(keyType);
                }
                key = keyReader.readObject(jsonReader, features);
            }

            Object value;
            if (valueType == null) {
                value = jsonReader.readAny();
            } else {
                if (valueReader == null) {
                    valueReader = jsonReader.getObjectReader(valueType);
                }
                value = valueReader.readObject(jsonReader, features);
            }

            return new AbstractMap.SimpleEntry(key, value);
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            jsonReader.nextIfMatch('{');
            Object key = jsonReader.readAny();
            jsonReader.nextIfMatch(':');

            Object value;
            if (valueType == null) {
                value = jsonReader.readAny();
            } else {
                if (valueReader == null) {
                    valueReader = jsonReader.getObjectReader(valueType);
                }
                value = valueReader.readObject(jsonReader, features);
            }

            jsonReader.nextIfMatch('}');
            jsonReader.nextIfMatch(',');
            return new AbstractMap.SimpleEntry(key, value);
        }
    }

    static class SingletonSetImpl
            extends PrimitiveImpl {
        static final Class TYPE = Collections.singleton(1).getClass();

        @Override
        public Class getObjectClass() {
            return TYPE;
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            int entryCnt = jsonReader.startArray();
            if (entryCnt != 1) {
                throw new JSONException("input not singleton");
            }
            Object value = jsonReader.read(Object.class);
            return Collections.singleton(value);
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            jsonReader.nextIfMatch('[');
            Object value = jsonReader.readAny();

            if (jsonReader.nextIfMatch(']')) {
                jsonReader.nextIfMatch(',');
            } else {
                throw new JSONException("input not singleton");
            }
            return Collections.singleton(value);
        }
    }

    static class GenericArrayImpl
            implements ObjectReader {
        final Type itemType;
        final Class<?> componentClass;
        ObjectReader itemObjectReader;

        public GenericArrayImpl(Type itemType) {
            this.itemType = itemType;
            this.componentClass = TypeUtils.getMapping(itemType);
        }

        @Override
        public Object createInstance(long features) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FieldReader getFieldReader(long hashCode) {
            return null;
        }

        @Override
        public Object readJSONBObject(JSONReader jsonReader, long features) {
            int entryCnt = jsonReader.startArray();

            if (entryCnt > 0 && itemObjectReader == null) {
                itemObjectReader = jsonReader
                        .getContext()
                        .getObjectReader(itemType);
            }

            Object array = Array.newInstance(componentClass, entryCnt);

            for (int i = 0; i < entryCnt; ++i) {
                Object item = itemObjectReader.readJSONBObject(jsonReader, 0);
                Array.set(array, i, item);
            }

            return array;
        }

        @Override
        public Object readObject(JSONReader jsonReader, long features) {
            if (itemObjectReader == null) {
                itemObjectReader = jsonReader
                        .getContext()
                        .getObjectReader(itemType);
            }

            if (jsonReader.isJSONB()) {
                return readJSONBObject(jsonReader, 0);
            }

            if (jsonReader.readIfNull()) {
                return null;
            }

            char ch = jsonReader.current();
            if (ch == '"') {
                String str = jsonReader.readString();
                if (str.isEmpty()) {
                    return null;
                }
                throw new JSONException("format error");
            }

            List<Object> list = new ArrayList<>();
            if (ch != '[') {
                throw new JSONException("format error : " + ch);
            }
            jsonReader.next();

            for (; ; ) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                Object item;
                if (itemObjectReader != null) {
                    item = itemObjectReader.readObject(jsonReader, 0);
                } else {
                    if (itemType == String.class) {
                        item = jsonReader.readString();
                    } else {
                        throw new JSONException("TODO : " + itemType);
                    }
                }

                list.add(item);

                if (jsonReader.nextIfMatch(',')) {
                    continue;
                }
            }

            jsonReader.nextIfMatch(',');

            Object array = Array.newInstance(componentClass, list.size());

            for (int i = 0; i < list.size(); ++i) {
                Array.set(array, i, list.get(i));
            }

            return array;
        }
    }
}
