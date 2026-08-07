package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Type;
import java.util.function.IntFunction;

public class ObjectReaderImplValueInt<T>
        implements ObjectReader<T> {
    final long features;
    final IntFunction<T> function;
    final JSONSchema schema;

    public ObjectReaderImplValueInt(
            Class<T> objectClass,
            long features,
            JSONSchema schema,
            IntFunction<T> function
    ) {
        this.features = features;
        this.schema = schema;
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

        if (schema != null) {
            schema.validate(value);
        }

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
                null,
                function
        );
    }

    public static <T> ObjectReaderImplValueInt<T> of(
            Class<T> objectClass,
            long features,
            JSONSchema schema,
            IntFunction<T> function
    ) {
        return new ObjectReaderImplValueInt(
                objectClass,
                features,
                schema,
                function
        );
    }
}
