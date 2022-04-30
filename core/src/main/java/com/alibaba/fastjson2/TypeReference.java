package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
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
 * <p>
 * {@code TypeReference<List<String>> typeReference = new TypeReference<List<String>>(){};}
 * <p>
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
    public TypeReference(Type type, boolean raw) {
        if (type == null) {
            throw new NullPointerException();
        }

        this.type = BeanUtils.canonicalize(type);
        this.rawType = (Class<? super T>) BeanUtils.getRawType(type);
    }

    protected TypeReference(Type... actualTypeArguments){
        Class<?> thisClass = this.getClass();
        Type superClass = thisClass.getGenericSuperclass();

        ParameterizedType argType = (ParameterizedType) ((ParameterizedType) superClass).getActualTypeArguments()[0];
        Type rawType = argType.getRawType();
        Type[] argTypes = argType.getActualTypeArguments();

        int actualIndex = 0;
        for (int i = 0; i < argTypes.length; ++i) {
            if (argTypes[i] instanceof TypeVariable &&
                    actualIndex < actualTypeArguments.length) {
                argTypes[i] = actualTypeArguments[actualIndex++];
            }
            // fix for openjdk and android env
            if (argTypes[i] instanceof GenericArrayType) {
                argTypes[i] = checkPrimitiveArray(
                        (GenericArrayType) argTypes[i]);
            }

            // 如果有多层泛型且该泛型已经注明实现的情况下，判断该泛型下一层是否还有泛型
            if(argTypes[i] instanceof ParameterizedType) {
                argTypes[i] = handlerParameterizedType((ParameterizedType) argTypes[i], actualTypeArguments, actualIndex);
            }
        }

        this.type = new ParameterizedTypeImpl(argTypes, thisClass, rawType);
        this.rawType = (Class<? super T>) BeanUtils.getRawType(type);
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
     * See {@link JSON#parseObject} for details
     *
     * <pre>{@code String text = "{\"id\":1,\"name\":\"kraity\"}";
     *
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
     * See {@link JSONObject#toJavaObject} for details
     * <p>
     * {@code Map<String, User> users = new TypeReference<HashMap<String, User>>(){}.parseObject(jsonObject);}
     *
     * @param object Specify the {@link JSONObject} to convert
     * @since 2.0.2
     */
    public T parseObject(JSONObject object) {
        return object.toJavaObject(type);
    }

    /**
     * See {@link JSON#parseArray} for details
     *
     * <pre>{@code String text = "[{\"id\":1,\"name\":\"kraity\"}]";
     *
     * List<User> users = new TypeReference<User>(){}.parseArray(text);
     * }</pre>
     *
     * @param text the JSON {@link String} to be parsed
     * @since 2.0.2
     */
    public List<T> parseArray(String text) {
        return JSON.parseArray(text, type);
    }

    /**
     * See {@link JSONArray#toJavaObject} for details
     * <p>
     * {@code List<User> users = new TypeReference<ArrayList<User>>(){}.parseObject(jsonArray);}
     *
     * @param object Specify the {@link JSONArray} to convert
     * @since 2.0.2
     */
    public T parseArray(JSONArray object) {
        return object.toJavaObject(type);
    }

    /**
     * @param type specify the {@link Type} to be converted
     */
    public static TypeReference<?> get(Type type) {
        return new TypeReference<Object>(type, true) {
            // nothing
        };
    }

    private static final Map primitiveTypeMap = new HashMap<Class, String>(8) {{
        put(boolean.class, "Z");
        put(char.class, "C");
        put(byte.class, "B");
        put(short.class, "S");
        put(int.class, "I");
        put(long.class, "J");
        put(float.class, "F");
        put(double.class, "D");
    }};

    static Type checkPrimitiveArray(GenericArrayType genericArrayType) {
        Type clz = genericArrayType;
        Type genericComponentType = genericArrayType.getGenericComponentType();

        String prefix = "[";
        while (genericComponentType instanceof GenericArrayType) {
            genericComponentType = ((GenericArrayType) genericComponentType)
                    .getGenericComponentType();
            prefix += prefix;
        }

        if (genericComponentType instanceof Class<?>) {
            Class<?> ck = (Class<?>) genericComponentType;
            if (ck.isPrimitive()) {
                try {
                    String postfix = (String) primitiveTypeMap.get(ck);
                    if (postfix != null) {
                        clz = Class.forName(prefix + postfix);
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        return clz;
    }

    private Type handlerParameterizedType(ParameterizedType type, Type[] actualTypeArguments, int actualIndex) {
        Class<?> thisClass = this.getClass();
        Type rawType = type.getRawType();
        Type[] argTypes = type.getActualTypeArguments();

        for(int i = 0; i < argTypes.length; ++i) {
            if (argTypes[i] instanceof TypeVariable && actualIndex < actualTypeArguments.length) {
                argTypes[i] = actualTypeArguments[actualIndex++];
            }

            // fix for openjdk and android env
            if (argTypes[i] instanceof GenericArrayType) {
                argTypes[i] = checkPrimitiveArray(
                        (GenericArrayType) argTypes[i]);
            }

            // 如果有多层泛型且该泛型已经注明实现的情况下，判断该泛型下一层是否还有泛型
            if(argTypes[i] instanceof ParameterizedType) {
                argTypes[i] = handlerParameterizedType((ParameterizedType) argTypes[i], actualTypeArguments, actualIndex);
            }
        }

        Type key = new ParameterizedTypeImpl(argTypes, thisClass, rawType);
        return key;
    }
}
