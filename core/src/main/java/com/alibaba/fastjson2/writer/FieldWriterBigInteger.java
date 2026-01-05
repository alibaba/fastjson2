package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Locale;
import java.util.function.Function;

final class FieldWriterBigInteger<T>
        extends FieldWriter<T> {
    final Function<T, BigInteger> function;

    FieldWriterBigInteger(
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
        this.function = function;
    }

    @Override
    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        BigInteger value = (BigInteger) getFieldValue(object);
        jsonWriter.writeBigInt(value, features);
    }

    @Override
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, T o) {
        BigInteger value = (BigInteger) propertyAccessor.getObject(o);
        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) == 0) {
                return false;
            }
        }
        writeFieldNameJSONB(jsonWriter);
        jsonWriter.writeBigInt(value, features);
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T o) {
        BigInteger value = (BigInteger) propertyAccessor.getObject(o);
        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) == 0) {
                return false;
            }
        }
        writeFieldNameUTF8(jsonWriter);
        jsonWriter.writeBigInt(value, features);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, T o) {
        BigInteger value = (BigInteger) propertyAccessor.getObject(o);
        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) == 0) {
                return false;
            }
        }
        writeFieldNameUTF16(jsonWriter);
        jsonWriter.writeBigInt(value, features);
        return true;
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
