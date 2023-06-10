package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.BiConsumer;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Locale;

final class FieldReaderBigDecimalFunc<T, V>
        extends FieldReader<T> {
    final BiConsumer<T, V> function;

    public FieldReaderBigDecimalFunc(
            String fieldName,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            Method method,
            BiConsumer<T, V> function) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Object value) {
        BigDecimal decimalValue = TypeUtils.toBigDecimal(value);

        try {
            function.accept(object, (V) decimalValue);
        } catch (Exception e) {
            throw new JSONException("set " + super.toString() + " error", e);
        }
    }

    @Override
    public void accept(T object, int value) {
        try {
            function.accept(object, (V) BigDecimal.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + super.toString() + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        try {
            function.accept(object, (V) BigDecimal.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + super.toString() + " error", e);
        }
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigDecimal fieldValue;
        try {
            fieldValue = jsonReader.readBigDecimal();
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
        return jsonReader.readBigDecimal();
    }
}
