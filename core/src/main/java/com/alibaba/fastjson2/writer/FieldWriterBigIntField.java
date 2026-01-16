package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.math.BigInteger;

import static com.alibaba.fastjson2.JSONWriter.*;

final class FieldWriterBigIntField<T>
        extends FieldWriter<T> {
    FieldWriterBigIntField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, null, label, BigInteger.class, BigInteger.class, field, null);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        BigInteger value = (BigInteger) getFieldValue(object);
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
    public void writeValue(JSONWriter jsonWriter, T object) {
        BigInteger value = (BigInteger) getFieldValue(object);
        jsonWriter.writeBigInt(value, features);
    }
}
