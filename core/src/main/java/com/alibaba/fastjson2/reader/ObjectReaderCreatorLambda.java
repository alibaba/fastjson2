package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorLambda;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public class ObjectReaderCreatorLambda
        extends ObjectReaderCreator {
    // Android not support
    private static final Map<Class, Class> classFunctionMapping = new HashMap<>();
    private static final Map<Class, MethodType> methodTypeMapping = new HashMap<>();
    private static final MethodType METHODTYPE_BiFunction = MethodType.methodType(BiConsumer.class);
    private static final MethodType METHODTYPE_Function = MethodType.methodType(Function.class);
    private static final MethodType METHODTYPE_VOID = MethodType.methodType(void.class);

    static {
        classFunctionMapping.put(boolean.class, ObjBoolConsumer.class);
        classFunctionMapping.put(byte.class, ObjByteConsumer.class);
        classFunctionMapping.put(short.class, ObjShortConsumer.class);
        classFunctionMapping.put(int.class, ObjIntConsumer.class);
        classFunctionMapping.put(long.class, ObjLongConsumer.class);
        classFunctionMapping.put(char.class, ObjCharConsumer.class);
        classFunctionMapping.put(float.class, ObjFloatConsumer.class);
        classFunctionMapping.put(double.class, ObjDoubleConsumer.class);

        classFunctionMapping.forEach((k, v) -> methodTypeMapping.put(k, MethodType.methodType(v)));
    }

    public static final ObjectReaderCreatorLambda INSTANCE = new ObjectReaderCreatorLambda();

    @Override
    public <T> ObjectReader<T> createObjectReader(Class<T> objectClass, Type objectType, boolean fieldBased, ObjectReaderProvider provider) {
        BeanInfo beanInfo = new BeanInfo();
        provider.getBeanInfo(beanInfo, objectClass);

        if (beanInfo.deserializer != null && ObjectReader.class.isAssignableFrom(beanInfo.deserializer)) {
            try {
                Constructor constructor = beanInfo.deserializer.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (ObjectReader<T>) constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new JSONException("create deserializer error", e);
            }
        }

        ObjectReader annotatedObjectReader = getAnnotatedObjectReader(provider, objectClass, beanInfo);
        if (annotatedObjectReader != null) {
            return annotatedObjectReader;
        }

        if (Enum.class.isAssignableFrom(objectClass) && (beanInfo.createMethod == null || beanInfo.createMethod.getParameterCount() == 1)) {
            return createEnumReader(objectClass, beanInfo.createMethod, provider);
        }

        if (fieldBased && (objectClass.isInterface() || objectClass.isInterface())) {
            fieldBased = false;
        }

        AtomicReference<Constructor> constructorRef = new AtomicReference();
        BeanUtils.constructor(objectClass, constructor -> {
            constructorRef.set(constructor);
        });

        int classModifiers = objectClass.getModifiers();

        Constructor constructor = constructorRef.get();
        if (constructor == null || !constructor.isAccessible() || Modifier.isAbstract(classModifiers) || Modifier.isInterface(classModifiers)) {
            return super.createObjectReader(objectClass, objectType, fieldBased, provider);
        }

        final Supplier<T> supplier;
        try {
            supplier = lambdaConstrunctor(objectClass);
        } catch (IllegalAccessException | NoSuchMethodException ignored) {
            return super.createObjectReader(objectClass, objectType, fieldBased, provider);
        } catch (Throwable e) {
            throw new JSONException("get constructor error, objectClass : " + objectClass, e);
        }

        Map<String, FieldReader> fieldReaders = new LinkedHashMap<>();
        // List<ObjectReader.FieldReader> fieldReaders = new ArrayList<>(methods.length);

        final FieldInfo fieldInfo = new FieldInfo();

        BeanUtils.setters(objectClass, method -> {
            fieldInfo.init();
            for (ObjectReaderModule module : provider.modules) {
                ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                if (annotationProcessor == null) {
                    continue;
                }
                annotationProcessor.getFieldInfo(fieldInfo, objectClass, method);
            }

            ObjectReader initReader = getInitReader(provider, method.getGenericReturnType(), method.getReturnType(), fieldInfo);

            String fieldName;
            if (method.getParameterCount() == 0) {
                fieldName = BeanUtils.getterName(method.getName(), beanInfo.namingStrategy);
                FieldReader fieldReader = super.createFieldReaderMethod(
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
                        initReader);
                FieldReader origin = fieldReaders.putIfAbsent(fieldName,
                        fieldReader
                );
                if (origin != null && origin.compareTo(fieldReader) > 0) {
                    fieldReaders.put(fieldName, fieldReader);
                }
                return;
            } else {
                fieldName = BeanUtils.setterName(method.getName(), beanInfo.namingStrategy);
            }

            Type fieldType = method.getGenericParameterTypes()[0];
            Class fieldClass = method.getParameterTypes()[0];
            FieldReader fieldReader = createFieldReaderLambda(
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
        });

        BeanUtils.declaredFields(objectClass,
                field -> {
                    fieldInfo.init();
                    fieldInfo.ignore = (field.getModifiers() & Modifier.PUBLIC) == 0;
                    provider.getFieldInfo(fieldInfo, objectClass, field);

                    if (fieldInfo.ignore) {
                        return;
                    }

                    String fieldName = field.getName();
                    Type fieldType = field.getGenericType();
                    Class<?> fieldClass = field.getType();
                    ObjectReader initReader = getInitReader(provider, fieldType, fieldClass, fieldInfo);
                    FieldReader<Object> fieldReader = createFieldReader(
                            objectClass,
                            objectClass,
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
                            initReader
                    );
                    fieldReaders.put(fieldName, fieldReader);
                }
        );

        FieldReader[] fieldReaderArray = new FieldReader[fieldReaders.size()];

        fieldReaders.values().toArray(fieldReaderArray);
        Arrays.sort(fieldReaderArray);

        Supplier<T> creator = createInstanceSupplier(objectClass);

        if (beanInfo.seeAlso != null && beanInfo.seeAlso.length != 0) {
            return createObjectReaderSeeAlso(objectClass, creator, beanInfo.typeKey, beanInfo.seeAlso, beanInfo.seeAlsoNames, fieldReaderArray);
        }

        return createObjectReader(objectClass, beanInfo.readerFeatures, supplier, null, fieldReaderArray);
    }

    @Override
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
        if ((method != null && (method.getReturnType() != void.class || method.getParameterCount() == 0))
                || !Modifier.isPublic(objectClass.getModifiers())
                || isExternalClass(objectClass)
                || initReader != null
        ) {
            return super.createFieldReaderMethod(objectClass, objectType, fieldName, ordinal, features, format, locale, defaultValue, schema, fieldType, fieldClass, method, initReader);
        }
        return createFieldReaderLambda(objectClass, objectType, fieldName, ordinal, features, format, locale, defaultValue, schema, fieldType, fieldClass, method, initReader);
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
            BiConsumer function = (BiConsumer) lambdaFunction(objectClass, fieldClass, method);
            return createFieldReader(objectClass, objectType, fieldName, fieldType, fieldClass, ordinal, features, format, defaultValue, jsonSchema, method, function, initReader);
        }

        if (fieldType == boolean.class) {
            ObjBoolConsumer function = (ObjBoolConsumer) lambdaFunction(objectClass, fieldClass, method);
            return new FieldReaderBoolValFunc<>(fieldName, ordinal, jsonSchema, method, function);
        }

        if (fieldType == byte.class) {
            ObjByteConsumer function = (ObjByteConsumer) lambdaFunction(objectClass, fieldClass, method);
            return new FieldReaderInt8ValueFunc<>(fieldName, ordinal, jsonSchema, method, function);
        }

        if (fieldType == short.class) {
            ObjShortConsumer function = (ObjShortConsumer) lambdaFunction(objectClass, fieldClass, method);
            return new FieldReaderInt16ValueFunc<>(fieldName, ordinal, features, format, locale, (Short) defaultValue, jsonSchema, method, function);
        }

        if (fieldType == int.class) {
            ObjIntConsumer function = (ObjIntConsumer) lambdaFunction(objectClass, fieldClass, method);
            return new FieldReaderInt32ValueFunc<>(fieldName, ordinal, (Integer) defaultValue, jsonSchema, method, function);
        }

        if (fieldType == long.class) {
            ObjLongConsumer function = (ObjLongConsumer) lambdaFunction(objectClass, fieldClass, method);
            return new FieldReaderInt64ValueFunc<>(fieldName, ordinal, (Long) defaultValue, jsonSchema, method, function);
        }

        if (fieldType == char.class) {
            ObjCharConsumer function = (ObjCharConsumer) lambdaFunction(objectClass, fieldClass, method);
            return new FieldReaderCharValueFunc<>(fieldName, ordinal, format, (Character) defaultValue, jsonSchema, method, function);
        }

        if (fieldType == float.class) {
            ObjFloatConsumer function = (ObjFloatConsumer) lambdaFunction(objectClass, fieldClass, method);
            return new FieldReaderFloatValueFunc<>(fieldName, ordinal, (Float) defaultValue, jsonSchema, method, function);
        }

        if (fieldType == double.class) {
            ObjDoubleConsumer function = (ObjDoubleConsumer) lambdaFunction(objectClass, fieldClass, method);
            return new FieldReaderDoubleValueFunc<>(fieldName, ordinal, (Double) defaultValue, jsonSchema, method, function);
        }

        BiConsumer function = (BiConsumer) lambdaFunction(objectClass, fieldClass, method);
        return createFieldReader(objectClass, objectType, fieldName, fieldType, fieldClass, ordinal, features, format, defaultValue, jsonSchema, method, function, initReader);
    }

    private static Object lambdaFunction(Class objectType, Class fieldClass, Method method) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodType invokedType = methodTypeMapping.getOrDefault(fieldClass, METHODTYPE_BiFunction);
        try {
            MethodHandle target = lookup.findVirtual(objectType,
                    method.getName(),
                    MethodType.methodType(void.class, fieldClass)
            );
            MethodType func = target.type();

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    invokedType,
                    func.erase(),
                    target,
                    func
            );

            return callSite
                    .getTarget()
                    .invoke();
        } catch (Throwable e) {
            throw new JSONException("create fieldReader error", e);
        }
    }

    @Override
    public <T, R> Function<T, R> createBuildFunction(Method builderMethod) {
        if (!Modifier.isPublic(builderMethod.getDeclaringClass().getModifiers())) {
            return super.createBuildFunction(builderMethod);
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType invokedType = METHODTYPE_Function;
        try {
            MethodHandle target = lookup.findVirtual(builderMethod.getDeclaringClass(),
                    builderMethod.getName(),
                    MethodType.methodType(builderMethod.getReturnType())
            );
            MethodType func = target.type();

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    invokedType,
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

    static <T> Supplier<T> lambdaConstrunctor(Class<T> objectType) throws Throwable {
        if (objectType == List.class) {
            return () -> (T) new ArrayList();
        }
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodHandle mh = lookup.findConstructor(objectType, METHODTYPE_VOID);
        CallSite callSite = LambdaMetafactory.metafactory(lookup,
                "get",
                MethodType.methodType(Supplier.class),
                mh.type().generic(), mh, mh.type());

        return (Supplier<T>) callSite
                .getTarget()
                .invokeExact();
    }

    static boolean isExternalClass(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();

        if (classLoader == null) {
            return false;
        }

        ClassLoader current = ObjectWriterCreatorLambda.class.getClassLoader();
        while (current != null) {
            if (current == classLoader) {
                return false;
            }

            current = current.getParent();
        }

        return true;
    }
}
