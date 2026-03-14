package com.alibaba.fastjson3;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Type token for capturing generic type information at runtime.
 * Use anonymous subclass to preserve generic type parameters.
 *
 * <pre>
 * // Capture List&lt;User&gt; type
 * TypeReference&lt;List&lt;User&gt;&gt; ref = new TypeReference&lt;List&lt;User&gt;&gt;() {};
 * List&lt;User&gt; users = JSON.parseObject(json, ref.getType());
 * </pre>
 *
 * @param <T> the referenced type
 */
public abstract class TypeReference<T> {
    private final Type type;

    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        } else {
            throw new JSONException("TypeReference must be created with actual type parameter");
        }
    }

    public Type getType() {
        return type;
    }
}
