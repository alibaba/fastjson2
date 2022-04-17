package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/** 
 * Represents a generic type {@code T}. Java doesn't yet provide a way to
 * represent generic types, so this class does. Forces clients to create a
 * subclass of this class which enables retrieval the type information even at
 * runtime.
 *
 * <p>For example, to create a type literal for {@code List<String>}, you can
 * create an empty anonymous inner class:
 *
 * <pre>
 * TypeReference&lt;List&lt;String&gt;&gt; list = new TypeReference&lt;List&lt;String&gt;&gt;() {};
 * </pre>
 * This syntax cannot be used to create type literals that have wildcard
 * parameters, such as {@code Class<?>} or {@code List<? extends CharSequence>}.
 */
public class TypeReference<T> {

    protected final Type type;
    final Class<? super T> rawType;
    final int hashCode;

    /**
     * Constructs a new type literal. Derives represented class from type
     * parameter.
     *
     * <p>Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy so we can reconstitute it
     * at runtime despite erasure.
     */
    protected TypeReference(){
        Type superClass = getClass().getGenericSuperclass();
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        this.rawType = (Class<? super T>) BeanUtils.getRawType(type);
        this.hashCode = type.hashCode();
    }

    TypeReference(Type type) {
        if (type == null) {
            throw new NullPointerException();
        }

        this.type = BeanUtils.canonicalize(type);
        this.rawType = (Class<? super T>) BeanUtils.getRawType(this.type);
        this.hashCode = this.type.hashCode();
    }
    
    /**
     * Gets underlying {@code Type} instance.
     */
    public Type getType() {
        return type;
    }

    public final Class<? super T> getRawType() {
        return rawType;
    }

    public static TypeReference<?> get(Type type) {
        return new TypeReference<Object>(type);
    }
}
