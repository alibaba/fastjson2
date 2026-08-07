package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Type;
import java.util.function.Function;

public class ObjectReaderImplValueString<T>
        implements ObjectReader<T> {
    final long features;
    final Function<String, T> function;
    final JSONSchema schema;

    public ObjectReaderImplValueString(
            Class<T> objectClass,
            long features,
            JSONSchema schema,
            Function<String, T> function
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

        String value = jsonReader.readString();

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

    public static <T> ObjectReaderImplValueString<T> of(
            Class<T> objectClass,
            Function<String, T> function
    ) {
        return new ObjectReaderImplValueString(
                objectClass,
                0,
                null,
                function
        );
    }

    public static <T> ObjectReaderImplValueString<T> of(
            Class<T> objectClass,
            long features,
            JSONSchema schema,
            Function<String, T> function
    ) {
        return new ObjectReaderImplValueString(
                objectClass,
                features,
                schema,
                function
        );
    }
}
