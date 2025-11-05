package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * Implementation of {@link ParameterizedType} that represents a parameterized type such as
 * {@code List<String>}, {@code Map<String, Integer>}, etc.
 *
 * <p>This class is used by fastjson2 to handle generic type information during serialization
 * and deserialization, allowing proper type handling for generic collections and classes.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Create a type for List<String>
 * ParameterizedType listType = new ParameterizedTypeImpl(List.class, String.class);
 *
 * // Create a type for Map<String, Integer>
 * ParameterizedType mapType = new ParameterizedTypeImpl(Map.class, String.class, Integer.class);
 *
 * // Use in JSON parsing
 * String json = "[\"a\", \"b\", \"c\"]";
 * List<String> list = JSON.parseObject(json, listType);
 * }</pre>
 *
 * @see ParameterizedType
 * @see java.lang.reflect.Type
 * @since 2.0.0
 */
@JSONType(deserializeFeatures = JSONReader.Feature.SupportAutoType, typeName = "java.lang.reflect.ParameterizedType")
public class ParameterizedTypeImpl
        implements ParameterizedType {
    private final Type[] actualTypeArguments;
    private final Type ownerType;
    private final Type rawType;

    /**
     * Constructs a ParameterizedType with the specified arguments, owner type, and raw type.
     * This constructor is primarily used for deserialization.
     *
     * @param actualTypeArguments the actual type arguments of this parameterized type
     * @param ownerType the owner type, if this is an inner class, or null
     * @param rawType the raw type (e.g., List.class for List<String>)
     */
    @JSONCreator
    public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = ownerType;
        this.rawType = rawType;
    }

    /**
     * Constructs a ParameterizedType with the specified raw type and type arguments.
     * The owner type is set to null.
     *
     * @param rawType the raw type (e.g., List.class for List<String>)
     * @param actualTypeArguments the actual type arguments (e.g., String.class for List<String>)
     */
    public ParameterizedTypeImpl(Type rawType, Type... actualTypeArguments) {
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = null;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParameterizedTypeImpl that = (ParameterizedTypeImpl) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(actualTypeArguments, that.actualTypeArguments)) {
            return false;
        }
        if (!Objects.equals(ownerType, that.ownerType)) {
            return false;
        }
        return Objects.equals(rawType, that.rawType);
    }

    @Override
    public int hashCode() {
        int result = actualTypeArguments != null ? Arrays.hashCode(actualTypeArguments) : 0;
        result = 31 * result + (ownerType != null ? ownerType.hashCode() : 0);
        result = 31 * result + (rawType != null ? rawType.hashCode() : 0);
        return result;
    }
}
