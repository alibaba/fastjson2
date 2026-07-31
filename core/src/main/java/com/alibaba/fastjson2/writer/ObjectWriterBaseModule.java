package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.*;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.modules.ObjectWriterAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.util.*;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import static com.alibaba.fastjson2.util.BeanUtils.*;

public class ObjectWriterBaseModule
        implements ObjectWriterModule {
    static ObjectWriter STACK_TRACE_ELEMENT_WRITER;

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
            if (objectClass != null) {
                Class superclass = objectClass.getSuperclass();
                if (superclass != Object.class && superclass != null && superclass != Enum.class) {
                    getBeanInfo(beanInfo, superclass);
                }

                Class[] interfaces = objectClass.getInterfaces();
                for (Class item : interfaces) {
                    if (item == Serializable.class) {
                        continue;
                    }
                    getBeanInfo(beanInfo, item);
                }

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
            for (int i = 0; i < annotations.length; i++) {
                Annotation annotation = annotations[i];
                Class annotationType = annotation.annotationType();
                if (jsonType == null) {
                    jsonType = findAnnotation(annotation, JSONType.class);
                }
                if (jsonType == annotation) {
                    continue;
                }

                if (annotationType == JSONCompiler.class) {
                    JSONCompiler compiler = (JSONCompiler) annotation;
                    if (compiler.value() == JSONCompiler.CompilerOption.LAMBDA) {
                        beanInfo.writerFeatures |= FieldInfo.JIT;
                    }
                }

                boolean useJacksonAnnotation = JSONFactory.isUseJacksonAnnotation();
                switch (annotationType.getName()) {
                    case "com.alibaba.fastjson.annotation.JSONType":
                        jsonType1x = annotation;
                        break;
                    case "com.fasterxml.jackson.annotation.JsonIgnoreProperties":
                        if (useJacksonAnnotation) {
                            processJacksonJsonIgnoreProperties(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonPropertyOrder":
                        if (useJacksonAnnotation) {
                            processJacksonJsonPropertyOrder(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonFormat":
                        if (useJacksonAnnotation) {
                            processJacksonJsonFormat(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonInclude":
                        if (useJacksonAnnotation) {
                            processJacksonJsonInclude(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonTypeInfo":
                        if (useJacksonAnnotation) {
                            processJacksonJsonTypeInfo(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.databind.annotation.JsonSerialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonSerialize(beanInfo, annotation);
                            if (beanInfo.serializer != null && Enum.class.isAssignableFrom(objectClass)) {
                                beanInfo.writeEnumAsJavaBean = true;
                            }
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonTypeName":
                        if (useJacksonAnnotation) {
                            processJacksonJsonTypeName(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonSubTypes":
                        if (useJacksonAnnotation) {
                            processJacksonJsonSubTypes(beanInfo, annotation);
                        }
                        break;
                    case "kotlin.Metadata":
                        beanInfo.kotlin = true;
                        // Kotlin support removed
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
                    for (int i = 0; i < mixInAnnotations.length; i++) {
                        Annotation annotation = mixInAnnotations[i];
                        Class<? extends Annotation> annotationType = annotation.annotationType();
                        jsonType = findAnnotation(annotation, JSONType.class);
                        if (jsonType == annotation) {
                            continue;
                        }

                        String annotationTypeName = annotationType.getName();
                        if ("com.alibaba.fastjson.annotation.JSONType".equals(annotationTypeName)) {
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
                    beanInfo.writeEnumAsJavaBean = true;
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

                String rootName = jsonType.rootName();
                if (!rootName.isEmpty()) {
                    beanInfo.rootName = rootName;
                }

                if (beanInfo.skipTransient) {
                    beanInfo.skipTransient = jsonType.skipTransient();
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
                fieldInfo.isTransient = true;
                if (fieldInfo.skipTransient && beanInfo.skipTransient) {
                    fieldInfo.ignore = true;
                }
            }

            JSONField jsonField = null;
            Annotation[] annotations = getAnnotations(field);
            if (annotations.length == 0 && false) {
                if (fieldInfo.ignore) {
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType() == JSONField.class) {
                            fieldInfo.ignore = !((JSONField) annotation).serialize();
                        }
                    }
                }
            }
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (jsonField == null) {
                    jsonField = findAnnotation(annotation, JSONField.class);
                    if (jsonField == annotation) {
                        continue;
                    }
                }

                String annotationTypeName = annotationType.getName();
                boolean useJacksonAnnotation = JSONFactory.isUseJacksonAnnotation();
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
                    case "com.fasterxml.jackson.annotation.JsonValue":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.VALUE_MASK;
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonRawValue":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.RAW_VALUE_MASK;
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
                    case "com.fasterxml.jackson.annotation.JsonInclude":
                        if (useJacksonAnnotation) {
                            processJacksonJsonInclude(beanInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.databind.annotation.JsonSerialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonSerialize(fieldInfo, annotation);
                        }
                        break;
                    case "com.google.gson.annotations.SerializedName":
                        if (JSONFactory.isUseGsonAnnotation()) {
                            processGsonSerializedName(fieldInfo, annotation);
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
                fieldInfo.writeUsing = null;
            }
        }

        private void processJacksonJsonSubTypes(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    if ("value".equals(name)) {
                        Annotation[] value = (Annotation[]) result;
                        if (value.length != 0) {
                            beanInfo.seeAlso = new Class[value.length];
                            beanInfo.seeAlsoNames = new String[value.length];
                            for (int i = 0; i < value.length; i++) {
                                Annotation item = value[i];
                                processJacksonJsonSubTypesType(beanInfo, i, item);
                            }
                        }
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
            String usingName = result.getName();
            String noneClassName1 = "com.fasterxml.jackson.databind.JsonSerializer$None";
            if (!noneClassName1.equals(usingName)
                    && ObjectWriter.class.isAssignableFrom(result)
            ) {
                return result;
            }

            if ("com.fasterxml.jackson.databind.ser.std.ToStringSerializer".equals(usingName)) {
                return null;
            }
            return null;
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
                            beanInfo.writerFeatures |= JSONWriter.Feature.WriteClassName.mask;
                        }
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
        }

        private void processJacksonJsonPropertyOrder(BeanInfo beanInfo, Annotation annotation) {
            Class<? extends Annotation> annotationClass = annotation.getClass();
            final AtomicBoolean alphabetic = new AtomicBoolean(false);
            BeanUtils.annotationMethods(annotationClass, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    if ("value".equals(name)) {
                        String[] value = (String[]) result;
                        if (value.length != 0) {
                            beanInfo.orders = value;
                        }
                    } else if ("alphabetic".equals(name)) {
                        alphabetic.set((Boolean) result);
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
            if (beanInfo.orders == null || beanInfo.orders.length == 0) {
                beanInfo.alphabetic = alphabetic.get();
            }
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
                        case "contentAs":
                            Class<?> contentAs = (Class) result;
                            if (contentAs != Void.class) {
                                fieldInfo.contentAs = contentAs;
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
                            if (!value.isEmpty()
                                    && (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty())
                            ) {
                                fieldInfo.fieldName = value;
                            }
                            break;
                        case "access": {
                            String access = ((Enum) result).name();
                            fieldInfo.ignore = "WRITE_ONLY".equals(access);
                            break;
                        }
                        case "index": {
                            int index = (Integer) result;
                            if (index != -1) {
                                fieldInfo.ordinal = index;
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
                    if ("value".equals(name)) {
                        String[] value = (String[]) result;
                        if (value.length != 0) {
                            beanInfo.ignores = value;
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
                            int ordinal = (Integer) result;
                            if (ordinal != 0) {
                                fieldInfo.ordinal = ordinal;
                            }
                            break;
                        }
                        case "serialize": {
                            boolean serialize = (Boolean) result;
                            if (!serialize) {
                                fieldInfo.ignore = true;
                            }
                            break;
                        }
                        case "unwrapped": {
                            if ((Boolean) result) {
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
                    case "DisableCircularReferenceDetect":
                        fieldInfo.features |= FieldInfo.DISABLE_REFERENCE_DETECT;
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

            if ("getTargetSql".equals(methodName)) {
                if (objectClass != null
                        && objectClass.getName().startsWith("com.baomidou.mybatisplus.")
                ) {
                    fieldInfo.features |= JSONWriter.Feature.IgnoreErrorGetter.mask;
                }
            }

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
                fieldInfo.isTransient = true;
                if (!beanInfo.skipTransient) {
                    fieldInfo.skipTransient = false;
                    fieldInfo.ignore = false;
                }
            }

            if (objectClass != null) {
                Class superclass = objectClass.getSuperclass();
                Method supperMethod = BeanUtils.getMethod(superclass, method);
                boolean ignore = fieldInfo.ignore;
                if (supperMethod != null) {
                    getFieldInfo(beanInfo, fieldInfo, superclass, supperMethod);
                    Field field = BeanUtils.getField(objectClass, method);
                    int supperMethodModifiers = supperMethod.getModifiers();
                    if (null != field && ignore != fieldInfo.ignore
                            && !Modifier.isAbstract(supperMethodModifiers)
                            && !supperMethod.equals(method)
                    ) {
                        fieldInfo.ignore = ignore;
                    }
                }

                Class[] interfaces = objectClass.getInterfaces();
                for (Class anInterface : interfaces) {
                    Method interfaceMethod = BeanUtils.getMethod(anInterface, method);
                    if (superclass != null && interfaceMethod != null) {
                        getFieldInfo(beanInfo, fieldInfo, superclass, interfaceMethod);
                    }
                }
            }

            fieldInfo.isPrivate = false;
            Annotation[] annotations = getAnnotations(method);
            processAnnotations(fieldInfo, annotations);

            if (!objectClass.getName().startsWith("java.lang") && !BeanUtils.isRecord(objectClass)) {
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
                String fieldName = BeanUtils.getterName(method, beanInfo.kotlin, null);
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
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                JSONField jsonField = findAnnotation(annotation, JSONField.class);
                if (Objects.nonNull(jsonField)) {
                    loadFieldInfo(fieldInfo, jsonField);
                    continue;
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
                    case "java.beans.Transient":
                        if (fieldInfo.skipTransient) {
                            fieldInfo.ignore = true;
                        }
                        fieldInfo.isTransient = true;
                        break;
                    case "com.fasterxml.jackson.annotation.JsonProperty": {
                        if (useJacksonAnnotation) {
                            processJacksonJsonProperty(fieldInfo, annotation);
                        }
                        break;
                    }
                    case "com.fasterxml.jackson.annotation.JsonFormat":
                        if (useJacksonAnnotation) {
                            processJacksonJsonFormat(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonValue":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.VALUE_MASK;
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonRawValue":
                        if (useJacksonAnnotation) {
                            fieldInfo.features |= FieldInfo.RAW_VALUE_MASK;
                        }
                        break;
                    case "com.fasterxml.jackson.databind.annotation.JsonSerialize":
                        if (useJacksonAnnotation) {
                            processJacksonJsonSerialize(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonInclude":
                        if (useJacksonAnnotation) {
                            processJacksonJsonInclude(fieldInfo, annotation);
                        }
                        break;
                    case "com.fasterxml.jackson.annotation.JsonUnwrapped":
                        if (useJacksonAnnotation) {
                            processJacksonJsonUnwrapped(fieldInfo, annotation);
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

            String locale = jsonField.locale();
            if (!locale.isEmpty()) {
                String[] parts = locale.split("_");
                if (parts.length == 2) {
                    fieldInfo.locale = new Locale(parts[0], parts[1]);
                }
            }

            boolean ignore = !jsonField.serialize();
            if (!fieldInfo.ignore) {
                fieldInfo.ignore = ignore;
            }

            if (!jsonField.skipTransient()) {
                fieldInfo.skipTransient = false;
                if (fieldInfo.isTransient && !fieldInfo.isPrivate) {
                    fieldInfo.ignore = false;
                }
            }

            if (jsonField.unwrapped()) {
                fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
            }

            for (JSONWriter.Feature feature : jsonField.serializeFeatures()) {
                fieldInfo.features |= feature.mask;
                if (fieldInfo.ignore && !fieldInfo.isTransient && !ignore && feature == JSONWriter.Feature.FieldBased) {
                    fieldInfo.ignore = false;
                }
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

            Class contentAs = jsonField.contentAs();
            if (contentAs != Void.class) {
                fieldInfo.contentAs = contentAs;
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
                    jsonFieldFormat = jsonFieldFormat.replace("T", "'T'");
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
            default:
                if (JdbcSupport.isClob(objectClass)) {
                    return JdbcSupport.createClobWriter(objectClass);
                }
                return null;
        }
    }

    @Override
    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        if (objectClass == JSONObject.class) {
            return new ObjectWriter<JSONObject>() {
                @Override
                public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                    if (object == null) {
                        jsonWriter.writeNull();
                        return;
                    }

                    Map map = (Map) object;
                    if (map.isEmpty()) {
                        jsonWriter.writeRaw('{', '}');
                        return;
                    }

                    jsonWriter.startObject();

                    boolean first = true;
                    for (Object o : map.entrySet()) {
                        Map.Entry entry = (Map.Entry) o;
                        Object value = entry.getValue();
                        if (value == null && (jsonWriter.getFeatures() & JSONWriter.Feature.WriteMapNullValue.mask) == 0) {
                            continue;
                        }

                        if (!first) {
                            jsonWriter.writeComma();
                        }
                        first = false;

                        Object key = entry.getKey();
                        if (key instanceof String) {
                            jsonWriter.writeString((String) key);
                        } else {
                            jsonWriter.writeAny(key);
                        }

                        jsonWriter.writeColon();

                        if (value == null) {
                            jsonWriter.writeNull();
                            continue;
                        }

                        Class<?> valueClass = value.getClass();
                        if (valueClass == String.class) {
                            jsonWriter.writeString((String) value);
                            continue;
                        }
                        if (valueClass == Integer.class) {
                            jsonWriter.writeInt32((Integer) value);
                            continue;
                        }
                        if (valueClass == Long.class) {
                            jsonWriter.writeInt64((Long) value);
                            continue;
                        }
                        if (valueClass == Boolean.class) {
                            jsonWriter.writeBool((Boolean) value);
                            continue;
                        }
                        if (valueClass == BigDecimal.class) {
                            jsonWriter.writeDecimal((BigDecimal) value, features, null);
                            continue;
                        }
                        if (valueClass == Double.class) {
                            jsonWriter.writeDouble((Double) value);
                            continue;
                        }
                        if (valueClass == Float.class) {
                            jsonWriter.writeFloat((Float) value);
                            continue;
                        }
                        if (valueClass == JSONArray.class) {
                            jsonWriter.write((JSONArray) value);
                            continue;
                        }
                        if (valueClass == JSONObject.class) {
                            jsonWriter.write((JSONObject) value);
                            continue;
                        }

                        ObjectWriter valueWriter = jsonWriter.context.getObjectWriter(valueClass, valueClass);
                        if (valueWriter != null) {
                            valueWriter.write(jsonWriter, value, key, valueClass, features);
                        } else {
                            jsonWriter.writeAny(value);
                        }
                    }

                    jsonWriter.endObject();
                }
            };
        }
        if (objectClass == JSONArray.class) {
            return new ObjectWriter<JSONArray>() {
                @Override
                public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                    if (object == null) {
                        jsonWriter.writeNull();
                        return;
                    }

                    List list = (List) object;
                    int size = list.size();
                    if (size == 0) {
                        jsonWriter.writeRaw('[', ']');
                        return;
                    }

                    jsonWriter.startArray();

                    for (int i = 0; i < size; i++) {
                        if (i != 0) {
                            jsonWriter.writeComma();
                        }

                        Object item = list.get(i);
                        if (item == null) {
                            jsonWriter.writeNull();
                            continue;
                        }

                        Class<?> itemClass = item.getClass();
                        if (itemClass == String.class) {
                            jsonWriter.writeString((String) item);
                            continue;
                        }
                        if (itemClass == Integer.class) {
                            jsonWriter.writeInt32((Integer) item);
                            continue;
                        }
                        if (itemClass == Long.class) {
                            jsonWriter.writeInt64((Long) item);
                            continue;
                        }
                        if (itemClass == Boolean.class) {
                            jsonWriter.writeBool((Boolean) item);
                            continue;
                        }
                        if (itemClass == BigDecimal.class) {
                            jsonWriter.writeDecimal((BigDecimal) item, features, null);
                            continue;
                        }
                        if (itemClass == Double.class) {
                            jsonWriter.writeDouble((Double) item);
                            continue;
                        }
                        if (itemClass == Float.class) {
                            jsonWriter.writeFloat((Float) item);
                            continue;
                        }
                        if (itemClass == JSONArray.class) {
                            jsonWriter.write((JSONArray) item);
                            continue;
                        }
                        if (itemClass == JSONObject.class) {
                            jsonWriter.write((JSONObject) item);
                            continue;
                        }

                        jsonWriter.writeAny(item);
                    }

                    jsonWriter.endArray();
                }
            };
        }
        return null;
    }

    private ObjectWriter createEnumWriter(Class enumClass) {
        return null;
    }

    static class VoidObjectWriter
            implements ObjectWriter {
        public static final VoidObjectWriter INSTANCE = new VoidObjectWriter();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        }
    }
}
