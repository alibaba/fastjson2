package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.codec.FieldInfo.JSON_AUTO_WIRED_ANNOTATED;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE_SUPPORT;

public class ObjectReaderCreator {
    public static final ObjectReaderCreator INSTANCE = new ObjectReaderCreator();

    public <T> ObjectReader<T> createObjectReaderNoneDefaultConstructor(Constructor constructor, String... paramNames) {
        Function<Map<Long, Object>, T> function = createFunction(constructor, paramNames);
        FieldReader[] fieldReaders = createFieldReaders(constructor.getParameters(), paramNames);
        return createObjectReaderNoneDefaultConstructor(constructor.getDeclaringClass(), function, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReaderNoneDefaultConstructor(
            Class objectClass,
            Constructor constructor,
            String[] paramNames,
            FieldReader[] paramFieldReaders,
            FieldReader[] setterFieldReaders
    ) {
        Function<Map<Long, Object>, T> function = createFunction(constructor, paramNames);
        return new ObjectReaderNoneDefaultConstructor(objectClass, null, null, 0, function, null, paramNames, paramFieldReaders, setterFieldReaders);
    }

    public <T> ObjectReader<T> createObjectReaderNoneDefaultConstructor(
            Class objectClass,
            Function<Map<Long, Object>, T> creator,
            FieldReader... fieldReaders
    ) {
        return new ObjectReaderNoneDefaultConstructor(objectClass, null, null, 0, creator, null, null, fieldReaders, null);
    }

    public <T> ObjectReader<T> createObjectReaderFactoryMethod(Method factoryMethod, String... paramNames) {
        Function<Map<Long, Object>, Object> factoryFunction = createFactoryFunction(factoryMethod, paramNames);
        FieldReader[] fieldReaders = createFieldReaders(factoryMethod.getParameters(), paramNames);
        return new ObjectReaderNoneDefaultConstructor(null, null, null, 0, factoryFunction, null, paramNames, fieldReaders, null);
    }

    public FieldReader[] createFieldReaders(Parameter[] parameters, String... paramNames) {
        FieldReader[] fieldReaders = new FieldReader[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String paramName;
            if (i < paramNames.length) {
                paramName = paramNames[i];
            } else {
                paramName = parameter.getName();
            }
            Type paramType = parameter.getParameterizedType();
            fieldReaders[i] = createFieldReaderParam(
                    null,
                    null,
                    paramName,
                    i,
                    0,
                    null,
                    paramType,
                    parameter.getType(),
                    paramName,
                    parameter,
                    null
            );
        }
        return fieldReaders;
    }

    public <T> Function<Map<Long, Object>, T> createFactoryFunction(Method factoryMethod, String... paramNames) {
        factoryMethod.setAccessible(true);
        return new FactoryFunction(factoryMethod, paramNames);
    }

    public <T> Function<Map<Long, Object>, T> createFunction(Constructor constructor, String... paramNames) {
        constructor.setAccessible(true);
        return new ConstructorFunction(constructor, paramNames);
    }

    public <T> Function<Map<Long, Object>, T> createFunction(Constructor constructor, Constructor markerConstructor, String... paramNames) {
        if (markerConstructor == null) {
            constructor.setAccessible(true);
        } else {
            markerConstructor.setAccessible(true);
        }
        return new ConstructorFunction(constructor, markerConstructor, paramNames);
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            FieldReader... fieldReaders
    ) {
        return createObjectReader(
                objectClass,
                null,
                0,
                null,
                createInstanceSupplier(objectClass),
                null,
                fieldReaders
        );
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            Supplier<T> defaultCreator,
            FieldReader... fieldReaders
    ) {
        return createObjectReader(objectClass, null, 0, null, defaultCreator, null, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectType,
            Class[] seeAlso,
            FieldReader... fieldReaders
    ) {
        Supplier<T> instanceSupplier = createInstanceSupplier(objectType);
        return new ObjectReaderSeeAlso(objectType, instanceSupplier, "@type", seeAlso, null, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectType,
            Supplier<T> defaultCreator,
            String typeKey,
            Class[] seeAlso,
            String[] seeAlsoNames,
            FieldReader... fieldReaders
    ) {
        return new ObjectReaderSeeAlso(objectType, defaultCreator, typeKey, seeAlso, seeAlsoNames, fieldReaders);
    }

    protected <T> ObjectReader<T> createObjectReaderWithBuilder(
            Class<T> objectClass,
            Type objectType,
            ObjectReaderProvider provider,
            BeanInfo beanInfo) {
        Function<Object, Object> builderFunction = null;
        if (beanInfo.buildMethod != null) {
            builderFunction = createBuildFunction(beanInfo.buildMethod);
        }
        Class builderClass = beanInfo.builder;

        String builderWithPrefix = beanInfo.builderWithPrefix;
        if (builderWithPrefix == null || builderWithPrefix.isEmpty()) {
            builderWithPrefix = "with";
        }
        int builderWithPrefixLenth = builderWithPrefix.length();

        Map<String, FieldReader> fieldReaders = new LinkedHashMap<>();

        final String prefix = builderWithPrefix;
        final FieldInfo fieldInfo = new FieldInfo();
        BeanUtils.setters(builderClass, false, method -> {
            fieldInfo.init();
            for (ObjectReaderModule module : provider.modules) {
                ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                if (annotationProcessor == null) {
                    continue;
                }
                annotationProcessor.getFieldInfo(fieldInfo, objectClass, method);
            }

            if (fieldInfo.ignore) {
                return;
            }

            String methodName = method.getName();
            String fieldName;
            if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
                final int methodNameLength = methodName.length();
                if (methodNameLength <= prefix.length() || !methodName.startsWith(prefix)) {
                    return;
                }

                fieldName = BeanUtils.setterName(methodName, builderWithPrefixLenth);
            } else {
                fieldName = fieldInfo.fieldName;
            }

            if (method.getParameterCount() == 0) {
                FieldReader fieldReader = createFieldReaderMethod(
                        builderClass,
                        builderClass,
                        fieldName,
                        fieldInfo.ordinal,
                        fieldInfo.features,
                        fieldInfo.format,
                        fieldInfo.locale,
                        fieldInfo.defaultValue,
                        fieldInfo.schema,
                        method.getGenericReturnType(),
                        method.getReturnType(),
                        method,
                        null
                );
                FieldReader origin = fieldReaders.putIfAbsent(fieldName,
                        fieldReader
                );
                if (origin != null && origin.compareTo(fieldReader) > 0) {
                    fieldReaders.put(fieldName, fieldReader);
                }
                return;
            }

            Type fieldType = method.getGenericParameterTypes()[0];
            Class fieldClass = method.getParameterTypes()[0];

            method.setAccessible(true);

            FieldReader fieldReader = createFieldReaderMethod(
                    builderClass,
                    objectType,
                    fieldName,
                    fieldInfo.ordinal,
                    fieldInfo.features,
                    fieldInfo.format,
                    fieldInfo.locale,
                    fieldInfo.defaultValue,
                    fieldInfo.schema,
                    fieldType,
                    fieldClass,
                    method,
                    null);

            FieldReader origin = fieldReaders.putIfAbsent(fieldName, fieldReader);
            if (origin != null && origin.compareTo(fieldReader) > 0) {
                fieldReaders.put(fieldName, fieldReader);
            }

            if (fieldInfo.alternateNames != null) {
                for (String alternateName : fieldInfo.alternateNames) {
                    if (fieldName.equals(alternateName)) {
                        continue;
                    }

                    fieldReaders
                            .putIfAbsent(alternateName,
                                    createFieldReaderMethod(
                                            builderClass,
                                            objectType,
                                            alternateName,
                                            fieldInfo.ordinal,
                                            fieldInfo.features,
                                            fieldInfo.format,
                                            fieldInfo.locale,
                                            fieldInfo.defaultValue,
                                            fieldInfo.schema,
                                            fieldType,
                                            fieldClass,
                                            method,
                                            null));
                }
            }
        });

        FieldReader[] fieldReaderArray = new FieldReader[fieldReaders.size()];
        fieldReaders.values().toArray(fieldReaderArray);
        Arrays.sort(fieldReaderArray);

        Supplier instanceSupplier = createInstanceSupplier(builderClass);
        return createObjectReader(builderClass, 0, instanceSupplier, builderFunction, fieldReaderArray);
    }

    protected <T> ObjectReader<T> createObjectReaderWithCreator(
            Class<T> objectClass,
            Type objectType,
            ObjectReaderProvider provider,
            BeanInfo beanInfo) {
        FieldInfo fieldInfo = new FieldInfo();

        Map<String, FieldReader> fieldReaders = new LinkedHashMap<>();

        Parameter[] parameters;
        String[] paramNames;
        if (beanInfo.creatorConstructor != null) {
            parameters = beanInfo.creatorConstructor.getParameters();
            paramNames = ASMUtils.lookupParameterNames(beanInfo.creatorConstructor);
        } else {
            parameters = beanInfo.createMethod.getParameters();
            paramNames = ASMUtils.lookupParameterNames(beanInfo.createMethod);
        }

        for (int i = 0; i < parameters.length; i++) {
            fieldInfo.init();

            Parameter parameter = parameters[i];

            for (ObjectReaderModule module : provider.modules) {
                ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                if (annotationProcessor == null) {
                    continue;
                }
                if (beanInfo.creatorConstructor != null) {
                    annotationProcessor.getFieldInfo(fieldInfo, objectClass, beanInfo.creatorConstructor, i, parameter);
                } else {
                    annotationProcessor.getFieldInfo(fieldInfo, objectClass, beanInfo.createMethod, i, parameter);
                }
            }

            if (parameters.length == 1 && (fieldInfo.features & FieldInfo.VALUE_MASK) != 0) {
                break;
            }

            String fieldName = fieldInfo.fieldName;
            if (fieldName == null || fieldName.isEmpty()) {
                if (beanInfo.createParameterNames != null && i < beanInfo.createParameterNames.length) {
                    fieldName = beanInfo.createParameterNames[i];
                }

                if (fieldName == null || fieldName.isEmpty()) {
                    fieldName = parameter.getName();
                }
            }
            if (fieldName == null || fieldName.isEmpty()) {
                fieldName = paramNames[i];
            } else if (fieldName.startsWith("arg")) {
                if (paramNames != null && paramNames.length > i) {
                    fieldName = paramNames[i];
                }
            } else {
                paramNames[i] = fieldName;
            }

            Type paramType = parameter.getParameterizedType();
            fieldReaders.put(fieldName,
                    createFieldReaderParam(
                            objectClass,
                            objectType,
                            fieldName,
                            i,
                            fieldInfo.features,
                            fieldInfo.format,
                            paramType,
                            parameter.getType(),
                            fieldName,
                            parameter,
                            null));

            if (fieldInfo.alternateNames != null) {
                for (String alternateName : fieldInfo.alternateNames) {
                    if (fieldName.equals(alternateName)) {
                        continue;
                    }

                    fieldReaders.putIfAbsent(alternateName,
                            createFieldReaderParam(
                                    objectClass,
                                    objectType,
                                    alternateName,
                                    i,
                                    fieldInfo.features,
                                    fieldInfo.format,
                                    paramType,
                                    parameter.getType(),
                                    fieldName,
                                    parameter,
                                    null
                            ));
                }
            }
        }

        if (parameters.length == 1 && (fieldInfo.features & FieldInfo.VALUE_MASK) != 0) {
            Type valueType = beanInfo.creatorConstructor == null
                    ? beanInfo.createMethod.getGenericParameterTypes()[0]
                    : beanInfo.creatorConstructor.getGenericParameterTypes()[0];
            Class valueClass = beanInfo.creatorConstructor == null
                    ? beanInfo.createMethod.getParameterTypes()[0]
                    : beanInfo.creatorConstructor.getParameterTypes()[0];

            JSONSchema jsonSchema = null;
            if (fieldInfo.schema != null && !fieldInfo.schema.isEmpty()) {
                JSONObject object = JSON.parseObject(fieldInfo.schema);
                if (!object.isEmpty()) {
                    jsonSchema = JSONSchema.of(object, valueClass);
                }
            }

            Object defaultValue = fieldInfo.defaultValue;
            if (defaultValue != null && defaultValue.getClass() != valueClass) {
                Function typeConvert = JSONFactory
                        .getDefaultObjectReaderProvider()
                        .getTypeConvert(defaultValue.getClass(), valueType);
                if (typeConvert != null) {
                    defaultValue = typeConvert.apply(defaultValue);
                } else {
                    throw new JSONException("illegal defaultValue : " + defaultValue + ", class " + valueClass.getName());
                }
            }

            return new ObjectReaderImplValue(
                    objectClass,
                    valueType,
                    valueClass,
                    fieldInfo.features,
                    fieldInfo.format,
                    defaultValue,
                    jsonSchema,
                    beanInfo.creatorConstructor,
                    beanInfo.createMethod,
                    null
            );
        }

        Function<Map<Long, Object>, Object> function;
        if (beanInfo.creatorConstructor != null) {
            function = createFunction(beanInfo.creatorConstructor, beanInfo.markerConstructor, paramNames);
        } else {
            function = createFactoryFunction(beanInfo.createMethod, paramNames);
        }

        FieldReader[] fieldReaderArray = new FieldReader[fieldReaders.size()];
        fieldReaders.values().toArray(fieldReaderArray);

        FieldReader[] setterFieldReaders = createFieldReaders(objectClass, objectType);
        Arrays.sort(setterFieldReaders);
        Arrays.sort(fieldReaderArray);

        return (ObjectReader<T>) new ObjectReaderNoneDefaultConstructor(
                objectClass,
                beanInfo.typeKey,
                beanInfo.typeName,
                beanInfo.readerFeatures,
                function,
                null,
                paramNames,
                fieldReaderArray,
                setterFieldReaders);
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            long features,
            Supplier<T> defaultCreator,
            Function buildFunction,
            FieldReader... fieldReaders
    ) {
        return createObjectReader(objectClass, null, features, null, defaultCreator, buildFunction, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            String typeKey,
            long features,
            JSONSchema schema,
            Supplier<T> defaultCreator,
            Function buildFunction,
            FieldReader... fieldReaders
    ) {
        if (objectClass != null) {
            int modifiers = objectClass.getModifiers();
            if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
                return new ObjectReaderAdapter(objectClass, typeKey, null, features, schema, defaultCreator, buildFunction, fieldReaders);
            }
        }

        switch (fieldReaders.length) {
            case 1:
                return new ObjectReader1(
                        objectClass,
                        features,
                        schema,
                        defaultCreator,
                        buildFunction,
                        fieldReaders[0]
                );
            case 2:
                return new ObjectReader2(
                        objectClass,
                        features,
                        schema,
                        defaultCreator,
                        buildFunction,
                        fieldReaders[0],
                        fieldReaders[1]);
            case 3:
                return new ObjectReader3(
                        objectClass,
                        defaultCreator,
                        features,
                        schema,
                        buildFunction,
                        fieldReaders[0],
                        fieldReaders[1],
                        fieldReaders[2]
                );
            case 4:
                return new ObjectReader4(
                        objectClass,
                        features,
                        schema,
                        defaultCreator,
                        buildFunction,
                        fieldReaders[0],
                        fieldReaders[1],
                        fieldReaders[2],
                        fieldReaders[3]
                );
            case 5:
                return new ObjectReader5(
                        objectClass,
                        defaultCreator,
                        features,
                        schema,
                        buildFunction,
                        fieldReaders[0],
                        fieldReaders[1],
                        fieldReaders[2],
                        fieldReaders[3],
                        fieldReaders[4]
                );
            case 6:
                return new ObjectReader6(
                        objectClass,
                        defaultCreator,
                        features,
                        schema,
                        buildFunction,
                        fieldReaders[0],
                        fieldReaders[1],
                        fieldReaders[2],
                        fieldReaders[3],
                        fieldReaders[4],
                        fieldReaders[5]
                );
            default:
                return new ObjectReaderAdapter(objectClass, typeKey, null, features, schema, defaultCreator, buildFunction, fieldReaders);
        }
    }

    public <T> ObjectReader<T> createObjectReader(Type objectType) {
        if (objectType instanceof Class) {
            return createObjectReader((Class<T>) objectType);
        }

        Class<T> objectClass = (Class<T>) TypeUtils.getMapping(objectType);
        FieldReader[] fieldReaderArray = createFieldReaders(objectClass, objectType);
        return createObjectReader(
                objectClass,
                createInstanceSupplier(objectClass),
                fieldReaderArray);
    }

    public <T> ObjectReader<T> createObjectReader(Class<T> objectType) {
        return createObjectReader(
                objectType,
                objectType,
                false,
                JSONFactory.getDefaultObjectReaderProvider()
        );
    }

    public <T> ObjectReader<T> createObjectReader(Class<T> objectType, boolean fieldBased) {
        return createObjectReader(
                objectType,
                objectType,
                fieldBased,
                JSONFactory.getDefaultObjectReaderProvider()
        );
    }

    protected ObjectReader getAnnotatedObjectReader(ObjectReaderProvider provider, Class objectClass, BeanInfo beanInfo) {
        if ((beanInfo.readerFeatures & JSON_AUTO_WIRED_ANNOTATED) == 0) {
            return null;
        }

        String fieldName = beanInfo.objectReaderFieldName;
        if (fieldName == null) {
            fieldName = "objectReader";
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
                    && ObjectReader.class.isAssignableFrom(field.getType())
                    && Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                return (ObjectReader) field.get(null);
            }
        } catch (Throwable ignored) {
            // ignored
        }
        return null;
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            Type objectType,
            boolean fieldBased,
            ObjectReaderProvider provider
    ) {
        BeanInfo beanInfo = new BeanInfo();
        if (fieldBased) {
            beanInfo.readerFeatures |= JSONReader.Feature.FieldBased.mask;
        }

        for (ObjectReaderModule module : provider.modules) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getBeanInfo(beanInfo, objectClass);
            }
        }

        if (beanInfo.deserializer != null && ObjectReader.class.isAssignableFrom(beanInfo.deserializer)) {
            try {
                return (ObjectReader<T>) beanInfo.deserializer.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create deserializer error", e);
            }
        }

        ObjectReader annotatedObjectReader = getAnnotatedObjectReader(provider, objectClass, beanInfo);
        if (annotatedObjectReader != null) {
            return annotatedObjectReader;
        }

        if (fieldBased) {
            beanInfo.readerFeatures |= JSONReader.Feature.FieldBased.mask;
        }

        if (Enum.class.isAssignableFrom(objectClass) && (beanInfo.createMethod == null || beanInfo.createMethod.getParameterCount() == 1)) {
            return createEnumReader(objectClass, beanInfo.createMethod, provider);
        }

        if (Throwable.class.isAssignableFrom(objectClass)) {
            fieldBased = false;
            beanInfo.readerFeatures |= JSONReader.Feature.IgnoreSetNullValue.mask;
        }

        if (fieldBased && objectClass.isInterface()) {
            fieldBased = false;
        }

        FieldReader[] fieldReaderArray = createFieldReaders(objectClass, objectType, beanInfo, fieldBased, provider);

        if (beanInfo.creatorConstructor != null || beanInfo.createMethod != null) {
            return createObjectReaderWithCreator(objectClass, objectType, provider, beanInfo);
        }

        if (beanInfo.builder != null) {
            return createObjectReaderWithBuilder(objectClass, objectType, provider, beanInfo);
        }

        Constructor creatorConstructor = beanInfo.creatorConstructor;

        final List<Constructor> alternateConstructors = new ArrayList<>();
        BeanUtils.constructor(objectClass, constructor -> {
            alternateConstructors.add(constructor);
        });

        if (Throwable.class.isAssignableFrom(objectClass)) {
            return new ObjectReaderException<>(objectClass, alternateConstructors, fieldReaderArray);
        }

        Constructor defaultConstructor = null;

        Class<?> declaringClass = objectClass.getDeclaringClass();

        int index = -1;
        for (int i = 0; i < alternateConstructors.size(); i++) {
            Constructor constructor = alternateConstructors.get(i);

            if (constructor.getParameterCount() == 0) {
                defaultConstructor = constructor;
            }

            if (declaringClass != null
                    && constructor.getParameterCount() == 1
                    && declaringClass.equals(constructor.getParameterTypes()[0])) {
                creatorConstructor = constructor;
                index = i;
                break;
            } else if (creatorConstructor == null) {
                creatorConstructor = constructor;
                index = i;
            } else if (constructor.getParameterCount() == 0) {
                creatorConstructor = constructor;
                index = i;
            } else if (creatorConstructor.getParameterCount() < constructor.getParameterCount()) {
                creatorConstructor = constructor;
                index = i;
            }
        }

        if (index != -1) {
            alternateConstructors.remove(index);
        }

        if (creatorConstructor != null && creatorConstructor.getParameterCount() != 0 && beanInfo.seeAlso == null) {
            creatorConstructor.setAccessible(true);
            String[] parameterNames = beanInfo.createParameterNames;
            if (parameterNames == null || parameterNames.length == 0) {
                parameterNames = ASMUtils.lookupParameterNames(creatorConstructor);
            }

            int matchCount = 0;
            if (defaultConstructor != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    String parameterName = parameterNames[i];
                    if (parameterName == null) {
                        continue;
                    }

                    for (int j = 0; j < fieldReaderArray.length; j++) {
                        FieldReader fieldReader = fieldReaderArray[j];
                        if (fieldReader != null) {
                            if (parameterName.equals(fieldReader.fieldName)) {
                                matchCount++;
                                break;
                            }
                        }
                    }
                }
            }

            if (!(fieldBased && UNSAFE_SUPPORT)
                    && !(Throwable.class.isAssignableFrom(objectClass))
                    && defaultConstructor == null
                    && matchCount != parameterNames.length) {
                if (creatorConstructor.getParameterCount() == 1) {
                    FieldInfo fieldInfo = new FieldInfo();
                    for (ObjectReaderModule module : provider.modules) {
                        ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                        if (annotationProcessor != null) {
                            annotationProcessor.getFieldInfo(fieldInfo, objectClass, creatorConstructor, 0, creatorConstructor.getParameters()[0]);
                        }
                    }
                    if ((fieldInfo.features & FieldInfo.VALUE_MASK) != 0) {
                        Type valueType = creatorConstructor.getGenericParameterTypes()[0];
                        Class valueClass = creatorConstructor.getParameterTypes()[0];

                        JSONSchema jsonSchema = null;
                        if (fieldInfo.schema != null && !fieldInfo.schema.isEmpty()) {
                            JSONObject object = JSON.parseObject(fieldInfo.schema);
                            if (!object.isEmpty()) {
                                jsonSchema = JSONSchema.of(object, valueClass);
                            }
                        }

                        Object defaultValue = fieldInfo.defaultValue;
                        if (defaultValue != null && defaultValue.getClass() != valueClass) {
                            Function typeConvert = JSONFactory
                                    .getDefaultObjectReaderProvider()
                                    .getTypeConvert(defaultValue.getClass(), valueType);
                            if (typeConvert != null) {
                                defaultValue = typeConvert.apply(defaultValue);
                            } else {
                                throw new JSONException("illegal defaultValue : " + defaultValue + ", class " + valueClass.getName());
                            }
                        }

                        return new ObjectReaderImplValue(
                                objectClass,
                                valueType,
                                valueClass,
                                fieldInfo.features,
                                fieldInfo.format,
                                defaultValue,
                                jsonSchema,
                                creatorConstructor,
                                null,
                                null
                        );
                    }
                }

                Function<Map<Long, Object>, T> function = new ConstructorFunction(alternateConstructors, creatorConstructor, parameterNames);
                FieldReader[] paramFieldReaders = createFieldReaders(creatorConstructor.getParameters(), parameterNames);
                return new ObjectReaderNoneDefaultConstructor(
                        objectClass,
                        beanInfo.typeKey,
                        beanInfo.typeName,
                        beanInfo.readerFeatures,
                        function,
                        alternateConstructors,
                        parameterNames,
                        paramFieldReaders,
                        fieldReaderArray
                );
            }
        }

        Supplier<T> creator = createInstanceSupplier(objectClass);

        if (beanInfo.seeAlso != null && beanInfo.seeAlso.length != 0) {
            return createObjectReaderSeeAlso(objectClass, creator, beanInfo.typeKey, beanInfo.seeAlso, beanInfo.seeAlsoNames, fieldReaderArray);
        }

        if (objectClass.isInterface()) {
            return new ObjectReaderInterface(
                    objectClass,
                    null,
                    null,
                    0L,
                    null,
                    null,
                    fieldReaderArray
            );
        }

        JSONSchema jsonSchema = JSONSchema.of(JSON.parseObject(beanInfo.schema), objectClass);
        return createObjectReader(
                objectClass,
                beanInfo.typeKey,
                beanInfo.readerFeatures,
                jsonSchema,
                creator,
                null,
                fieldReaderArray);
    }

    public <T> FieldReader[] createFieldReaders(Class<T> objectClass) {
        return createFieldReaders(
                objectClass,
                objectClass,
                null,
                false,
                JSONFactory.getDefaultObjectReaderProvider()
        );
    }

    public <T> FieldReader[] createFieldReaders(Class<T> objectClass, Type objectType) {
        return createFieldReaders(
                objectClass,
                objectType,
                null,
                false,
                JSONFactory.getDefaultObjectReaderProvider()
        );
    }

    protected void createFieldReader(
            Class objectClass,
            Type objectType,
            String namingStrategy,
            FieldInfo fieldInfo,
            Field field,
            Map<String, FieldReader> fieldReaders,
            ObjectReaderProvider provider
    ) {
        for (ObjectReaderModule module : provider.modules) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getFieldInfo(fieldInfo, objectClass, field);
            }
        }

        if (fieldInfo.ignore) {
            return;
        }

        String fieldName;
        if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
            fieldName = field.getName();
            if (namingStrategy != null) {
                fieldName = BeanUtils.fieldName(fieldName, namingStrategy);
            }
        } else {
            fieldName = fieldInfo.fieldName;
        }

        Type fieldType = field.getGenericType();
        Class<?> fieldClass = field.getType();

        FieldReader<Object> fieldReader = createFieldReader(
                objectClass,
                objectType,
                fieldName,
                fieldInfo.ordinal,
                fieldInfo.features,
                fieldInfo.format,
                fieldInfo.locale,
                fieldInfo.defaultValue,
                fieldInfo.schema,
                fieldType,
                fieldClass,
                field,
                fieldInfo.getInitReader());

        FieldReader previous = fieldReaders.putIfAbsent(fieldName, fieldReader);
        if (previous != null) {
            int cmp = fieldReader.compareTo(previous);
            if (cmp > 0) {
                fieldReaders.put(fieldName, fieldReader);
            }
        }

        if (fieldInfo.alternateNames != null) {
            for (String alternateName : fieldInfo.alternateNames) {
                if (fieldName.equals(alternateName)) {
                    continue;
                }

                fieldReaders
                        .putIfAbsent(
                                alternateName,
                                createFieldReader(
                                        objectClass,
                                        objectType,
                                        alternateName,
                                        fieldInfo.ordinal,
                                        fieldInfo.features,
                                        null,
                                        fieldInfo.locale,
                                        fieldInfo.defaultValue,
                                        fieldInfo.schema,
                                        fieldType,
                                        fieldClass,
                                        field,
                                        null
                                ));
            }
        }
    }

    protected void createFieldReader(
            Class objectClass,
            Type objectType,
            String namingStrategy,
            String[] orders,
            FieldInfo fieldInfo,
            Method method,
            Map<String, FieldReader> fieldReaders,
            ObjectReaderProvider provider
    ) {
        for (ObjectReaderModule module : provider.modules) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor == null) {
                continue;
            }
            annotationProcessor.getFieldInfo(fieldInfo, objectClass, method);
        }

        if (fieldInfo.ignore) {
            return;
        }

        String fieldName;
        if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                fieldName = BeanUtils.setterName(methodName, namingStrategy);
            } else {
                fieldName = BeanUtils.getterName(method, namingStrategy);
            }

            if (fieldName.length() > 2
                    && fieldName.charAt(0) >= 'A' && fieldName.charAt(0) <= 'Z'
                    && fieldName.charAt(1) >= 'A' && fieldName.charAt(1) <= 'Z'
            ) {
                char[] chars = fieldName.toCharArray();
                chars[0] = (char) (chars[0] + 32);
                String fieldName1 = new String(chars);
                Field field = BeanUtils.getDeclaredField(objectClass, fieldName1);
                if (field != null && Modifier.isPublic(field.getModifiers())) {
                    fieldName = field.getName();
                }
            }
        } else {
            fieldName = fieldInfo.fieldName;
        }

        if (orders != null && orders.length > 0) {
            boolean match = false;
            for (int i = 0; i < orders.length; i++) {
                if (fieldName.equals(orders[i])) {
                    fieldInfo.ordinal = i;
                    match = true;
                    break;
                }
            }
            if (!match) {
                if (fieldInfo.ordinal == 0) {
                    fieldInfo.ordinal = orders.length;
                }
            }
        }

        int parameterCount = method.getParameterCount();
        if (parameterCount == 0) {
            FieldReader fieldReader = createFieldReaderMethod(
                    objectClass,
                    objectType,
                    fieldName,
                    fieldInfo.ordinal,
                    fieldInfo.features,
                    fieldInfo.format,
                    fieldInfo.locale,
                    fieldInfo.defaultValue,
                    fieldInfo.schema,
                    method.getGenericReturnType(),
                    method.getReturnType(),
                    method,
                    fieldInfo.getInitReader()
            );
            FieldReader origin = fieldReaders.putIfAbsent(fieldName,
                    fieldReader
            );
            if (origin != null && origin.compareTo(fieldReader) > 0) {
                fieldReaders.put(fieldName, fieldReader);
            }
            return;
        }

        if (parameterCount == 2) {
            Class<?> fieldClass = method.getParameterTypes()[1];
            Type fieldType = method.getGenericParameterTypes()[1];
            method.setAccessible(true);
            FieldReaderAnySetter anySetter = new FieldReaderAnySetter(fieldType, fieldClass, fieldInfo.ordinal, fieldInfo.features, fieldInfo.format, null, method);
            fieldReaders.put(anySetter.fieldName, anySetter);
            return;
        }

        Type fieldType = method.getGenericParameterTypes()[0];
        Class fieldClass = method.getParameterTypes()[0];

        ObjectReader initReader = fieldInfo.getInitReader();
        if (initReader == null) {
            if (fieldClass == long.class || fieldClass == Long.class) {
                ObjectReader objectReader = provider.getObjectReader(Long.class);
                if (objectReader != ObjectReaderImplInt64.INSTANCE) {
                    initReader = objectReader;
                }
            }
        }

        FieldReader fieldReader = createFieldReaderMethod(
                objectClass,
                objectType,
                fieldName,
                fieldInfo.ordinal,
                fieldInfo.features,
                fieldInfo.format,
                fieldInfo.locale,
                fieldInfo.defaultValue,
                fieldInfo.schema,
                fieldType,
                fieldClass,
                method,
                initReader
        );

        FieldReader origin = fieldReaders.putIfAbsent(fieldName, fieldReader);
        if (origin != null && origin.compareTo(fieldReader) > 0) {
            fieldReaders.put(fieldName, fieldReader);
        }

        if (fieldInfo.alternateNames != null) {
            for (String alternateName : fieldInfo.alternateNames) {
                if (fieldName.equals(alternateName)) {
                    continue;
                }

                fieldReaders
                        .putIfAbsent(alternateName,
                                createFieldReaderMethod(
                                        objectClass,
                                        objectType,
                                        alternateName,
                                        fieldInfo.ordinal,
                                        fieldInfo.features,
                                        fieldInfo.format,
                                        fieldInfo.locale,
                                        fieldInfo.defaultValue,
                                        fieldInfo.schema,
                                        fieldType,
                                        fieldClass,
                                        method,
                                        initReader
                                ));
            }
        }
    }

    protected <T> FieldReader[] createFieldReaders(Class<T> objectClass, Type objectType, BeanInfo beanInfo, boolean fieldBased, ObjectReaderProvider provider) {
        if (beanInfo == null) {
            beanInfo = new BeanInfo();
            for (ObjectReaderModule module : provider.modules) {
                ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                if (annotationProcessor != null) {
                    annotationProcessor.getBeanInfo(beanInfo, objectClass);
                }
            }
        }

        final String namingStrategy = beanInfo.namingStrategy;

        Map<String, FieldReader> fieldReaders = new LinkedHashMap<>();

        final long beanFeatures = beanInfo.readerFeatures;
        final FieldInfo fieldInfo = new FieldInfo();
        final String[] orders = beanInfo.orders;
        if (fieldBased) {
            BeanUtils.declaredFields(objectClass, field -> {
                fieldInfo.init();
                fieldInfo.features |= JSONReader.Feature.FieldBased.mask;
                fieldInfo.features |= beanFeatures;
                createFieldReader(objectClass, objectType, namingStrategy, fieldInfo, field, fieldReaders, provider);
            });
        } else {
            BeanUtils.fields(objectClass, field -> {
                fieldInfo.init();
                fieldInfo.features |= beanFeatures;
                createFieldReader(objectClass, objectType, namingStrategy, fieldInfo, field, fieldReaders, provider);
            });

            BeanUtils.setters(objectClass, method -> {
                fieldInfo.init();
                fieldInfo.features |= beanFeatures;
                createFieldReader(objectClass, objectType, namingStrategy, orders, fieldInfo, method, fieldReaders, provider);
            });

            if (objectClass.isInterface()) {
                BeanUtils.getters(objectClass, method -> {
                    fieldInfo.init();
                    fieldInfo.features |= beanFeatures;
                    createFieldReader(objectClass, objectType, namingStrategy, orders, fieldInfo, method, fieldReaders, provider);
                });
            }
        }

        FieldReader[] fieldReaderArray = new FieldReader[fieldReaders.size()];
        fieldReaders.values().toArray(fieldReaderArray);
        Arrays.sort(fieldReaderArray);
        return fieldReaderArray;
    }

    public <T> Supplier<T> createInstanceSupplier(Class<T> objectClass) {
        if (objectClass.isInterface()) {
            return null;
        }

        int modifiers = objectClass.getModifiers();
        if (Modifier.isAbstract(modifiers)) {
            return null;
        }

        final Constructor<T> constructor;
        try {
            constructor = objectClass.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (Throwable e) {
            throw new JSONException("get constructor error, class " + objectClass.getName(), e);
        }

        return new ConstructorSupplier(constructor);
    }

    public <T> Supplier<T> createInstanceSupplier(Constructor constructor) {
        return new ConstructorSupplier(constructor);
    }

    public <T, R> Function<T, R> createBuildFunction(Method builderMethod) {
        builderMethod.setAccessible(true);

        return (T o) -> {
            try {
                return (R) builderMethod.invoke(o);
            } catch (Throwable e) {
                throw new JSONException("create instance error", e);
            }
        };
    }

    public <T> FieldReader createFieldReader(
            Class<T> objectType,
            String fieldName,
            Type fieldType,
            Class fieldClass,
            Method method
    ) {
        return createFieldReaderMethod(objectType, objectType, fieldName, 0, 0L, null, null, null, null, fieldType, fieldClass, method, null);
    }

    public <T> FieldReader createFieldReader(
            Class<T> objectType,
            String fieldName,
            String format,
            Type fieldType,
            Class fieldClass,
            Method method
    ) {
        return createFieldReaderMethod(objectType, fieldName, format, fieldType, fieldClass, method);
    }

    public <T> FieldReader createFieldReaderMethod(
            Class<T> objectClass,
            String fieldName,
            String format,
            Type fieldType,
            Class fieldClass,
            Method method
    ) {
        return createFieldReaderMethod(objectClass, objectClass, fieldName, 0, 0L, format, null, null, null, fieldType, fieldClass, method, null);
    }

    public <T> FieldReader createFieldReaderParam(
            Class<T> objectClass,
            Type objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Type fieldType,
            Class fieldClass,
            String paramName,
            Parameter parameter,
            JSONSchema schema
    ) {
        if (fieldType == byte.class || fieldType == Byte.class) {
            return new FieldReaderInt8Param(fieldName, fieldClass, paramName, parameter, ordinal, features, format, schema);
        }

        if (fieldType == short.class || fieldType == Short.class) {
            return new FieldReaderInt16Param(fieldName, fieldClass, paramName, parameter, ordinal, features, format, schema);
        }

        if (fieldType == int.class || fieldType == Integer.class) {
            return new FieldReaderInt32Param(fieldName, fieldClass, paramName, parameter, ordinal, features, format, schema);
        }

        if (fieldType == long.class || fieldType == Long.class) {
            return new FieldReaderInt64Param(fieldName, fieldClass, paramName, parameter, ordinal, features, format, schema);
        }

        return new FieldReaderObjectParam(fieldName, fieldType, fieldClass, paramName, parameter, ordinal, features, format, schema);
    }

    public <T> FieldReader createFieldReaderMethod(
            Class<T> objectClass,
            Type objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            String schema,
            Type fieldType,
            Class fieldClass,
            Method method,
            ObjectReader initReader
    ) {
        if (method != null) {
            method.setAccessible(true);
        }

        if (defaultValue != null && defaultValue.getClass() != fieldClass) {
            Function typeConvert = JSONFactory
                    .getDefaultObjectReaderProvider()
                    .getTypeConvert(defaultValue.getClass(), fieldType);
            if (typeConvert != null) {
                defaultValue = typeConvert.apply(defaultValue);
            } else {
                throw new JSONException("illegal defaultValue : " + defaultValue + ", class " + fieldClass.getName());
            }
        }

        JSONSchema jsonSchema = null;
        if (schema != null && !schema.isEmpty()) {
            JSONObject object = JSON.parseObject(schema);
            if (!object.isEmpty()) {
                jsonSchema = JSONSchema.of(object, fieldClass);
            }
        }

        if (initReader != null) {
            FieldReaderObject fieldReaderObjectMethod = new FieldReaderObject(
                    fieldName,
                    fieldType,
                    fieldClass,
                    ordinal,
                    features | FieldInfo.READ_USING_MASK,
                    format,
                    locale,
                    defaultValue,
                    jsonSchema,
                    method,
                    null,
                    null
            );
            fieldReaderObjectMethod.initReader = initReader;
            return fieldReaderObjectMethod;
        }

        if (fieldType == boolean.class) {
            return new FieldReaderBoolValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, (Boolean) defaultValue, jsonSchema, method);
        }

        if (fieldType == Boolean.class) {
            return new FieldReaderBoolMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Boolean) defaultValue, jsonSchema, method);
        }

        if (fieldType == byte.class) {
            return new FieldReaderInt8ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Byte) defaultValue, jsonSchema, method);
        }

        if (fieldType == short.class) {
            return new FieldReaderInt16ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Short) defaultValue, jsonSchema, method);
        }

        if (fieldType == int.class) {
            return new FieldReaderInt32ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, (Integer) defaultValue, jsonSchema, method);
        }

        if (fieldType == long.class) {
            return new FieldReaderInt64ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Long) defaultValue, jsonSchema, method);
        }

        if (fieldType == float.class) {
            return new FieldReaderFloatValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Float) defaultValue, jsonSchema, method);
        }

        if (fieldType == double.class) {
            return new FieldReaderDoubleValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Double) defaultValue, jsonSchema, method);
        }

        if (fieldType == Byte.class) {
            return new FieldReaderInt8Method(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Byte) defaultValue, jsonSchema, method);
        }

        if (fieldType == Short.class) {
            return new FieldReaderInt16Method(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Short) defaultValue, jsonSchema, method);
        }

        if (fieldType == Integer.class) {
            return new FieldReaderInt32Method(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Integer) defaultValue, jsonSchema, method);
        }

        if (fieldType == Long.class) {
            return new FieldReaderInt64Method(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Long) defaultValue, jsonSchema, method);
        }

        if (fieldType == Float.class) {
            return new FieldReaderFloatMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Float) defaultValue, jsonSchema, method);
        }

        if (fieldType == Double.class) {
            return new FieldReaderDoubleMethod(fieldName, fieldType, fieldClass, ordinal, features, format, (Double) defaultValue, jsonSchema, method);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldReaderBigDecimalMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (BigDecimal) defaultValue, jsonSchema, method);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldReaderBigIntegerMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (BigInteger) defaultValue, jsonSchema, method);
        }

        if (fieldType == String.class) {
            return new FieldReaderStringMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (String) defaultValue, jsonSchema, method);
        }

        if (method.getParameterCount() == 0) {
            if (fieldClass == AtomicInteger.class) {
                return new FieldReaderAtomicIntegerMethodReadOnly(fieldName, fieldClass, ordinal, jsonSchema, method);
            }

            if (fieldClass == AtomicLong.class) {
                return new FieldReaderAtomicLongReadOnly(fieldName, fieldClass, ordinal, jsonSchema, method);
            }

            if (fieldClass == AtomicIntegerArray.class) {
                return new FieldReaderAtomicIntegerArrayReadOnly(fieldName, fieldClass, ordinal, jsonSchema, method);
            }

            if (fieldClass == AtomicLongArray.class) {
                return new FieldReaderAtomicLongArrayReadOnly(fieldName, fieldClass, ordinal, jsonSchema, method);
            }

            if (fieldClass == AtomicBoolean.class) {
                return new FieldReaderAtomicBooleanMethodReadOnly(fieldName, fieldClass, ordinal, jsonSchema, method);
            }

            if (fieldClass == AtomicReference.class) {
                return new FieldReaderAtomicReferenceMethodReadOnly(fieldName, fieldType, fieldClass, ordinal, jsonSchema, method);
            }

            if (Collection.class.isAssignableFrom(fieldClass)) {
                return new FieldReaderCollectionMethodReadOnly(fieldName, fieldType, fieldClass, ordinal, features, format, jsonSchema, method);
            }

            if (Map.class.isAssignableFrom(fieldClass)) {
                return new FieldReaderMapMethodReadOnly(fieldName, fieldType, fieldClass, ordinal, features, format, jsonSchema, method);
            }

            if (!objectClass.isInterface()) {
                return null;
            }
        }

        Type fieldTypeResolved = null;
        Class fieldClassResolved = null;
        if (!(fieldType instanceof Class)) {
            fieldTypeResolved = BeanUtils.getFieldType(TypeReference.get(objectType), objectClass, method, fieldType);
            fieldClassResolved = TypeUtils.getMapping(fieldTypeResolved);
        }

        if (fieldClass == List.class) {
            if (fieldTypeResolved instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) fieldTypeResolved;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length == 1) {
                    Type itemType = actualTypeArguments[0];
                    Class itemClass = TypeUtils.getMapping(itemType);

                    if (itemClass == String.class) {
                        return new FieldReaderList(fieldName, fieldTypeResolved, fieldClass, String.class, String.class, ordinal, features, format, locale, null, jsonSchema, method, null, null);
                    }

                    return new FieldReaderList(fieldName, fieldTypeResolved, fieldClassResolved, itemType, itemClass, ordinal, features, format, locale, null, jsonSchema, method, null, null);
                }
            }
            return new FieldReaderList(fieldName, fieldType, fieldClass, Object.class, Object.class, ordinal, features, format, locale, null, jsonSchema, method, null, null);
        }

        if (fieldClass == Date.class) {
            return new FieldReaderDateMethod(fieldName, fieldClass, ordinal, features, format, locale, jsonSchema, method);
        }

        if (fieldClass == StackTraceElement[].class && method.getDeclaringClass() == Throwable.class) {
            features |= JSONReader.Feature.IgnoreSetNullValue.mask;
        }

        return new FieldReaderObject(
                fieldName,
                fieldTypeResolved != null ? fieldTypeResolved : fieldType,
                fieldClass,
                ordinal,
                features,
                format,
                locale,
                defaultValue,
                jsonSchema,
                method,
                null,
                null
        );
    }

    public <T> FieldReader<T> createFieldReader(
            String fieldName,
            Type fieldType,
            Field field
    ) {
        return createFieldReader(fieldName, null, fieldType, field);
    }

    public <T> FieldReader<T> createFieldReader(
            String fieldName,
            String format,
            Type fieldType,
            Field field
    ) {
        Class objectClass = field.getDeclaringClass();
        return createFieldReader(objectClass, objectClass, fieldName, 0, format, fieldType, field.getType(), field);
    }

    public <T> FieldReader<T> createFieldReader(
            Class objectClass,
            Type objectType,
            String fieldName,
            long features,
            String format,
            Type fieldType,
            Class fieldClass,
            Field field
    ) {
        return createFieldReader(objectClass, objectType, fieldName, 0, features, format, null, null, null, fieldType, field.getType(), field, null);
    }

    public <T> FieldReader<T> createFieldReader(
            Class objectClass,
            Type objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            String schema,
            Type fieldType,
            Class fieldClass,
            Field field,
            ObjectReader initReader
    ) {
        if (defaultValue != null && defaultValue.getClass() != fieldClass) {
            Function typeConvert = JSONFactory
                    .getDefaultObjectReaderProvider()
                    .getTypeConvert(defaultValue.getClass(), fieldType);
            if (typeConvert != null) {
                defaultValue = typeConvert.apply(defaultValue);
            } else {
                throw new JSONException("illegal defaultValue : " + defaultValue + ", class " + fieldClass.getName());
            }
        }

        JSONSchema jsonSchema = null;
        if (schema != null && !schema.isEmpty()) {
            JSONObject object = JSON.parseObject(schema);
            if (!object.isEmpty()) {
                jsonSchema = JSONSchema.of(object, fieldClass);
            }
        }

        if (field != null) {
            String objectClassName = objectClass.getName();
            if (!objectClassName.startsWith("java.lang") && !objectClassName.startsWith("java.time")) {
                field.setAccessible(true);
            }
        }

        if (initReader != null) {
            FieldReaderObjectField fieldReader = new FieldReaderObjectField(fieldName, fieldType, fieldClass, ordinal, features | FieldInfo.READ_USING_MASK, format, defaultValue, jsonSchema, field);
            fieldReader.initReader = initReader;
            return fieldReader;
        }

        if (fieldClass == int.class) {
            return new FieldReaderInt32ValueField(fieldName, fieldClass, ordinal, format, (Integer) defaultValue, jsonSchema, field);
        }
        if (fieldClass == Integer.class) {
            return new FieldReaderInt32Field(fieldName, fieldClass, ordinal, features, format, (Integer) defaultValue, jsonSchema, field);
        }

        if (fieldClass == long.class) {
            return new FieldReaderInt64ValueField(fieldName, fieldClass, ordinal, features, format, (Long) defaultValue, jsonSchema, field);
        }
        if (fieldClass == Long.class) {
            return new FieldReaderInt64Field(fieldName, fieldClass, ordinal, features, format, (Long) defaultValue, jsonSchema, field);
        }

        if (fieldClass == short.class) {
            return new FieldReaderInt16ValueField(fieldName, fieldClass, ordinal, features, format, (Short) defaultValue, jsonSchema, field);
        }

        if (fieldClass == Short.class) {
            return new FieldReaderInt16Field(fieldName, fieldClass, ordinal, features, format, (Short) defaultValue, jsonSchema, field);
        }

        if (fieldClass == boolean.class) {
            return new FieldReaderBoolValueField(fieldName, fieldClass, ordinal, features, format, (Boolean) defaultValue, jsonSchema, field);
        }

        if (fieldClass == Boolean.class) {
            return new FieldReaderBoolField(fieldName, fieldClass, ordinal, features, format, (Boolean) defaultValue, jsonSchema, field);
        }

        if (fieldClass == byte.class) {
            return new FieldReaderInt8ValueField(fieldName, fieldClass, ordinal, features, format, (Byte) defaultValue, jsonSchema, field);
        }

        if (fieldClass == Byte.class) {
            return new FieldReaderInt8Field(fieldName, fieldClass, ordinal, features, format, (Byte) defaultValue, jsonSchema, field);
        }

        if (fieldClass == float.class) {
            return new FieldReaderFloatValueField(fieldName, fieldClass, ordinal, features, format, (Float) defaultValue, jsonSchema, field);
        }
        if (fieldClass == Float.class) {
            return new FieldReaderFloatField(fieldName, fieldClass, ordinal, features, format, (Float) defaultValue, jsonSchema, field);
        }

        if (fieldClass == double.class) {
            return new FieldReaderDoubleValueField(fieldName, fieldClass, ordinal, features, format, (Double) defaultValue, jsonSchema, field);
        }
        if (fieldClass == Double.class) {
            return new FieldReaderDoubleField(fieldName, fieldClass, ordinal, features, format, (Double) defaultValue, jsonSchema, field);
        }

        if (fieldClass == char.class) {
            return new FieldReaderCharValueField(fieldName, ordinal, features, format, (Character) defaultValue, jsonSchema, field);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldReaderBigDecimalField(fieldName, fieldClass, ordinal, features, format, (BigDecimal) defaultValue, jsonSchema, field);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldReaderBigIntegerField(fieldName, fieldClass, ordinal, features, format, (BigInteger) defaultValue, jsonSchema, field);
        }

        if (fieldClass == String.class) {
            return new FieldReaderStringField(fieldName, fieldClass, ordinal, features, format, (String) defaultValue, jsonSchema, field);
        }

        if (fieldClass == Date.class) {
            return new FieldReaderDateField(fieldName, fieldClass, ordinal, features, format, locale, (Date) defaultValue, jsonSchema, field);
        }

        if (fieldClass == AtomicBoolean.class) {
            return new FieldReaderAtomicBooleanFieldReadOnly(fieldName, fieldClass, ordinal, format, (AtomicBoolean) defaultValue, jsonSchema, field);
        }

        if (fieldClass == AtomicReference.class) {
            return new FieldReaderAtomicReferenceField(fieldName, fieldType, fieldClass, ordinal, format, jsonSchema, field);
        }

        Type fieldTypeResolved = null;
        Class fieldClassResolved = null;
        if (!(fieldType instanceof Class)) {
            fieldTypeResolved = BeanUtils.getFieldType(TypeReference.get(objectType), objectClass, field, fieldType);
            fieldClassResolved = TypeUtils.getMapping(fieldTypeResolved);
        }

        boolean finalField = Modifier.isFinal(field.getModifiers());
        if (Collection.class.isAssignableFrom(fieldClass)) {
            if (fieldTypeResolved instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) fieldTypeResolved;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (actualTypeArguments.length == 1) {
                    Type itemType = actualTypeArguments[0];
                    Class itemClass = TypeUtils.getMapping(itemType);
                    if (itemClass == String.class) {
                        if (finalField) {
                            if (UNSAFE_SUPPORT && (features & JSONReader.Feature.FieldBased.mask) != 0) {
                                return new FieldReaderListFieldUF(fieldName, fieldTypeResolved, fieldClassResolved, String.class, String.class, ordinal, features, format, locale, null, jsonSchema, field);
                            }

                            return new FieldReaderCollectionFieldReadOnly(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, format, jsonSchema, field);
                        }

                        if (UNSAFE_SUPPORT) {
                            return new FieldReaderListFieldUF(fieldName, fieldTypeResolved, fieldClassResolved, String.class, String.class, ordinal, features, format, locale, null, jsonSchema, field);
                        }

                        return new FieldReaderList(fieldName, fieldTypeResolved, fieldClassResolved, String.class, String.class, ordinal, features, format, locale, null, jsonSchema, null, field, null);
                    }

                    if (UNSAFE_SUPPORT) {
                        return new FieldReaderListFieldUF(
                                fieldName,
                                fieldTypeResolved,
                                fieldClassResolved,
                                itemType,
                                itemClass,
                                ordinal,
                                features,
                                format,
                                locale,
                                (Collection) defaultValue,
                                jsonSchema,
                                field
                        );
                    }

                    return new FieldReaderList(fieldName, fieldTypeResolved, fieldClassResolved, itemType, itemClass, ordinal, features, format, locale, (Collection) defaultValue, jsonSchema, null, field, null);
                }
            }

            Type itemType = null;
            if (fieldType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) fieldType).getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    itemType = actualTypeArguments[0];
                }
            }
            if (itemType == null) {
                itemType = Object.class;
            }
            Class itemClass = TypeUtils.getClass(itemType);

            if (UNSAFE_SUPPORT) {
                return new FieldReaderListFieldUF(
                        fieldName,
                        fieldType,
                        fieldClass,
                        itemType,
                        itemClass,
                        ordinal,
                        features,
                        format,
                        locale,
                        (Collection) defaultValue,
                        jsonSchema,
                        field
                );
            }

            return new FieldReaderList(
                    fieldName,
                    fieldType,
                    fieldClass,
                    itemType,
                    itemClass,
                    ordinal,
                    features,
                    format,
                    locale,
                    (Collection) defaultValue,
                    jsonSchema,
                    null,
                    field,
                    null
            );
        }

        if (Map.class.isAssignableFrom(fieldClass)) {
            if (fieldTypeResolved instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) fieldTypeResolved;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (actualTypeArguments.length == 2) {
                    if (finalField && (!UNSAFE_SUPPORT || (features & JSONReader.Feature.FieldBased.mask) == 0)) {
                        return new FieldReaderMapFieldReadOnly(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, format, jsonSchema, field);
                    }
                }
            }
        }

        if (finalField) {
            if (fieldClass == int[].class) {
                return new FieldReaderInt32ValueArrayFinalField(fieldName, fieldClass, ordinal, features, format, (int[]) defaultValue, jsonSchema, field);
            }

            if (fieldClass == long[].class) {
                return new FieldReaderInt64ValueArrayFinalField(fieldName, fieldClass, ordinal, features, format, (long[]) defaultValue, jsonSchema, field);
            }
        }

        if (fieldClassResolved != null) {
            if (UNSAFE_SUPPORT) {
                return new FieldReaderObjectFieldUF(
                        fieldName,
                        fieldTypeResolved,
                        fieldClass,
                        ordinal,
                        features,
                        format,
                        defaultValue,
                        jsonSchema,
                        field);
            } else {
                return new FieldReaderObjectField(
                        fieldName,
                        fieldTypeResolved,
                        fieldClass,
                        ordinal,
                        features,
                        format,
                        defaultValue,
                        jsonSchema,
                        field);
            }
        }

        if (UNSAFE_SUPPORT) {
            return new FieldReaderObjectFieldUF(fieldName, fieldType, fieldClass, ordinal, features, format, defaultValue, jsonSchema, field);
        }
        return new FieldReaderObjectField(fieldName, fieldType, fieldClass, ordinal, features, format, defaultValue, jsonSchema, field);
    }

    <T, V> FieldReader createFieldReader(
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            Method method,
            BiConsumer<T, V> function
    ) {
        return createFieldReader(null, null, fieldName, fieldType, fieldClass, 0, 0, null, null, null, method, function, null);
    }

    <T, V> FieldReader createFieldReader(
            Class objectClass,
            Type objectType,
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            BiConsumer<T, V> function,
            ObjectReader initReader
    ) {
        if (fieldClass == Integer.class) {
            return new FieldReaderInt32Func<>(fieldName, fieldClass, ordinal, format, null, defaultValue, schema, method, function);
        }

        if (fieldClass == Long.class) {
            return new FieldReaderInt64Func<>(fieldName, fieldClass, ordinal, format, null, defaultValue, schema, method, function);
        }

        if (fieldClass == String.class) {
            return new FieldReaderStringFunc<>(fieldName, fieldClass, ordinal, features, format, null, defaultValue, schema, method, function);
        }

        if (fieldClass == Boolean.class) {
            return new FieldReaderBoolFunc<>(fieldName, fieldClass, ordinal, format, null, defaultValue, schema, method, function);
        }

        if (fieldClass == Short.class) {
            return new FieldReaderInt16Func(fieldName, fieldClass, ordinal, format, null, defaultValue, schema, method, function);
        }

        if (fieldClass == Byte.class) {
            return new FieldReaderInt8Func(fieldName, fieldClass, ordinal, format, null, defaultValue, schema, method, function);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldReaderBigDecimalFunc(fieldName, fieldClass, ordinal, format, null, defaultValue, schema, method, function);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldReaderBigIntegerFunc(fieldName, fieldClass, ordinal, format, null, defaultValue, schema, method, function);
        }

        if (fieldClass == Number.class) {
            return new FieldReaderNumberFunc(fieldName, fieldClass, ordinal, format, null, (Number) defaultValue, schema, method, function);
        }

        if (fieldClass == Date.class) {
            return new FieldReaderDateFunc(fieldName, fieldClass, ordinal, features, format, null, (Date) defaultValue, schema, method, function);
        }

        Type fieldTypeResolved = null;
        Class fieldClassResolved = null;

        if (!(fieldType instanceof Class)) {
            fieldTypeResolved = BeanUtils.getFieldType(TypeReference.get(objectType), objectClass, method, fieldType);
            fieldClassResolved = TypeUtils.getMapping(fieldTypeResolved);
        }

        if (fieldClass == List.class || fieldClass == ArrayList.class) {
            Type itemType = Object.class;
            Class itemClass = Object.class;
            if (fieldTypeResolved instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) fieldTypeResolved;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (actualTypeArguments.length == 1) {
                    itemType = actualTypeArguments[0];
                    itemClass = TypeUtils.getMapping(itemType);
                    if (itemClass == String.class) {
                        return new FieldReaderList(fieldName, fieldTypeResolved, fieldClassResolved, String.class, String.class, ordinal, features, format, null, defaultValue, schema, method, null, function);
                    }
                }
            }

            return new FieldReaderList(fieldName, fieldTypeResolved, fieldClassResolved, itemType, itemClass, ordinal, features, format, null, defaultValue, schema, method, null, function);
        }

        if (fieldTypeResolved != null) {
            return new FieldReaderObjectFunc<>(fieldName, fieldTypeResolved, fieldClass, ordinal, features, format, null, defaultValue, schema, method, function, initReader);
        }

        return new FieldReaderObjectFunc<>(fieldName, fieldType, fieldClass, ordinal, features, format, null, defaultValue, schema, method, function, initReader);
    }

    protected ObjectReader createEnumReader(
            Class objectClass,
            Method createMethod,
            ObjectReaderProvider provider
    ) {
        FieldInfo fieldInfo = new FieldInfo();

        Enum[] ordinalEnums = (Enum[]) objectClass.getEnumConstants();

        Map<Long, Enum> enumMap = new HashMap();
        for (int i = 0; ordinalEnums != null && i < ordinalEnums.length; ++i) {
            Enum e = ordinalEnums[i];
            String name = e.name();
            long hash = Fnv.hashCode64(name);
            enumMap.put(hash, e);

            try {
                fieldInfo.init();
                Field field = objectClass.getField(name);
                for (ObjectReaderModule module : provider.modules) {
                    ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                    if (annotationProcessor != null) {
                        annotationProcessor.getFieldInfo(fieldInfo, objectClass, field);
                    }
                }
                String jsonFieldName = fieldInfo.fieldName;
                if (jsonFieldName != null && !jsonFieldName.isEmpty() && !jsonFieldName.equals(name)) {
                    long jsonFieldNameHash = Fnv.hashCode64(jsonFieldName);
                    enumMap.putIfAbsent(jsonFieldNameHash, e);
                }
                if (fieldInfo.alternateNames != null) {
                    for (String alternateName : fieldInfo.alternateNames) {
                        if (alternateName == null || alternateName.isEmpty()) {
                            continue;
                        }
                        long alternateNameHash = Fnv.hashCode64(alternateName);
                        enumMap.putIfAbsent(alternateNameHash, e);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        for (int i = 0; ordinalEnums != null && i < ordinalEnums.length; ++i) {
            Enum e = ordinalEnums[i];
            String name = e.name();
            long hashLCase = Fnv.hashCode64LCase(name);
            enumMap.putIfAbsent(hashLCase, e);
        }

        long[] enumNameHashCodes = new long[enumMap.size()];
        {
            int i = 0;
            for (Long h : enumMap.keySet()) {
                enumNameHashCodes[i++] = h;
            }
            Arrays.sort(enumNameHashCodes);
        }

        Member enumValueField = BeanUtils.getEnumValueField(objectClass, provider);
        if (enumValueField == null && provider.modules.size() > 0) {
            if (provider != null) {
                Class fieldClassMixInSource = provider.getMixIn(objectClass);
                if (fieldClassMixInSource != null) {
                    Member mixedValueField = BeanUtils.getEnumValueField(fieldClassMixInSource, provider);
                    if (mixedValueField instanceof Field) {
                        try {
                            enumValueField = objectClass.getField(mixedValueField.getName());
                        } catch (NoSuchFieldException ignored) {
                        }
                    } else if (mixedValueField instanceof Method) {
                        try {
                            enumValueField = objectClass.getMethod(mixedValueField.getName());
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                }
            }
        }

        Enum[] enums = new Enum[enumNameHashCodes.length];
        for (int i = 0; i < enumNameHashCodes.length; ++i) {
            long hash = enumNameHashCodes[i];
            Enum e = enumMap.get(hash);
            enums[i] = e;
        }

        if (createMethod == null && enumValueField == null) {
            if (ordinalEnums != null && ordinalEnums.length == 2) {
                Enum first = ordinalEnums[0];

                int matchCount = 0;
                for (int i = 0; i < enums.length; i++) {
                    if (enums[i] == first) {
                        matchCount++;
                    }
                }

                if (matchCount == 2) {
                    return new ObjectReaderImplEnum2X4(objectClass, enums, ordinalEnums, enumNameHashCodes);
                }
            }
        }

        return new ObjectReaderImplEnum(objectClass, createMethod, enumValueField, enums, ordinalEnums, enumNameHashCodes);
    }
}
