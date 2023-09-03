package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.support.LambdaMiscCodec;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import static com.alibaba.fastjson2.util.BeanUtils.SUPER;
import static com.alibaba.fastjson2.util.TypeUtils.*;

public class ObjectReaderCreator {
    public static final boolean JIT = !JDKUtils.ANDROID && !JDKUtils.GRAAL;
    public static final ObjectReaderCreator INSTANCE = new ObjectReaderCreator();

    protected final AtomicInteger jitErrorCount = new AtomicInteger();
    protected volatile Throwable jitErrorLast;

    protected static final Map<Class, LambdaSetterInfo> methodTypeMapping = new HashMap<>();

    static class LambdaSetterInfo {
        final Class fieldClass;
        final MethodType sameMethodMethod;
        final MethodType methodType;
        final MethodType invokedType;

        LambdaSetterInfo(Class fieldClass, Class functionClass) {
            this.fieldClass = fieldClass;
            this.sameMethodMethod = MethodType.methodType(void.class, Object.class, fieldClass);
            this.methodType = MethodType.methodType(void.class, fieldClass);
            this.invokedType = MethodType.methodType(functionClass);
        }
    }

    static {
        methodTypeMapping.put(boolean.class, new LambdaSetterInfo(boolean.class, ObjBoolConsumer.class));
        methodTypeMapping.put(byte.class, new LambdaSetterInfo(byte.class, ObjByteConsumer.class));
        methodTypeMapping.put(short.class, new LambdaSetterInfo(short.class, ObjShortConsumer.class));
        methodTypeMapping.put(int.class, new LambdaSetterInfo(int.class, ObjIntConsumer.class));
        methodTypeMapping.put(long.class, new LambdaSetterInfo(long.class, ObjLongConsumer.class));
        methodTypeMapping.put(char.class, new LambdaSetterInfo(char.class, ObjCharConsumer.class));
        methodTypeMapping.put(float.class, new LambdaSetterInfo(float.class, ObjFloatConsumer.class));
        methodTypeMapping.put(double.class, new LambdaSetterInfo(double.class, ObjDoubleConsumer.class));
    }

    public <T> ObjectReader<T> createObjectReaderNoneDefaultConstructor(Constructor constructor, String... paramNames) {
        Function<Map<Long, Object>, T> function = createFunction(constructor, paramNames);
        Class declaringClass = constructor.getDeclaringClass();
        FieldReader[] fieldReaders = createFieldReaders(
                JSONFactory.getDefaultObjectReaderProvider(),
                declaringClass,
                declaringClass,
                constructor,
                constructor.getParameters(),
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
                JSONFactory.getDefaultObjectReaderProvider(),
                null,
                null,
                factoryMethod,
                factoryMethod.getParameters(),
                paramNames
        );
        return new ObjectReaderNoneDefaultConstructor(
                null,
                null,
                null,
                0,
                factoryFunction,
                null,
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
            Executable owner,
            Parameter[] parameters,
            String... paramNames
    ) {
        Class<?> declaringClass = null;
        if (owner != null) {
            declaringClass = owner.getDeclaringClass();
        }

        FieldReader[] fieldReaders = new FieldReader[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            FieldInfo fieldInfo = new FieldInfo();

            Parameter parameter = parameters[i];
            String paramName;
            if (i < paramNames.length) {
                paramName = paramNames[i];
            } else {
                paramName = parameter.getName();
            }

            if (owner instanceof Constructor) {
                provider.getFieldInfo(fieldInfo, declaringClass, (Constructor) owner, i, parameter);
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

            ObjectReader initReader = getInitReader(provider, parameter.getParameterizedType(), parameter.getType(), fieldInfo);
            Type paramType = parameter.getParameterizedType();
            fieldReaders[i] = createFieldReaderParam(
                    null,
                    null,
                    fieldName,
                    i,
                    fieldInfo.features,
                    fieldInfo.format,
                    paramType,
                    parameter.getType(),
                    paramName,
                    declaringClass,
                    parameter,
                    null,
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
                null,
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
        return createObjectReader(objectClass, null, 0, null, defaultCreator, null, fieldReaders);
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
        int builderWithPrefixLenth = builderWithPrefix.length();

        Map<String, FieldReader> fieldReaders = new LinkedHashMap<>();

        final String prefix = builderWithPrefix;
        final FieldInfo fieldInfo = new FieldInfo();
        BeanUtils.setters(builderClass, false, method -> {
            fieldInfo.init();
            provider.getFieldInfo(fieldInfo, objectClass, method);

            if (fieldInfo.ignore) {
                return;
            }

            String methodName = method.getName();
            String fieldName;
            if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
                final int methodNameLength = methodName.length();
                boolean prefixNotMach = methodNameLength <= prefix.length() || !methodName.startsWith(prefix);
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
                    fieldName = BeanUtils.setterName(methodName, builderWithPrefixLenth);
                }
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
        Parameter[] parameters;
        String[] paramNames;
        if (beanInfo.creatorConstructor != null) {
            parameters = beanInfo.creatorConstructor.getParameters();
            declaringClass = beanInfo.creatorConstructor.getDeclaringClass();
            paramNames = ASMUtils.lookupParameterNames(beanInfo.creatorConstructor);
        } else {
            parameters = beanInfo.createMethod.getParameters();
            declaringClass = beanInfo.createMethod.getDeclaringClass();
            paramNames = ASMUtils.lookupParameterNames(beanInfo.createMethod);
        }

        for (int i = 0; i < parameters.length; i++) {
            fieldInfo.init();

            Parameter parameter = parameters[i];

            if (beanInfo.creatorConstructor != null) {
                provider.getFieldInfo(fieldInfo, objectClass, beanInfo.creatorConstructor, i, parameter);
            } else {
                provider.getFieldInfo(fieldInfo, objectClass, beanInfo.createMethod, i, parameter);
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

            String finalFieldName = fieldName;
            Class<?> paramClass = parameter.getType();
            BeanUtils.getters(objectClass, method -> {
                if (method.getReturnType() != paramClass) {
                    return;
                }

                FieldInfo methodFieldInfo = new FieldInfo();
                provider.getFieldInfo(methodFieldInfo, objectClass, method);
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

            Type paramType = parameter.getParameterizedType();
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
                    parameter,
                    null,
                    initReader
            );
            fieldReaders.put(fieldName, fieldReaderParam);

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
                                    paramClass,
                                    fieldName,
                                    declaringClass,
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

            boolean jit = JIT || (fieldInfo.features & FieldInfo.JIT) != 0 || (beanInfo.readerFeatures & FieldInfo.JIT) != 0;
            Function function = null;
            if (defaultValue == null && jit) {
                if (valueClass == int.class) {
                    IntFunction intFunction = null;
                    if (beanInfo.creatorConstructor != null) {
                        intFunction = createIntFunction(beanInfo.creatorConstructor);
                    } else if (beanInfo.createMethod != null) {
                        intFunction = createIntFunction(beanInfo.createMethod);
                    }

                    if (intFunction != null) {
                        return ObjectReaderImplValueInt.of(objectClass, fieldInfo.features, jsonSchema, intFunction);
                    }
                } else if (valueClass == String.class) {
                    if (beanInfo.creatorConstructor != null) {
                        function = createStringFunction(beanInfo.creatorConstructor);
                    } else if (beanInfo.createMethod != null) {
                        function = createStringFunction(beanInfo.createMethod);
                    }

                    if (function != null) {
                        return ObjectReaderImplValueString.of(objectClass, fieldInfo.features, jsonSchema, function);
                    }
                }
            }

            if (jit && !valueClass.isPrimitive()) {
                if (beanInfo.creatorConstructor != null) {
                    function = createValueFunction(beanInfo.creatorConstructor, valueClass);
                } else if (beanInfo.createMethod != null) {
                    function = createValueFunction(beanInfo.createMethod, valueClass);
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
                    function
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

        FieldReader[] setterFieldReaders = createFieldReaders(objectClass, objectType);
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
                null,
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
                createSupplier(objectClass),
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
        boolean allReadOnlyOrZero = true;
        for (int i = 0; i < fieldReaderArray.length; i++) {
            FieldReader fieldReader = fieldReaderArray[i];
            if (!fieldReader.isReadOnly()) {
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
        BeanUtils.constructor(objectClass, alternateConstructors::add);

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
            boolean record = BeanUtils.isRecord(objectClass);

            creatorConstructor.setAccessible(true);
            String[] parameterNames = beanInfo.createParameterNames;
            if (record && parameterNames == null) {
                parameterNames = BeanUtils.getRecordFieldNames(objectClass);
            }

            if (parameterNames == null || parameterNames.length == 0) {
                parameterNames = ASMUtils.lookupParameterNames(creatorConstructor);

                Parameter[] parameters = creatorConstructor.getParameters();
                FieldInfo fieldInfo = new FieldInfo();
                for (int i = 0; i < parameters.length && i < parameterNames.length; i++) {
                    fieldInfo.init();

                    Parameter parameter = parameters[i];

                    provider.getFieldInfo(fieldInfo, objectClass, creatorConstructor, i, parameter);
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

            if (!(fieldBased)
                    && !(Throwable.class.isAssignableFrom(objectClass))
                    && defaultConstructor == null
                    && matchCount != parameterNames.length) {
                if (creatorConstructor.getParameterCount() == 1) {
                    FieldInfo fieldInfo = new FieldInfo();
                    provider.getFieldInfo(fieldInfo, objectClass, creatorConstructor, 0, creatorConstructor.getParameters()[0]);
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

                if (allReadOnlyOrZero && fieldReaderArray.length != 0 && alternateConstructors.isEmpty()) {
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

                Function function = null;
                BiFunction biFunction = null;
                if (JIT) {
                    if (creatorConstructor.getParameterCount() == 1) {
                        function = LambdaMiscCodec.createFunction(creatorConstructor);
                    } else if (creatorConstructor.getParameterCount() == 2) {
                        biFunction = LambdaMiscCodec.createBiFunction(creatorConstructor);
                    }
                }

                Function<Map<Long, Object>, T> constructorFunction = new ConstructorFunction(
                        alternateConstructors,
                        creatorConstructor,
                        function,
                        biFunction,
                        null,
                        parameterNames
                );

                FieldReader[] paramFieldReaders = createFieldReaders(
                        provider,
                        objectClass,
                        objectType,
                        creatorConstructor,
                        creatorConstructor.getParameters(),
                        parameterNames
                );
                return new ObjectReaderNoneDefaultConstructor(
                        objectClass,
                        beanInfo.typeKey,
                        beanInfo.typeName,
                        beanInfo.readerFeatures,
                        constructorFunction,
                        alternateConstructors,
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
        JSONSchema jsonSchema = JSONSchema.of(JSON.parseObject(beanInfo.schema), objectClass);
        ObjectReader<T> objectReader = createObjectReader(
                objectClass,
                beanInfo.typeKey,
                beanInfo.readerFeatures,
                jsonSchema,
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
        String schema = fieldInfo.schema;
        if (fieldInfo.required) {
            if (schema == null) {
                schema = "{\"required\":true}";
            }
        }

        FieldReader<Object> fieldReader = createFieldReader(
                objectClass,
                objectType,
                fieldName,
                fieldInfo.ordinal,
                fieldInfo.features,
                fieldInfo.format,
                fieldInfo.locale,
                fieldInfo.defaultValue,
                schema,
                fieldType,
                fieldClass,
                field,
                initReader
        );

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

                FieldReader<Object> fieldReader1 = createFieldReader(
                        objectClass,
                        objectType,
                        alternateName,
                        fieldInfo.ordinal,
                        fieldInfo.features,
                        null,
                        fieldInfo.locale,
                        fieldInfo.defaultValue,
                        schema,
                        fieldType,
                        fieldClass,
                        field,
                        null
                );
                fieldReaders.putIfAbsent(alternateName, fieldReader1);
            }
        }
    }

    protected void createFieldReader(
            Class objectClass,
            Type objectType,
            String namingStrategy,
            String[] orders,
            BeanInfo beanInfo,
            FieldInfo fieldInfo,
            Method method,
            Map<String, FieldReader> fieldReaders,
            ObjectReaderProvider provider
    ) {
        provider.getFieldInfo(fieldInfo, objectClass, method);

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

        int parameterCount = method.getParameterCount();
        if (parameterCount == 0) {
            Type fieldType = method.getGenericReturnType();
            Class<?> fieldClass = method.getReturnType();
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
        if (fieldType instanceof Class && Collection.class.isAssignableFrom((Class<?>) fieldType)) {
            Class[] interfaces = objectClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                Method interfaceMethod = BeanUtils.getMethod(interfaces[i], method);
                if (interfaceMethod != null) {
                    Type[] genericParameterTypes = interfaceMethod.getGenericParameterTypes();
                    if (genericParameterTypes.length == 1
                            && genericParameterTypes[0] instanceof ParameterizedType
                    ) {
                        fieldType = genericParameterTypes[0];
                    }
                }
            }
        }

        // skip function
        if (isFunction(fieldClass)) {
            return;
        }

        ObjectReader initReader = getInitReader(provider, fieldType, fieldClass, fieldInfo);
        FieldReader fieldReader = null;
        boolean jit = (fieldInfo.features & FieldInfo.JIT) != 0;
        if (jit) {
            try {
                fieldReader = createFieldReaderLambda(
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
            } catch (Throwable ignored) {
                jitErrorCount.incrementAndGet();
                jitErrorLast = ignored;
            }
        }

        if (fieldReader == null) {
            fieldReader = createFieldReaderMethod(
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
        }

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

    protected <T> FieldReader[] createFieldReaders(
            Class<T> objectClass,
            Type objectType,
            BeanInfo beanInfo,
            boolean fieldBased,
            ObjectReaderProvider provider
    ) {
        if (beanInfo == null) {
            beanInfo = new BeanInfo();
            for (ObjectReaderModule module : provider.modules) {
                ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                if (annotationProcessor != null) {
                    annotationProcessor.getBeanInfo(beanInfo, objectClass);
                }
            }
        }

        boolean recoard = BeanUtils.isRecord(objectClass);
        final String namingStrategy = beanInfo.namingStrategy;

        Map<String, FieldReader> fieldReaders = new LinkedHashMap<>();

        BeanInfo finalBeanInfo = beanInfo;
        final long beanFeatures = beanInfo.readerFeatures;
        final String beanFormat = beanInfo.format;
        final FieldInfo fieldInfo = new FieldInfo();
        final String[] orders = beanInfo.orders;
        if (fieldBased) {
            BeanUtils.declaredFields(objectClass, field -> {
                fieldInfo.init();
                fieldInfo.features |= JSONReader.Feature.FieldBased.mask;
                fieldInfo.features |= beanFeatures;
                fieldInfo.format = beanFormat;

                createFieldReader(objectClass, objectType, namingStrategy, fieldInfo, field, fieldReaders, provider);
            });
        } else {
            if (!recoard) {
                BeanUtils.declaredFields(objectClass, field -> {
                    fieldInfo.init();
                    fieldInfo.ignore = (field.getModifiers() & Modifier.PUBLIC) == 0;
                    fieldInfo.features |= beanFeatures;
                    fieldInfo.format = beanFormat;

                    createFieldReader(objectClass, objectType, namingStrategy, fieldInfo, field, fieldReaders, provider);
                    if (fieldInfo.required) {
                        String fieldName = fieldInfo.fieldName;
                        if (fieldName == null || fieldName.isEmpty()) {
                            fieldName = field.getName();
                        }
                        finalBeanInfo.required(fieldName);
                    }
                });
            }

            Class mixIn = provider.getMixIn(objectClass);
            BeanUtils.setters(objectClass, mixIn, method -> {
                fieldInfo.init();
                fieldInfo.features |= beanFeatures;
                fieldInfo.format = beanFormat;
                createFieldReader(objectClass, objectType, namingStrategy, orders, finalBeanInfo, fieldInfo, method, fieldReaders, provider);
            });

            if (objectClass.isInterface()) {
                BeanUtils.getters(objectClass, method -> {
                    fieldInfo.init();
                    fieldInfo.features |= beanFeatures;
                    createFieldReader(objectClass, objectType, namingStrategy, orders, finalBeanInfo, fieldInfo, method, fieldReaders, provider);
                });
            }
        }

        Class<? super T> superclass = objectClass.getSuperclass();
        if (BeanUtils.isExtendedMap(objectClass)) {
            Type superType = objectClass.getGenericSuperclass();
            FieldReader fieldReader = ObjectReaders.fieldReader(
                    SUPER,
                    superType,
                    superclass,
                    (o, f) -> {
                        Map thisMap = (Map) o;
                        Map superMap = (Map) f;
                        // avoid putAll oom
                        for (Object value : superMap.entrySet()) {
                            Map.Entry entry = (Map.Entry) value;
                            thisMap.put(entry.getKey(), entry.getValue());
                        }
                    }
            );
            fieldReaders.put(SUPER, fieldReader);
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
        jit &= JIT;

        if (jit) {
            Class declaringClass = constructor.getDeclaringClass();
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
            try {
                if (constructor.getParameterCount() == 0) {
                    MethodHandle handle = lookup.findConstructor(declaringClass, METHOD_TYPE_VOID);
                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "get",
                            METHOD_TYPE_SUPPLIER,
                            METHOD_TYPE_OBJECT,
                            handle,
                            METHOD_TYPE_OBJECT
                    );
                    return (Supplier) callSite.getTarget().invokeExact();
                }
            } catch (Throwable e) {
                jitErrorCount.incrementAndGet();
                jitErrorLast = e;
            }
        }

        return new ConstructorSupplier(constructor);
    }

    protected <T> IntFunction<T> createIntFunction(Constructor constructor) {
        Class declaringClass = constructor.getDeclaringClass();
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
        try {
            MethodHandle handle = lookup.findConstructor(declaringClass, METHOD_TYPE_VOID_INT);
            MethodType instantiatedMethodType = MethodType.methodType(declaringClass, int.class);
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_INT_FUNCTION,
                    METHOD_TYPE_OBJECT_INT,
                    handle,
                    instantiatedMethodType
            );
            return (IntFunction) callSite.getTarget().invokeExact();
        } catch (Throwable e) {
            jitErrorCount.incrementAndGet();
            jitErrorLast = e;
        }

        return null;
    }

    protected <T> IntFunction<T> createIntFunction(Method factoryMethod) {
        Class declaringClass = factoryMethod.getDeclaringClass();
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
        try {
            MethodType methodType = MethodType.methodType(factoryMethod.getReturnType(), int.class);
            MethodHandle handle = lookup.findStatic(declaringClass, factoryMethod.getName(), methodType);
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_INT_FUNCTION,
                    METHOD_TYPE_OBJECT_INT,
                    handle,
                    methodType
            );
            return (IntFunction) callSite.getTarget().invokeExact();
        } catch (Throwable e) {
            jitErrorCount.incrementAndGet();
            jitErrorLast = e;
        }

        return null;
    }

    protected <T> Function<String, T> createStringFunction(Constructor constructor) {
        Class declaringClass = constructor.getDeclaringClass();
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
        try {
            MethodHandle handle = lookup.findConstructor(declaringClass, METHOD_TYPE_VOID_STRING);
            MethodType instantiatedMethodType = MethodType.methodType(declaringClass, String.class);
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_FUNCTION,
                    METHOD_TYPE_OBJECT_OBJECT,
                    handle,
                    instantiatedMethodType
            );
            return (Function<String, T>) callSite.getTarget().invokeExact();
        } catch (Throwable e) {
            jitErrorCount.incrementAndGet();
            jitErrorLast = e;
        }

        return null;
    }

    protected <T> Function<String, T> createStringFunction(Method factoryMethod) {
        Class declaringClass = factoryMethod.getDeclaringClass();
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
        try {
            MethodType methodType = MethodType.methodType(factoryMethod.getReturnType(), String.class);
            MethodHandle handle = lookup.findStatic(declaringClass, factoryMethod.getName(), methodType);
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_FUNCTION,
                    METHOD_TYPE_OBJECT_OBJECT,
                    handle,
                    methodType
            );
            return (Function<String, T>) callSite.getTarget().invokeExact();
        } catch (Throwable e) {
            jitErrorCount.incrementAndGet();
            jitErrorLast = e;
        }

        return null;
    }

    protected <I, T> Function<I, T> createValueFunction(Constructor<T> constructor, Class<I> valueClass) {
        Class declaringClass = constructor.getDeclaringClass();
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
        try {
            MethodType methodType = MethodType.methodType(void.class, valueClass);
            MethodHandle handle = lookup.findConstructor(declaringClass, methodType);
            MethodType instantiatedMethodType = MethodType.methodType(declaringClass, valueClass);
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_FUNCTION,
                    METHOD_TYPE_OBJECT_OBJECT,
                    handle,
                    instantiatedMethodType
            );
            return (Function<I, T>) callSite.getTarget().invokeExact();
        } catch (Throwable e) {
            jitErrorCount.incrementAndGet();
            jitErrorLast = e;
        }

        return null;
    }

    protected <I, T> Function<I, T> createValueFunction(Method factoryMethod, Class valueClass) {
        Class declaringClass = factoryMethod.getDeclaringClass();
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
        try {
            MethodType methodType = MethodType.methodType(factoryMethod.getReturnType(), valueClass);
            MethodHandle handle = lookup.findStatic(declaringClass, factoryMethod.getName(), methodType);
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_FUNCTION,
                    METHOD_TYPE_OBJECT_OBJECT,
                    handle,
                    methodType
            );
            return (Function<I, T>) callSite.getTarget().invokeExact();
        } catch (Throwable e) {
            jitErrorCount.incrementAndGet();
            jitErrorLast = e;
        }

        return null;
    }

    public <T, R> Function<T, R> createBuildFunction(Method builderMethod) {
        try {
            return createBuildFunctionLambda(builderMethod);
        } catch (Throwable e) {
            jitErrorCount.incrementAndGet();
            jitErrorLast = e;
        }

        builderMethod.setAccessible(true);

        return (T o) -> {
            try {
                return (R) builderMethod.invoke(o);
            } catch (Throwable e) {
                throw new JSONException("create instance error", e);
            }
        };
    }

    <T, R> Function<T, R> createBuildFunctionLambda(Method builderMethod) {
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(builderMethod.getDeclaringClass());
        try {
            MethodHandle target = lookup.findVirtual(builderMethod.getDeclaringClass(),
                    builderMethod.getName(),
                    MethodType.methodType(builderMethod.getReturnType())
            );
            MethodType func = target.type();

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_FUNCTION,
                    func.erase(),
                    target,
                    func
            );

            Object object = callSite
                    .getTarget()
                    .invoke();
            return (Function<T, R>) object;
        } catch (Throwable e) {
            throw new JSONException("create fieldReader error", e);
        }
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
            Class declaringClass,
            Parameter parameter,
            JSONSchema schema
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
                parameter,
                schema,
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
            Parameter parameter,
            JSONSchema schema,
            ObjectReader initReader
    ) {
        if (initReader != null) {
            FieldReaderObjectParam paramReader = new FieldReaderObjectParam(
                    fieldName,
                    fieldType,
                    fieldClass,
                    paramName,
                    parameter,
                    ordinal,
                    features,
                    format,
                    schema
            );
            paramReader.initReader = initReader;
            return paramReader;
        }

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

        Type fieldTypeResolved = null;
        Class fieldClassResolved = null;
        if (!(fieldType instanceof Class) && objectType != null) {
            fieldTypeResolved = BeanUtils.getParamType(TypeReference.get(objectType), objectClass, declaringClass, parameter, fieldType);
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

        return new FieldReaderObjectParam(fieldName, fieldTypeResolved, fieldClassResolved, paramName, parameter, ordinal, features, format, schema);
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
            return new FieldReaderBoolValueMethod(fieldName, ordinal, features, format, (Boolean) defaultValue, jsonSchema, method);
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
            return new FieldReaderInt32Method(fieldName, ordinal, features, format, locale, (Integer) defaultValue, jsonSchema, method);
        }

        if (fieldType == Long.class) {
            return new FieldReaderInt64Method(fieldName, ordinal, features, format, locale, (Long) defaultValue, jsonSchema, method);
        }

        if (fieldType == Float.class) {
            return new FieldReaderFloatMethod(fieldName, ordinal, features, format, locale, (Float) defaultValue, jsonSchema, method);
        }

        if (fieldType == Double.class) {
            return new FieldReaderDoubleMethod(fieldName, ordinal, features, format, (Double) defaultValue, jsonSchema, method);
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

        if (fieldType == LocalDate.class) {
            return new FieldReaderLocalDate(
                    fieldName,
                    fieldType,
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

        if (fieldType == OffsetDateTime.class) {
            return new FieldReaderOffsetDateTime(
                    fieldName,
                    fieldType,
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
                    jsonSchema,
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
                    jsonSchema,
                    method,
                    null,
                    null
            );
        }

        Type fieldTypeResolved = null;
        Class fieldClassResolved = null;
        if (!(fieldType instanceof Class) || !(objectType instanceof Class)) {
            fieldTypeResolved = BeanUtils.getFieldType(TypeReference.get(objectType), objectClass, method, fieldType);
            fieldClassResolved = TypeUtils.getMapping(fieldTypeResolved);
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
                Field field = null;
                String methodName = method.getName();
                if (methodName.startsWith("get")) {
                    String getterName = BeanUtils.getterName(methodName, PropertyNamingStrategy.CamelCase.name());
                    field = BeanUtils.getDeclaredField(method.getDeclaringClass(), getterName);
                }
                return new FieldReaderCollectionMethodReadOnly(
                        fieldName,
                        fieldTypeResolved != null ? fieldTypeResolved : fieldType,
                        fieldClass,
                        ordinal,
                        features,
                        format,
                        jsonSchema,
                        method,
                        field
                );
            }

            if (Map.class.isAssignableFrom(fieldClass)) {
                Field field = null;
                String methodName = method.getName();
                if (methodName.startsWith("get")) {
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
                        jsonSchema,
                        method,
                        field
                );
            }

            if (!objectClass.isInterface()) {
                return null;
            }
        }

        if (fieldClass == List.class
                || fieldClass == ArrayList.class
                || fieldClass == LinkedList.class
        ) {
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
            return new FieldReaderDate(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, jsonSchema, null, method, null);
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
                    jsonSchema,
                    method,
                    null,
                    (BiConsumer<Throwable, StackTraceElement[]>) Throwable::setStackTrace
            );
        }

        Field field = null;
        if ((features & FieldInfo.UNWRAPPED_MASK) != 0) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
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
                jsonSchema,
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
            Field field
    ) {
        return createFieldReader(fieldName, null, field.getGenericType(), field);
    }

    public <T> FieldReader createFieldReader(
            String fieldName,
            Method method
    ) {
        Class<?> declaringClass = method.getDeclaringClass();
        int parameterCount = method.getParameterCount();

        Class fieldClass;
        Type fieldType;
        if (parameterCount == 0) {
            fieldClass = method.getReturnType();
            fieldType = method.getGenericReturnType();
        } else if (parameterCount == 1) {
            fieldClass = method.getParameterTypes()[0];
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
                null,
                fieldType,
                fieldClass,
                method,
                null
        );
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
            String schema,
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
            return new FieldReaderBoolValueField(fieldName, ordinal, features, format, (Boolean) defaultValue, jsonSchema, field);
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
            return new FieldReaderDate(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, jsonSchema, field, null, null);
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
                            if ((features & JSONReader.Feature.FieldBased.mask) != 0) {
                                return new FieldReaderListField(fieldName, fieldTypeResolved, fieldClassResolved, String.class, String.class, ordinal, features, format, locale, null, jsonSchema, field);
                            }

                            return new FieldReaderCollectionFieldReadOnly(fieldName, fieldTypeResolved, fieldClassResolved, ordinal, features, format, jsonSchema, field);
                        }

                        return new FieldReaderListField(fieldName, fieldTypeResolved, fieldClassResolved, String.class, String.class, ordinal, features, format, locale, null, jsonSchema, field);
                    }

                    return new FieldReaderListField(
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

            return new FieldReaderListField(
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

        if (Map.class.isAssignableFrom(fieldClass)) {
            if (fieldTypeResolved instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) fieldTypeResolved;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (actualTypeArguments.length == 2) {
                    if (finalField && ((features & JSONReader.Feature.FieldBased.mask) == 0)) {
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
            if ((features & FieldInfo.UNWRAPPED_MASK) != 0
                    && Map.class.isAssignableFrom(fieldClassResolved)
            ) {
                return new FieldReaderMapFieldReadOnly(fieldName,
                        fieldTypeResolved,
                        fieldClass,
                        ordinal,
                        features,
                        format,
                        jsonSchema,
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
                    jsonSchema,
                    field);
        }

        if (fieldClass == LocalDateTime.class) {
            return new FieldReaderLocalDateTime(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, jsonSchema, field, null, null);
        }

        if (fieldClass == ZonedDateTime.class) {
            return new FieldReaderZonedDateTime(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, jsonSchema, field, null, null);
        }

        if (fieldClass == Instant.class) {
            return new FieldReaderInstant(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, jsonSchema, field, null, null);
        }

        return new FieldReaderObjectField(fieldName, fieldType, fieldClass, ordinal, features, format, defaultValue, jsonSchema, field);
    }

    public <T, V> FieldReader createFieldReader(
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            long features,
            BiConsumer<T, V> function
    ) {
        return createFieldReader(null, null, fieldName, fieldType, fieldClass, 0, features, null, null, null, null, null, function, null);
    }

    public <T, V> FieldReader createFieldReader(
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            Method method,
            BiConsumer<T, V> function
    ) {
        return createFieldReader(null, null, fieldName, fieldType, fieldClass, 0, 0, null, null, null, null, method, function, null);
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
            JSONSchema schema,
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
                    schema,
                    method,
                    null,
                    function
            );
            fieldReaderObjectMethod.initReader = initReader;
            return fieldReaderObjectMethod;
        }

        if (fieldClass == Integer.class) {
            return new FieldReaderInt32Func<>(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, function);
        }

        if (fieldClass == Long.class) {
            return new FieldReaderInt64Func<>(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, function);
        }

        if (fieldClass == String.class) {
            return new FieldReaderStringFunc<>(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, function);
        }

        if (fieldClass == Boolean.class) {
            return new FieldReaderBoolFunc<>(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, function);
        }

        if (fieldClass == Short.class) {
            return new FieldReaderInt16Func(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, function);
        }

        if (fieldClass == Byte.class) {
            return new FieldReaderInt8Func(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, function);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldReaderBigDecimalFunc(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, function);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldReaderBigIntegerFunc(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, function);
        }

        if (fieldClass == Float.class) {
            return new FieldReaderFloatFunc(fieldName, fieldClass, ordinal, features, format, locale, (Float) defaultValue, schema, method, function);
        }

        if (fieldClass == Double.class) {
            return new FieldReaderDoubleFunc(fieldName, fieldClass, ordinal, features, format, locale, (Double) defaultValue, schema, method, function);
        }

        if (fieldClass == Number.class) {
            return new FieldReaderNumberFunc(fieldName, fieldClass, ordinal, features, format, locale, (Number) defaultValue, schema, method, function);
        }

        if (fieldClass == Date.class) {
            return new FieldReaderDate(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, null, method, function);
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
                        return new FieldReaderList(fieldName, fieldTypeResolved, fieldClassResolved, String.class, String.class, ordinal, features, format, locale, defaultValue, schema, method, null, function);
                    }
                }
            }

            return new FieldReaderList(fieldName, fieldTypeResolved, fieldClassResolved, itemType, itemClass, ordinal, features, format, locale, defaultValue, schema, method, null, function);
        }

        if (fieldTypeResolved != null) {
            return new FieldReaderObjectFunc<>(fieldName, fieldTypeResolved, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, function, null);
        }

        return new FieldReaderObjectFunc<>(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, function, null);
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
                Field field = objectClass.getField(name);
                provider.getFieldInfo(fieldInfo, objectClass, field);
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

        Enum[] enums = new Enum[enumNameHashCodes.length];
        for (int i = 0; i < enumNameHashCodes.length; ++i) {
            long hash = enumNameHashCodes[i];
            Enum e = enumMap.get(hash);
            enums[i] = e;
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
            if (fieldClass == long.class || fieldClass == Long.class) {
                ObjectReader objectReader = provider.getObjectReader(Long.class);
                if (objectReader != ObjectReaderImplInt64.INSTANCE) {
                    initReader = objectReader;
                }
            } else if (fieldClass == BigDecimal.class) {
                ObjectReader objectReader = provider.getObjectReader(BigDecimal.class);
                if (objectReader != ObjectReaderImplBigDecimal.INSTANCE) {
                    initReader = objectReader;
                }
            } else if (fieldClass == BigInteger.class) {
                ObjectReader objectReader = provider.getObjectReader(BigInteger.class);
                if (objectReader != ObjectReaderImplBigInteger.INSTANCE) {
                    initReader = objectReader;
                }
            } else if (fieldClass == Date.class) {
                ObjectReader objectReader = provider.getObjectReader(Date.class);
                if (objectReader != ObjectReaderImplDate.INSTANCE) {
                    initReader = objectReader;
                }
            }
        }
        return initReader;
    }

    protected <T> FieldReader createFieldReaderLambda(
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
            BiConsumer function = (BiConsumer) lambdaSetter(objectClass, fieldClass, method);
            return createFieldReader(objectClass, objectType, fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, jsonSchema, method, function, initReader);
        }

        if (fieldType == boolean.class) {
            ObjBoolConsumer function = (ObjBoolConsumer) lambdaSetter(objectClass, fieldClass, method);
            return new FieldReaderBoolValFunc<>(fieldName, ordinal, jsonSchema, method, function);
        }

        if (fieldType == byte.class) {
            ObjByteConsumer function = (ObjByteConsumer) lambdaSetter(objectClass, fieldClass, method);
            return new FieldReaderInt8ValueFunc<>(fieldName, ordinal, jsonSchema, method, function);
        }

        if (fieldType == short.class) {
            ObjShortConsumer function = (ObjShortConsumer) lambdaSetter(objectClass, fieldClass, method);
            return new FieldReaderInt16ValueFunc<>(fieldName, ordinal, features, format, locale, (Short) defaultValue, jsonSchema, method, function);
        }

        if (fieldType == int.class) {
            ObjIntConsumer function = (ObjIntConsumer) lambdaSetter(objectClass, fieldClass, method);
            return new FieldReaderInt32ValueFunc<>(fieldName, ordinal, (Integer) defaultValue, jsonSchema, method, function);
        }

        if (fieldType == long.class) {
            ObjLongConsumer function = (ObjLongConsumer) lambdaSetter(objectClass, fieldClass, method);
            return new FieldReaderInt64ValueFunc<>(fieldName, ordinal, (Long) defaultValue, jsonSchema, method, function);
        }

        if (fieldType == char.class) {
            ObjCharConsumer function = (ObjCharConsumer) lambdaSetter(objectClass, fieldClass, method);
            return new FieldReaderCharValueFunc<>(fieldName, ordinal, format, (Character) defaultValue, jsonSchema, method, function);
        }

        if (fieldType == float.class) {
            ObjFloatConsumer function = (ObjFloatConsumer) lambdaSetter(objectClass, fieldClass, method);
            return new FieldReaderFloatValueFunc<>(fieldName, ordinal, (Float) defaultValue, jsonSchema, method, function);
        }

        if (fieldType == double.class) {
            ObjDoubleConsumer function = (ObjDoubleConsumer) lambdaSetter(objectClass, fieldClass, method);
            return new FieldReaderDoubleValueFunc<>(fieldName, ordinal, (Double) defaultValue, jsonSchema, method, function);
        }

        BiConsumer consumer = (BiConsumer) lambdaSetter(objectClass, fieldClass, method);
        return createFieldReader(objectClass, objectType, fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, jsonSchema, method, consumer, null);
    }

    protected Object lambdaSetter(Class objectClass, Class fieldClass, Method method) {
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);

        Class<?> returnType = method.getReturnType();
        LambdaSetterInfo lambdaInfo = methodTypeMapping.get(fieldClass);

        MethodType samMethodType;
        MethodType invokedType;
        MethodType methodType = null;
        if (lambdaInfo != null) {
            samMethodType = lambdaInfo.sameMethodMethod;
            invokedType = lambdaInfo.invokedType;
            if (returnType == void.class) {
                methodType = lambdaInfo.methodType;
            }
        } else {
            samMethodType = METHOD_TYPE_VOO;
            invokedType = METHOD_TYPE_BI_CONSUMER;
        }

        if (methodType == null) {
            methodType = MethodType.methodType(returnType, fieldClass);
        }

        try {
            MethodHandle target = lookup.findVirtual(objectClass, method.getName(), methodType);
            MethodType instantiatedMethodType = MethodType.methodType(void.class, objectClass, fieldClass);
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    invokedType,
                    samMethodType,
                    target,
                    instantiatedMethodType
            );

            return callSite
                    .getTarget()
                    .invoke();
        } catch (Throwable e) {
            throw new JSONException("create fieldReader error", e);
        }
    }

    public Function<Consumer, ByteArrayValueConsumer> createByteArrayValueConsumerCreator(
            Class objectClass,
            FieldReader[] fieldReaderArray
    ) {
        return null;
    }

    public Function<Consumer, CharArrayValueConsumer> createCharArrayValueConsumerCreator(
            Class objectClass,
            FieldReader[] fieldReaderArray
    ) {
        return null;
    }
}
