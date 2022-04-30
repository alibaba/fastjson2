package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

public class ObjectReaderCreator {
    public static final ObjectReaderCreator INSTANCE = new ObjectReaderCreator();

    public <T> ObjectReader<T> createObjectReaderNoneDefaultConstrutor(Constructor constructor, String... paramNames) {
        Function<Map<Long, Object>, T> function = createFunction(constructor, paramNames);
        FieldReader[] fieldReaders = createFieldReaders(constructor.getParameters(), paramNames);
        return createObjectReaderNoneDefaultConstrutor(constructor.getDeclaringClass(), function, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReaderNoneDefaultConstrutor(
            Class objectClass
            , Constructor constructor
            , String[] paramNames
            , FieldReader[] paramFieldReaders
            , FieldReader[] setterFieldReaders
    ) {
        Function<Map<Long, Object>, T> function = createFunction(constructor, paramNames);
        return new ObjectReaderNoneDefaultConstrutor(objectClass, null, 0, function, null, paramNames, paramFieldReaders, setterFieldReaders);
    }

    public <T> ObjectReader<T> createObjectReaderNoneDefaultConstrutor(
            Class objectClass,
            Function<Map<Long, Object>, T> creator,
            FieldReader... fieldReaders
    ) {

        return new ObjectReaderNoneDefaultConstrutor(objectClass, null, 0, creator, null, null, fieldReaders, null);
    }

    public <T> ObjectReader<T> createObjectReaderFactoryMethod(Method factoryMethod, String... paramNames) {
        Function<Map<Long, Object>, Object> factoryFunction = createFactoryFunction(factoryMethod, paramNames);
        FieldReader[] fieldReaders = createFieldReaders(factoryMethod.getParameters(), paramNames);
        return new ObjectReaderNoneDefaultConstrutor(null, null, 0, factoryFunction, null, paramNames, fieldReaders, null);
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
                    null
                    , null
                    , paramName
                    , null
                    , paramType
                    , parameter.getType()
                    , paramName
                    , parameter
                    , i
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

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectType,
            FieldReader... fieldReaders
    ) {
        return createObjectReader(
                objectType
                , null
                , 0
                , createInstanceSupplier(objectType)
                , null, fieldReaders
        );
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectType,
            Supplier<T> defaultCreator,
            FieldReader... fieldReaders
    ) {
        return createObjectReader(objectType, null, 0, defaultCreator, null, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectType
            , Class[] seeAlso
            , FieldReader... fieldReaders
    ) {
        Supplier<T> instanceSupplier = createInstanceSupplier(objectType);
        return new ObjectReaderSeeAlso(objectType, instanceSupplier, "@type", seeAlso, null, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectType
            , Supplier<T> defaultCreator
            , String typeKey
            , Class[] seeAlso
            , String[] seeAlsoNames
            , FieldReader... fieldReaders
    ) {
        return new ObjectReaderSeeAlso(objectType, defaultCreator, typeKey, seeAlso, seeAlsoNames, fieldReaders);
    }

    protected <T> ObjectReader<T> createObjectReaderWithBuilder(
            Class<T> objectType
            , List<ObjectReaderModule> modules
            , BeanInfo beanInfo) {
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
            for (ObjectReaderModule module : modules) {
                ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                if (annotationProcessor == null) {
                    continue;
                }
                annotationProcessor.getFieldInfo(fieldInfo, objectType, method);
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
                FieldReader fieldReader = createFieldReader(builderClass
                        , fieldName
                        , method.getGenericReturnType()
                        , method.getReturnType()
                        , method);
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
                    builderClass
                    , objectType
                    , fieldName
                    , fieldInfo.ordinal
                    , fieldInfo.features
                    , fieldInfo.format
                    , fieldType
                    , fieldClass
                    , method
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
                            .putIfAbsent(alternateName
                                    , createFieldReaderMethod(
                                            builderClass
                                            , objectType
                                            , alternateName
                                            , fieldInfo.ordinal
                                            , fieldInfo.features
                                            , fieldInfo.format
                                            , fieldType
                                            , fieldClass
                                            , method
                                    )
                            );
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
            Class<T> objectClass
            , List<ObjectReaderModule> modules
            , BeanInfo beanInfo) {
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

            for (ObjectReaderModule module : modules) {
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

            String fieldName = fieldInfo.fieldName;
            if (fieldName == null || fieldName.isEmpty()) {
                if (beanInfo.createParameterNames != null && i < beanInfo.createParameterNames.length) {
                    fieldName = beanInfo.createParameterNames[i];
                }

                if (fieldName == null || fieldName.isEmpty()) {
                    fieldName = parameter.getName();
                }
            }
            if (fieldName == null || fieldName.isEmpty() || fieldName.startsWith("arg")) {
                fieldName = paramNames[i];
            } else {
                paramNames[i] = fieldName;
            }

            Type paramType = parameter.getParameterizedType();
            fieldReaders.put(fieldName
                    , createFieldReaderParam(objectClass, objectClass, fieldName, fieldInfo.format, paramType, parameter.getType(), fieldName, parameter, i));

            if (fieldInfo.alternateNames != null) {
                for (String alternateName : fieldInfo.alternateNames) {
                    if (fieldName.equals(alternateName)) {
                        continue;
                    }

                    fieldReaders.putIfAbsent(alternateName
                            , createFieldReaderParam(objectClass, objectClass, alternateName, fieldInfo.format, paramType, parameter.getType(), fieldName, parameter, i));
                }
            }
        }

        Function<Map<Long, Object>, Object> function = null;
        if (beanInfo.creatorConstructor != null) {
            function = createFunction(beanInfo.creatorConstructor, paramNames);
        } else {
            function = createFactoryFunction(beanInfo.createMethod, paramNames);
        }

        FieldReader[] fieldReaderArray = new FieldReader[fieldReaders.size()];
        fieldReaders.values().toArray(fieldReaderArray);

        FieldReader[] setterFieldReaders = createFieldReaders(objectClass);
        Arrays.sort(setterFieldReaders);
        Arrays.sort(fieldReaderArray);

        return (ObjectReader<T>) new ObjectReaderNoneDefaultConstrutor(objectClass, beanInfo.typeName, beanInfo.readerFeatures, function, null, paramNames, fieldReaderArray, setterFieldReaders);
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass
            , long features
            , Supplier<T> defaultCreator
            , Function buildFunction
            , FieldReader... fieldReaders
    ) {
        return createObjectReader(objectClass, null, features, defaultCreator, buildFunction, fieldReaders);
    }

    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass
            , String typeKey
            , long features
            , Supplier<T> defaultCreator,
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
                        objectClass
                        , features
                        , defaultCreator
                        , buildFunction
                        , fieldReaders[0]
                );
            case 2:
                return new ObjectReader2(
                        objectClass
                        , features
                        , defaultCreator
                        , buildFunction
                        , fieldReaders[0]
                        , fieldReaders[1]);
            case 3:
                return new ObjectReader3(
                        objectClass
                        , defaultCreator
                        , features
                        , buildFunction
                        , fieldReaders[0]
                        , fieldReaders[1]
                        , fieldReaders[2]
                );
            case 4:
                return new ObjectReader4(
                        objectClass
                        , features
                        , defaultCreator
                        , buildFunction
                        , fieldReaders[0]
                        , fieldReaders[1]
                        , fieldReaders[2]
                        , fieldReaders[3]
                );
            case 5:
                return new ObjectReader5(
                        objectClass
                        , defaultCreator
                        , features
                        , buildFunction
                        , fieldReaders[0]
                        , fieldReaders[1]
                        , fieldReaders[2]
                        , fieldReaders[3]
                        , fieldReaders[4]
                );
            case 6:
                return new ObjectReader6(
                        objectClass, defaultCreator
                        , features
                        , buildFunction
                        , fieldReaders[0]
                        , fieldReaders[1]
                        , fieldReaders[2]
                        , fieldReaders[3]
                        , fieldReaders[4]
                        , fieldReaders[5]
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
                objectClass
                , createInstanceSupplier(objectClass)
                , fieldReaderArray);
    }

    public <T> ObjectReader<T> createObjectReader(Class<T> objectType) {
        return createObjectReader(
                objectType
                , objectType
                , false
                , JSONFactory
                        .getDefaultObjectReaderProvider()
                        .modules
        );
    }

    public <T> ObjectReader<T> createObjectReader(Class<T> objectType, boolean fieldBased) {
        return createObjectReader(
                objectType
                , objectType
                , fieldBased
                , JSONFactory
                        .getDefaultObjectReaderProvider()
                        .modules
        );
    }

    public <T> ObjectReader<T> createObjectReader(Class<T> objectClass, Type objectType, boolean fieldBased, List<ObjectReaderModule> modules) {
        BeanInfo beanInfo = new BeanInfo();

        for (ObjectReaderModule module : modules) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getBeanInfo(beanInfo, objectClass);
            }
        }

        if (fieldBased) {
            beanInfo.readerFeatures |= JSONReader.Feature.FieldBased.mask;
        }

        if (Enum.class.isAssignableFrom(objectClass)) {
            return createEnumReader(objectClass, beanInfo.createMethod, modules);
        }

        if (fieldBased && JDKUtils.JVM_VERSION >= 11 && !JDKUtils.LANG_UNNAMED && Throwable.class.isAssignableFrom(objectClass)) {
            fieldBased = false;
            beanInfo.readerFeatures |= JSONReader.Feature.IgnoreSetNullValue.mask;
        }

        if (fieldBased && objectClass.isInterface()) {
            fieldBased = false;
        }

        FieldReader[] fieldReaderArray = createFieldReaders(objectClass, objectType, beanInfo, fieldBased, modules);

        if (!fieldBased && Throwable.class.isAssignableFrom(objectClass)) {
            try {
                Constructor constructor = null;
                {
                    Constructor<?>[] constructors = objectClass.getConstructors();
                    for (Constructor<?> c : constructors) {
                        if (c.getParameterCount() != 2) {
                            continue;
                        }
                        Class<?>[] parameterTypes = c.getParameterTypes();
                        if (parameterTypes[0] == String.class && Throwable.class.isAssignableFrom(parameterTypes[1])) {
                            constructor = c;
                            break;
                        }
                    }
                }

                if (constructor != null) {
                    String[] paramNames = {"message", "cause"};
                    FieldReader[] constructorParamReaders = createFieldReaders(constructor.getParameters(), paramNames);
                    Arrays.sort(fieldReaderArray);
                    return createObjectReaderNoneDefaultConstrutor(
                            objectClass
                            , constructor
                            , paramNames
                            , constructorParamReaders
                            , fieldReaderArray
                    );
                }
            } catch (Throwable ignored) {
                //
            }
        }

        if (beanInfo.creatorConstructor != null || beanInfo.createMethod != null) {
            return createObjectReaderWithCreator(objectClass, modules, beanInfo);
        }

        if (beanInfo.builder != null) {
            return createObjectReaderWithBuilder(objectClass, modules, beanInfo);
        }

        Constructor creatorConstructor = beanInfo.creatorConstructor;


        final List<Constructor> alternateConstructors = new ArrayList<>();
        BeanUtils.constructor(objectClass, constructor -> {
            alternateConstructors.add(constructor);
        });

        Constructor defaultConstructor = null;

        int index = -1;
        for (int i = 0; i < alternateConstructors.size(); i++) {
            Constructor constructor = alternateConstructors.get(i);

            if (constructor.getParameterCount() == 0) {
                defaultConstructor = constructor;
            }

            if (creatorConstructor == null) {
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
                    for (int j = 0; j < fieldReaderArray.length; j++) {
                        if (parameterNames[i].equals(fieldReaderArray[j].getFieldName())) {
                            matchCount++;
                            break;
                        }
                    }
                }
            }

            if (!(fieldBased && JDKUtils.UNSAFE_SUPPORT)
                    && !(Throwable.class.isAssignableFrom(objectClass))
                    && defaultConstructor == null
                    && matchCount != parameterNames.length) {

                Function<Map<Long, Object>, T> function = new ConstructorFunction(alternateConstructors, creatorConstructor, parameterNames);
                FieldReader[] paramFieldReaders = createFieldReaders(creatorConstructor.getParameters(), parameterNames);
                return new ObjectReaderNoneDefaultConstrutor(
                        objectClass
                        , beanInfo.typeName
                        , beanInfo.readerFeatures
                        , function
                        , alternateConstructors
                        , parameterNames
                        , paramFieldReaders
                        , fieldReaderArray
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

        return createObjectReader(
                objectClass
                , beanInfo.typeKey
                , beanInfo.readerFeatures
                , creator
                , null
                , fieldReaderArray);
    }

    public <T> FieldReader[] createFieldReaders(Class<T> objectClass) {
        return createFieldReaders(
                objectClass
                , objectClass
                , null
                , false
                , JSONFactory
                        .getDefaultObjectReaderProvider()
                        .modules
        );
    }

    public <T> FieldReader[] createFieldReaders(Class<T> objectClass, Type objectType) {
        return createFieldReaders(
                objectClass
                , objectType
                , null
                , false
                , JSONFactory
                        .getDefaultObjectReaderProvider()
                        .modules
        );
    }

    protected void createFieldReader(
            Class objectClass,
            Type objectType,
            FieldInfo fieldInfo,
            Field field,
            Map<String, FieldReader> fieldReaders,
            List<ObjectReaderModule> modules) {
        for (ObjectReaderModule module : modules
        ) {
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
        } else {
            fieldName = fieldInfo.fieldName;
        }

        Type fieldType = field.getGenericType();
        Class<?> fieldClass = field.getType();

        FieldReader<Object> fieldReader = createFieldReader(
                objectClass
                , objectType
                , fieldName
                , fieldInfo.ordinal
                , fieldInfo.features
                , fieldInfo.format
                , fieldType
                , fieldClass
                , field);
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
                        .putIfAbsent(alternateName
                                , createFieldReader(
                                        objectClass
                                        , objectType
                                        , alternateName
                                        , fieldInfo.ordinal
                                        , fieldInfo.features
                                        , null
                                        , fieldType
                                        , fieldClass
                                        , field
                                )
                        );
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
            List<ObjectReaderModule> modules) {
        for (ObjectReaderModule module : modules) {
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
            fieldName = BeanUtils.setterName(method.getName(), namingStrategy);
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

        if (method.getParameterCount() == 0) {
            FieldReader fieldReader = createFieldReader(objectClass
                    , fieldName
                    , method.getGenericReturnType()
                    , method.getReturnType()
                    , method);
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

        FieldReader fieldReader = createFieldReaderMethod(
                objectClass
                , objectType
                , fieldName
                , fieldInfo.ordinal
                , fieldInfo.features
                , fieldInfo.format
                , fieldType
                , fieldClass
                , method
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
                        .putIfAbsent(alternateName
                                , createFieldReaderMethod(
                                        objectClass
                                        , objectType
                                        , alternateName
                                        , fieldInfo.ordinal
                                        , fieldInfo.features
                                        , fieldInfo.format
                                        , fieldType
                                        , fieldClass
                                        , method
                                )
                        );
            }
        }
    }

    protected <T> FieldReader[] createFieldReaders(Class<T> objectClass, Type objectType, BeanInfo beanInfo, boolean fieldBased, List<ObjectReaderModule> modules) {
        if (beanInfo == null) {
            beanInfo = new BeanInfo();
            for (ObjectReaderModule module : modules) {
                ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                if (annotationProcessor != null) {
                    annotationProcessor.getBeanInfo(beanInfo, objectClass);
                }
            }
        }

        final String namingStrategy = beanInfo.namingStrategy;

        Map<String, FieldReader> fieldReaders = new LinkedHashMap<>();

        final FieldInfo fieldInfo = new FieldInfo();
        final String[] orders = beanInfo.orders;
        if (fieldBased) {
            BeanUtils.declaredFields(objectClass, field -> {
                fieldInfo.init();
                fieldInfo.features |= JSONReader.Feature.FieldBased.mask;
                createFieldReader(objectClass, objectType, fieldInfo, field, fieldReaders, Collections.emptyList());
            });
        } else {
            BeanUtils.fields(objectClass, field -> {
                fieldInfo.init();
                createFieldReader(objectClass, objectType, fieldInfo, field, fieldReaders, modules);
            });

            BeanUtils.setters(objectClass, method -> {
                fieldInfo.init();
                createFieldReader(objectClass, objectType, namingStrategy, orders, fieldInfo, method, fieldReaders, modules);
            });
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

        return () -> {
            try {
                return constructor.newInstance();
            } catch (Throwable e) {
                throw new JSONException("create instance error", e);
            }
        };
    }

    public <T> Supplier<T> createInstanceSupplier(Constructor<T> constructor) {
        return () -> {
            try {
                return constructor.newInstance();
            } catch (Throwable e) {
                throw new JSONException("create instance error", e);
            }
        };
    }

    public <T> Supplier<T> createInstanceSupplier(Method staticFactoryMethod) {
        return () -> {
            try {
                return (T) staticFactoryMethod.invoke(null);
            } catch (Throwable e) {
                throw new JSONException("create instance error", e);
            }
        };
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
            Class<T> objectType
            , String fieldName
            , Type fieldType
            , Class fieldClass
            , Method method
    ) {
        return createFieldReaderMethod(objectType, objectType, fieldName, 0, 0L, null, fieldType, fieldClass, method);
    }

    public <T> FieldReader createFieldReader(
            Class<T> objectType
            , String fieldName
            , String format
            , Type fieldType
            , Class fieldClass
            , Method method
    ) {
        return createFieldReaderMethod(objectType, fieldName, format, fieldType, fieldClass, method);
    }

    public <T> FieldReader createFieldReaderMethod(
            Class<T> objectClass
            , String fieldName
            , String format
            , Type fieldType
            , Class fieldClass
            , Method method
    ) {
        return createFieldReaderMethod(objectClass, objectClass, fieldName, 0, 0L, format, fieldType, fieldClass, method);
    }

    protected <T> FieldReader createFieldReaderParam(
            Class<T> objectClass
            , Type objectType
            , String fieldName
            , String format
            , Type fieldType
            , Class fieldClass
            , String paramName
            , Parameter parameter
            , int ordinal
    ) {
        if (fieldType == byte.class || fieldType == Byte.class) {
            return new FieldReaderInt8Param(fieldName, fieldClass, paramName, parameter, ordinal);
        }

        if (fieldType == short.class || fieldType == Short.class) {
            return new FieldReaderInt16Param(fieldName, fieldClass, paramName, parameter, ordinal);
        }

        if (fieldType == int.class || fieldType == Integer.class) {
            return new FieldReaderInt32Param(fieldName, fieldClass, paramName, parameter, ordinal);
        }

        if (fieldType == long.class || fieldType == Long.class) {
            return new FieldReaderInt64Param(fieldName, fieldClass, paramName, parameter, ordinal);
        }

        return new FieldReaderObjectParam(fieldName, fieldType, fieldClass, paramName, parameter, ordinal, 0, format);
    }

    public <T> FieldReader createFieldReaderMethod(
            Class<T> objectClass
            , Type objectType
            , String fieldName
            , int ordinal
            , long features
            , String format
            , Type fieldType
            , Class fieldClass
            , Method method
    ) {
        if (method != null) {
            method.setAccessible(true);
        }

        if (fieldType == boolean.class) {
            return new FieldReaderBoolValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }
        if (fieldType == Boolean.class) {
            return new FieldReaderBoolMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == byte.class) {
            return new FieldReaderInt8ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == short.class) {
            return new FieldReaderInt16ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == int.class) {
            return new FieldReaderInt32ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == long.class) {
            return new FieldReaderInt64ValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == float.class) {
            return new FieldReaderFloatValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == double.class) {
            return new FieldReaderDoubleValueMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == Byte.class) {
            return new FieldReaderInt8Method(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == Short.class) {
            return new FieldReaderInt16Method(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == Integer.class) {
            return new FieldReaderInt32Method(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == Long.class) {
            return new FieldReaderInt64Method(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == Float.class) {
            return new FieldReaderFloatMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == Double.class) {
            return new FieldReaderDoubleMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldReaderBigDecimalMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldReaderBigIntegerMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldType == String.class) {
            return new FieldReaderStringMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (method.getParameterCount() == 0) {
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
                return new FieldReaderCollectionMethodReadOnly(fieldName, fieldType, fieldClass, ordinal, features, format, method);
            }

            return null;
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
                        return new FieldReaderListStrMethod(fieldName, fieldTypeResolved, fieldClass, ordinal, features, format, method);
                    }

                    return new FieldReaderListMethod(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, itemType, method);
                }
            }
            return new FieldReaderListMethod(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        }

        if (fieldClass == Date.class) {
            return new FieldReaderDateMethod(fieldName, fieldClass, ordinal, features, format, method);
        }

        if (fieldClass == StackTraceElement[].class && method.getDeclaringClass() == Throwable.class) {
            features |= JSONReader.Feature.IgnoreSetNullValue.mask;
        }

        return new FieldReaderObjectMethod(
                fieldName
                , fieldTypeResolved != null ? fieldTypeResolved : fieldType
                , fieldClass
                , ordinal
                , features
                , format
                , method
        );
    }

    public <T> FieldReader<T> createFieldReader(
            String fieldName
            , Type fieldType
            , Field field
    ) {
        return createFieldReader(fieldName, null, fieldType, field);
    }

    public <T> FieldReader<T> createFieldReader(
            String fieldName
            , String format
            , Type fieldType
            , Field field
    ) {
        Class objectClass = field.getDeclaringClass();
        return createFieldReader(objectClass, objectClass, fieldName, 0, format, fieldType, field.getType(), field);
    }

    public <T> FieldReader<T> createFieldReader(
            Class objectClass
            , Type objectType
            , String fieldName
            , long features
            , String format
            , Type fieldType
            , Class fieldClass
            , Field field
    ) {
        return createFieldReader(objectClass, objectType, fieldName, 0, features, format, fieldType, field.getType(), field);
    }

    public <T> FieldReader<T> createFieldReader(
            Class objectClass
            , Type objectType
            , String fieldName
            , int ordinal
            , long features
            , String format
            , Type fieldType
            , Class fieldClass
            , Field field
    ) {
        if (field != null) {
            if (!objectClass.getName().startsWith("java.lang")) {
                field.setAccessible(true);
            }
        }

        if (fieldClass == int.class) {
            return new FieldReaderInt32ValueField(fieldName, fieldClass, ordinal, field);
        }
        if (fieldClass == Integer.class) {
            return new FieldReaderInt32Field(fieldName, fieldClass, ordinal, field);
        }

        if (fieldClass == long.class) {
            return new FieldReaderInt64ValueField(fieldName, fieldClass, ordinal, field);
        }
        if (fieldClass == Long.class) {
            return new FieldReaderInt64Field(fieldName, fieldClass, ordinal, field);
        }

        if (fieldClass == short.class) {
            return new FieldReaderInt16ValueField(fieldName, fieldClass, ordinal, field);
        }

        if (fieldClass == Short.class) {
            return new FieldReaderInt16Field(fieldName, fieldClass, ordinal, field);
        }

        if (fieldClass == boolean.class) {
            return new FieldReaderBoolValueField(fieldName, fieldClass, ordinal, field);
        }
        if (fieldClass == Boolean.class) {
            return new FieldReaderBoolField(fieldName, fieldClass, ordinal, field);
        }

        if (fieldClass == byte.class) {
            return new FieldReaderInt8ValueField(fieldName, fieldClass, ordinal, field);
        }

        if (fieldClass == Byte.class) {
            return new FieldReaderInt8Field(fieldName, fieldClass, ordinal, field);
        }

        if (fieldClass == float.class) {
            return new FieldReaderFloatValueField(fieldName, fieldClass, ordinal, features, format, field);
        }
        if (fieldClass == Float.class) {
            return new FieldReaderFloatField(fieldName, fieldClass, ordinal, features, format, field);
        }

        if (fieldClass == double.class) {
            return new FieldReaderDoubleValueField(fieldName, fieldClass, ordinal, features, format, field);
        }
        if (fieldClass == Double.class) {
            return new FieldReaderDoubleField(fieldName, fieldClass, ordinal, field);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldReaderBigDecimalField(fieldName, fieldClass, ordinal, features, format, field);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldReaderBigIntegerField(fieldName, fieldClass, ordinal, field);
        }

        if (fieldClass == String.class) {
            return new FieldReaderStringField(fieldName, fieldClass, ordinal, features, format, field);
        }

        if (fieldClass == Date.class) {
            return new FieldReaderDateField(fieldName, fieldClass, ordinal, features, format, field);
        }

        if (fieldClass == AtomicBoolean.class) {
            return new FieldReaderAtomicBooleanFieldReadOnly(fieldName, fieldClass, ordinal, field);
        }

        if (fieldClass == AtomicReference.class) {
            return new FieldReaderAtomicReferenceField(fieldName, fieldType, fieldClass, ordinal, field);
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
                            if (JDKUtils.UNSAFE_SUPPORT && (features & JSONReader.Feature.FieldBased.mask) != 0) {
                                return new FieldReaderListStrFieldUF(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, format, field);
                            }

                            return new FieldReaderCollectionFieldReadOnly(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, format, field);
                        }

                        if (JDKUtils.UNSAFE_SUPPORT) {
                            return new FieldReaderListStrFieldUF(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, format, field);
                        }

                        return new FieldReaderListStrField(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, format, field);
                    }

                    if (JDKUtils.UNSAFE_SUPPORT) {
                        return new FieldReaderListFieldUF(fieldName, fieldTypeResolved, fieldClassResolved, itemType, ordinal, features, format, field);
                    }

                    return new FieldReaderListField(fieldName, fieldTypeResolved, fieldClassResolved, itemType, ordinal, features, format, field);
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

            if (JDKUtils.UNSAFE_SUPPORT) {
                return new FieldReaderListFieldUF(fieldName, fieldType, fieldClass, itemType, ordinal, features, format, field);
            }
            return new FieldReaderListField(fieldName, fieldType, fieldClass, itemType, ordinal, features, format, field);
        }

        if (Map.class.isAssignableFrom(fieldClass)) {
            if (fieldTypeResolved instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) fieldTypeResolved;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (actualTypeArguments.length == 2) {
                    if (finalField && (!JDKUtils.UNSAFE_SUPPORT || (features & JSONReader.Feature.FieldBased.mask) == 0)) {
                        return new FielderReaderImplMapFieldReadOnly(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, format, field);
                    }
                }
            }
        }

        if (finalField) {
            if (fieldClass == int[].class) {
                return new FieldReaderInt32ValueArrayFinalField(fieldName, fieldClass, ordinal, field);
            }

            if (fieldClass == long[].class) {
                return new FieldReaderInt64ValueArrayFinalField(fieldName, fieldClass, ordinal, field);
            }
        }

        if (fieldClassResolved != null) {
            if (JDKUtils.UNSAFE_SUPPORT) {
                return new FieldReaderObjectFieldUF(
                        fieldName
                        , fieldTypeResolved
                        , fieldClass
                        , ordinal
                        , features
                        , format
                        , field);
            } else {
                return new FieldReaderObjectField(
                        fieldName
                        , fieldTypeResolved
                        , fieldClass
                        , ordinal
                        , features
                        , format
                        , field);
            }
        }

        if (JDKUtils.UNSAFE_SUPPORT) {
            return new FieldReaderObjectFieldUF(fieldName, fieldType, fieldClass, ordinal, features, format, field);
        }
        return new FieldReaderObjectField(fieldName, fieldType, fieldClass, ordinal, features, format, field);
    }

    <T, V> FieldReader createFieldReader(
            String fieldName
            , Type fieldType
            , Class<V> fieldClass
            , Method method
            , BiConsumer<T, V> function
    ) {
        return createFieldReader(null, null, fieldName, fieldType, fieldClass, 0, 0, null, method, function);
    }

    <T, V> FieldReader createFieldReader(
            Class objectClass
            , Type objectType
            , String fieldName
            , Type fieldType
            , Class<V> fieldClass
            , int ordinal
            , long features
            , String format
            , Method method
            , BiConsumer<T, V> function
    ) {
        if (fieldClass == Integer.class) {
            return new FieldReaderInt32Func<>(fieldName, fieldClass, ordinal, method, function);
        }

        if (fieldClass == Long.class) {
            return new FieldReaderInt64Func<>(fieldName, fieldClass, ordinal, method, function);
        }

        if (fieldClass == String.class) {
            return new FieldReaderStringFunc<>(fieldName, fieldClass, ordinal, features, format, method, function);
        }

        if (fieldClass == Boolean.class) {
            return new FieldReaderBoolFunc<>(fieldName, fieldClass, ordinal, method, function);
        }

        if (fieldClass == Short.class) {
            return new FieldReaderInt16Func(fieldName, fieldClass, ordinal, method, function);
        }

        if (fieldClass == Byte.class) {
            return new FieldReaderInt8Func(fieldName, fieldClass, ordinal, method, function);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldReaderBigDecimalFunc(fieldName, fieldClass, ordinal, method, function);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldReaderBigIntegerFunc(fieldName, fieldClass, ordinal, method, function);
        }

        if (fieldClass == Number.class) {
            return new FieldReaderNumberFunc(fieldName, fieldClass, ordinal, method, function);
        }

        if (fieldClass == Date.class) {
            return new FieldReaderDateFunc(fieldName, fieldClass, ordinal, features, format, method, function);
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
                        return new FieldReaderListStrFunc(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, method, function);
                    }
                }
            }

            return new FieldReaderListFunc(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, method, function);
        }

        if (fieldTypeResolved != null) {
            return new FieldReaderObjectFunc<>(fieldName, fieldTypeResolved, fieldClass, ordinal, features, format, method, function);
        }

        return new FieldReaderObjectFunc<>(fieldName, fieldType, fieldClass, ordinal, features, format, method, function);
    }


    protected ObjectReader createEnumReader(
            Class objectClass,
            Method createMethod,
            List<ObjectReaderModule> modules
    ) {
        FieldInfo fieldInfo = new FieldInfo();

        Enum[] ordinalEnums = (Enum[]) objectClass.getEnumConstants();

        Map<Long, Enum> enumMap = new HashMap();
        for (int i = 0; i < ordinalEnums.length; ++i) {
            Enum e = ordinalEnums[i];
            String name = e.name();
            long hash = Fnv.hashCode64(name);
            enumMap.put(hash, e);

            try {
                fieldInfo.init();
                Field field = objectClass.getField(name);
                for (ObjectReaderModule module : modules) {
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

        for (int i = 0; i < ordinalEnums.length; ++i) {
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

        Member enumValueField = BeanUtils.getEnumValueField(objectClass);
        if (enumValueField == null && modules.size() > 0) {
            ObjectReaderProvider provider = modules.get(0).getProvider();
            if (provider != null) {
                Class fieldClassMixInSource = provider.getMixIn(objectClass);
                if (fieldClassMixInSource != null) {
                    Member mixedValueField = BeanUtils.getEnumValueField(fieldClassMixInSource);
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
            if (ordinalEnums.length == 2) {
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
