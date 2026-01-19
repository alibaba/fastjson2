package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Locale;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.*;

final class FieldWriterBigInt<T>
        extends FieldWriter<T> {
    FieldWriterBigInt(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Field field,
            Method method,
            Function<T, BigInteger> function
    ) {
        super(fieldName, ordinal, features, format, locale, label, BigInteger.class, BigInteger.class, field, method, function);
    }

    @Override
    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        BigInteger value = (BigInteger) propertyAccessor.getObject(object);
        jsonWriter.writeBigInt(value, features);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        BigInteger value;
        try {
            value = (BigInteger) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        long features = this.features | jsonWriter.getFeatures();
        if (value == null) {
            if ((features & (MASK_WRITE_MAP_NULL_VALUE | MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0) {
                return false;
            }
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeBigInt(value, features);
        return true;
    }
}
