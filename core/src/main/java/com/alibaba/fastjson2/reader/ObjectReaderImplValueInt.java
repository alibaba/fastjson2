package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.IntFunction;

import java.lang.reflect.Type;

public class ObjectReaderImplValueInt<T>
        implements ObjectReader<T> {
    final long features;
    final IntFunction<T> function;

    public ObjectReaderImplValueInt(
            Class<T> objectClass,
            long features,
            IntFunction<T> function
    ) {
        this.features = features;
        this.function = function;
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return readObject(jsonReader, fieldType, fieldName, features);
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNullOrEmptyString()) {
            return null;
        }

        int value = jsonReader.readInt32Value();

        T object;
        try {
            object = function.apply(value);
        } catch (Exception ex) {
            throw new JSONException(jsonReader.info("create object error"), ex);
        }

        return object;
    }

    public static <T> ObjectReaderImplValueInt<T> of(
            Class<T> objectClass,
            IntFunction<T> function
    ) {
        return new ObjectReaderImplValueInt(
                objectClass,
                0L,
                function
        );
    }

    public static <T> ObjectReaderImplValueInt<T> of(
            Class<T> objectClass,
            long features,
            IntFunction<T> function
    ) {
        return new ObjectReaderImplValueInt(
                objectClass,
                features,
                function
        );
    }
}
