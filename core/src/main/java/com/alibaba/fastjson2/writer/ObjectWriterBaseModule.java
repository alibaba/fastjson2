package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.*;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.modules.ObjectWriterAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.support.money.MoneySupport;
import com.alibaba.fastjson2.util.*;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

import static com.alibaba.fastjson2.codec.FieldInfo.JSON_AUTO_WIRED_ANNOTATED;
import static com.alibaba.fastjson2.util.AnnotationUtils.getAnnotations;
import static com.alibaba.fastjson2.util.BeanUtils.*;

public class ObjectWriterBaseModule
        implements ObjectWriterModule {
    static ObjectWriterAdapter STACK_TRACE_ELEMENT_WRITER;

    final ObjectWriterProvider provider;
    final WriterAnnotationProcessor annotationProcessor;

    public ObjectWriterBaseModule(ObjectWriterProvider provider) {
        this.provider = provider;
        this.annotationProcessor = new WriterAnnotationProcessor();
    }

    @Override
    public ObjectWriterProvider getProvider() {
        return provider;
    }

    @Override
    public ObjectWriterAnnotationProcessor getAnnotationProcessor() {
        return annotationProcessor;
    }

    public class WriterAnnotationProcessor
            implements ObjectWriterAnnotationProcessor {
        @Override
        public void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
            Class superclass = objectClass.getSuperclass();
            if (superclass != Object.class && superclass != null && superclass != Enum.class) {
                getBeanInfo(beanInfo, superclass);

                if (beanInfo.seeAlso != null && beanInfo.seeAlsoNames != null) {
                    for (int i = 0; i < beanInfo.seeAlso.length; i++) {
                        Class seeAlso = beanInfo.seeAlso[i];
                        if (seeAlso == objectClass && i < beanInfo.seeAlsoNames.length) {
                            String seeAlsoName = beanInfo.seeAlsoNames[i];
                            if (seeAlsoName != null && seeAlsoName.length() != 0) {
                                beanInfo.typeName = seeAlsoName;
                                break;
                            }
                        }
                    }
                }
            }

            Annotation jsonType1x = null;
            JSONType jsonType = null;
            Annotation[] annotations = getAnnotations(objectClass);
            for (Annotation annotation : annotations) {
                Class annotationType = annotation.annotationType();
                jsonType = AnnotationUtils.findAnnotation(annotation, JSONType.class);
                if (jsonType == annotation) {
                    continue;
                }

                if (annotationType == JSONAutowired.class) {
                    beanInfo.writerFeatures |= JSON_AUTO_WIRED_ANNOTATED;

                    JSONAutowired autowired = (JSONAutowired) annotation;
                    String fieldName = autowired.writer();
                    if (!fieldName.isEmpty()) {
                        beanInfo.objectWriterFieldName = fieldName;
                    }
                    continue;
                }

                boolean useJacksonAnnotation = JSONFactory.isUseJacksonAnnotation();
                switch (annotationType.getName()) {
                    case "com.alibaba.fastjson.annotation.JSONType":
                        jsonType1x = annotation;
                        break;
                    case "com.fasterxml.jackson.annotation.JsonIgnoreProperties":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonIgnoreProperties":
                        if (useJacksonAnnotation) {
                            processJacksonJsonIgnoreProperties(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonPropertyOrder":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonPropertyOrder":
                        if (useJacksonAnnotation) {
                            processJacksonJsonPropertyOrder(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonFormat":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonFormat":
                        if (useJacksonAnnotation) {
                            processJacksonJsonFormat(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonTypeInfo":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonTypeInfo":
                        if (useJacksonAnnotation) {
                            processJacksonJsonTypeInfo(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.databind.annotation.JsonSerialize":
                    case "com.alibaba.fastjson2.adapter.jackson.databind.annotation.JsonSerialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonSerialize(beanInfo, annotation);
                            if (beanInfo.serializer != null && Enum.class.isAssignableFrom(objectClass)) {
                                beanInfo.writeEnumAsJavaBean = true;
                            }
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonTypeName":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonTypeName":
                        if (useJacksonAnnotation) {
                            processJacksonJsonTypeName(beanInfo, annotation);
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
                        BeanUtils.getKotlinConstructor(objectClass, beanInfo);
                        beanInfo.createParameterNames = BeanUtils.getKotlinConstructorParameters(objectClass);
                        break;
                    default:
                        break;
                }
            }

            if (jsonType == null) {
                Class mixInSource = provider.mixInCache.get(objectClass);

                if (mixInSource != null) {
                    beanInfo.mixIn = true;

                    Annotation[] mixInAnnotations = getAnnotations(mixInSource);
                    for (Annotation annotation : mixInAnnotations) {
                        Class<? extends Annotation> annotationType = annotation.annotationType();
                        jsonType = AnnotationUtils.findAnnotation(annotation, JSONType.class);
                        if (jsonType == annotation) {
                            continue;
                        }

                        if (annotationType == JSONAutowired.class) {
                            beanInfo.writerFeatures |= JSON_AUTO_WIRED_ANNOTATED;

                            JSONAutowired autowired = (JSONAutowired) annotation;
                            String fieldName = autowired.writer();
                            if (!fieldName.isEmpty()) {
                                beanInfo.objectWriterFieldName = fieldName;
                            }
                            continue;
                        }

                        String annotationTypeName = annotationType.getName();
                        switch (annotationTypeName) {
                            case "com.alibaba.fastjson.annotation.JSONType":
                                jsonType1x = annotation;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            if (jsonType != null) {
                Class<?>[] classes = jsonType.seeAlso();
                if (classes.length != 0) {
                    beanInfo.seeAlso = classes;
                }

                String typeKey = jsonType.typeKey();
                if (!typeKey.isEmpty()) {
                    beanInfo.typeKey = typeKey;
                }

                String typeName = jsonType.typeName();
                if (!typeName.isEmpty()) {
                    beanInfo.typeName = typeName;
                }

                for (JSONWriter.Feature feature : jsonType.serializeFeatures()) {
                    beanInfo.writerFeatures |= feature.mask;
                }

                beanInfo.namingStrategy =
                        jsonType.naming().name();

                String[] ignores = jsonType.ignores();
                if (ignores.length > 0) {
                    beanInfo.ignores = ignores;
                }

                String[] includes = jsonType.includes();
                if (includes.length > 0) {
                    beanInfo.includes = includes;
                }

                String[] orders = jsonType.orders();
                if (orders.length > 0) {
                    beanInfo.orders = orders;
                }

                Class<?> serializer = jsonType.serializer();
                if (ObjectWriter.class.isAssignableFrom(serializer)) {
                    beanInfo.serializer = serializer;
                }

                Class<? extends Filter>[] serializeFilters = jsonType.serializeFilters();
                if (serializeFilters.length != 0) {
                    beanInfo.serializeFilters = serializeFilters;
                }

                String format = jsonType.format();
                if (!format.isEmpty()) {
                    beanInfo.format = format;
                }

                String locale = jsonType.locale();
                if (!locale.isEmpty()) {
                    String[] parts = locale.split("_");
                    if (parts.length == 2) {
                        beanInfo.locale = new Locale(parts[0], parts[1]);
                    }
                }

                if (!jsonType.alphabetic()) {
                    beanInfo.alphabetic = false;
                }

                if (jsonType.writeEnumAsJavaBean()) {
                    beanInfo.writeEnumAsJavaBean = true;
                }
            } else if (jsonType1x != null) {
                final Annotation annotation = jsonType1x;
                BeanUtils.annotationMethods(jsonType1x.annotationType(), method -> BeanUtils.processJSONType1x(beanInfo, annotation, method));
            }

            if (beanInfo.seeAlso != null && beanInfo.seeAlso.length != 0
                    && (beanInfo.typeName == null || beanInfo.typeName.length() == 0)) {
                for (Class seeAlsoClass : beanInfo.seeAlso) {
                    if (seeAlsoClass == objectClass) {
                        beanInfo.typeName = objectClass.getSimpleName();
                        break;
                    }
                }
            }
        }

        @Override
        public void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectType, Field field) {
            Class mixInSource = provider.mixInCache.get(objectType);

            if (mixInSource != null && mixInSource != objectType) {
                Field mixInField = null;
                try {
                    mixInField = mixInSource.getDeclaredField(field.getName());
                } catch (Exception ignored) {
                }

                if (mixInField != null) {
                    getFieldInfo(beanInfo, fieldInfo, mixInSource, mixInField);
                }
            }

            Class fieldClassMixInSource = provider.mixInCache.get(field.getType());
            if (fieldClassMixInSource != null) {
                fieldInfo.fieldClassMixIn = true;
            }

            int modifiers = field.getModifiers();
            boolean isTransient = Modifier.isTransient(modifiers);
            if (isTransient) {
                fieldInfo.ignore = true;
            }

            JSONField jsonField = null;
            Annotation[] annotations = getAnnotations(field);
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (jsonField == null) {
                    jsonField = AnnotationUtils.findAnnotation(annotation, JSONField.class);
                    if (jsonField == annotation) {
                        continue;
                    }
                }

                String annotationTypeName = annotationType.getName();
                boolean useJacksonAnnotation = JSONFactory.isUseJacksonAnnotation();
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
                    case "com.fasterxml.jackson.annotation.JsonValue":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonValue":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.VALUE_MASK;
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonRawValue":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonRawValue":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.RAW_VALUE_MASK;
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
                    case "com.alibaba.fastjson2.adapter.jackson.databind.annotation.JsonSerialize":
                    case "com.fasterxml.jackson.databind.annotation.JsonSerialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonSerialize(fieldInfo, annotation);
                        }
                        break;
                    default:
                        break;
                }
            }

            if (jsonField == null) {
                return;
            }

            loadFieldInfo(fieldInfo, jsonField);

            Class writeUsing = jsonField.writeUsing();
            if (ObjectWriter.class.isAssignableFrom(writeUsing)) {
                fieldInfo.writeUsing = writeUsing;
            }

            Class serializeUsing = jsonField.serializeUsing();
            if (ObjectWriter.class.isAssignableFrom(serializeUsing)) {
                fieldInfo.writeUsing = serializeUsing;
            }

            if (jsonField.jsonDirect()) {
                fieldInfo.features |= FieldInfo.RAW_VALUE_MASK;
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
                            Annotation[] value = (Annotation[]) result;
                            if (value.length != 0) {
                                beanInfo.seeAlso = new Class[value.length];
                                beanInfo.seeAlsoNames = new String[value.length];
                                for (int i = 0; i < value.length; i++) {
                                    Annotation item = value[i];
                                    processJacksonJsonSubTypesType(beanInfo, i, item);
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

        private void processJacksonJsonSerialize(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "using": {
                            Class using = processUsing((Class) result);
                            if (using != null) {
                                beanInfo.serializer = using;
                            }
                            break;
                        }
                        case "keyUsing":
                            Class keyUsing = processUsing((Class) result);
                            if (keyUsing != null) {
                                beanInfo.serializer = keyUsing;
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

        private Class processUsing(Class result) {
            Class writeUsing = result;
            String usingName = writeUsing.getName();
            String noneClassName0 = "com.alibaba.fastjson2.adapter.jackson.databind.JsonSerializer$None";
            String noneClassName1 = "com.fasterxml.jackson.databind.JsonSerializer$None";
            if (!noneClassName0.equals(usingName)
                    && !noneClassName1.equals(usingName)
                    && ObjectWriter.class.isAssignableFrom(writeUsing)
            ) {
                return writeUsing;
            }
            return null;
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
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteClassName.mask;
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

        private void processJacksonJsonPropertyOrder(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "value": {
                            String[] value = (String[]) result;
                            if (value.length != 0) {
                                beanInfo.orders = value;
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

        private void processJacksonJsonSerialize(FieldInfo fieldInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "using":
                            Class using = processUsing((Class) result);
                            if (using != null) {
                                fieldInfo.writeUsing = using;
                            }
                            break;
                        case "keyUsing":
                            Class keyUsing = processUsing((Class) result);
                            if (keyUsing != null) {
                                fieldInfo.keyUsing = keyUsing;
                            }
                            break;
                        case "valueUsing":
                            Class valueUsing = processUsing((Class) result);
                            if (valueUsing != null) {
                                fieldInfo.valueUsing = valueUsing;
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

        private void processJacksonJsonProperty(FieldInfo fieldInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "value":
                            String value = (String) result;
                            if (!value.isEmpty()) {
                                fieldInfo.fieldName = value;
                            }
                            break;
                        case "access": {
                            String access = ((Enum) result).name();
                            switch (access) {
                                case "WRITE_ONLY":
                                    fieldInfo.ignore = true;
                                    break;
                                default:
                                    fieldInfo.ignore = false;
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

        private void processJacksonJsonIgnoreProperties(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    switch (name) {
                        case "value": {
                            String[] value = (String[]) result;
                            if (value.length != 0) {
                                beanInfo.ignores = value;
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
                            loadJsonFieldFormat(fieldInfo, (String) result);
                            break;
                        }
                        case "label": {
                            String value = (String) result;
                            if (!value.isEmpty()) {
                                fieldInfo.label = value;
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
                        case "ordinal": {
                            Integer ordinal = (Integer) result;
                            if (ordinal.intValue() != 0) {
                                fieldInfo.ordinal = ordinal;
                            }
                            break;
                        }
                        case "serialize": {
                            Boolean serialize = (Boolean) result;
                            if (!serialize.booleanValue()) {
                                fieldInfo.ignore = true;
                            }
                            break;
                        }
                        case "unwrapped": {
                            Boolean unwrapped = (Boolean) result;
                            if (unwrapped.booleanValue()) {
                                fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
                            }
                            break;
                        }
                        case "serialzeFeatures": {
                            Enum[] features = (Enum[]) result;
                            applyFeatures(fieldInfo, features);
                            break;
                        }
                        case "serializeUsing": {
                            Class writeUsing = (Class) result;
                            if (ObjectWriter.class.isAssignableFrom(writeUsing)) {
                                fieldInfo.writeUsing = writeUsing;
                            }
                            break;
                        }
                        case "jsonDirect": {
                            Boolean jsonDirect = (Boolean) result;
                            if (jsonDirect) {
                                fieldInfo.features |= FieldInfo.RAW_VALUE_MASK;
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

        private void applyFeatures(FieldInfo fieldInfo, Enum[] features) {
            for (Enum feature : features) {
                switch (feature.name()) {
                    case "UseISO8601DateFormat":
                        fieldInfo.format = "iso8601";
                        break;
                    case "WriteMapNullValue":
                        fieldInfo.features |= JSONWriter.Feature.WriteNulls.mask;
                        break;
                    case "WriteNullListAsEmpty":
                        fieldInfo.features |= JSONWriter.Feature.WriteNullListAsEmpty.mask;
                        break;
                    case "WriteNullStringAsEmpty":
                        fieldInfo.features |= JSONWriter.Feature.WriteNullStringAsEmpty.mask;
                        break;
                    case "WriteNullNumberAsZero":
                        fieldInfo.features |= JSONWriter.Feature.WriteNullNumberAsZero.mask;
                        break;
                    case "WriteNullBooleanAsFalse":
                        fieldInfo.features |= JSONWriter.Feature.WriteNullBooleanAsFalse.mask;
                        break;
                    case "BrowserCompatible":
                        fieldInfo.features |= JSONWriter.Feature.BrowserCompatible.mask;
                        break;
                    case "WriteClassName":
                        fieldInfo.features |= JSONWriter.Feature.WriteClassName.mask;
                        break;
                    case "WriteNonStringValueAsString":
                        fieldInfo.features |= JSONWriter.Feature.WriteNonStringValueAsString.mask;
                        break;
                    case "WriteEnumUsingToString":
                        fieldInfo.features |= JSONWriter.Feature.WriteEnumUsingToString.mask;
                        break;
                    case "NotWriteRootClassName":
                        fieldInfo.features |= JSONWriter.Feature.NotWriteRootClassName.mask;
                        break;
                    case "IgnoreErrorGetter":
                        fieldInfo.features |= JSONWriter.Feature.IgnoreErrorGetter.mask;
                        break;
                    case "WriteBigDecimalAsPlain":
                        fieldInfo.features |= JSONWriter.Feature.WriteBigDecimalAsPlain.mask;
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectClass, Method method) {
            Class mixInSource = provider.mixInCache.get(objectClass);
            String methodName = method.getName();

            if (mixInSource != null && mixInSource != objectClass) {
                Method mixInMethod = null;
                try {
                    mixInMethod = mixInSource.getDeclaredMethod(methodName, method.getParameterTypes());
                } catch (Exception ignored) {
                }

                if (mixInMethod != null) {
                    getFieldInfo(beanInfo, fieldInfo, mixInSource, mixInMethod);
                }
            }

            Class fieldClassMixInSource = provider.mixInCache.get(method.getReturnType());
            if (fieldClassMixInSource != null) {
                fieldInfo.fieldClassMixIn = true;
            }

            if (JDKUtils.CLASS_TRANSIENT != null && method.getAnnotation(JDKUtils.CLASS_TRANSIENT) != null) {
                fieldInfo.ignore = true;
            }

            Annotation[] annotations = getAnnotations(method);
            processAnnotations(fieldInfo, annotations);

            if (!objectClass.getName().startsWith("java.lang") && !BeanUtils.isRecord(objectClass)) {
                String fieldName = BeanUtils.getterName(method, null);

                char firstChar = fieldName.charAt(0);
                final String fieldName0;
                if (firstChar >= 'A' && firstChar <= 'Z') {
                    char[] chars = fieldName.toCharArray();
                    chars[0] = (char) (firstChar + 32);
                    fieldName0 = new String(chars);
                } else if (firstChar >= 'a' && firstChar <= 'z' && fieldName.length() > 2 && fieldName.charAt(1) == '_') {
                    char[] chars = fieldName.toCharArray();
                    chars[0] = (char) (firstChar - 32);
                    fieldName0 = new String(chars);
                } else {
                    fieldName0 = null;
                }
                BeanUtils.declaredFields(objectClass, field -> {
                    String name = field.getName();
                    if (name.equals(fieldName)
                            || (name.equals(methodName) && field.getType() == boolean.class)
                            || (fieldName0 != null && name.equals(fieldName0))
                    ) {
                        fieldInfo.features |= FieldInfo.FIELD_MASK;
                        getFieldInfo(beanInfo, fieldInfo, objectClass, field);
                    }
                });
            }

            if (beanInfo.kotlin && beanInfo.creatorConstructor != null && beanInfo.createParameterNames != null) {
                String fieldName = BeanUtils.getterName(method, null);
                for (int i = 0; i < beanInfo.createParameterNames.length; i++) {
                    if (fieldName.equals(beanInfo.createParameterNames[i])) {
                        Annotation[] parameterAnnotations = beanInfo.creatorConstructor.getParameterAnnotations()[i];
                        processAnnotations(fieldInfo, parameterAnnotations);
                        break;
                    }
                }
            }
        }

        private void processAnnotations(FieldInfo fieldInfo, Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                JSONField jsonField = AnnotationUtils.findAnnotation(annotation, JSONField.class);
                if (Objects.nonNull(jsonField)) {
                    loadFieldInfo(fieldInfo, jsonField);
                    continue;
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
                    case "java.beans.Transient":
                        fieldInfo.ignore = true;
                        fieldInfo.isTransient = true;
                        break;
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonProperty":
                    case "com.fasterxml.jackson.annotation.JsonProperty": {
                        if (useJacksonAnnotation) {
                            processJacksonJsonProperty(fieldInfo, annotation);
                        }
                        break;
                    }
                    case "com.fasterxml.jackson.annotation.JsonFormat":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonFormat":
                        if (useJacksonAnnotation) {
                            processJacksonJsonFormat(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonValue":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonValue":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.VALUE_MASK;
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonRawValue":
                    case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonRawValue":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.RAW_VALUE_MASK;
                        }
                        break;
                    case "com.alibaba.fastjson2.adapter.jackson.databind.annotation.JsonSerialize":
                    case "com.fasterxml.jackson.databind.annotation.JsonSerialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonSerialize(fieldInfo, annotation);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * load {@link JSONField} into {@link FieldInfo} params
         *
         * @param fieldInfo Java Field Info
         * @param jsonField {@link JSONField} JSON Field Info
         */
        private void loadFieldInfo(FieldInfo fieldInfo, JSONField jsonField) {
            String jsonFieldName = jsonField.name();
            if (!jsonFieldName.isEmpty()) {
                fieldInfo.fieldName = jsonFieldName;
            }

            String defaultValue = jsonField.defaultValue();
            if (!defaultValue.isEmpty()) {
                fieldInfo.defaultValue = defaultValue;
            }

            loadJsonFieldFormat(fieldInfo, jsonField.format());

            String label = jsonField.label();
            if (!label.isEmpty()) {
                fieldInfo.label = label;
            }

            if (!fieldInfo.ignore) {
                fieldInfo.ignore = !jsonField.serialize();
            }

            if (jsonField.unwrapped()) {
                fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
            }

            for (JSONWriter.Feature feature : jsonField.serializeFeatures()) {
                fieldInfo.features |= feature.mask;
            }

            int ordinal = jsonField.ordinal();
            if (ordinal != 0) {
                fieldInfo.ordinal = ordinal;
            }

            if (jsonField.value()) {
                fieldInfo.features |= FieldInfo.VALUE_MASK;
            }

            if (jsonField.jsonDirect()) {
                fieldInfo.features |= FieldInfo.RAW_VALUE_MASK;
            }
        }

        /**
         * load {@link JSONField} format params into FieldInfo
         *
         * @param fieldInfo Java Field Info
         * @param jsonFieldFormat {@link JSONField} format params
         */
        private void loadJsonFieldFormat(FieldInfo fieldInfo, String jsonFieldFormat) {
            if (!jsonFieldFormat.isEmpty()) {
                jsonFieldFormat = jsonFieldFormat.trim();

                if (jsonFieldFormat.indexOf('T') != -1 && !jsonFieldFormat.contains("'T'")) {
                    jsonFieldFormat = jsonFieldFormat.replaceAll("T", "'T'");
                }

                if (!jsonFieldFormat.isEmpty()) {
                    fieldInfo.format = jsonFieldFormat;
                }
            }
        }
    }

    ObjectWriter getExternalObjectWriter(String className, Class objectClass) {
        switch (className) {
            case "java.sql.Time":
                return JdbcSupport.createTimeWriter(null);
            case "java.sql.Timestamp":
                return JdbcSupport.createTimestampWriter(objectClass, null);
            case "org.joda.time.chrono.GregorianChronology":
                return JodaSupport.createGregorianChronologyWriter(objectClass);
            case "org.joda.time.chrono.ISOChronology":
                return JodaSupport.createISOChronologyWriter(objectClass);
            case "org.joda.time.LocalDate":
                return JodaSupport.createLocalDateWriter(objectClass, null);
            case "org.joda.time.LocalDateTime":
                return JodaSupport.createLocalDateTimeWriter(objectClass, null);
            default:
                if (JdbcSupport.isClob(objectClass)) {
                    return JdbcSupport.createClobWriter(objectClass);
                }
                return null;
        }
    }

    @Override
    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        if (objectType == String.class) {
            return ObjectWriterImplString.INSTANCE;
        }

        if (objectClass == null) {
            if (objectType instanceof Class) {
                objectClass = (Class) objectType;
            } else {
                objectClass = TypeUtils.getMapping(objectType);
            }
        }

        String className = objectClass.getName();
        ObjectWriter externalObjectWriter = getExternalObjectWriter(className, objectClass);
        if (externalObjectWriter != null) {
            return externalObjectWriter;
        }

        switch (className) {
            case "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList":
            case "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet":
                return null;
            case "org.javamoney.moneta.internal.JDKCurrencyAdapter":
                return ObjectWriterImplToString.INSTANCE;
            case "com.fasterxml.jackson.databind.node.ObjectNode":
                return ObjectWriterImplToString.DIRECT;
            case "org.javamoney.moneta.Money":
                return MoneySupport.createMonetaryAmountWriter();
            case "org.javamoney.moneta.spi.DefaultNumberValue":
                return MoneySupport.createNumberValueWriter();
            case "net.sf.json.JSONNull":
            case "java.net.Inet4Address":
            case "java.net.Inet6Address":
            case "java.net.InetSocketAddress":
            case "java.text.SimpleDateFormat":
            case "java.util.regex.Pattern":
                return ObjectWriterMisc.INSTANCE;
            case "org.apache.commons.lang3.tuple.Pair":
            case "org.apache.commons.lang3.tuple.MutablePair":
            case "org.apache.commons.lang3.tuple.ImmutablePair":
                return new ApacheLang3Support.PairWriter(objectClass);
            default:
                break;
        }

        if (objectType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) objectType;
            Type rawType = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (rawType == List.class || rawType == ArrayList.class) {
                if (actualTypeArguments.length == 1
                        && actualTypeArguments[0] == String.class) {
                    return ObjectWriterImplListStr.INSTANCE;
                }

                objectType = rawType;
            }

            if (Map.class.isAssignableFrom(objectClass)) {
                return ObjectWriterImplMap.of(objectType, objectClass);
            }

            if (objectClass == Optional.class) {
                if (actualTypeArguments.length == 1) {
                    return new ObjectWriterImplOptional(actualTypeArguments[0], null, null);
                }
            }
        }

        if (objectType == LinkedList.class) {
            return ObjectWriterImplList.INSTANCE;
        }

        if (objectType == ArrayList.class
                || objectType == List.class
                || List.class.isAssignableFrom(objectClass)) {
            return ObjectWriterImplList.INSTANCE;
        }

        if (Collection.class.isAssignableFrom(objectClass)) {
            return ObjectWriterImplCollection.INSTANCE;
        }

        if (Map.class.isAssignableFrom(objectClass)) {
            return ObjectWriterImplMap.of(objectClass);
        }

        if (Map.Entry.class.isAssignableFrom(objectClass)) {
            String objectClassName = objectClass.getName();
            if (!"org.apache.commons.lang3.tuple.ImmutablePair".equals(objectClassName)
                    && !"org.apache.commons.lang3.tuple.MutablePair".equals(objectClassName)
            ) {
                return ObjectWriterImplMapEntry.INSTANCE;
            }
        }

        if (objectType == Integer.class) {
            return ObjectWriterImplInt32.INSTANCE;
        }

        if (objectType == AtomicInteger.class) {
            return ObjectWriterImplAtomicInteger.INSTANCE;
        }

        if (objectType == Byte.class) {
            return ObjectWriterImplInt8.INSTANCE;
        }

        if (objectType == Short.class) {
            return ObjectWriterImplInt16.INSTANCE;
        }

        if (objectType == Long.class) {
            return ObjectWriterImplInt64.INSTANCE;
        }

        if (objectType == AtomicLong.class) {
            return ObjectWriterImplAtomicLong.INSTANCE;
        }

        if (objectType == AtomicReference.class) {
            return ObjectWriterImplAtomicReference.INSTANCE;
        }

        if (objectType == Float.class) {
            return ObjectWriterImplFloat.INSTANCE;
        }

        if (objectType == Double.class) {
            return ObjectWriterImplDouble.INSTANCE;
        }

        if (objectType == BigInteger.class) {
            return ObjectWriterBigInteger.INSTANCE;
        }

        if (objectType == BigDecimal.class) {
            return ObjectWriterImplBigDecimal.INSTANCE;
        }

        if (objectType == BitSet.class) {
            return ObjectWriterImplBitSet.INSTANCE;
        }

        if (objectType == OptionalInt.class) {
            return ObjectWriterImplOptionalInt.INSTANCE;
        }

        if (objectType == OptionalLong.class) {
            return ObjectWriterImplOptionalLong.INSTANCE;
        }

        if (objectType == OptionalDouble.class) {
            return ObjectWriterImplOptionalDouble.INSTANCE;
        }

        if (objectType == Optional.class) {
            return ObjectWriterImplOptional.INSTANCE;
        }

        if (objectType == Boolean.class) {
            return ObjectWriterImplBoolean.INSTANCE;
        }

        if (objectType == AtomicBoolean.class) {
            return ObjectWriterImplAtomicBoolean.INSTANCE;
        }

        if (objectType == AtomicIntegerArray.class) {
            return ObjectWriterImplAtomicIntegerArray.INSTANCE;
        }

        if (objectType == AtomicLongArray.class) {
            return ObjectWriterImplAtomicLongArray.INSTANCE;
        }

        if (objectType == Character.class) {
            return ObjectWriterImplCharacter.INSTANCE;
        }

        if (objectType instanceof Class) {
            Class clazz = (Class) objectType;

            if (TimeUnit.class.isAssignableFrom(clazz)) {
                return new ObjectWriterImplEnum(null, TimeUnit.class, null, null, 0);
            }

            if (Enum.class.isAssignableFrom(clazz)) {
                ObjectWriter enumWriter = createEnumWriter(clazz);
                if (enumWriter != null) {
                    return enumWriter;
                }
            }

            if (JSONPath.class.isAssignableFrom(clazz)) {
                return ObjectWriterImplToString.INSTANCE;
            }

            if (clazz == boolean[].class) {
                return ObjectWriterImplBoolValueArray.INSTANCE;
            }

            if (clazz == char[].class) {
                return ObjectWriterImplCharValueArray.INSTANCE;
            }

            if (clazz == StringBuffer.class || clazz == StringBuilder.class) {
                return ObjectWriterImplToString.INSTANCE;
            }

            if (clazz == byte[].class) {
                return ObjectWriterImplInt8ValueArray.INSTANCE;
            }

            if (clazz == short[].class) {
                return ObjectWriterImplInt16ValueArray.INSTANCE;
            }

            if (clazz == int[].class) {
                return ObjectWriterImplInt32ValueArray.INSTANCE;
            }

            if (clazz == long[].class) {
                return ObjectWriterImplInt64ValueArray.INSTANCE;
            }

            if (clazz == float[].class) {
                return ObjectWriterImplFloatValueArray.INSTANCE;
            }

            if (clazz == double[].class) {
                return ObjectWriterImplDoubleValueArray.INSTANCE;
            }

            if (clazz == Byte[].class) {
                return ObjectWriterImplInt8Array.INSTANCE;
            }

            if (clazz == Integer[].class) {
                return ObjectWriterImplInt32Array.INSTANCE;
            }

            if (clazz == Long[].class) {
                return ObjectWriterImplInt64Array.INSTANCE;
            }

            if (clazz == AtomicLongArray.class) {
                return ObjectWriterImplAtomicLongArray.INSTANCE;
            }

            if (String[].class == clazz) {
                return ObjectWriterImplStringArray.INSTANCE;
            }

            if (Object[].class.isAssignableFrom(clazz)) {
                if (clazz == Object[].class) {
                    return ObjectWriterArray.INSTANCE;
                } else {
                    return new ObjectWriterArray(clazz.getComponentType());
                }
            }

            if (clazz == UUID.class) {
                return ObjectWriterImplUUID.INSTANCE;
            }

            if (clazz == Locale.class) {
                return ObjectWriterImplLocale.INSTANCE;
            }

            if (clazz == Currency.class) {
                return ObjectWriterImplCurrency.INSTANCE;
            }

            if (TimeZone.class.isAssignableFrom(clazz)) {
                return ObjectWriterImplTimeZone.INSTANCE;
            }

            if (clazz == URI.class
                    || clazz == URL.class
                    || clazz == File.class
                    || ZoneId.class.isAssignableFrom(clazz)
                    || Charset.class.isAssignableFrom(clazz)) {
                return ObjectWriterImplToString.INSTANCE;
            }

            externalObjectWriter = getExternalObjectWriter(clazz.getName(), clazz);
            if (externalObjectWriter != null) {
                return externalObjectWriter;
            }

            BeanInfo beanInfo = new BeanInfo();
            Class mixIn = provider.getMixIn(clazz);
            if (mixIn != null) {
                annotationProcessor.getBeanInfo(beanInfo, mixIn);
            }

            if (Date.class.isAssignableFrom(clazz)) {
                if (beanInfo.format != null || beanInfo.locale != null) {
                    return new ObjectWriterImplDate(beanInfo.format, beanInfo.locale);
                }

                return ObjectWriterImplDate.INSTANCE;
            }

            if (Calendar.class.isAssignableFrom(clazz)) {
                if (beanInfo.format != null || beanInfo.locale != null) {
                    return new ObjectWriterImplCalendar(beanInfo.format, beanInfo.locale);
                }

                return ObjectWriterImplCalendar.INSTANCE;
            }

            if (ZonedDateTime.class == clazz) {
                if (beanInfo.format != null || beanInfo.locale != null) {
                    return new ObjectWriterImplZonedDateTime(beanInfo.format, beanInfo.locale);
                }

                return ObjectWriterImplZonedDateTime.INSTANCE;
            }

            if (OffsetDateTime.class == clazz) {
                if (beanInfo.format != null || beanInfo.locale != null) {
                    return new ObjectWriterImplOffsetDateTime(beanInfo.format, beanInfo.locale);
                }

                return ObjectWriterImplOffsetDateTime.INSTANCE;
            }

            if (LocalDateTime.class == clazz) {
                if (beanInfo.format != null || beanInfo.locale != null) {
                    return new ObjectWriterImplLocalDateTime(beanInfo.format, beanInfo.locale);
                }

                return ObjectWriterImplLocalDateTime.INSTANCE;
            }

            if (LocalDate.class == clazz) {
                if (beanInfo.format != null || beanInfo.locale != null) {
                    return new ObjectWriterImplLocalDate(beanInfo.format, beanInfo.locale);
                }

                return ObjectWriterImplLocalDate.INSTANCE;
            }

            if (LocalTime.class == clazz) {
                if (beanInfo.format != null || beanInfo.locale != null) {
                    return new ObjectWriterImplLocalTime(beanInfo.format, beanInfo.locale);
                }

                return ObjectWriterImplLocalTime.INSTANCE;
            }

            if (OffsetTime.class == clazz) {
                if (beanInfo.format != null || beanInfo.locale != null) {
                    return new ObjectWriterImplOffsetTime(beanInfo.format, beanInfo.locale);
                }

                return ObjectWriterImplOffsetTime.INSTANCE;
            }

            if (Instant.class == clazz) {
                if (beanInfo.format != null || beanInfo.locale != null) {
                    return new ObjectWriterImplInstant(beanInfo.format, beanInfo.locale);
                }

                return ObjectWriterImplInstant.INSTANCE;
            }

            if (StackTraceElement.class == clazz) {
                if (STACK_TRACE_ELEMENT_WRITER == null) {
                    STACK_TRACE_ELEMENT_WRITER = new ObjectWriterAdapter(StackTraceElement.class, Arrays.asList(
                            new FieldWriter[]{
                                    ObjectWriters.fieldWriter("fileName", String.class, StackTraceElement::getFileName),
                                    ObjectWriters.fieldWriter("lineNumber", StackTraceElement::getLineNumber),
                                    ObjectWriters.fieldWriter("className", String.class, StackTraceElement::getClassName),
                                    ObjectWriters.fieldWriter("methodName", String.class, StackTraceElement::getMethodName),
                            }
                    ));
                }
                return STACK_TRACE_ELEMENT_WRITER;
            }

            if (Class.class == clazz) {
                return ObjectWriterImplClass.INSTANCE;
            }

            if (Method.class == clazz) {
                return new ObjectWriterAdapter<>(
                        Method.class,
                        Arrays.asList(
                                ObjectWriters.fieldWriter("declaringClass", Class.class, Method::getDeclaringClass),
                                ObjectWriters.fieldWriter("name", String.class, Method::getName),
                                ObjectWriters.fieldWriter("parameterTypes", Class[].class, Method::getParameterTypes)
                        )
                );
            }

            if (Field.class == clazz) {
                return new ObjectWriterAdapter<>(
                        Method.class,
                        Arrays.asList(
                                ObjectWriters.fieldWriter("declaringClass", Class.class, Field::getDeclaringClass),
                                ObjectWriters.fieldWriter("name", String.class, Field::getName)
                        )
                );
            }

            if (ParameterizedType.class.isAssignableFrom(clazz)) {
                return ObjectWriters.objectWriter(
                        ParameterizedType.class,
                        ObjectWriters.fieldWriter("actualTypeArguments", Type[].class, ParameterizedType::getActualTypeArguments),
                        ObjectWriters.fieldWriter("ownerType", Type.class, ParameterizedType::getOwnerType),
                        ObjectWriters.fieldWriter("rawType", Type.class, ParameterizedType::getRawType)
                );
            }
        }

        return null;
    }

    private ObjectWriter createEnumWriter(Class enumClass) {
        if (!enumClass.isEnum()) {
            Class superclass = enumClass.getSuperclass();
            if (superclass.isEnum()) {
                enumClass = superclass;
            }
        }

        Member valueField = BeanUtils.getEnumValueField(enumClass, provider);
        if (valueField == null) {
            Class mixInSource = provider.mixInCache.get(enumClass);
            Member mixedValueField = BeanUtils.getEnumValueField(mixInSource, provider);
            if (mixedValueField instanceof Field) {
                try {
                    valueField = enumClass.getField(mixedValueField.getName());
                } catch (NoSuchFieldException ignored) {
                }
            } else if (mixedValueField instanceof Method) {
                try {
                    valueField = enumClass.getMethod(mixedValueField.getName());
                } catch (NoSuchMethodException ignored) {
                }
            }
        }

        BeanInfo beanInfo = new BeanInfo();
        annotationProcessor.getBeanInfo(beanInfo, enumClass);
        if (beanInfo.writeEnumAsJavaBean) {
            return null;
        }

        String[] annotationNames = BeanUtils.getEnumAnnotationNames(enumClass);
        return new ObjectWriterImplEnum(null, enumClass, valueField, annotationNames, 0);
    }

    abstract static class PrimitiveImpl<T>
            implements ObjectWriter<T> {
        @Override
        public void writeArrayMappingJSONB(JSONWriter jsonWriter,
                                           Object object,
                                           Object fieldName,
                                           Type fieldType,
                                           long features) {
            writeJSONB(jsonWriter, object, null, null, 0);
        }

        @Override
        public void writeArrayMapping(JSONWriter jsonWriter,
                                      Object object,
                                      Object fieldName,
                                      Type fieldType,
                                      long features) {
            write(jsonWriter, object, null, null, 0);
        }
    }

    static class VoidObjectWriter
            implements ObjectWriter {
        public static final VoidObjectWriter INSTANCE = new VoidObjectWriter();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        }
    }
}
