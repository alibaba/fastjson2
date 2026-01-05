package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.ToLongFunction;

import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_ERROR_GETTER;
import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_DEFAULT_VALUE;

@SuppressWarnings("ALL")
final class FieldWriterInt64Val
        extends FieldWriterInt64 {
    FieldWriterInt64Val(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            ToLongFunction function
    ) {
        super(name, ordinal, features, format, label, long.class, field, method, function);
    }

    @Override
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, Object object) {
        long features = jsonWriter.getFeatures(this.features);
        long value;
        try {
            value = propertyAccessor.getLongValue(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
            return false;
        }

        writeInt64JSONB(jsonWriter, value);
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, Object object) {
        long features = jsonWriter.getFeatures(this.features);
        long value;
        try {
            value = propertyAccessor.getLongValue(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
            return false;
        }
        jsonWriter.writeNameRaw(fieldNameUTF8(features));
        utf8Impl.accept(jsonWriter, value);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, Object object) {
        long features = jsonWriter.getFeatures(this.features);
        long value;
        try {
            value = propertyAccessor.getLongValue(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
            return false;
        }
        jsonWriter.writeNameRaw(fieldNameUTF16(features));
        utf16Impl.accept(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        jsonWriter.writeInt64(
                propertyAccessor.getLongValue(object)
        );
    }
}
