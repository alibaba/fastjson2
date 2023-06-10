package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONPObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.*;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.time.ZoneId;
import com.alibaba.fastjson2.util.*;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

import static com.alibaba.fastjson2.util.BeanUtils.*;

public class ObjectWriterModule {
    static ObjectWriterAdapter STACK_TRACE_ELEMENT_WRITER;

    final ObjectWriterProvider provider;

    public ObjectWriterModule(ObjectWriterProvider provider) {
        this.provider = provider;
    }

    public void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
        if (objectClass != null) {
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
        }

        Annotation jsonType1x = null;
        JSONType jsonType = null;
        Annotation[] annotations = objectClass.getDeclaredAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            Class annotationType = annotation.annotationType();
            jsonType = findAnnotation(annotation, JSONType.class);
            if (jsonType == annotation) {
                continue;
            }

            switch (annotationType.getName()) {
                case "com.alibaba.fastjson.annotation.JSONType":
                    jsonType1x = annotation;
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

                Annotation[] mixInAnnotations = mixInSource.getDeclaredAnnotations();
                for (int i = 0; i < mixInAnnotations.length; i++) {
                    Annotation annotation = mixInAnnotations[i];
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    jsonType = findAnnotation(annotation, JSONType.class);
                    if (jsonType == annotation) {
                        continue;
                    }

                    String annotationTypeName = annotationType.getName();
                    if (annotationTypeName.equals("com.alibaba.fastjson.annotation.JSONType")) {
                        jsonType1x = annotation;
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

            JSONWriter.Feature[] serializeFeatures = jsonType.serializeFeatures();
            for (int i = 0; i < serializeFeatures.length; i++) {
                JSONWriter.Feature feature = serializeFeatures[i];
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

    public void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectClass, Field field) {
        if (objectClass != null) {
            Class mixInSource = provider.mixInCache.get(objectClass);

            if (mixInSource != null && mixInSource != objectClass) {
                Field mixInField = null;
                try {
                    mixInField = mixInSource.getDeclaredField(field.getName());
                } catch (Exception ignored) {
                }

                if (mixInField != null) {
                    getFieldInfo(beanInfo, fieldInfo, mixInSource, mixInField);
                }
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
        Annotation[] annotations = field.getDeclaredAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (jsonField == null) {
                jsonField = findAnnotation(annotation, JSONField.class);
                if (jsonField == annotation) {
                    continue;
                }
            }

            String annotationTypeName = annotationType.getName();
            if (annotationTypeName.equals("com.alibaba.fastjson.annotation.JSONField")) {
                processJSONField1x(fieldInfo, annotation);
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

        if ((fieldInfo.features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0
                && !String.class.equals(field.getType())
                && fieldInfo.writeUsing == null
        ) {
            fieldInfo.writeUsing = ObjectWriterImplToString.class;
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
        for (int i = 0; i < features.length; i++) {
            Enum feature = features[i];
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

        if (objectClass != null) {
            Class superclass = objectClass.getSuperclass();
            Method supperMethod = BeanUtils.getMethod(superclass, method);
            if (supperMethod != null) {
                getFieldInfo(beanInfo, fieldInfo, superclass, supperMethod);
            }

            Class[] interfaces = objectClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                Method interfaceMethod = BeanUtils.getMethod(interfaces[i], method);
                if (interfaceMethod != null) {
                    getFieldInfo(beanInfo, fieldInfo, superclass, interfaceMethod);
                }
            }
        }

        Annotation[] annotations = method.getDeclaredAnnotations();
        processAnnotations(fieldInfo, annotations);

        if (!objectClass.getName().startsWith("java.lang", 0)) {
            Field methodField = getField(objectClass, method);
            if (methodField != null) {
                fieldInfo.features |= FieldInfo.FIELD_MASK;
                getFieldInfo(beanInfo, fieldInfo, objectClass, methodField);
            }
        }

        if (beanInfo.kotlin
                && beanInfo.creatorConstructor != null
                && beanInfo.createParameterNames != null
        ) {
            String fieldName = BeanUtils.getterName(method, null);
            for (int i = 0; i < beanInfo.createParameterNames.length; i++) {
                if (fieldName.equals(beanInfo.createParameterNames[i])) {
                    Annotation[][] creatorConsParamAnnotations
                            = beanInfo.creatorConstructor.getParameterAnnotations();
                    if (i < creatorConsParamAnnotations.length) {
                        Annotation[] parameterAnnotations = creatorConsParamAnnotations[i];
                        processAnnotations(fieldInfo, parameterAnnotations);
                        break;
                    }
                }
            }
        }
    }

    private void processAnnotations(FieldInfo fieldInfo, Annotation[] annotations) {
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            Class<? extends Annotation> annotationType = annotation.annotationType();
            JSONField jsonField = findAnnotation(annotation, JSONField.class);
            if (jsonField != null) {
                loadFieldInfo(fieldInfo, jsonField);
                continue;
            }

            String annotationTypeName = annotationType.getName();
            switch (annotationTypeName) {
                case "com.alibaba.fastjson.annotation.JSONField":
                    processJSONField1x(fieldInfo, annotation);
                    break;
                case "java.beans.Transient":
                    fieldInfo.ignore = true;
                    fieldInfo.isTransient = true;
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

        JSONWriter.Feature[] serializeFeatures = jsonField.serializeFeatures();
        for (int i = 0; i < serializeFeatures.length; i++) {
            JSONWriter.Feature feature = serializeFeatures[i];
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

        Class serializeUsing = jsonField.serializeUsing();
        if (ObjectWriter.class.isAssignableFrom(serializeUsing)) {
            fieldInfo.writeUsing = serializeUsing;
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

    ObjectWriter getExternalObjectWriter(String className, Class objectClass) {
        switch (className) {
            case "java.sql.Time":
                return JdbcSupport.TimeWriter.of(null);
            case "java.sql.Timestamp":
                return new JdbcSupport.TimestampWriter(null);
            default:
                if (java.sql.Clob.class.isAssignableFrom(objectClass)) {
                    return new JdbcSupport.ClobWriter();
                }
                return null;
        }
    }

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
            case "net.sf.json.JSONNull":
            case "java.net.Inet4Address":
            case "java.net.Inet6Address":
            case "java.net.InetSocketAddress":
            case "java.text.SimpleDateFormat":
            case "java.util.regex.Pattern":
                return ObjectWriterMisc.INSTANCE;
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

            if (String[].class == clazz) {
                return ObjectWriterImplStringArray.INSTANCE;
            }

            if (BigDecimal[].class == clazz) {
                return ObjectWriterImpDecimalArray.INSTANCE;
            }

            if (Object[].class.isAssignableFrom(clazz)) {
                if (clazz == Object[].class) {
                    return ObjectWriterArray.INSTANCE;
                } else {
                    Class componentType = clazz.getComponentType();
                    if (Modifier.isFinal(componentType.getModifiers())) {
                        return new ObjectWriterArrayFinal(componentType, null);
                    } else {
                        return new ObjectWriterArray(componentType);
                    }
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

            if (JSONPObject.class.isAssignableFrom(clazz)) {
                return new ObjectWriterImplJSONP();
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
                getBeanInfo(beanInfo, mixIn);
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

            if (StackTraceElement.class == clazz) {
                if (STACK_TRACE_ELEMENT_WRITER == null) {
                    STACK_TRACE_ELEMENT_WRITER = new ObjectWriterAdapter(
                            StackTraceElement.class,
                            null,
                            null,
                            0,
                            Arrays.asList(
                                    ObjectWriters.fieldWriter("fileName", String.class, StackTraceElement::getFileName),
                                    ObjectWriters.fieldWriter("lineNumber", StackTraceElement::getLineNumber),
                                    ObjectWriters.fieldWriter("className", String.class, StackTraceElement::getClassName),
                                    ObjectWriters.fieldWriter("methodName", String.class, StackTraceElement::getMethodName)));
                }
                return STACK_TRACE_ELEMENT_WRITER;
            }

            if (Class.class == clazz) {
                return ObjectWriterImplClass.INSTANCE;
            }

            if (Method.class == clazz) {
                return new ObjectWriterAdapter<>(
                        Method.class,
                        null,
                        null,
                        0,
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
                        null,
                        null,
                        0,
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
        getBeanInfo(beanInfo, enumClass);
        if (beanInfo.writeEnumAsJavaBean) {
            return null;
        }

        String[] annotationNames = BeanUtils.getEnumAnnotationNames(enumClass);
        return new ObjectWriterImplEnum(null, enumClass, valueField, annotationNames, 0);
    }

    static class VoidObjectWriter
            implements ObjectWriter {
        public static final VoidObjectWriter INSTANCE = new VoidObjectWriter();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        }
    }
}
