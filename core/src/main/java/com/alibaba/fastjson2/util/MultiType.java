package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSON;

import java.lang.reflect.Type;

/**
 * Represents a union of multiple types. This is used internally by fastjson2
 * to support scenarios where a field or parameter can accept multiple different types.
 *
 * <p>For example, a field that can be either a String or an Integer could be
 * represented using MultiType.
 *
 * @see Type
 * @since 2.0.0
 */
public class MultiType
        implements Type {
    private final Type[] types;

    /**
     * Constructs a MultiType with the specified types.
     *
     * @param types the array of types that this MultiType represents
     */
    public MultiType(Type... types) {
        this.types = types;
    }

    /**
     * Returns the number of types in this MultiType.
     *
     * @return the size of the type array
     */
    public int size() {
        return types.length;
    }

    /**
     * Returns the type at the specified index.
     *
     * @param index the index of the type to retrieve
     * @return the type at the specified index
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     */
    public Type getType(int index) {
        return types[index];
    }

    @Override
    public String toString() {
        return JSON.toJSONString(types);
    }
}
