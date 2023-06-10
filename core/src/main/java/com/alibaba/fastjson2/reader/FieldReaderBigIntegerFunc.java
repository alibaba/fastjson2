package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.BiConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Locale;

final class FieldReaderBigIntegerFunc<T, V>
        extends FieldReader<T> {
    final BiConsumer<T, V> function;

    public FieldReaderBigIntegerFunc(
            String fieldName,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            Method method,
            BiConsumer<T, V> function
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Object value) {
        BigInteger bigInteger = TypeUtils.toBigInteger(value);

        try {
            function.accept(object, (V) bigInteger);
        } catch (Exception e) {
            throw new JSONException("set " + super.toString() + " error", e);
        }
    }

    @Override
    public void accept(T object, int value) {
        try {
            function.accept(object,
                    (V) BigInteger.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + super.toString() + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        try {
            function.accept(object, (V) BigInteger.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + super.toString() + " error", e);
        }
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigInteger fieldValue;
        try {
            fieldValue = jsonReader.readBigInteger();
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                fieldValue = null;
            } else {
                throw e;
            }
        }

        function.accept(object, (V) fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBigInteger();
    }
}
