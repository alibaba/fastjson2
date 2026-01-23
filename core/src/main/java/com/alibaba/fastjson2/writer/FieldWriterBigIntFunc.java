package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.*;

final class FieldWriterBigIntFunc<T>
        extends FieldWriter<T> {
    final Function<T, BigInteger> function;

    FieldWriterBigIntFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            Function<T, BigInteger> function
    ) {
        super(fieldName, ordinal, features, format, null, label, BigInteger.class, BigInteger.class, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        BigInteger value = (BigInteger) getFieldValue(object);
        jsonWriter.writeBigInt(value, features);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T o) {
        BigInteger value = function.apply(o);
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

    @Override
    public Function getFunction() {
        return function;
    }
}
