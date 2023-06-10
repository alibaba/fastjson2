package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.*;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.time.ZoneId;
import com.alibaba.fastjson2.util.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.regex.Pattern;

import static com.alibaba.fastjson2.util.BeanUtils.*;

public final class ObjectReaderModule {
    final ObjectReaderProvider provider;

    public ObjectReaderModule(ObjectReaderProvider provider) {
        this.provider = provider;
    }

    public void getBeanInfo(BeanInfo beanInfo, Class<?> objectClass) {
        Class mixInSource = provider.mixInCache.get(objectClass);
        if (mixInSource != null && mixInSource != objectClass) {
            beanInfo.mixIn = true;
            getBeanInfo(beanInfo, mixInSource.getDeclaredAnnotations());

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

            BeanInfo superBeanInfo = new BeanInfo();
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

        Annotation[] annotations = objectClass.getDeclaredAnnotations();
        getBeanInfo(beanInfo, annotations);

        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            String annotationTypeName = annotationType.getName();
            switch (annotationTypeName) {
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

        if (beanInfo.creatorConstructor == null
                && (beanInfo.readerFeatures & JSONReader.Feature.FieldBased.mask) == 0
                && beanInfo.kotlin) {
            BeanUtils.getKotlinConstructor(objectClass, beanInfo);
            beanInfo.createParameterNames = BeanUtils.getKotlinConstructorParameters(objectClass);
        }
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
                                processSeeAlsoAnnotation(itemBeanInfo, item);
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
                    case "deserializer": {
                        Class<?> deserializer = (Class) result;
                        if (ObjectReader.class.isAssignableFrom(deserializer)) {
                            beanInfo.deserializer = deserializer;
                        }
                        break;
                    }
                    case "parseFeatures": {
                        Enum[] features = (Enum[]) result;
                        for (int i = 0; i < features.length; i++) {
                            Enum feature = features[i];
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
                        for (int i = 0; i < features.length; i++) {
                            beanInfo.readerFeatures |= features[i].mask;
                        }
                        break;
                    }
                    case "builder": {
                        Class<?> builderClass = (Class) result;
                        if (builderClass != void.class && builderClass != Void.class) {
                            beanInfo.builder = builderClass;

                            for (Annotation builderAnnotation : builderClass.getDeclaredAnnotations()) {
                                Class<? extends Annotation> builderAnnotationClass = builderAnnotation.annotationType();
                                String builderAnnotationName = builderAnnotationClass.getName();

                                if (builderAnnotationName.equals("com.alibaba.fastjson.annotation.JSONPOJOBuilder")) {
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
                        break;
                    }
                    case "deserializeUsing": {
                        Class<?> deserializeUsing = (Class) result;
                        if (ObjectReader.class.isAssignableFrom(deserializeUsing)) {
                            beanInfo.deserializer = deserializeUsing;
                        }
                        break;
                    }
                    case "autoTypeBeforeHandler":
                    case "autoTypeCheckHandler": {
                        Class<?> autoTypeCheckHandler = (Class) result;
                        if (JSONReader.AutoTypeBeforeHandler.class.isAssignableFrom(autoTypeCheckHandler)) {
                            beanInfo.autoTypeBeforeHandler = (Class<JSONReader.AutoTypeBeforeHandler>) autoTypeCheckHandler;
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

    private void processSeeAlsoAnnotation(BeanInfo beanInfo, Class<?> objectClass) {
        Class mixInSource = provider.mixInCache.get(objectClass);
        if (mixInSource != null && mixInSource != objectClass) {
            beanInfo.mixIn = true;
            processSeeAlsoAnnotation(beanInfo, mixInSource.getDeclaredAnnotations());
        }

        processSeeAlsoAnnotation(beanInfo, objectClass.getDeclaredAnnotations());
    }

    private void processSeeAlsoAnnotation(BeanInfo beanInfo, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> itemAnnotationType = annotation.annotationType();
            BeanUtils.annotationMethods(itemAnnotationType, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    if (name.equals("typeName")) {
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

    public void getFieldInfo(
            FieldInfo fieldInfo,
            Class objectClass,
            Constructor constructor,
            int paramIndex,
            Annotation[][] parameterAnnotations
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
                    Annotation[] mixInParamAnnotations = mixInConstructor.getParameterAnnotations()[paramIndex];
                    processAnnotation(fieldInfo, mixInParamAnnotations);
                }
            }
        }

        Annotation[] annotations = null;
        if (parameterAnnotations == null) {
            parameterAnnotations = constructor.getParameterAnnotations();
        }

        int paIndex;
        if (parameterAnnotations.length == constructor.getParameterTypes().length) {
            paIndex = paramIndex;
        } else {
            paIndex = paramIndex - 1;
        }
        if (paIndex >= 0 && paIndex < parameterAnnotations.length) {
            annotations = parameterAnnotations[paIndex];
        }

        if (annotations != null && annotations.length > 0) {
            processAnnotation(fieldInfo, annotations);
        }
    }

    public void getFieldInfo(
            FieldInfo fieldInfo,
            Class objectClass,
            Method method,
            int paramIndex
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
                    Annotation[] mixInParamAnnotations = mixInMethod.getParameterAnnotations()[paramIndex];
                    processAnnotation(fieldInfo, mixInParamAnnotations);
                }
            }
        }

        Annotation[] parameterAnnotations = method.getParameterAnnotations()[paramIndex];
        processAnnotation(fieldInfo, parameterAnnotations);
    }

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

        Annotation[] annotations = field.getDeclaredAnnotations();
        if (annotations.length > 0) {
            processAnnotation(fieldInfo, annotations);
        }
    }

    public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method) {
        String methodName = method.getName();

        if (objectClass != null) {
            Class superclass = objectClass.getSuperclass();
            if (superclass != Object.class && superclass != null) {
                Method supperMethod = BeanUtils.getMethod(superclass, method);
                if (supperMethod != null) {
                    getFieldInfo(fieldInfo, superclass, supperMethod);
                }
            }

            Class[] interfaces = objectClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                Class item = interfaces[i];
                if (item == Serializable.class) {
                    continue;
                }

                Method interfaceMethod = BeanUtils.getMethod(item, method);
                if (interfaceMethod != null && superclass != null) {
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

        Annotation[] annotations = method.getDeclaredAnnotations();
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

            String annotationTypeName = annotationType.getName();
            if (annotationTypeName.equals("com.alibaba.fastjson.annotation.JSONField")) {
                processJSONField1x(fieldInfo, annotation);
            }
        }

        String fieldName;
        if (methodName.startsWith("set", 0)) {
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

        BeanUtils.getFieldInfo(objectClass, fieldInfo, this, fieldName, fieldName1, fieldName2);
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

            String annotationTypeName = annotationType.getName();
            if (annotationTypeName.equals("com.alibaba.fastjson.annotation.JSONField")) {
                processJSONField1x(fieldInfo, annotation);
            }
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
                Collections.addAll(nameSet, alternateNames);
                nameSet.addAll(Arrays.asList(fieldInfo.alternateNames));
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

        Class deserializeUsing = jsonField.deserializeUsing();
        if (ObjectReader.class.isAssignableFrom(deserializeUsing)) {
            fieldInfo.readUsing = deserializeUsing;
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

        Annotation[] annotations = constructor.getDeclaredAnnotations();

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

            if (annotationType.getName().equals("com.alibaba.fastjson.annotation.JSONCreator")) {
                creatorMethod = true;
                annotationMethods(annotationType, m1 -> {
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
            if (methodName.equals("values")) {
                return;
            }
        }

        Annotation[] annotations = method.getDeclaredAnnotations();

        boolean creatorMethod = false;
        JSONCreator jsonCreator = null;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            jsonCreator = findAnnotation(annotation, JSONCreator.class);
            if (jsonCreator == annotation) {
                continue;
            }

            if (annotationType.getName().equals("com.alibaba.fastjson.annotation.JSONCreator")) {
                creatorMethod = true;
                annotationMethods(annotationType, m1 -> {
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

        if (type == UUID.class) {
            return ObjectReaderImplUUID.INSTANCE;
        }

        if (type == AtomicBoolean.class) {
            return new ObjectReaderImplFromBoolean(
                    AtomicBoolean.class,
                    (Function<Boolean, AtomicBoolean>) AtomicBoolean::new
            );
        }

        if (type == URI.class) {
            return new ObjectReaderImplFromString<URI>(
                    URI.class,
                    URI::create
            );
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
            return new ObjectReaderImplFromString<Pattern>(Pattern.class, Pattern::compile);
        }

        if (type == SimpleDateFormat.class) {
            return new ObjectReaderImplFromString<SimpleDateFormat>(SimpleDateFormat.class, SimpleDateFormat::new);
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

        final String typeName;
        if (type instanceof Class) {
            typeName = ((Class<?>) type).getName();
        } else {
            typeName = "";
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

        if (type == Calendar.class) {
            return ObjectReaderImplCalendar.INSTANCE;
        }

        if (type == Date.class) {
            return ObjectReaderImplDate.INSTANCE;
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

        if (type == String[].class) {
            return ObjectReaderImplStringArray.INSTANCE;
        }

        if (type == AtomicInteger.class) {
            return new ObjectReaderImplFromInt(AtomicInteger.class, AtomicInteger::new);
        }

        if (type == AtomicLong.class) {
            return new ObjectReaderImplFromLong(AtomicLong.class, AtomicLong::new);
        }

        if (type == AtomicIntegerArray.class) {
            return new ObjectReaderImplInt32ValueArray(AtomicIntegerArray.class, AtomicIntegerArray::new);
            //return ObjectReaderImplAtomicIntegerArray.INSTANCE;
        }

        if (type == AtomicLongArray.class) {
            return new ObjectReaderImplInt64ValueArray(AtomicLongArray.class, AtomicLongArray::new);
//            return ObjectReaderImplAtomicLongArray.INSTANCE;
        }

        if (type == AtomicReference.class) {
            return ObjectReaderImplAtomicReference.INSTANCE;
        }

        if (type instanceof MultiType) {
            return new ObjectArrayReaderMultiType((MultiType) type);
        }

        if (type instanceof MapMultiValueType) {
            return new ObjectReaderImplMapMultiValueType((MapMultiValueType) type);
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

        if (type == ConcurrentLinkedQueue.class
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

            if (Collection.class.isAssignableFrom(objectClass)) {
                return ObjectReaderImplList.of(objectClass, objectClass, 0);
            }

            if (objectClass.isArray()) {
                Class componentType = objectClass.getComponentType();
                if (componentType == Object.class) {
                    return ObjectArrayReader.INSTANCE;
                }
                return new ObjectArrayTypedReader(objectClass);
            }

            if (JSONPObject.class.isAssignableFrom(objectClass)) {
                return new ObjectReaderImplJSONP(objectClass);
            }

            ObjectReaderCreator creator = JSONFactory
                    .defaultObjectReaderProvider
                    .creator;

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

                if (rawType == ConcurrentLinkedQueue.class
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
                return new JdbcSupport.TimeReader(null, null);
            case "java.sql.Timestamp":
                return new JdbcSupport.TimestampReader(null, null);
            case "java.sql.Date":
                return new JdbcSupport.DateReader(null, null);
            case "java.util.RegularEnumSet":
            case "java.util.JumboEnumSet":
                return ObjectReaderImplList.of(type, TypeUtils.getClass(type), 0);
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
