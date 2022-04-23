package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to
 * represent generic types, so this class does. Forces clients to create a
 * subclass of this class which enables retrieval the type information even at runtime.
 * <p>
 * This syntax cannot be used to create type literals that have wildcard
 * parameters, such as {@link Class<T>} or {@code List<? extends CharSequence>}.
 * <p>
 * For example, to create a type literal for {@link List<String>}, you can
 * create an empty anonymous inner class:
 * <code>
 * TypeReference<List<String>> typeReference = new TypeReference<List<String>>(){};
 * </code>
 * For example, use it quickly
 * <code>
 * String text = "{\"id\":1,\"name\":\"kraity\"}";
 * User user = new TypeReference<User>(){}.parseObject(text);
 * </code>
 * <p>
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
    public TypeReference(Type type) {
        if (type == null) {
            throw new NullPointerException();
        }

        this.type = BeanUtils.canonicalize(type);
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
     * <code>
     * String text = "{\"id\":1,\"name\":\"kraity\"}";
     * <p>
     * User user = new TypeReference<User>(){}.parseObject(text);
     * </code>
     *
     * @param text the JSON {@link String} to be parsed
     * @since 2.0.2
     */
    public T parseObject(String text) {
        return JSON.parseObject(text, type);
    }

    /**
     * See {@link JSONObject#toJavaObject} for details
     *
     * <code>
     * Map<String, User> users = new TypeReference<HashMap<String, User>>(){}.parseObject(jsonObject);
     * </code>
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
     * <code>
     * String text = "[{\"id\":1,\"name\":\"kraity\"}]";
     * <p>
     * List<User> users = new TypeReference<User>(){}.parseArray(text);
     * </code>
     *
     * @param text the JSON {@link String} to be parsed
     * @since 2.0.2
     */
    public List<T> parseArray(String text) {
        return JSON.parseArray(text, type);
    }

    /**
     * See {@link JSONArray#toJavaObject} for details
     *
     * <code>
     * List<User> users = new TypeReference<ArrayList<User>>(){}.parseObject(jsonArray);
     * </code>
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
        return new TypeReference<Object>(type) {
            // nothing
        };
    }
}
