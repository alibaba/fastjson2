package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.*;

import static com.alibaba.fastjson2.codec.FieldInfo.READ_ONLY;

public class ObjectReaderCreator {
    public static final ObjectReaderCreator INSTANCE = new ObjectReaderCreator();

    public <T> ObjectReader<T> createObjectReaderNoneDefaultConstructor(Constructor constructor, String... paramNames) {
        Function<Map<Long, Object>, T> function = createFunction(constructor, paramNames);
        Class declaringClass = constructor.getDeclaringClass();
        FieldReader[] fieldReaders = createFieldReaders(
                JSONFactory.defaultObjectReaderProvider,
                declaringClass,
                declaringClass,
                constructor,
                constructor.getParameterTypes(),
                paramNames
        );
        return createObjectReaderNoneDefaultConstructor(declaringClass, function, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReaderNoneDefaultConstructor(
            Class objectClass,
            Function<Map<Long, Object>, T> creator,
            FieldReader... fieldReaders
    ) {
        return new ObjectReaderNoneDefaultConstructor(
                objectClass,
                null,
                null,
                0,
                creator,
                null,
                fieldReaders,
                null,
                null,
                null
        );
    }

    public <T> ObjectReader<T> createObjectReaderFactoryMethod(Method factoryMethod, String... paramNames) {
        Function<Map<Long, Object>, Object> factoryFunction = createFactoryFunction(factoryMethod, paramNames);
        FieldReader[] fieldReaders = createFieldReaders(
                JSONFactory.defaultObjectReaderProvider,
                null,
                null,
                factoryMethod,
                factoryMethod.getParameterTypes(),
                paramNames
        );
        return new ObjectReaderNoneDefaultConstructor(
                null,
                null,
                null,
                0,
                factoryFunction,
                paramNames,
                fieldReaders,
                null,
                null,
                null
        );
    }

    public FieldReader[] createFieldReaders(
            ObjectReaderProvider provider,
            Class objectClass,
            Type objectType,
            AccessibleObject owner,
            Class[] parameters,
            String... paramNames
    ) {
        Class<?> declaringClass = null;
        Type[] paramTypes = null;
        Annotation[][] parameterAnnotations = null;
        if (owner instanceof Constructor) {
            declaringClass = ((Constructor<?>) owner).getDeclaringClass();
            paramTypes = ((Constructor<?>) owner).getGenericParameterTypes();
            parameterAnnotations = ((Constructor<?>) owner).getParameterAnnotations();
        } else if (owner instanceof Method) {
            declaringClass = ((Method) owner).getDeclaringClass();
            paramTypes = ((Method) owner).getGenericParameterTypes();
            parameterAnnotations = ((Method) owner).getParameterAnnotations();
        }

        FieldReader[] fieldReaders = new FieldReader[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            FieldInfo fieldInfo = new FieldInfo();

            String paramName;
            if (i < paramNames.length) {
                paramName = paramNames[i];
            } else {
                paramName = "arg" + i;
            }

            if (owner instanceof Constructor) {
                provider.getFieldInfo(fieldInfo, declaringClass, (Constructor) owner, i, parameterAnnotations);
            }

            if (owner instanceof Constructor) {
                Field field = BeanUtils.getDeclaredField(declaringClass, paramName);
                if (field != null) {
                    provider.getFieldInfo(fieldInfo, declaringClass, field);
                }
            }

            String fieldName;
            if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
                fieldName = paramName;
            } else {
                fieldName = fieldInfo.fieldName;
            }

            if (fieldName == null) {
                fieldName = "arg" + i;
            }

            if (paramName == null) {
                paramName = "arg" + i;
            }

            Type paramType = paramTypes[i];
            ObjectReader initReader = getInitReader(provider, paramType, parameters[i], fieldInfo);
            fieldReaders[i] = createFieldReaderParam(
                    null,
                    null,
                    fieldName,
                    i,
                    fieldInfo.features,
                    fieldInfo.format,
                    paramType,
                    parameters[i],
                    paramName,
                    declaringClass,
                    initReader
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
        return new ConstructorFunction(
                null,
                constructor,
                null,
                null,
                null,
                paramNames
        );
    }

    public <T> Function<Map<Long, Object>, T> createFunction(
            Constructor constructor,
            Constructor markerConstructor,
            String... paramNames
    ) {
        if (markerConstructor == null) {
            constructor.setAccessible(true);
        } else {
            markerConstructor.setAccessible(true);
        }
        return new ConstructorFunction(
                null,
                constructor,
                null,
                null,
                markerConstructor,
                paramNames
        );
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            FieldReader... fieldReaders
    ) {
        return createObjectReader(
                objectClass,
                null,
                0,
                createSupplier(objectClass),
                null,
                fieldReaders
        );
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            Supplier<T> defaultCreator,
            FieldReader... fieldReaders
    ) {
        return createObjectReader(objectClass, null, 0, defaultCreator, null, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectType,
            Class[] seeAlso,
            FieldReader... fieldReaders
    ) {
        Supplier<T> instanceSupplier = createSupplier(objectType);
        return new ObjectReaderSeeAlso(
                objectType,
                instanceSupplier,
                "@type",
                seeAlso,
                null,
                null,
                fieldReaders
        );
    }

    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectClass,
            String typeKey,
            Class[] seeAlso,
            String[] seeAlsoNames,
            FieldReader... fieldReaders
    ) {
        Supplier<T> creator = createSupplier(objectClass);
        return new ObjectReaderSeeAlso(
                objectClass,
                creator,
                typeKey,
                seeAlso,
                seeAlsoNames,
                null,
                fieldReaders
        );
    }

    /**
     * @since 2.0.24
     */
    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectClass,
            String typeKey,
            Class[] seeAlso,
            String[] seeAlsoNames,
            Class seeAlsoDefault,
            FieldReader... fieldReaders
    ) {
        Supplier<T> creator = createSupplier(objectClass);
        return new ObjectReaderSeeAlso(
                objectClass,
                creator,
                typeKey,
                seeAlso,
                seeAlsoNames,
                seeAlsoDefault,
                fieldReaders
        );
    }

    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectType,
            Supplier<T> defaultCreator,
            String typeKey,
            Class[] seeAlso,
            String[] seeAlsoNames,
            FieldReader... fieldReaders
    ) {
        return new ObjectReaderSeeAlso(
                objectType,
                defaultCreator,
                typeKey,
                seeAlso,
                seeAlsoNames,
                null,
                fieldReaders
        );
    }

    protected <T> ObjectReader<T> createObjectReaderWithBuilder(
            Class<T> objectClass,
            Type objectType,
            ObjectReaderProvider provider,
            BeanInfo beanInfo
    ) {
        Function<Object, Object> builderFunction = null;
        if (beanInfo.buildMethod != null) {
            builderFunction = createBuildFunction(beanInfo.buildMethod);
        }
        Class builderClass = beanInfo.builder;

        String builderWithPrefix = beanInfo.builderWithPrefix;
        if (builderWithPrefix == null || builderWithPrefix.isEmpty()) {
            builderWithPrefix = "with";
        }
        int builderWithPrefixLength = builderWithPrefix.length();

        Map<String, FieldReader> fieldReaders = new LinkedHashMap<>();

        final String prefix = builderWithPrefix;
        final FieldInfo fieldInfo = new FieldInfo();
        BeanUtils.setters(builderClass, false, method -> {
            fieldInfo.init();
            provider.module.getFieldInfo(fieldInfo, objectClass, method);

            if (fieldInfo.ignore) {
                return;
            }

            String methodName = method.getName();
            String fieldName;
            if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
                final int methodNameLength = methodName.length();
                boolean prefixNotMach = methodNameLength <= prefix.length() || !methodName.startsWith(prefix, 0);
                if (prefixNotMach) {
                    if ((method.getDeclaringClass() != Object.class && method.getReturnType() == builderClass)
                            && (method.getAnnotation(JSONField.class) != null
                            || (beanInfo.readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) != 0)
                    ) {
                        fieldName = methodName;
                    } else {
                        return;
                    }
                } else {
                    fieldName = BeanUtils.setterName(methodName, builderWithPrefixLength);
                }
            } else {
                fieldName = fieldInfo.fieldName;
            }

            if (method.getParameterTypes().length == 0) {
                FieldReader fieldReader = createFieldReaderMethod(
                        builderClass,
                        builderClass,
                        fieldName,
                        fieldInfo.ordinal,
                        fieldInfo.features,
                        fieldInfo.format,
                        fieldInfo.locale,
                        fieldInfo.defaultValue,
                        method.getGenericReturnType(),
                        method.getReturnType(),
                        method,
                        null
                );
                FieldReader origin = fieldReaders.get(fieldName);
                if (origin == null) {
                    fieldReaders.put(fieldName, fieldReader);
                } else if (origin.compareTo(fieldReader) > 0) {
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
                    fieldType,
                    fieldClass,
                    method,
                    null);

            FieldReader origin = fieldReaders.get(fieldName);
            if (origin == null) {
                fieldReaders.put(fieldName, fieldReader);
            } else if (origin.compareTo(fieldReader) > 0) {
                fieldReaders.put(fieldName, fieldReader);
            }

            String[] alternateNames = fieldInfo.alternateNames;
            if (alternateNames != null) {
                for (int i = 0; i < alternateNames.length; i++) {
                    String alternateName = alternateNames[i];
                    if (fieldName.equals(alternateName)) {
                        continue;
                    }

                    FieldReader alterOrigin = fieldReaders.get(alternateName);
                    if (alterOrigin == null) {
                        fieldReaders.put(
                                alternateName,
                                createFieldReaderMethod(
                                        builderClass,
                                        objectType,
                                        alternateName,
                                        fieldInfo.ordinal,
                                        fieldInfo.features,
                                        fieldInfo.format,
                                        fieldInfo.locale,
                                        fieldInfo.defaultValue,
                                        fieldType,
                                        fieldClass,
                                        method,
                                        null));
                    }
                }
            }
        });

        FieldReader[] fieldReaderArray = new FieldReader[fieldReaders.size()];
        fieldReaders.values().toArray(fieldReaderArray);
        Arrays.sort(fieldReaderArray);

        Supplier instanceSupplier = createSupplier(builderClass);
        return createObjectReader(builderClass, 0, instanceSupplier, builderFunction, fieldReaderArray);
    }

    protected <T> ObjectReader<T> createObjectReaderWithCreator(
            Class<T> objectClass,
            Type objectType,
            ObjectReaderProvider provider,
            BeanInfo beanInfo
    ) {
        FieldInfo fieldInfo = new FieldInfo();

        Map<String, FieldReader> fieldReaders = new LinkedHashMap<>();

        Class declaringClass;
        Class[] parameters;
        String[] paramNames;
        Type[] genericParameterTypes;
        Annotation[][] parameterAnnotations;
        if (beanInfo.creatorConstructor != null) {
            parameters = beanInfo.creatorConstructor.getParameterTypes();
            declaringClass = beanInfo.creatorConstructor.getDeclaringClass();
            paramNames = BeanUtils.lookupParameterNames(beanInfo.creatorConstructor);
            genericParameterTypes = beanInfo.creatorConstructor.getGenericParameterTypes();
            parameterAnnotations = beanInfo.creatorConstructor.getParameterAnnotations();
        } else {
            parameters = beanInfo.createMethod.getParameterTypes();
            declaringClass = beanInfo.createMethod.getDeclaringClass();
            paramNames = new String[parameters.length];
            genericParameterTypes = beanInfo.createMethod.getGenericParameterTypes();
            parameterAnnotations = beanInfo.createMethod.getParameterAnnotations();
        }

        for (int i = 0; i < parameters.length; i++) {
            fieldInfo.init();

            if (beanInfo.creatorConstructor != null) {
                provider.getFieldInfo(fieldInfo, objectClass, beanInfo.creatorConstructor, i, parameterAnnotations);
            } else {
                provider.getFieldInfo(fieldInfo, objectClass, beanInfo.createMethod, i);
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
                    fieldName = "arg" + i;
                }
            }
            if (fieldName.startsWith("arg", 0)) {
                if (paramNames.length > i) {
                    fieldName = paramNames[i];
                }
            } else {
                paramNames[i] = fieldName;
            }

            String finalFieldName = fieldName;
            Class<?> paramClass = parameters[i];
            BeanUtils.getters(objectClass, method -> {
                if (method.getReturnType() != paramClass) {
                    return;
                }

                FieldInfo methodFieldInfo = new FieldInfo();
                provider.module.getFieldInfo(methodFieldInfo, objectClass, method);
                String methodFieldName = methodFieldInfo.fieldName;
                if (methodFieldName == null) {
                    methodFieldName = BeanUtils.getterName(method, PropertyNamingStrategy.CamelCase.name());
                }

                if (methodFieldInfo.readUsing != null && finalFieldName.equals(methodFieldName)
                ) {
                    fieldInfo.readUsing = methodFieldInfo.readUsing;
                }
            });

            if (fieldName == null || fieldName.isEmpty()) {
                fieldName = "arg" + i;
            }

            Type paramType = genericParameterTypes[i];
            ObjectReader initReader = getInitReader(provider, paramType, paramClass, fieldInfo);
            FieldReader fieldReaderParam = createFieldReaderParam(
                    objectClass,
                    objectType,
                    fieldName,
                    i,
                    fieldInfo.features,
                    fieldInfo.format,
                    paramType,
                    paramClass,
                    fieldName,
                    declaringClass,
                    initReader
            );
            fieldReaders.put(fieldName, fieldReaderParam);

            String[] alternateNames = fieldInfo.alternateNames;
            if (alternateNames != null) {
                for (int j = 0; j < alternateNames.length; j++) {
                    String alternateName = alternateNames[j];
                    if (fieldName.equals(alternateName)) {
                        continue;
                    }
                    FieldReader alterOrigin = fieldReaders.get(alternateName);
                    if (alterOrigin == null) {
                        fieldReaders.put(
                                alternateName,
                                createFieldReaderParam(
                                        objectClass,
                                        objectType,
                                        alternateName,
                                        i,
                                        fieldInfo.features,
                                        fieldInfo.format,
                                        paramType,
                                        paramClass,
                                        fieldName,
                                        declaringClass,
                                        null
                                ));
                    }
                }
            }
        }

        if (parameters.length == 1 && (fieldInfo.features & FieldInfo.VALUE_MASK) != 0) {
            Type valueType = beanInfo.creatorConstructor == null
                    ? beanInfo.createMethod.getGenericParameterTypes()[0]
                    : genericParameterTypes[0];
            Class valueClass = beanInfo.creatorConstructor == null
                    ? beanInfo.createMethod.getParameterTypes()[0]
                    : beanInfo.creatorConstructor.getParameterTypes()[0];

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
        Arrays.sort(fieldReaderArray);

        FieldReader[] setterFieldReaders = createFieldReaders(objectClass, objectType, null, false, provider);
        Arrays.sort(setterFieldReaders);

        boolean[] flags = null;
        int maskCount = 0;
        for (int i = 0; i < setterFieldReaders.length; i++) {
            FieldReader setterFieldReader = setterFieldReaders[i];
            if (fieldReaders.containsKey(setterFieldReader.fieldName)) {
                if (flags == null) {
                    flags = new boolean[setterFieldReaders.length];
                }
                flags[i] = true;
                maskCount++;
            }
        }
        if (maskCount > 0) {
            FieldReader[] array = new FieldReader[setterFieldReaders.length - maskCount];
            int index = 0;
            for (int i = 0; i < setterFieldReaders.length; i++) {
                if (!flags[i]) {
                    array[index++] = setterFieldReaders[i];
                }
            }
            setterFieldReaders = array;
        }

        return (ObjectReader<T>) new ObjectReaderNoneDefaultConstructor(
                objectClass,
                beanInfo.typeKey,
                beanInfo.typeName,
                beanInfo.readerFeatures,
                function,
                paramNames,
                fieldReaderArray,
                setterFieldReaders,
                beanInfo.seeAlso,
                beanInfo.seeAlsoNames
        );
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            long features,
            Supplier<T> defaultCreator,
            Function buildFunction,
            FieldReader... fieldReaders
    ) {
        return createObjectReader(objectClass, null, features, defaultCreator, buildFunction, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            String typeKey,
            long features,
            Supplier<T> defaultCreator,
            Function buildFunction,
            FieldReader... fieldReaders
    ) {
        if (objectClass != null) {
            int modifiers = objectClass.getModifiers();
            if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
                return new ObjectReaderAdapter(objectClass, typeKey, null, features, defaultCreator, buildFunction, fieldReaders);
            }
        }

        switch (fieldReaders.length) {
            case 1:
                return new ObjectReader1(
                        objectClass,
                        features,
                        defaultCreator,
                        buildFunction,
                        fieldReaders[0]
                );
            case 2:
                return new ObjectReader2(
                        objectClass,
                        features,
                        defaultCreator,
                        buildFunction,
                        fieldReaders[0],
                        fieldReaders[1]);
            case 3:
                return new ObjectReader3(
                        objectClass,
                        defaultCreator,
                        features,
                        buildFunction,
                        fieldReaders[0],
                        fieldReaders[1],
                        fieldReaders[2]
                );
            case 4:
                return new ObjectReader4(
                        objectClass,
                        features,
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
                        buildFunction,
                        fieldReaders[0],
                        fieldReaders[1],
                        fieldReaders[2],
                        fieldReaders[3],
                        fieldReaders[4],
                        fieldReaders[5]
                );
            default:
                return new ObjectReaderAdapter(objectClass, typeKey, null, features, defaultCreator, buildFunction, fieldReaders);
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
                createSupplier(objectClass),
                fieldReaderArray);
    }

    public <T> ObjectReader<T> createObjectReader(Class<T> objectType) {
        return createObjectReader(
                objectType,
                objectType,
                false,
                JSONFactory.defaultObjectReaderProvider
        );
    }

    public <T> ObjectReader<T> createObjectReader(Class<T> objectType, boolean fieldBased) {
        return createObjectReader(
                objectType,
                objectType,
                fieldBased,
                JSONFactory.defaultObjectReaderProvider
        );
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

        provider.getBeanInfo(beanInfo, objectClass);

        if (beanInfo.deserializer != null && ObjectReader.class.isAssignableFrom(beanInfo.deserializer)) {
            try {
                Constructor constructor = beanInfo.deserializer.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (ObjectReader<T>) constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new JSONException("create deserializer error", e);
            }
        }

        if (fieldBased) {
            beanInfo.readerFeatures |= JSONReader.Feature.FieldBased.mask;
        }

        if (Enum.class.isAssignableFrom(objectClass) && (beanInfo.createMethod == null || beanInfo.createMethod.getParameterTypes().length == 1)) {
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
        boolean allReadOnlyOrZero = true;
        for (int i = 0; i < fieldReaderArray.length; i++) {
            if (!fieldReaderArray[i].isReadOnly()) {
                allReadOnlyOrZero = false;
                break;
            }
        }

        if (beanInfo.creatorConstructor != null || beanInfo.createMethod != null) {
            return createObjectReaderWithCreator(objectClass, objectType, provider, beanInfo);
        }

        if (beanInfo.builder != null) {
            return createObjectReaderWithBuilder(objectClass, objectType, provider, beanInfo);
        }

        Constructor creatorConstructor = null;

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

            int parameterCount = constructor.getParameterTypes().length;
            if (parameterCount == 0) {
                defaultConstructor = constructor;
            }

            if (declaringClass != null
                    && parameterCount == 1
                    && declaringClass.equals(constructor.getParameterTypes()[0])) {
                creatorConstructor = constructor;
                index = i;
                break;
            } else if (creatorConstructor == null) {
                creatorConstructor = constructor;
                index = i;
            } else if (parameterCount == 0) {
                creatorConstructor = constructor;
                index = i;
            } else if (creatorConstructor.getParameterTypes().length < parameterCount) {
                creatorConstructor = constructor;
                index = i;
            }
        }

        int creatorConstructorParameterCount;
        if (creatorConstructor != null) {
            creatorConstructorParameterCount = creatorConstructor.getParameterTypes().length;
        } else {
            creatorConstructorParameterCount = -1;
        }

        if (index != -1) {
            alternateConstructors.remove(index);
        }

        if (creatorConstructor != null && creatorConstructorParameterCount != 0 && beanInfo.seeAlso == null) {
            creatorConstructor.setAccessible(true);
            String[] parameterNames = beanInfo.createParameterNames;
            if (parameterNames == null || parameterNames.length == 0) {
                parameterNames = BeanUtils.lookupParameterNames(creatorConstructor);

                Class[] parameters = creatorConstructor.getParameterTypes();
                Annotation[][] parameterAnnotations = creatorConstructor.getParameterAnnotations();
                FieldInfo fieldInfo = new FieldInfo();
                for (int i = 0; i < parameters.length && i < parameterNames.length; i++) {
                    fieldInfo.init();

                    provider.getFieldInfo(fieldInfo, objectClass, creatorConstructor, i, parameterAnnotations);
                    if (fieldInfo.fieldName != null) {
                        parameterNames[i] = fieldInfo.fieldName;
                    }
                }
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

            if (!fieldBased
                    && !(Throwable.class.isAssignableFrom(objectClass))
                    && defaultConstructor == null
                    && matchCount != parameterNames.length) {
                if (creatorConstructorParameterCount == 1) {
                    FieldInfo fieldInfo = new FieldInfo();
                    provider.getFieldInfo(fieldInfo, objectClass, creatorConstructor, 0, null);
                    if ((fieldInfo.features & FieldInfo.VALUE_MASK) != 0) {
                        Type valueType = creatorConstructor.getGenericParameterTypes()[0];
                        Class valueClass = creatorConstructor.getParameterTypes()[0];

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
                                creatorConstructor,
                                null,
                                null
                        );
                    }
                }

                if (allReadOnlyOrZero && fieldReaderArray.length != 0 && alternateConstructors.isEmpty()
                ) {
                    for (int i = 0; i < parameterNames.length; i++) {
                        String paramName = parameterNames[i];
                        for (FieldReader fieldReader : fieldReaderArray) {
                            if (fieldReader.field != null
                                    && fieldReader.field.getName().equals(paramName)
                                    && !fieldReader.fieldName.equals(paramName)
                            ) {
                                parameterNames[i] = fieldReader.fieldName;
                                break;
                            }
                        }
                    }
                }

                Function<Map<Long, Object>, T> constructorFunction = new ConstructorFunction(
                        alternateConstructors,
                        creatorConstructor,
                        null,
                        null,
                        null,
                        parameterNames
                );

                FieldReader[] paramFieldReaders = createFieldReaders(
                        provider,
                        objectClass,
                        objectType,
                        creatorConstructor,
                        creatorConstructor.getParameterTypes(),
                        parameterNames
                );
                return new ObjectReaderNoneDefaultConstructor(
                        objectClass,
                        beanInfo.typeKey,
                        beanInfo.typeName,
                        beanInfo.readerFeatures,
                        constructorFunction,
                        parameterNames,
                        paramFieldReaders,
                        fieldReaderArray,
                        null,
                        null
                );
            }
        }

        if (beanInfo.seeAlso != null && beanInfo.seeAlso.length != 0) {
            return createObjectReaderSeeAlso(
                    objectClass,
                    beanInfo.typeKey,
                    beanInfo.seeAlso,
                    beanInfo.seeAlsoNames,
                    beanInfo.seeAlsoDefault,
                    fieldReaderArray
            );
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

        Supplier<T> creator = createSupplier(objectClass);
        ObjectReader<T> objectReader = createObjectReader(
                objectClass,
                beanInfo.typeKey,
                beanInfo.readerFeatures,
                creator,
                null,
                fieldReaderArray);

        if (objectReader instanceof ObjectReaderBean) {
            JSONReader.AutoTypeBeforeHandler beforeHandler = null;
            if (beanInfo.autoTypeBeforeHandler != null) {
                try {
                    Constructor constructor = beanInfo.autoTypeBeforeHandler.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    beforeHandler = (JSONReader.AutoTypeBeforeHandler) constructor.newInstance();
                } catch (Exception ignored) {
                    // ignored
                }
            }

            if (beforeHandler != null) {
                ((ObjectReaderBean<T>) objectReader).setAutoTypeBeforeHandler(beforeHandler);
            }
        }

        return objectReader;
    }

    public <T> FieldReader[] createFieldReaders(Class<T> objectClass) {
        return createFieldReaders(
                objectClass,
                objectClass,
                null,
                false,
                JSONFactory.defaultObjectReaderProvider
        );
    }

    public <T> FieldReader[] createFieldReaders(Class<T> objectClass, Type objectType) {
        return createFieldReaders(
                objectClass,
                objectType,
                null,
                false,
                JSONFactory.defaultObjectReaderProvider
        );
    }

    final class FieldConsumer
            implements Consumer {
        final Class objectClass;
        final Type objectType;
        final String namingStrategy;
        final FieldInfo fieldInfo;
        final Map<String, FieldReader> fieldReaders;
        final ObjectReaderProvider provider;
        final BeanInfo beanInfo;
        final boolean fieldBased;

        public FieldConsumer(
                Class objectClass,
                Type objectType,
                String namingStrategy,
                FieldInfo fieldInfo,
                Map<String, FieldReader> fieldReaders,
                ObjectReaderProvider provider,
                BeanInfo beanInfo,
                boolean fieldBased
        ) {
            this.objectClass = objectClass;
            this.objectType = objectType;
            this.namingStrategy = namingStrategy;
            this.fieldInfo = fieldInfo;
            this.fieldReaders = fieldReaders;
            this.provider = provider;
            this.beanInfo = beanInfo;
            this.fieldBased = fieldBased;
        }

        @Override
        public void accept(Object o) {
            Field field = (Field) o;

            fieldInfo.init();
            if (fieldBased) {
                fieldInfo.features |= JSONReader.Feature.FieldBased.mask;
            } else {
                fieldInfo.ignore = (field.getModifiers() & Modifier.PUBLIC) == 0;
            }
            fieldInfo.features |= beanInfo.readerFeatures;
            fieldInfo.format = beanInfo.format;

            provider.getFieldInfo(fieldInfo, objectClass, field);

            if (fieldInfo.ignore) {
                boolean unwrap = (fieldInfo.features & FieldInfo.UNWRAPPED_MASK) != 0
                        && Map.class.isAssignableFrom(field.getType());
                if (!unwrap) {
                    return;
                }
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

            ObjectReader initReader = getInitReader(provider, fieldType, fieldClass, fieldInfo);

            FieldReader<Object> fieldReader = createFieldReader(
                    objectClass,
                    objectType,
                    fieldName,
                    fieldInfo.ordinal,
                    fieldInfo.features,
                    fieldInfo.format,
                    fieldInfo.locale,
                    fieldInfo.defaultValue,
                    fieldType,
                    fieldClass,
                    field,
                    initReader
            );

            FieldReader previous = fieldReaders.get(fieldName);
            if (previous == null) {
                fieldReaders.put(fieldName, fieldReader);
            } else {
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

                    FieldReader<Object> fieldReader1 = createFieldReader(
                            objectClass,
                            objectType,
                            alternateName,
                            fieldInfo.ordinal,
                            fieldInfo.features,
                            null,
                            fieldInfo.locale,
                            fieldInfo.defaultValue,
                            fieldType,
                            fieldClass,
                            field,
                            null
                    );
                    FieldReader alterOrigin = fieldReaders.get(alternateName);
                    if (alterOrigin == null) {
                        fieldReaders.put(alternateName, fieldReader1);
                    }
                }
            }
        }
    }

    final class CreateFieldReaderConsumer
            implements Consumer {
        final Class objectClass;
        final Type objectType;
        final String namingStrategy;
        final String[] orders;
        final BeanInfo beanInfo;
        final FieldInfo fieldInfo;
        final Map<String, FieldReader> fieldReaders;
        final ObjectReaderProvider provider;

        CreateFieldReaderConsumer(
                Class objectClass,
                Type objectType,
                String namingStrategy,
                String[] orders,
                BeanInfo beanInfo,
                FieldInfo fieldInfo,
                Map<String, FieldReader> fieldReaders,
                ObjectReaderProvider provider
        ) {
            this.objectClass = objectClass;
            this.objectType = objectType;
            this.namingStrategy = namingStrategy;
            this.orders = orders;
            this.beanInfo = beanInfo;
            this.fieldInfo = fieldInfo;
            this.fieldReaders = fieldReaders;
            this.provider = provider;
        }

        @Override
        public void accept(Object o) {
            Method method = (Method) o;

            fieldInfo.init();
            fieldInfo.features |= beanInfo.readerFeatures;
            fieldInfo.format = beanInfo.format;

            provider.module.getFieldInfo(fieldInfo, objectClass, method);

            if (fieldInfo.ignore) {
                return;
            }

            String fieldName;
            if (fieldInfo.fieldName == null) {
                String methodName = method.getName();
                if (methodName.startsWith("set", 0)) {
                    fieldName = BeanUtils.setterName(methodName, namingStrategy);
                    if (fieldInfo.alternateNames == null) {
                        String findName = methodName.substring(3);
                        Field field = BeanUtils.getDeclaredField(objectClass, findName);
                        if (field != null) {
                            fieldInfo.alternateNames = new String[]{findName};
                        }
                    }
                } else {
                    fieldName = BeanUtils.getterName(method, namingStrategy);
                }

                char c0 = '\0', c1;
                int len = fieldName.length();
                if (len > 0) {
                    c0 = fieldName.charAt(0);
                }

                if ((len == 1 && c0 >= 'a' && c0 <= 'z')
                        || (len > 2 && c0 >= 'A' && c0 <= 'Z' && (c1 = fieldName.charAt(1)) >= 'A' && c1 <= 'Z')
                ) {
                    char[] chars = fieldName.toCharArray();
                    if (len == 1) {
                        chars[0] = (char) (chars[0] - 32);
                    } else {
                        chars[0] = (char) (chars[0] + 32);
                    }
                    String fieldName1 = new String(chars);
                    Field field = BeanUtils.getDeclaredField(objectClass, fieldName1);
                    if (field != null) {
                        if (Modifier.isPublic(field.getModifiers())) {
                            fieldName = field.getName();
                        } else if (len == 1) {
                            fieldInfo.alternateNames = new String[]{fieldName};
                            fieldName = field.getName();
                        }
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

            Class<?>[] parameterTypes = method.getParameterTypes();
            int parameterCount = parameterTypes.length;
            if (parameterCount == 0) {
                FieldReader fieldReader = createFieldReaderMethod(
                        objectClass,
                        objectType,
                        fieldName,
                        fieldInfo.ordinal,
                        fieldInfo.features | READ_ONLY,
                        fieldInfo.format,
                        fieldInfo.locale,
                        fieldInfo.defaultValue,
                        method.getGenericReturnType(),
                        method.getReturnType(),
                        method,
                        fieldInfo.getInitReader()
                );
                FieldReader origin = fieldReaders.get(fieldName);
                if (origin == null) {
                    fieldReaders.put(fieldName, fieldReader);
                } else if (origin.compareTo(fieldReader) > 0) {
                    fieldReaders.put(fieldName, fieldReader);
                }
                return;
            }

            if (parameterCount == 2) {
                Class<?> fieldClass = parameterTypes[1];
                Type fieldType = method.getGenericParameterTypes()[1];
                method.setAccessible(true);
                FieldReaderAnySetter anySetter = new FieldReaderAnySetter(fieldType, fieldClass, fieldInfo.ordinal, fieldInfo.features, fieldInfo.format, method);
                fieldReaders.put(anySetter.fieldName, anySetter);
                return;
            }

            Class fieldClass = parameterTypes[0];
            Type fieldType;
            if (fieldClass.isPrimitive() || fieldClass == String.class || fieldClass.isEnum()) {
                fieldType = fieldClass;
            } else {
                fieldType = method.getGenericParameterTypes()[0];
            }

            ObjectReader initReader = getInitReader(provider, fieldType, fieldClass, fieldInfo);
            FieldReader fieldReader = null;
            fieldReader = createFieldReaderMethod(
                    objectClass,
                    objectType,
                    fieldName,
                    fieldInfo.ordinal,
                    fieldInfo.features,
                    fieldInfo.format,
                    fieldInfo.locale,
                    fieldInfo.defaultValue,
                    fieldType,
                    fieldClass,
                    method,
                    initReader
            );

            FieldReader origin = fieldReaders.get(fieldName);
            if (origin == null) {
                fieldReaders.put(fieldName, fieldReader);
            } else if (origin.compareTo(fieldReader) > 0) {
                fieldReaders.put(fieldName, fieldReader);
            }

            if (fieldInfo.alternateNames != null) {
                for (String alternateName : fieldInfo.alternateNames) {
                    if (fieldName.equals(alternateName)) {
                        continue;
                    }

                    FieldReader alterOrigin = fieldReaders.get(alternateName);
                    if (alterOrigin == null) {
                        fieldReaders.put(
                                alternateName,
                                createFieldReaderMethod(
                                        objectClass,
                                        objectType,
                                        alternateName,
                                        fieldInfo.ordinal,
                                        fieldInfo.features,
                                        fieldInfo.format,
                                        fieldInfo.locale,
                                        fieldInfo.defaultValue,
                                        fieldType,
                                        fieldClass,
                                        method,
                                        initReader
                                ));
                    }
                }
            }
        }
    }

    protected <T> FieldReader[] createFieldReaders(
            Class<T> objectClass,
            Type objectType,
            BeanInfo beanInfo,
            boolean fieldBased,
            ObjectReaderProvider provider
    ) {
        if (beanInfo == null) {
            beanInfo = new BeanInfo();
            provider.module.getBeanInfo(beanInfo, objectClass);
        }

        final String namingStrategy = beanInfo.namingStrategy;

        Map<String, FieldReader> fieldReaders = new LinkedHashMap<>();

        final FieldInfo fieldInfo = new FieldInfo();
        final String[] orders = beanInfo.orders;

        FieldConsumer fieldConsumer = new FieldConsumer(
                objectClass,
                objectType,
                namingStrategy,
                fieldInfo,
                fieldReaders,
                provider,
                beanInfo,
                fieldBased
        );
        if (fieldBased) {
            BeanUtils.declaredFields(objectClass, fieldConsumer);
        } else {
            BeanUtils.declaredFields(objectClass, fieldConsumer);

            Class mixIn = provider.mixInCache.get(objectClass);
            final Consumer<Method> consumer = new CreateFieldReaderConsumer(objectClass, objectType, namingStrategy, orders, beanInfo, fieldInfo, fieldReaders, provider);
            BeanUtils.setters(objectClass, mixIn, consumer);

            if (objectClass.isInterface()) {
                BeanUtils.getters(objectClass, consumer);
            }
        }

        FieldReader[] fieldReaderArray = new FieldReader[fieldReaders.size()];
        fieldReaders.values().toArray(fieldReaderArray);
        Arrays.sort(fieldReaderArray);
        return fieldReaderArray;
    }

    public <T> Supplier<T> createSupplier(Class<T> objectClass) {
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

        return createSupplier(constructor, true);
    }

    public <T> Supplier<T> createSupplier(Constructor constructor, boolean jit) {
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
        return createFieldReaderMethod(
                objectType,
                objectType,
                fieldName,
                0,
                0L,
                null,
                null,
                null,
                fieldType,
                fieldClass,
                method,
                null
        );
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
        return createFieldReaderMethod(
                objectClass,
                objectClass,
                fieldName,
                0,
                0L,
                format,
                null,
                null,
                fieldType,
                fieldClass,
                method,
                null
        );
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
            Class declaringClass
    ) {
        return createFieldReaderParam(
                objectClass,
                objectType,
                fieldName,
                ordinal,
                features,
                format,
                fieldType,
                fieldClass,
                paramName,
                declaringClass,
                null
        );
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
            Class declaringClass,
            ObjectReader initReader
    ) {
        if (initReader != null) {
            FieldReaderObjectParam paramReader = new FieldReaderObjectParam(
                    fieldName,
                    fieldType,
                    fieldClass,
                    paramName,
                    ordinal,
                    features,
                    format
            );
            paramReader.initReader = initReader;
            return paramReader;
        }

        if (fieldType == byte.class || fieldType == Byte.class) {
            return new FieldReaderInt8Param(fieldName, fieldClass, paramName, ordinal, features, format);
        }

        if (fieldType == short.class || fieldType == Short.class) {
            return new FieldReaderInt16Param(fieldName, fieldClass, paramName, ordinal, features, format);
        }

        if (fieldType == int.class || fieldType == Integer.class) {
            return new FieldReaderInt32Param(fieldName, fieldClass, paramName, ordinal, features, format);
        }

        if (fieldType == long.class || fieldType == Long.class) {
            return new FieldReaderInt64Param(fieldName, fieldClass, paramName, ordinal, features, format);
        }

        Type fieldTypeResolved = null;
        Class fieldClassResolved = null;
        if (!(fieldType instanceof Class) && objectType != null) {
            fieldTypeResolved = BeanUtils.getParamType(TypeReference.get(objectType), objectClass, declaringClass, fieldType);
            if (fieldTypeResolved != null) {
                fieldClassResolved = TypeUtils.getMapping(fieldTypeResolved);
            }
        }
        if (fieldTypeResolved == null) {
            fieldTypeResolved = fieldType;
        }
        if (fieldClassResolved == null) {
            fieldClassResolved = fieldClass;
        }

        return new FieldReaderObjectParam(fieldName, fieldTypeResolved, fieldClassResolved, paramName, ordinal, features, format);
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
                    method,
                    null,
                    null
            );
            fieldReaderObjectMethod.initReader = initReader;
            return fieldReaderObjectMethod;
        }

        if (fieldType == boolean.class) {
            return new FieldReaderBoolValueMethod(fieldName, ordinal, features, format, (Boolean) defaultValue, method);
        }

        if (fieldType == Boolean.class) {
            return new FieldReaderBoolMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Boolean) defaultValue, method);
        }

        if (fieldType == byte.class) {
            return new FieldReaderInt8ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Byte) defaultValue, method);
        }

        if (fieldType == short.class) {
            return new FieldReaderInt16ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Short) defaultValue, method);
        }

        if (fieldType == int.class) {
            return new FieldReaderInt32ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, (Integer) defaultValue, method);
        }

        if (fieldType == long.class) {
            return new FieldReaderInt64ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Long) defaultValue, method);
        }

        if (fieldType == float.class) {
            return new FieldReaderFloatValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Float) defaultValue, method);
        }

        if (fieldType == double.class) {
            return new FieldReaderDoubleValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Double) defaultValue, method);
        }

        if (fieldType == Byte.class) {
            return new FieldReaderInt8Method(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Byte) defaultValue, method);
        }

        if (fieldType == Short.class) {
            return new FieldReaderInt16Method(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (Short) defaultValue, method);
        }

        if (fieldType == Integer.class) {
            return new FieldReaderInt32Method(fieldName, ordinal, features, format, locale, (Integer) defaultValue, method);
        }

        if (fieldType == Long.class) {
            return new FieldReaderInt64Method(fieldName, ordinal, features, format, locale, (Long) defaultValue, method);
        }

        if (fieldType == Float.class) {
            return new FieldReaderFloatMethod(fieldName, ordinal, features, format, locale, (Float) defaultValue, method);
        }

        if (fieldType == Double.class) {
            return new FieldReaderDoubleMethod(fieldName, ordinal, features, format, (Double) defaultValue, method);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldReaderBigDecimalMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (BigDecimal) defaultValue, method);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldReaderBigIntegerMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (BigInteger) defaultValue, method);
        }

        if (fieldType == String.class) {
            return new FieldReaderStringMethod(fieldName, fieldType, fieldClass, ordinal, features, format, locale, (String) defaultValue, method);
        }

        if (fieldType == UUID.class) {
            return new FieldReaderUUID(
                    fieldName,
                    fieldType,
                    fieldClass,
                    ordinal,
                    features,
                    format,
                    locale,
                    defaultValue,
                    method,
                    null,
                    null
            );
        }

        if (fieldType == String[].class) {
            return new FieldReaderStringArray(
                    fieldName,
                    fieldType,
                    fieldClass,
                    ordinal,
                    features,
                    format,
                    locale,
                    defaultValue,
                    method,
                    null,
                    null
            );
        }

        if (method.getParameterTypes().length == 0) {
            if (fieldClass == AtomicInteger.class) {
                return new FieldReaderAtomicIntegerMethodReadOnly(fieldName, fieldClass, ordinal, method);
            }

            if (fieldClass == AtomicLong.class) {
                return new FieldReaderAtomicLongReadOnly(fieldName, fieldClass, ordinal, method);
            }

            if (fieldClass == AtomicIntegerArray.class) {
                return new FieldReaderAtomicIntegerArrayReadOnly(fieldName, fieldClass, ordinal, method);
            }

            if (fieldClass == AtomicLongArray.class) {
                return new FieldReaderAtomicLongArrayReadOnly(fieldName, fieldClass, ordinal, method);
            }

            if (fieldClass == AtomicBoolean.class) {
                return new FieldReaderAtomicBooleanMethodReadOnly(fieldName, fieldClass, ordinal, method);
            }

            if (fieldClass == AtomicReference.class) {
                return new FieldReaderAtomicReferenceMethodReadOnly(fieldName, fieldType, fieldClass, ordinal, method);
            }

            if (Collection.class.isAssignableFrom(fieldClass)) {
                Field field = null;
                String methodName = method.getName();
                if (methodName.startsWith("get", 0)) {
                    String getterName = BeanUtils.getterName(methodName, PropertyNamingStrategy.CamelCase.name());
                    field = BeanUtils.getDeclaredField(method.getDeclaringClass(), getterName);
                }
                return new FieldReaderCollectionMethodReadOnly(
                        fieldName,
                        fieldType,
                        fieldClass,
                        ordinal,
                        features,
                        format,
                        method,
                        field
                );
            }

            if (Map.class.isAssignableFrom(fieldClass)) {
                Field field = null;
                String methodName = method.getName();
                if (methodName.startsWith("get", 0)) {
                    String getterName = BeanUtils.getterName(methodName, PropertyNamingStrategy.CamelCase.name());
                    field = BeanUtils.getDeclaredField(method.getDeclaringClass(), getterName);
                }
                return new FieldReaderMapMethodReadOnly(
                        fieldName,
                        fieldType,
                        fieldClass,
                        ordinal,
                        features,
                        format,
                        method,
                        field
                );
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

        if (fieldClass == List.class || fieldClass == ArrayList.class) {
            if (fieldTypeResolved instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) fieldTypeResolved;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length == 1) {
                    Type itemType = actualTypeArguments[0];
                    Class itemClass = TypeUtils.getMapping(itemType);

                    if (itemClass == String.class) {
                        return new FieldReaderList(fieldName, fieldTypeResolved, fieldClass, String.class, String.class, ordinal, features, format, locale, null, method, null, null);
                    }

                    return new FieldReaderList(fieldName, fieldTypeResolved, fieldClassResolved, itemType, itemClass, ordinal, features, format, locale, null, method, null, null);
                }
            }
            return new FieldReaderList(fieldName, fieldType, fieldClass, Object.class, Object.class, ordinal, features, format, locale, null, method, null, null);
        }

        if (fieldClass == Date.class) {
            return new FieldReaderDate(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, null, method, null);
        }

        if (fieldClass == StackTraceElement[].class && method.getDeclaringClass() == Throwable.class) {
            return new FieldReaderStackTrace(
                    fieldName,
                    fieldTypeResolved != null ? fieldTypeResolved : fieldType,
                    fieldClass,
                    ordinal,
                    features,
                    format,
                    locale,
                    defaultValue,
                    method,
                    null,
                    (BiConsumer<Throwable, StackTraceElement[]>) Throwable::setStackTrace
            );
        }

        Field field = null;
        if ((features & FieldInfo.UNWRAPPED_MASK) != 0) {
            String methodName = method.getName();
            if (methodName.startsWith("set", 0)) {
                String setterName = BeanUtils.setterName(methodName, PropertyNamingStrategy.CamelCase.name());
                field = BeanUtils.getDeclaredField(method.getDeclaringClass(), setterName);
                try {
                    field.setAccessible(true);
                } catch (Throwable ignored) {
                    // ignored
                }
            }
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
                method,
                field,
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
        return createFieldReader(
                objectClass,
                objectType,
                fieldName,
                0,
                features,
                format,
                null,
                null,
                fieldType,
                field.getType(),
                field,
                null
        );
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
            Type fieldType,
            Class fieldClass,
            Field field,
            ObjectReader initReader
    ) {
        if (defaultValue != null && defaultValue.getClass() != fieldClass) {
            ObjectReaderProvider provider = JSONFactory
                    .getDefaultObjectReaderProvider();
            Function typeConvert = provider.getTypeConvert(defaultValue.getClass(), fieldType);
            if (typeConvert != null) {
                defaultValue = typeConvert.apply(defaultValue);
            } else {
                throw new JSONException("illegal defaultValue : " + defaultValue + ", class " + fieldClass.getName());
            }
        }

        if (field != null) {
            String objectClassName = objectClass.getName();
            if (!objectClassName.startsWith("java.lang", 0)) {
                field.setAccessible(true);
            }
        }

        if (initReader != null) {
            FieldReaderObjectField fieldReader = new FieldReaderObjectField(fieldName, fieldType, fieldClass, ordinal, features | FieldInfo.READ_USING_MASK, format, defaultValue, field);
            fieldReader.initReader = initReader;
            return fieldReader;
        }

        if (fieldClass == int.class) {
            return new FieldReaderInt32ValueField(fieldName, fieldClass, ordinal, format, (Integer) defaultValue, field);
        }
        if (fieldClass == Integer.class) {
            return new FieldReaderInt32Field(fieldName, fieldClass, ordinal, features, format, (Integer) defaultValue, field);
        }

        if (fieldClass == long.class) {
            return new FieldReaderInt64ValueField(fieldName, fieldClass, ordinal, features, format, (Long) defaultValue, field);
        }
        if (fieldClass == Long.class) {
            return new FieldReaderInt64Field(fieldName, fieldClass, ordinal, features, format, (Long) defaultValue, field);
        }

        if (fieldClass == short.class) {
            return new FieldReaderInt16ValueField(fieldName, fieldClass, ordinal, features, format, (Short) defaultValue, field);
        }

        if (fieldClass == Short.class) {
            return new FieldReaderInt16Field(fieldName, fieldClass, ordinal, features, format, (Short) defaultValue, field);
        }

        if (fieldClass == boolean.class) {
            return new FieldReaderBoolValueField(fieldName, ordinal, features, format, (Boolean) defaultValue, field);
        }

        if (fieldClass == Boolean.class) {
            return new FieldReaderBoolField(fieldName, fieldClass, ordinal, features, format, (Boolean) defaultValue, field);
        }

        if (fieldClass == byte.class) {
            return new FieldReaderInt8ValueField(fieldName, fieldClass, ordinal, features, format, (Byte) defaultValue, field);
        }

        if (fieldClass == Byte.class) {
            return new FieldReaderInt8Field(fieldName, fieldClass, ordinal, features, format, (Byte) defaultValue, field);
        }

        if (fieldClass == float.class) {
            return new FieldReaderFloatValueField(fieldName, fieldClass, ordinal, features, format, (Float) defaultValue, field);
        }
        if (fieldClass == Float.class) {
            return new FieldReaderFloatField(fieldName, fieldClass, ordinal, features, format, (Float) defaultValue, field);
        }

        if (fieldClass == double.class) {
            return new FieldReaderDoubleValueField(fieldName, fieldClass, ordinal, features, format, (Double) defaultValue, field);
        }
        if (fieldClass == Double.class) {
            return new FieldReaderDoubleField(fieldName, fieldClass, ordinal, features, format, (Double) defaultValue, field);
        }

        if (fieldClass == char.class) {
            return new FieldReaderCharValueField(fieldName, ordinal, features, format, (Character) defaultValue, field);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldReaderBigDecimalField(fieldName, fieldClass, ordinal, features, format, (BigDecimal) defaultValue, field);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldReaderBigIntegerField(fieldName, fieldClass, ordinal, features, format, (BigInteger) defaultValue, field);
        }

        if (fieldClass == String.class) {
            return new FieldReaderStringField(fieldName, fieldClass, ordinal, features, format, (String) defaultValue, field);
        }

        if (fieldClass == Date.class) {
            return new FieldReaderDate(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, field, null, null);
        }

        if (fieldClass == AtomicBoolean.class) {
            return new FieldReaderAtomicBooleanFieldReadOnly(fieldName, fieldClass, ordinal, format, (AtomicBoolean) defaultValue, field);
        }

        if (fieldClass == AtomicReference.class) {
            return new FieldReaderAtomicReferenceField(fieldName, fieldType, fieldClass, ordinal, format, field);
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
                            if ((features & JSONReader.Feature.FieldBased.mask) != 0) {
                                return new FieldReaderListFieldUF(fieldName, fieldTypeResolved, fieldClassResolved, String.class, String.class, ordinal, features, format, locale, null, field);
                            }

                            return new FieldReaderCollectionFieldReadOnly(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, format, field);
                        }

                        return new FieldReaderListFieldUF(fieldName, fieldTypeResolved, fieldClassResolved, String.class, String.class, ordinal, features, format, locale, null, field);
                    }

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
                            field
                    );
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
                    field
            );
        }

        if (Map.class.isAssignableFrom(fieldClass)) {
            if (fieldTypeResolved instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) fieldTypeResolved;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (actualTypeArguments.length == 2) {
                    if (finalField && ((features & JSONReader.Feature.FieldBased.mask) == 0)) {
                        return new FieldReaderMapFieldReadOnly(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, format, field);
                    }
                }
            }
        }

        if (finalField) {
            if (fieldClass == int[].class) {
                return new FieldReaderInt32ValueArrayFinalField(fieldName, fieldClass, ordinal, features, format, (int[]) defaultValue, field);
            }

            if (fieldClass == long[].class) {
                return new FieldReaderInt64ValueArrayFinalField(fieldName, fieldClass, ordinal, features, format, (long[]) defaultValue, field);
            }
        }

        if (fieldClassResolved != null) {
            if ((features & FieldInfo.UNWRAPPED_MASK) != 0
                    && Map.class.isAssignableFrom(fieldClassResolved)
            ) {
                return new FieldReaderMapFieldReadOnly(fieldName,
                        fieldTypeResolved,
                        fieldClass,
                        ordinal,
                        features,
                        format,
                        field);
            }

            return new FieldReaderObjectField(
                    fieldName,
                    fieldTypeResolved,
                    fieldClass,
                    ordinal,
                    features,
                    format,
                    defaultValue,
                    field);
        }

        return new FieldReaderObjectField(fieldName, fieldType, fieldClass, ordinal, features, format, defaultValue, field);
    }

    public <T, V> FieldReader createFieldReader(
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            Method method,
            BiConsumer<T, V> function
    ) {
        return createFieldReader(null, null, fieldName, fieldType, fieldClass, 0, 0, null, null, null, method, function, null);
    }

    public <T, V> FieldReader createFieldReader(
            Class objectClass,
            Type objectType,
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            Method method,
            BiConsumer<T, V> function,
            ObjectReader initReader
    ) {
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
                    method,
                    null,
                    function
            );
            fieldReaderObjectMethod.initReader = initReader;
            return fieldReaderObjectMethod;
        }

        if (fieldClass == Integer.class) {
            return new FieldReaderInt32Func<>(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, method, function);
        }

        if (fieldClass == Long.class) {
            return new FieldReaderInt64Func<>(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, method, function);
        }

        if (fieldClass == String.class) {
            return new FieldReaderStringFunc<>(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, method, function);
        }

        if (fieldClass == Boolean.class) {
            return new FieldReaderBoolFunc<>(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, method, function);
        }

        if (fieldClass == Short.class) {
            return new FieldReaderInt16Func(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, method, function);
        }

        if (fieldClass == Byte.class) {
            return new FieldReaderInt8Func(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, method, function);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldReaderBigDecimalFunc(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, method, function);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldReaderBigIntegerFunc(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, method, function);
        }

        if (fieldClass == Float.class) {
            return new FieldReaderFloatFunc(fieldName, fieldClass, ordinal, features, format, locale, (Float) defaultValue, method, function);
        }

        if (fieldClass == Double.class) {
            return new FieldReaderDoubleFunc(fieldName, fieldClass, ordinal, features, format, locale, (Double) defaultValue, method, function);
        }

        if (fieldClass == Number.class) {
            return new FieldReaderNumberFunc(fieldName, fieldClass, ordinal, features, format, locale, (Number) defaultValue, method, function);
        }

        if (fieldClass == Date.class) {
            return new FieldReaderDate(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, null, method, function);
        }

        Type fieldTypeResolved = null;
        Class fieldClassResolved = null;

        if (!(fieldType instanceof Class)) {
            TypeReference<?> objectTypeReference;
            if (objectType == null) {
                objectTypeReference = null;
            } else {
                objectTypeReference = TypeReference.get(objectType);
            }
            fieldTypeResolved = BeanUtils.getFieldType(objectTypeReference, objectClass, method, fieldType);
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
                        return new FieldReaderList(fieldName, fieldTypeResolved, fieldClassResolved, String.class, String.class, ordinal, features, format, locale, defaultValue, method, null, function);
                    }
                }
            }

            return new FieldReaderList(fieldName, fieldTypeResolved, fieldClassResolved, itemType, itemClass, ordinal, features, format, locale, defaultValue, method, null, function);
        }

        if (fieldTypeResolved != null) {
            return new FieldReaderObjectFunc<>(fieldName, fieldTypeResolved, fieldClass, ordinal, features, format, locale, defaultValue, method, function, null);
        }

        return new FieldReaderObjectFunc<>(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, method, function, null);
    }

    protected ObjectReader createEnumReader(
            Class objectClass,
            Method createMethod,
            ObjectReaderProvider provider
    ) {
        FieldInfo fieldInfo = new FieldInfo();

        Enum[] ordinalEnums = (Enum[]) objectClass.getEnumConstants();

        Map<Long, Enum> enumMap = new LinkedHashMap<>();
        for (int i = 0; ordinalEnums != null && i < ordinalEnums.length; ++i) {
            Enum e = ordinalEnums[i];
            String name = e.name();
            long hash = Fnv.hashCode64(name);
            enumMap.put(hash, e);

            try {
                fieldInfo.init();
                Field field = BeanUtils.getField(objectClass, name);
                provider.getFieldInfo(fieldInfo, objectClass, field);
                String jsonFieldName = fieldInfo.fieldName;
                if (jsonFieldName != null && !jsonFieldName.isEmpty() && !jsonFieldName.equals(name)) {
                    long jsonFieldNameHash = Fnv.hashCode64(jsonFieldName);
                    enumMap.put(jsonFieldNameHash, e);
                }
                if (fieldInfo.alternateNames != null) {
                    for (String alternateName : fieldInfo.alternateNames) {
                        if (alternateName == null || alternateName.isEmpty()) {
                            continue;
                        }
                        long alternateNameHash = Fnv.hashCode64(alternateName);
                        enumMap.put(alternateNameHash, e);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        for (int i = 0; ordinalEnums != null && i < ordinalEnums.length; ++i) {
            Enum e = ordinalEnums[i];
            String name = e.name();
            long hashLCase = Fnv.hashCode64LCase(name);
            enumMap.put(hashLCase, e);
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
        if (enumValueField == null) {
            if (provider != null) {
                Class fieldClassMixInSource = provider.mixInCache.get(objectClass);
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

    static ObjectReader getInitReader(
            ObjectReaderProvider provider,
            Type fieldType,
            Class fieldClass,
            FieldInfo fieldInfo
    ) {
        ObjectReader initReader = fieldInfo.getInitReader();
        if (initReader == null && Map.class.isAssignableFrom(fieldClass) && (fieldInfo.keyUsing != null || fieldInfo.valueUsing != null)) {
            ObjectReader keyReader = null;
            if (fieldInfo.keyUsing != null) {
                try {
                    Constructor<?> constructor = fieldInfo.keyUsing.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    keyReader = (ObjectReader) constructor.newInstance();
                } catch (Exception ignored) {
                    // ignored
                }
            }

            ObjectReader valueReader = null;
            if (fieldInfo.valueUsing != null) {
                try {
                    Constructor<?> constructor = fieldInfo.valueUsing.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    valueReader = (ObjectReader) constructor.newInstance();
                } catch (Exception ignored) {
                    // ignored
                }
            }

            if (keyReader != null || valueReader != null) {
                ObjectReader reader = ObjectReaderImplMap.of(fieldType, fieldClass, fieldInfo.features);
                if (reader instanceof ObjectReaderImplMapTyped) {
                    ObjectReaderImplMapTyped mapReader = (ObjectReaderImplMapTyped) reader;
                    if (keyReader != null) {
                        mapReader.keyObjectReader = keyReader;
                    }
                    if (valueReader != null) {
                        mapReader.valueObjectReader = valueReader;
                    }

                    return mapReader;
                }
            }
        }
        if (initReader == null) {
            if (fieldClass == BigDecimal.class) {
                ObjectReader objectReader = provider.getObjectReader(BigDecimal.class, false);
                if (objectReader != ObjectReaderImplBigDecimal.INSTANCE) {
                    initReader = objectReader;
                }
            } else if (fieldClass == BigInteger.class) {
                ObjectReader objectReader = provider.getObjectReader(BigInteger.class, false);
                if (objectReader != ObjectReaderImplBigInteger.INSTANCE) {
                    initReader = objectReader;
                }
            } else if (fieldClass == Date.class) {
                ObjectReader objectReader = provider.getObjectReader(Date.class, false);
                if (objectReader != ObjectReaderImplDate.INSTANCE) {
                    initReader = objectReader;
                }
            }
        }
        return initReader;
    }

    public <T> FieldReader<T> createFieldReader(
            String fieldName,
            Field field
    ) {
        return createFieldReader(fieldName, null, field.getGenericType(), field);
    }

    public <T> FieldReader createFieldReader(
            String fieldName,
            Method method
    ) {
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?>[] parameterTypes = method.getParameterTypes();

        Class fieldClass;
        Type fieldType;
        if (parameterTypes.length == 0) {
            fieldClass = method.getReturnType();
            fieldType = method.getGenericReturnType();
        } else if (parameterTypes.length == 1) {
            fieldClass = parameterTypes[0];
            fieldType = method.getGenericParameterTypes()[0];
        } else {
            throw new JSONException("illegal setter method " + method);
        }

        return createFieldReaderMethod(
                declaringClass,
                declaringClass,
                fieldName,
                0,
                0L,
                null,
                null,
                null,
                fieldType,
                fieldClass,
                method,
                null
        );
    }
}
