package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.ObjBoolConsumer;
import com.alibaba.fastjson2.function.ObjByteConsumer;
import com.alibaba.fastjson2.function.ObjCharConsumer;
import com.alibaba.fastjson2.function.ObjFloatConsumer;
import com.alibaba.fastjson2.function.ObjShortConsumer;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

import static com.alibaba.fastjson2.util.BeanUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.*;

/**
 * ObjectReaderCreator is responsible for creating ObjectReader instances for
 * deserializing JSON data into Java objects. It provides factory methods for
 * creating ObjectReaders for various types of objects and fields.
 *
 * <p>This class supports various features including:
 * <ul>
 *   <li>Creation of ObjectReaders for different object types</li>
 *   <li>Creation of FieldReaders for different field types</li>
 *   <li>Lambda expression support for setter methods</li>
 *   <li>Custom field reader creation with various configurations</li>
 *   <li>JIT compilation support for improved performance</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Get default creator
 * ObjectReaderCreator creator = JSONFactory.getDefaultObjectReaderCreator();
 *
 * // Create ObjectReader for a class
 * ObjectReader&lt;User&gt; reader = creator.createObjectReader(User.class);
 *
 * // Create FieldReader for a field
 * Field field = User.class.getDeclaredField("name");
 * FieldReader&lt;User&gt; fieldReader = creator.createFieldReader("name", String.class, field);
 * </pre>
 *
 * @since 2.0.0
 */
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
        throw new UnsupportedOperationException();
    }

    public <T> ObjectReader<T> createObjectReaderNoneDefaultConstructor(
            Class objectClass,
            Function<Map<Long, Object>, T> creator,
            Object... fieldReaders
    ) {
        throw new UnsupportedOperationException();
    }

    public <T> ObjectReader<T> createObjectReaderFactoryMethod(Method factoryMethod, String... paramNames) {
        throw new UnsupportedOperationException();
    }

    public Object[] createFieldReaders(
            ObjectReaderProvider provider,
            Class objectClass,
            Type objectType,
            Executable owner,
            Parameter[] parameters,
            String... paramNames
    ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a Function that can instantiate objects using the specified factory method and parameter names.
     *
     * @param <T> the type of objects that the Function can create
     * @param factoryMethod the factory method to use for creating instances
     * @param paramNames the parameter names to use for the factory method
     * @return a Function that can create new instances of the specified type using the factory method
     */
    public <T> Function<Map<Long, Object>, T> createFactoryFunction(Method factoryMethod, String... paramNames) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a Function that can instantiate objects using the specified constructor and parameter names.
     *
     * @param <T> the type of objects that the Function can create
     * @param constructor the constructor to use for creating instances
     * @param paramNames the parameter names to use for the constructor
     * @return a Function that can create new instances of the specified type
     */
    public <T> Function<Map<Long, Object>, T> createFunction(Constructor constructor, String... paramNames) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a Function that can instantiate objects using the specified constructor, marker constructor, and parameter names.
     *
     * @param <T> the type of objects that the Function can create
     * @param constructor the constructor to use for creating instances
     * @param markerConstructor the marker constructor to use
     * @param paramNames the parameter names to use for the constructor
     * @return a Function that can create new instances of the specified type
     */
    public <T> Function<Map<Long, Object>, T> createFunction(
            Constructor constructor,
            Constructor markerConstructor,
            String... paramNames
    ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates an ObjectReader for the specified object type with the given field readers.
     *
     * @param <T> the type of objects that the ObjectReader can deserialize
     * @param objectClass the class of objects to deserialize
     * @param fieldReaders the field readers to use for deserialization
     * @return an ObjectReader instance for the specified type
     */
    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            Object... fieldReaders
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

    /**
     * Creates an ObjectReader for the specified object type with a default creator and field readers.
     *
     * @param <T> the type of objects that the ObjectReader can deserialize
     * @param objectClass the class of objects to deserialize
     * @param defaultCreator the supplier function to create new instances of the object
     * @param fieldReaders the field readers to use for deserialization
     * @return an ObjectReader instance for the specified type
     */
    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            Supplier<T> defaultCreator,
            Object... fieldReaders
    ) {
        return createObjectReader(objectClass, null, 0, null, defaultCreator, null, fieldReaders);
    }

    /**
     * Creates an ObjectReader for the specified object type with see-also support.
     *
     * @param <T> the type of objects that the ObjectReader can deserialize
     * @param objectType the class of objects to deserialize
     * @param seeAlso the see-also classes
     * @param fieldReaders the field readers to use for deserialization
     * @return an ObjectReader instance for the specified type
     */
    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectType,
            Class[] seeAlso,
            Object... fieldReaders
    ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates an ObjectReader for the specified object type with see-also support and custom type key.
     *
     * @param <T> the type of objects that the ObjectReader can deserialize
     * @param objectClass the class of objects to deserialize
     * @param typeKey the type key to use
     * @param seeAlso the see-also classes
     * @param seeAlsoNames the see-also class names
     * @param fieldReaders the field readers to use for deserialization
     * @return an ObjectReader instance for the specified type
     */
    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectClass,
            String typeKey,
            Class[] seeAlso,
            String[] seeAlsoNames,
            Object... fieldReaders
    ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates an ObjectReader for the specified object type with see-also support, custom type key, and default class.
     *
     * @param <T> the type of objects that the ObjectReader can deserialize
     * @param objectClass the class of objects to deserialize
     * @param typeKey the type key to use
     * @param seeAlso the see-also classes
     * @param seeAlsoNames the see-also class names
     * @param seeAlsoDefault the default see-also class
     * @param fieldReaders the field readers to use for deserialization
     * @return an ObjectReader instance for the specified type
     * @since 2.0.24
     */
    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectClass,
            String typeKey,
            Class[] seeAlso,
            String[] seeAlsoNames,
            Class seeAlsoDefault,
            Object... fieldReaders
    ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates an ObjectReader for the specified object type with see-also support, custom creator, and type key.
     *
     * @param <T> the type of objects that the ObjectReader can deserialize
     * @param objectType the class of objects to deserialize
     * @param defaultCreator the supplier function to create new instances of the object
     * @param typeKey the type key to use
     * @param seeAlso the see-also classes
     * @param seeAlsoNames the see-also class names
     * @param fieldReaders the field readers to use for deserialization
     * @return an ObjectReader instance for the specified type
     */
    public <T> ObjectReader<T> createObjectReaderSeeAlso(
            Class<T> objectType,
            Supplier<T> defaultCreator,
            String typeKey,
            Class[] seeAlso,
            String[] seeAlsoNames,
            Object... fieldReaders
    ) {
        throw new UnsupportedOperationException();
    }

    protected <T> ObjectReader<T> createObjectReaderWithBuilder(
            Class<T> objectClass,
            Type objectType,
            ObjectReaderProvider provider,
            BeanInfo beanInfo
    ) {
        throw new UnsupportedOperationException();
    }

    protected <T> ObjectReader<T> createObjectReaderWithCreator(
            Class<T> objectClass,
            Type objectType,
            ObjectReaderProvider provider,
            BeanInfo beanInfo
    ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates an ObjectReader for the specified object type with comprehensive configuration.
     *
     * @param <T> the type of objects that the ObjectReader can deserialize
     * @param objectClass the class of objects to deserialize
     * @param features the features to use for deserialization
     * @param defaultCreator the supplier function to create new instances of the object
     * @param buildFunction the build function to use
     * @param fieldReaders the field readers to use for deserialization
     * @return an ObjectReader instance for the specified type
     */
    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            long features,
            Supplier<T> defaultCreator,
            Function buildFunction,
            Object... fieldReaders
    ) {
        return createObjectReader(objectClass, null, features, null, defaultCreator, buildFunction, fieldReaders);
    }

    /**
     * Creates an ObjectReader for the specified object type with type key, features, schema, and comprehensive configuration.
     *
     * @param <T> the type of objects that the ObjectReader can deserialize
     * @param objectClass the class of objects to deserialize
     * @param typeKey the type key to use
     * @param features the features to use for deserialization
     * @param schema the JSON schema to use
     * @param defaultCreator the supplier function to create new instances of the object
     * @param buildFunction the build function to use
     * @param fieldReaders the field readers to use for deserialization
     * @return an ObjectReader instance for the specified type
     */
    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            String typeKey,
            long features,
            Object schema,
            Supplier<T> defaultCreator,
            Function buildFunction,
            Object... fieldReaders
    ) {
        return createObjectReader(
                objectClass,
                typeKey,
                null,
                features,
                schema,
                defaultCreator,
                buildFunction,
                fieldReaders);
    }

    /**
     * Creates an ObjectReader for the specified object type with comprehensive configuration including root name.
     *
     * @param <T> the type of objects that the ObjectReader can deserialize
     * @param objectClass the class of objects to deserialize
     * @param typeKey the type key to use
     * @param rootName the root name to use
     * @param features the features to use for deserialization
     * @param schema the JSON schema to use
     * @param defaultCreator the supplier function to create new instances of the object
     * @param buildFunction the build function to use
     * @param fieldReaders the field readers to use for deserialization
     * @return an ObjectReader instance for the specified type
     */
    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            String typeKey,
            String rootName,
            long features,
            Object schema,
            Supplier<T> defaultCreator,
            Function buildFunction,
            Object... fieldReaders
    ) {
        throw new UnsupportedOperationException();
    }

    public <T> ObjectReader<T> createObjectReader(Type objectType) {
        if (objectType instanceof Class) {
            return createObjectReader((Class<T>) objectType);
        }

        Class<T> objectClass = (Class<T>) TypeUtils.getMapping(objectType);
        Object[] fieldReaderArray = createFieldReaders(objectClass, objectType);
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
        if (Map.class.isAssignableFrom(objectClass)) {
            return new ObjectReader<T>() {
                @Override
                public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
                    return (T) jsonReader.readObject();
                }
            };
        }
        if (Collection.class.isAssignableFrom(objectClass)) {
            return new ObjectReader<T>() {
                @Override
                public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
                    return (T) jsonReader.readArray();
                }
            };
        }
        if (objectClass == Object.class) {
            return new ObjectReader<T>() {
                @Override
                public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
                    if (jsonReader.isObject()) {
                        return (T) jsonReader.readObject();
                    }
                    if (jsonReader.isArray()) {
                        return (T) jsonReader.readArray();
                    }
                    if (jsonReader.isString()) {
                        return (T) jsonReader.readString();
                    }
                    if (jsonReader.isBool()) {
                        return (T) (Boolean) jsonReader.readBoolValue();
                    }
                    if (jsonReader.isNumber()) {
                        return (T) jsonReader.readNumber();
                    }
                    jsonReader.readNull();
                    return null;
                }
            };
        }
        throw new UnsupportedOperationException();
    }

    protected <T> ObjectReader<T> createNoneDefaultConstructorObjectReader(
            Class objectClass,
            BeanInfo beanInfo,
            Function<Map<Long, Object>, T> constructorFunction,
            List<Constructor> alternateConstructors,
            String[] parameterNames,
            Object[] paramFieldReaders,
            Object[] fieldReaderArray
    ) {
        throw new UnsupportedOperationException();
    }

    public <T> Object[] createFieldReaders(Class<T> objectClass) {
        return createFieldReaders(
                objectClass,
                objectClass,
                null,
                false,
                JSONFactory.getDefaultObjectReaderProvider()
        );
    }

    public <T> Object[] createFieldReaders(Class<T> objectClass, Type objectType) {
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
            String[] orders,
            FieldInfo fieldInfo,
            Field field,
            Map<String, List> fieldReaders,
            ObjectReaderProvider provider
    ) {
        throw new UnsupportedOperationException();
    }

    protected void createFieldReader(
            Class objectClass,
            Type objectType,
            String namingStrategy,
            String[] orders,
            BeanInfo beanInfo,
            FieldInfo fieldInfo,
            Method method,
            Map<String, List> fieldReaders,
            ObjectReaderProvider provider
    ) {
        throw new UnsupportedOperationException();
    }

    protected <T> Object[] createFieldReaders(
            Class<T> objectClass,
            Type objectType,
            BeanInfo beanInfo,
            boolean fieldBased,
            ObjectReaderProvider provider
    ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a Supplier function for the specified object class that can create new instances.
     *
     * @param <T> the type of objects that the Supplier can create
     * @param objectClass the class of objects to create
     * @return a Supplier function that can create new instances of the specified class, or null if creation is not possible
     */
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
            boolean innerClass = objectClass.getName().indexOf('$') != -1;
            if (innerClass && !Modifier.isStatic(objectClass.getModifiers())) {
                constructor = objectClass.getDeclaredConstructor(objectClass.getDeclaringClass());
            } else {
                constructor = objectClass.getDeclaredConstructor();
            }
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (Throwable e) {
            throw new JSONException("get constructor error, class " + objectClass.getName(), e);
        }

        return createSupplier(constructor);
    }

    /**
     * Creates a Supplier function for the specified constructor that can create new instances.
     *
     * @param <T> the type of objects that the Supplier can create
     * @param constructor the constructor to use for creating instances
     * @return a Supplier function that can create new instances using the specified constructor
     */
    public <T> Supplier<T> createSupplier(Constructor<T> constructor) {
        return createSupplier(constructor, true);
    }

    /**
     * Creates a Supplier function for the specified constructor with JIT compilation option.
     *
     * @param <T> the type of objects that the Supplier can create
     * @param constructor the constructor to use for creating instances
     * @param jit whether to use JIT compilation for improved performance
     * @return a Supplier function that can create new instances using the specified constructor
     */
    public <T> Supplier<T> createSupplier(Constructor constructor, boolean jit) {
        throw new UnsupportedOperationException();
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

    /**
     * Creates a Function that can build objects using the specified builder method.
     *
     * @param <T> the type of objects that the Function can build
     * @param <R> the return type of the builder method
     * @param builderMethod the builder method to use for building objects
     * @return a Function that can build objects using the specified builder method
     */
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

    /**
     * Creates a Function that can build objects using the specified builder method with lambda optimization.
     *
     * @param <T> the type of objects that the Function can build
     * @param <R> the return type of the builder method
     * @param builderMethod the builder method to use for building objects
     * @return a Function that can build objects using the specified builder method
     * @throws Throwable if an error occurs during lambda creation
     */
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

    /**
     * Creates a FieldReader for the specified field with default configuration.
     *
     * @param <T> the type of objects that contain the field
     * @param objectType the class containing the field
     * @param fieldName the name of the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param method the method to use for reading the field
     * @return a FieldReader instance for the specified field
     */
    public <T> Object createFieldReader(
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

    /**
     * Creates a FieldReader for the specified field with format configuration.
     *
     * @param <T> the type of objects that contain the field
     * @param objectType the class containing the field
     * @param fieldName the name of the field
     * @param format the date format to use for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param method the method to use for reading the field
     * @return a FieldReader instance for the specified field
     */
    public <T> Object createFieldReader(
            Class<T> objectType,
            String fieldName,
            String format,
            Type fieldType,
            Class fieldClass,
            Method method
    ) {
        return createFieldReaderMethod(objectType, fieldName, format, fieldType, fieldClass, method);
    }

    /**
     * Creates a FieldReader for the specified method with default configuration.
     *
     * @param <T> the type of objects that contain the field
     * @param objectType the class containing the field
     * @param fieldName the name of the field
     * @param format the date format to use for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param method the method to use for reading the field
     * @return a FieldReader instance for the specified field
     */
    public <T> Object createFieldReaderMethod(
            Class<T> objectType,
            String fieldName,
            String format,
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

    /**
     * Creates a FieldReader for the specified parameter with default configuration.
     *
     * @param <T> the type of objects that contain the field
     * @param objectClass the class containing the field
     * @param objectType the type of the object
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use for the field
     * @param format the date format to use for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param paramName the name of the parameter
     * @param declaringClass the declaring class of the parameter
     * @param parameter the parameter to create a reader for
     * @param schema the JSON schema to use
     * @return a FieldReader instance for the specified parameter
     */
    public <T> Object createFieldReaderParam(
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
            Object schema
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

    /**
     * Creates a FieldReader for the specified parameter with initialization reader.
     *
     * @param <T> the type of objects that contain the field
     * @param objectClass the class containing the field
     * @param objectType the type of the object
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use for the field
     * @param format the date format to use for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param paramName the name of the parameter
     * @param declaringClass the declaring class of the parameter
     * @param parameter the parameter to create a reader for
     * @param schema the JSON schema to use
     * @param initReader the initialization reader to use
     * @return a FieldReader instance for the specified parameter
     */
    public <T> Object createFieldReaderParam(
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
            Object schema,
            ObjectReader initReader
    ) {
        return createFieldReaderParam(
                objectClass,
                objectType,
                fieldName,
                ordinal,
                features,
                format,
                null,
                null,
                fieldType,
                fieldClass,
                paramName,
                declaringClass,
                parameter,
                schema,
                initReader
        );
    }

    /**
     * Creates a FieldReader for the specified parameter with comprehensive configuration.
     *
     * @param <T> the type of objects that contain the field
     * @param objectClass the class containing the field
     * @param objectType the type of the object
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use for the field
     * @param format the date format to use for the field
     * @param locale the locale to use for the field
     * @param defaultValue the default value for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param paramName the name of the parameter
     * @param declaringClass the declaring class of the parameter
     * @param parameter the parameter to create a reader for
     * @param schema the JSON schema to use
     * @param initReader the initialization reader to use
     * @return a FieldReader instance for the specified parameter
     */
    /**
     * Creates a FieldReader for the specified parameter with comprehensive configuration including locale, default value, and initialization reader.
     *
     * @param <T> the type of objects that contain the field
     * @param objectClass the class containing the field
     * @param objectType the type of the object
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use for the field
     * @param format the date format to use for the field
     * @param locale the locale to use for the field
     * @param defaultValue the default value for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param paramName the name of the parameter
     * @param declaringClass the declaring class of the parameter
     * @param parameter the parameter to create a reader for
     * @param schema the JSON schema to use for the field
     * @param initReader the initialization reader to use
     * @return a FieldReader instance for the specified parameter
     */
    /**
     * Creates a FieldReader for the specified parameter with comprehensive configuration.
     *
     * @param <T> the type of objects that contain the field
     * @param objectClass the class containing the field
     * @param objectType the type of the object
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use for the field
     * @param format the date format to use for the field
     * @param locale the locale to use for the field
     * @param defaultValue the default value for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param paramName the name of the parameter
     * @param declaringClass the declaring class of the parameter
     * @param parameter the parameter to create a reader for
     * @param schema the JSON schema to use for the field
     * @param initReader the initialization reader to use
     * @return a FieldReader instance for the specified parameter
     */
    public <T> Object createFieldReaderParam(
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
            String paramName,
            Class declaringClass,
            Parameter parameter,
            Object schema,
            ObjectReader initReader
    ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a FieldReader for the specified method with comprehensive configuration.
     *
     * @param <T> the type of objects that contain the field
     * @param objectClass the class containing the field
     * @param objectType the type of the object
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use for the field
     * @param format the date format to use for the field
     * @param locale the locale to use for the field
     * @param defaultValue the default value for the field
     * @param schema the schema to use for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param method the method to use for reading the field
     * @param initReader the initialization reader to use
     * @return a FieldReader instance for the specified method
     */
    public <T> Object createFieldReaderMethod(
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
        return createFieldReaderMethod(
                objectClass,
                objectType,
                fieldName,
                ordinal,
                features,
                format,
                locale,
                defaultValue,
                schema,
                fieldType,
                fieldClass,
                method,
                initReader,
                null,
                null
        );
    }

    /**
     * Creates a FieldReader for the specified method with comprehensive configuration including array-to-map options.
     *
     * @param <T> the type of objects that contain the field
     * @param objectClass the class containing the field
     * @param objectType the type of the object
     * @param fieldName the name of the field
     * @param ordinal the ordinal position of the field
     * @param features the features to use for the field
     * @param format the date format to use for the field
     * @param locale the locale to use for the field
     * @param defaultValue the default value for the field
     * @param schema the schema to use for the field
     * @param fieldType the type of the field
     * @param fieldClass the class of the field
     * @param method the method to use for reading the field
     * @param initReader the initialization reader to use
     * @param keyName the key name for array-to-map conversion
     * @param arrayToMapDuplicateHandler the duplicate handler for array-to-map conversion
     * @return a FieldReader instance for the specified method
     */
    public <T> Object createFieldReaderMethod(
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
            ObjectReader initReader,
            String keyName,
            BiConsumer arrayToMapDuplicateHandler
    ) {
        throw new UnsupportedOperationException();
    }

    public <T> Object createFieldReader(
            String fieldName,
            Type fieldType,
            Field field
    ) {
        return createFieldReader(fieldName, null, fieldType, field);
    }

    public <T> Object createFieldReader(
            String fieldName,
            Field field
    ) {
        return createFieldReader(fieldName, null, field.getGenericType(), field);
    }

    /**
     * Creates a FieldReader for the specified method with minimal configuration.
     *
     * @param fieldName the name of the field
     * @param method the method to create a reader for
     * @param <T> the type of objects that the FieldReader can deserialize
     * @return a FieldReader instance for the specified method
     * @throws JSONException if the method has an illegal number of parameters
     */
    public <T> Object createFieldReader(
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

    public <T> Object createFieldReader(
            String fieldName,
            String format,
            Type fieldType,
            Field field
    ) {
        Class objectClass = field.getDeclaringClass();
        return createFieldReader(objectClass, objectClass, fieldName, 0, format, fieldType, field.getType(), field);
    }

    public <T> Object createFieldReader(
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
                null,
                null,
                null
        );
    }

    public <T> Object createFieldReader(
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
        return createFieldReader(
                objectClass,
                objectType,
                fieldName,
                0,
                features,
                format,
                locale,
                defaultValue,
                schema,
                fieldType,
                field.getType(),
                field,
                initReader,
                null,
                null
        );
    }

    public <T> Object createFieldReader(
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
            ObjectReader initReader,
            String keyName,
            BiConsumer arrayToMapDuplicateHandler
    ) {
        throw new UnsupportedOperationException();
    }

    public <T, V> Object createFieldReader(
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            long features,
            BiConsumer<T, V> function
    ) {
        throw new UnsupportedOperationException();
    }

    public <T, V> Object createFieldReader(
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            Method method,
            BiConsumer<T, V> function
    ) {
        throw new UnsupportedOperationException();
    }

    public <T, V> Object createFieldReader(
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
            Object schema,
            Method method,
            BiConsumer<T, V> function,
            ObjectReader initReader
    ) {
        throw new UnsupportedOperationException();
    }

    protected ObjectReader createEnumReader(
            Class objectClass,
            Method createMethod,
            ObjectReaderProvider provider
    ) {
        throw new UnsupportedOperationException();
    }

    static ObjectReader getInitReader(
            ObjectReaderProvider provider,
            Type fieldType,
            Class fieldClass,
            FieldInfo fieldInfo
    ) {
        return null;
    }

    protected <T> Object createFieldReaderLambda(
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
        throw new UnsupportedOperationException();
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

    public Object createByteArrayValueConsumerCreator(
            Class objectClass,
            Object[] fieldReaderArray
    ) {
        return null;
    }

    public Object createCharArrayValueConsumerCreator(
            Class objectClass,
            Object[] fieldReaderArray
    ) {
        return null;
    }

    private List listOf(Object fieldReader) {
        List list = new ArrayList<>();
        list.add(fieldReader);
        return list;
    }

    private void putIfAbsent(Map<String, List> fieldReaders,
                             String fieldName,
                             Object fieldReader,
                             Class objectClass) {
        throw new UnsupportedOperationException();
    }

    private Object[] toFieldReaderArray(Map<String, List> fieldReaders) {
        int size = fieldReaders.values().stream().mapToInt(Collection::size).sum();
        Object[] fieldReaderArray = new Object[size];
        List<Object> all = new java.util.ArrayList<>();
        for (List list : fieldReaders.values()) {
            all.addAll(list);
        }
        all.toArray(fieldReaderArray);
        Arrays.sort(fieldReaderArray);
        return fieldReaderArray;
    }
}
