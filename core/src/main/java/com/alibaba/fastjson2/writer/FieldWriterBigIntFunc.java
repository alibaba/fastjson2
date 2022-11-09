package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.function.Function;

final class FieldWriterBigIntFunc<T>
        extends FieldWriter<T> {
    final Function<T, BigInteger> function;

    FieldWriterBigIntFunc(String fieldName, int ordinal, long features, String format, String label, Method method, Function<T, BigInteger> function) {
        super(fieldName, ordinal, features, format, label, BigInteger.class, BigInteger.class, null, method);
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
        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) == 0) {
                return false;
            }
        }
        writeFieldName(jsonWriter);
        jsonWriter.writeBigInt(value, features);
        return true;
    }
}
