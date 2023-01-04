package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.MultiType;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to
 * represent generic types, so this class does. Forces clients to create a
 * subclass of this class which enables retrieval the type information even at runtime.
 * <p>
 * This syntax cannot be used to create type literals that have wildcard
 * parameters, such as {@code Class<T>} or {@code List<? extends CharSequence>}.
 * <p>
 * For example, to create a type literal for {@code List<String>}, you can
 * create an empty anonymous inner class:
 * <pre>{@code
 * TypeReference<List<String>> typeReference = new TypeReference<List<String>>(){};
 * }</pre>
 * For example, use it quickly
 * <pre>{@code String text = "{\"id\":1,\"name\":\"kraity\"}";
 * User user = new TypeReference<User>(){}.parseObject(text);
 * }</pre>
 */
public abstract class TypeReference<T> {
    protected final Type type;
    protected final Class<? super T> rawType;

    /**
     * Constructs a new type literal. Derives represented class from type parameter.
     * <p>
     * Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy, so we can reconstitute it at runtime despite erasure.
     */
    @SuppressWarnings("unchecked")
    public TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        rawType = (Class<? super T>) BeanUtils.getRawType(type);
    }

    /**
     * @param type specify the {@link Type} to be converted
     * @throws NullPointerException If the {@link Type} is null
     */
    @SuppressWarnings("unchecked")
    private TypeReference(Type type, boolean raw) {
        if (type == null) {
            throw new NullPointerException();
        }

        this.type = BeanUtils.canonicalize(type);
        this.rawType = (Class<? super T>) BeanUtils.getRawType(type);
    }

    /**
     * E.g.
     * <pre>{@code
     * Class<T> klass = ...;
     * TypeReference<Response<T>> ref = new TypeReference<Response<T>>(new Type[]{klass}){};
     * }</pre>
     *
     * @param actualTypeArguments an array of Type objects representing the actual type arguments to this type
     * @throws NullPointerException If the {@code actualTypeArguments} is null or empty
     * @since 2.0.2
     */
    @SuppressWarnings("unchecked")
    public TypeReference(Type... actualTypeArguments) {
        if (actualTypeArguments == null
                || actualTypeArguments.length == 0) {
            throw new NullPointerException();
        }

        Class<?> thisClass = getClass();
        Type superClass = thisClass.getGenericSuperclass();
        ParameterizedType argType = (ParameterizedType) ((ParameterizedType) superClass).getActualTypeArguments()[0];

        type = canonicalize(thisClass, argType, actualTypeArguments, 0);
        rawType = (Class<? super T>) BeanUtils.getRawType(type);
    }

    /**
     * Get the {@link Type}
     */
    public final Type getType() {
        return type;
    }

    /**
     * Get the raw {@link Class}
     */
    public final Class<? super T> getRawType() {
        return rawType;
    }

    /**
     * See {@link JSON#parseObject(String, Type)} for details
     *
     * <pre>{@code
     * String text = "{\"id\":1,\"name\":\"kraity\"}";
     * User user = new TypeReference<User>(){}.parseObject(text);
     * }</pre>
     *
     * @param text the JSON {@link String} to be parsed
     * @since 2.0.2
     */
    public T parseObject(String text) {
        return JSON.parseObject(text, type);
    }

    /**
     * See {@link JSON#parseObject(byte[], Type)} for details
     *
     * <pre>{@code
     * String utf8Bytes = "{\"id\":1,\"name\":\"kraity\"}".getBytes(StandardCharsets.UTF_8);
     * User user = new TypeReference<User>(){}.parseObject(utf8Bytes);
     * }</pre>
     *
     * @param utf8Bytes UTF8 encoded JSON byte array to parse
     * @since 2.0.3
     */
    public T parseObject(byte[] utf8Bytes) {
        return JSON.parseObject(utf8Bytes, type);
    }

    /**
     * See {@link JSON#parseArray(String, JSONReader.Feature...)} for details
     *
     * <pre>{@code
     * String text = "[{\"id\":1,\"name\":\"kraity\"}]";
     * List<User> users = new TypeReference<User>(){}.parseArray(text);
     * }</pre>
     *
     * @param text the JSON {@link String} to be parsed
     * @param features features to be enabled in parsing
     * @since 2.0.2
     */
    public List<T> parseArray(String text, JSONReader.Feature... features) {
        return JSON.parseArray(text, type, features);
    }

    /**
     * See {@link JSON#parseArray(byte[], Type, JSONReader.Feature...)} for details
     *
     * <pre>{@code
     * String utf8Bytes = "[{\"id\":1,\"name\":\"kraity\"}]".getBytes(StandardCharsets.UTF_8);
     * List<User> users = new TypeReference<User>(){}.parseArray(utf8Bytes);
     * }</pre>
     *
     * @param utf8Bytes UTF8 encoded JSON byte array to parse
     * @param features features to be enabled in parsing
     * @since 2.0.3
     */
    public List<T> parseArray(byte[] utf8Bytes, JSONReader.Feature... features) {
        return JSON.parseArray(utf8Bytes, type, features);
    }

    /**
     * See {@link JSONArray#to(Type)} for details
     *
     * <pre>{@code
     * JSONArray array = ...
     * List<User> users = new TypeReference<ArrayList<User>>(){}.to(array);
     * }</pre>
     *
     * @param array specify the {@link JSONArray} to convert
     * @since 2.0.4
     */
    public T to(JSONArray array) {
        return array.to(type);
    }

    /**
     * See {@link JSONObject#to(Type, JSONReader.Feature...)} for details
     *
     * <pre>{@code
     * JSONObject object = ...
     * Map<String, User> users = new TypeReference<HashMap<String, User>>(){}.to(object);
     * }</pre>
     *
     * @param object specify the {@link JSONObject} to convert
     * @param features features to be enabled in parsing
     * @since 2.0.4
     */
    public T to(JSONObject object, JSONReader.Feature... features) {
        return object.to(type, features);
    }

    /**
     * See {@link JSONArray#toJavaObject(Type)} for details
     *
     * @param array specify the {@link JSONArray} to convert
     * @deprecated since 2.0.4, please use {@link #to(JSONArray)}
     */
    @Deprecated
    public T toJavaObject(JSONArray array) {
        return array.to(type);
    }

    /**
     * See {@link JSONObject#to(Type, JSONReader.Feature...)} for details
     *
     * @param object specify the {@link JSONObject} to convert
     * @param features features to be enabled in parsing
     * @deprecated since 2.0.4, please use {@link #to(JSONObject, JSONReader.Feature...)}
     */
    @Deprecated
    public T toJavaObject(JSONObject object, JSONReader.Feature... features) {
        return object.to(type, features);
    }

    /**
     * @param type specify the {@link Type} to be converted
     */
    public static TypeReference<?> get(Type type) {
        return new TypeReference<Object>(type, true) {
            // nothing
        };
    }

    /**
     * @param thisClass this class
     * @param type the parameterizedType
     * @param actualTypeArguments an array of Type objects representing the actual type arguments to this type
     * @param actualIndex the actual index
     * @since 2.0.3
     */
    private static Type canonicalize(Class<?> thisClass,
                                     ParameterizedType type,
                                     Type[] actualTypeArguments,
                                     int actualIndex) {
        Type rawType = type.getRawType();
        Type[] argTypes = type.getActualTypeArguments();

        for (int i = 0; i < argTypes.length; ++i) {
            if (argTypes[i] instanceof TypeVariable
                    && actualIndex < actualTypeArguments.length) {
                argTypes[i] = actualTypeArguments[actualIndex++];
            }

            // fix for openjdk and android env
            if (argTypes[i] instanceof GenericArrayType) {
                Type componentType = argTypes[i];

                int dimension = 0;
                while (componentType instanceof GenericArrayType) {
                    dimension++;
                    componentType = ((GenericArrayType) componentType).getGenericComponentType();
                }

                if (componentType instanceof Class<?>) {
                    Class<?> cls = (Class<?>) componentType;
                    Loader:
                    if (cls.isPrimitive()) {
                        final char ch;
                        if (cls == int.class) {
                            ch = 'I';
                        } else if (cls == long.class) {
                            ch = 'J';
                        } else if (cls == float.class) {
                            ch = 'F';
                        } else if (cls == double.class) {
                            ch = 'D';
                        } else if (cls == boolean.class) {
                            ch = 'Z';
                        } else if (cls == char.class) {
                            ch = 'C';
                        } else if (cls == byte.class) {
                            ch = 'B';
                        } else if (cls == short.class) {
                            ch = 'S';
                        } else {
                            break Loader;
                        }

                        char[] chars = new char[dimension + 1];
                        for (int j = 0; j < dimension; j++) {
                            chars[j] = '[';
                        }
                        chars[dimension] = ch;
                        String typeName = new String(chars);
                        argTypes[i] = TypeUtils.loadClass(typeName);
                    }
                }
            }

            // if it is a ParameterizedType,
            // iterate to find the real Type
            if (argTypes[i] instanceof ParameterizedType) {
                argTypes[i] = canonicalize(
                        thisClass, (ParameterizedType) argTypes[i],
                        actualTypeArguments, actualIndex
                );
            }
        }

        return new ParameterizedTypeImpl(
                argTypes, thisClass, rawType
        );
    }

    public static Type of(Type... types) {
        return new MultiType(types);
    }

    public static Type collectionType(
            Class<? extends Collection> collectionClass,
            Class<?> elementClass
    ) {
        return new ParameterizedTypeImpl(collectionClass, elementClass);
    }

    public static Type arrayType(Class<?> elementType) {
        return new BeanUtils.GenericArrayTypeImpl(elementType);
    }

    public static Type mapType(
            Class<? extends Map> mapClass,
            Class<?> keyClass, Class<?> valueClass
    ) {
        return new ParameterizedTypeImpl(mapClass, keyClass, valueClass);
    }

    public static Type parametricType(Class<?> parametrized, Class<?>... parameterClasses) {
        return new ParameterizedTypeImpl(parametrized, parameterClasses);
    }

    public static Type parametricType(Class<?> parametrized, Type... parameterTypes) {
        return new ParameterizedTypeImpl(parametrized, parameterTypes);
    }
}
