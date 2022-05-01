package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.modules.ObjectWriterAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.support.money.MoneySupport;
import com.alibaba.fastjson2.util.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

class ObjectWriterBaseModule implements ObjectWriterModule {
    final ObjectWriterProvider provider;
    final WriterAnnotationProcessor annotationProcessor;

    public ObjectWriterBaseModule(ObjectWriterProvider provider) {
        this.provider = provider;
        this.annotationProcessor = new WriterAnnotationProcessor();
    }

    @Override
    public ObjectWriterAnnotationProcessor getAnnotationProcessor() {
        return annotationProcessor;
    }

    class WriterAnnotationProcessor implements ObjectWriterAnnotationProcessor {
        @Override
        public void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
            Class superclass = objectClass.getSuperclass();
            if (superclass != Object.class && superclass != null) {
                getBeanInfo(beanInfo, superclass);
            }

            Annotation jsonType1x = null;
            JSONType jsonType = null;
            Annotation[] annotations = objectClass.getAnnotations();
            for (Annotation annotation : annotations) {
                Class annotationType = annotation.annotationType();
                if (annotationType == JSONType.class) {
                    jsonType = (JSONType) annotation;
                    continue;
                }

                switch (annotationType.getName()) {
                    case "com.alibaba.fastjson.annotation.JSONType":
                        jsonType1x = annotation;
                        break;
                    default:
                        break;
                }
            }

            if (jsonType == null) {
                Class mixInSource = provider.mixInCache.get(objectClass);
                if (mixInSource == null) {
                    String typeName = objectClass.getName();
                    switch (typeName) {
                        case "org.apache.commons.lang3.tuple.ImmutablePair":
                            provider.mixIn(objectClass, mixInSource = ApacheLang3Support.PairMixIn.class);
                            break;
                        case "org.apache.commons.lang3.tuple.MutablePair":
                            provider.mixIn(objectClass, mixInSource = ApacheLang3Support.MutablePairMixIn.class);
                            break;
                        default:
                            break;
                    }
                }

                if (mixInSource != null) {
                    beanInfo.mixIn = true;

                    Annotation[] mixInAnnotations = mixInSource.getAnnotations();
                    for (Annotation annotation : mixInAnnotations) {
                        Class<? extends Annotation> annotationType = annotation.annotationType();
                        if (annotationType == JSONType.class) {
                            jsonType = (JSONType) annotation;
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

                for (JSONWriter.Feature feature : jsonType.serializeFeatures1()) {
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
            } else if (jsonType1x != null) {
                final Annotation annotation = jsonType1x;
                BeanUtils.annotationMethods(jsonType1x.annotationType(), method -> processJSONType1x(beanInfo, annotation, method));
            }

            if (beanInfo.seeAlso != null && beanInfo.seeAlso.length != 0) {
                for (Class seeAlsoClass : beanInfo.seeAlso) {
                    if (seeAlsoClass == objectClass) {
                        beanInfo.typeName = objectClass.getSimpleName();
                    }
                }
            }
        }

        @Override
        public void getFieldInfo(FieldInfo fieldInfo, Class objectType, Field field) {
            Class mixInSource = provider.mixInCache.get(objectType);
            if (objectType == null) {
                String typeName = objectType.getName();
                switch (typeName) {
                    case "org.apache.commons.lang3.tuple.ImmutablePair":
                        provider.mixIn(objectType, mixInSource = ApacheLang3Support.PairMixIn.class);
                        break;
                    default:
                        break;
                }
            }

            if (mixInSource != null && mixInSource != objectType) {
                Field mixInField = null;
                try {
                    mixInField = mixInSource.getDeclaredField(field.getName());
                } catch (Exception ignored) {
                }

                if (mixInField != null) {
                    getFieldInfo(fieldInfo, mixInSource, mixInField);
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
            Annotation[] annotations = field.getAnnotations();
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
                    case "com.alibaba.fastjson.annotation.JSONField":
                        processJSONField1x(fieldInfo, annotation);
                        break;
                    default:
                        break;
                }
            }

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

                if (!jsonFieldFormat.isEmpty()) {
                    fieldInfo.format = jsonFieldFormat;
                }
            }

            if (!fieldInfo.ignore) {
                fieldInfo.ignore = !jsonField.serialize();
            }

            if (jsonField.unwrapped()) {
                fieldInfo.format = "unwrapped";
            }

            for (JSONWriter.Feature feature : jsonField.serializeFeatures()) {
                fieldInfo.features |= feature.mask;
            }

            int ordinal = jsonField.ordinal();
            if (ordinal != 0) {
                fieldInfo.ordinal = ordinal;
            }

            Class writeUsing = jsonField.writeUsing();
            if (writeUsing != void.class
                    && writeUsing != Void.class
                    && ObjectWriter.class.isAssignableFrom(writeUsing)
            ) {
                fieldInfo.writeUsing = writeUsing;
            }
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

                                if (!format.isEmpty()) {
                                    fieldInfo.format = format;
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
                                fieldInfo.format = "unwrapped";
                            }
                            break;
                        }
                        case "serialzeFeatures": {
                            Enum[] features = (Enum[]) result;
                            applyFeatures(fieldInfo, features);
                            break;
                        }
                        default:
                            break;
                    }
                } catch (Throwable ignored) {

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
                    case "WriteNullStringAsEmpty":
                        fieldInfo.features |= JSONWriter.Feature.NullAsDefaultValue.mask;
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

            Class fieldClassMixInSource = provider.mixInCache.get(method.getReturnType());
            if (fieldClassMixInSource != null) {
                fieldInfo.fieldClassMixIn = true;
            }

            if (JDKUtils.CLASS_TRANSIENT != null && method.getAnnotation(JDKUtils.CLASS_TRANSIENT) != null) {
                fieldInfo.ignore = true;
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
                    case "com.alibaba.fastjson.annotation.JSONField":
                        processJSONField1x(fieldInfo, annotation);
                        break;
                    case "java.beans.Transient":
                        fieldInfo.isTransient = true;
                        break;
                    default:
                        break;
                }
            }

            if (jsonField != null) {
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

                    if (!jsonFieldFormat.isEmpty()) {
                        fieldInfo.format = jsonFieldFormat;
                    }
                }

                if (!fieldInfo.ignore) {
                    fieldInfo.ignore = !jsonField.serialize();
                }

                if (jsonField.unwrapped()) {
                    fieldInfo.format = "unwrapped";
                }

                for (JSONWriter.Feature feature : jsonField.serializeFeatures()) {
                    fieldInfo.features |= feature.mask;
                }

                int ordinal = jsonField.ordinal();
                if (ordinal != 0) {
                    fieldInfo.ordinal = ordinal;
                }
            }

            if (!objectClass.getName().startsWith("java.lang")) {
                String fieldName = BeanUtils.getterName(methodName, null);

                BeanUtils.declaredFields(objectClass, field -> {
                    if (field.getName().equalsIgnoreCase(fieldName)) {
                        int modifiers = field.getModifiers();
                        if ((!Modifier.isPublic(modifiers)) && !Modifier.isStatic(modifiers)) {
                            getFieldInfo(fieldInfo, objectClass, field);
                        }
                    }
                });
            }
        }
    }

    private void processJSONType1x(BeanInfo beanInfo, Annotation jsonType1x, Method method) {
        try {
            Object result = method.invoke(jsonType1x);
            switch (method.getName()) {
                case "seeAlso": {
                    Class<?>[] classes = (Class[]) result;
                    if (classes.length != 0) {
                        beanInfo.seeAlso = classes;
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
                case "typeKey": {
                    String typeKey = (String) result;
                    if (!typeKey.isEmpty()) {
                        beanInfo.typeKey = typeKey;
                    }
                    break;
                }
                case "serializeFeatures":
                case "serialzeFeatures": {
                    Enum[] serializeFeatures = (Enum[]) result;
                    for (Enum feature : serializeFeatures) {
                        switch (feature.name()) {
                            case "WriteMapNullValue":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteNulls.mask;
                                break;
                            case "WriteNullListAsEmpty":
                            case "WriteNullStringAsEmpty":
                                beanInfo.writerFeatures |= JSONWriter.Feature.NullAsDefaultValue.mask;
                                break;
                            case "BrowserCompatible":
                                beanInfo.writerFeatures |= JSONWriter.Feature.BrowserCompatible.mask;
                                break;
                            case "WriteClassName":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteClassName.mask;
                                break;
                            case "WriteNonStringValueAsString":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteNonStringValueAsString.mask;
                                break;
                            case "WriteEnumUsingToString":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteEnumUsingToString.mask;
                                break;
                            case "NotWriteRootClassName":
                                beanInfo.writerFeatures |= JSONWriter.Feature.NotWriteRootClassName.mask;
                                break;
                            case "IgnoreErrorGetter":
                                beanInfo.writerFeatures |= JSONWriter.Feature.IgnoreErrorGetter.mask;
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                }
                case "serializeEnumAsJavaBean": {
                    boolean serializeEnumAsJavaBean = (Boolean) result;
                    if (serializeEnumAsJavaBean) {
                        beanInfo.writeEnumAsJavaBean = true;
                    }
                    break;
                }
                case "naming": {
                    Enum naming = (Enum) result;
                    beanInfo.namingStrategy = naming.name();
                    break;
                }
                case "ignores": {
                    String[] fields = (String[]) result;
                    if (fields.length != 0) {
                        beanInfo.ignores = fields;
                    }
                    break;
                }
                case "includes": {
                    String[] fields = (String[]) result;
                    if (fields.length != 0) {
                        beanInfo.includes = fields;
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
                default:
                    break;
            }
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
    }

    ObjectWriter getExternalObjectWriter(String className, Class objectClass) {
        switch (className) {
            case "java.sql.Time":
                return JdbcSupport.createTimeWriter(null);
            case "java.sql.Timestamp":
                return JdbcSupport.createTimestampWriter(null);
            case "org.joda.time.chrono.GregorianChronology":
                return JodaSupport.createGregorianChronologyWriter(objectClass);
            case "org.joda.time.chrono.ISOChronology":
                return JodaSupport.createISOChronologyWriter(objectClass);
            case "org.joda.time.LocalDate":
                return JodaSupport.createLocalDateWriter(objectClass);
            case "org.joda.time.LocalDateTime":
                return JodaSupport.createLocalDateTimeWriter(objectClass);
//            case "com.alibaba.fastjson.JSONObject":
//                return Fastjson1xSupport.createObjectReader();
            default:
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
            case "org.javamoney.moneta.Money":
                return MoneySupport.createMonetaryAmountWriter();
            case "org.javamoney.moneta.spi.DefaultNumberValue":
                return MoneySupport.createNumberValueWriter();
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

        if (Iterable.class.isAssignableFrom(objectClass)) {
            return ObjectWriterImplIterable.INSTANCE;
        }

        if (Map.class.isAssignableFrom(objectClass)) {
            return ObjectWriterImplMap.of(objectClass);
        }

        if (Map.Entry.class.isAssignableFrom(objectClass)) {
            String objectClassName = objectClass.getName();
            if (!objectClassName.equals("org.apache.commons.lang3.tuple.ImmutablePair") && !objectClassName.equals("org.apache.commons.lang3.tuple.MutablePair")
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

        if (objectType == OptionalInt.class) {
            return ObjectWriterImplOptionalInt.INSTANCE;
        }

        if (objectType == OptionalLong.class) {
            return ObjectWriterImplOptionalLong.INSTANCE;
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

            if (clazz.isEnum()) {
                Member valueField = BeanUtils.getEnumValueField(clazz);
                if (valueField == null) {
                    Class mixInSource = provider.mixInCache.get(objectClass);
                    Member mixedValueField = BeanUtils.getEnumValueField(mixInSource);
                    if (mixedValueField instanceof Field) {
                        try {
                            valueField = clazz.getField(mixedValueField.getName());
                        } catch (NoSuchFieldException ignored) {

                        }
                    } else if (mixedValueField instanceof Method) {
                        try {
                            valueField = clazz.getMethod(mixedValueField.getName());
                        } catch (NoSuchMethodException ignored) {

                        }
                    }
                }

                return new ObjectWriterImplEnum(null, clazz, valueField, 0);
            }

            if (TimeUnit.class.isAssignableFrom(clazz)) {
                return new ObjectWriterImplEnum(null, TimeUnit.class, null, 0);
            }

            if (clazz == boolean[].class) {
                return ObjectWriterImplBoolValueArray.INSTANCE;
            }

            if (clazz == char[].class) {
                return ObjectWriterImplCharValueArray.INSTANCE;
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
                    || ZoneId.class.isAssignableFrom(clazz)
                    || Charset.class.isAssignableFrom(clazz)) {

                return ObjectWriterImplToString.INSTANCE;
            }

            if (clazz == Inet4Address.class || clazz == Inet4Address.class) {
                return ObjectWriterImplInetAddress.INSTANCE;
            }

            externalObjectWriter = getExternalObjectWriter(clazz.getName(), clazz);
            if (externalObjectWriter != null) {
                return externalObjectWriter;
            }

            if (Date.class.isAssignableFrom(clazz)) {
                return ObjectWriterImplDate.INSTANCE;
            }

            if (Calendar.class.isAssignableFrom(clazz)) {
                return ObjectWriterImplCalendar.INSTANCE;
            }

            if (ZonedDateTime.class == clazz) {
                return ObjectWriterImplZonedDateTime.INSTANCE;
            }

            if (OffsetDateTime.class == clazz) {
                return ObjectWriterImplOffsetDateTime.INSTANCE;
            }

            if (LocalDateTime.class == clazz) {
                return ObjectWriterImplLocalDateTime.INSTANCE;
            }

            if (LocalDate.class == clazz) {
                return ObjectWriterImplLocalDate.INSTANCE;
            }

            if (LocalTime.class == clazz) {
                return ObjectWriterImplLocalTime.INSTANCE;
            }

            if (OffsetTime.class == clazz) {
                return ObjectWriterImplOffsetTime.INSTANCE;
            }

            if (Instant.class == clazz) {
                return ObjectWriterImplInstant.INSTANCE;
            }

            if (StackTraceElement.class == clazz) {
                return ObjectWriterImplStackTraceElement.INSTANCE;
            }

            if (Class.class == clazz) {
                return ObjectWriterImplClass.INSTANCE;
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

    static abstract class PrimitiveImpl<T> implements ObjectWriter<T> {
        @Override
        public void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            writeJSONB(jsonWriter, object, null, null, 0);
        }

        @Override
        public void writeArrayMapping(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            write(jsonWriter, object, null, null, 0);
        }
    }

    static class VoidObjectWriter implements ObjectWriter {
        public final static VoidObjectWriter INSTANCE = new VoidObjectWriter();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {

        }
    }

}
