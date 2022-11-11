package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Function;

public class ObjectReaderImplValue<I, T>
        implements ObjectReader<T> {
    final Type valueType;
    final Class<I> valueClass;
    final long features;
    final Constructor<T> constructor;
    final Method factoryMethod;
    final Function<I, T> function;
    final JSONSchema schema;
    final Object emptyVariantArgs;
    ObjectReader valueReader;

    public ObjectReaderImplValue(
            Class<T> objectClass,
            Type valueType,
            Class<I> valueClass,
            long features,
            String format,
            Object defaultValue,
            JSONSchema schema,
            Constructor<T> constructor,
            Method factoryMethod,
            Function<I, T> function
    ) {
        this.valueType = valueType;
        this.valueClass = valueClass;
        this.features = features;
        this.schema = schema;
        this.constructor = constructor;
        this.factoryMethod = factoryMethod;
        this.function = function;

        if (factoryMethod != null && factoryMethod.getParameterCount() == 2) {
            Class<?> varArgType = factoryMethod.getParameterTypes()[1].getComponentType();
            emptyVariantArgs = Array.newInstance(varArgType, 0);
        } else {
            emptyVariantArgs = null;
        }
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return readObject(jsonReader, fieldType, fieldName, features);
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (valueReader == null) {
            valueReader = jsonReader.getObjectReader(valueType);
        }

        I value = (I) valueReader.readObject(jsonReader, fieldType, fieldName, features | this.features);

        if (value == null) {
            return null;
        }

        if (schema != null) {
            schema.validate(value);
        }

        T object;

        if (function != null) {
            try {
                object = function.apply(value);
            } catch (Exception ex) {
                throw new JSONException(jsonReader.info("create object error"), ex);
            }
        } else if (constructor != null) {
            try {
                object = constructor.newInstance(value);
            } catch (Exception ex) {
                throw new JSONException(jsonReader.info("create object error"), ex);
            }
        } else if (factoryMethod != null) {
            try {
                if (emptyVariantArgs != null) {
                    object = (T) factoryMethod.invoke(null, value, emptyVariantArgs);
                } else {
                    object = (T) factoryMethod.invoke(null, value);
                }
            } catch (Exception ex) {
                throw new JSONException(jsonReader.info("create object error"), ex);
            }
        } else {
            throw new JSONException(jsonReader.info("create object error"));
        }

        return object;
    }

    public static <I, T> ObjectReaderImplValue<I, T> of(Class<T> objectClass, Class<I> valueClass, Method method) {
        return new ObjectReaderImplValue(objectClass, valueClass, valueClass, 0L, null, null, null, null, method, null);
    }

    public static <I, T> ObjectReaderImplValue<I, T> of(Class<T> objectClass, Class<I> valueClass, Function<I, T> function) {
        return new ObjectReaderImplValue(objectClass, valueClass, valueClass, 0L, null, null, null, null, null, function);
    }
}
