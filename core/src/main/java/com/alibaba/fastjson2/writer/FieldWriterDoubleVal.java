package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.ObjDoubleConsumer;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;
import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_ERROR_GETTER;
import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_DEFAULT_VALUE;

final class FieldWriterDoubleVal<T>
        extends FieldWriter<T> {
    private final ObjDoubleConsumer<JSONWriter> writerImpl;

    FieldWriterDoubleVal(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Object function
    ) {
        super(name, ordinal, features, format, null, label, fieldType, fieldClass, field, method, function);

        if (decimalFormat != null) {
            writerImpl = (w, v) -> w.writeDouble(v, decimalFormat);
        } else {
            if ((features & WriteNonStringValueAsString.mask) != 0) {
                writerImpl = JSONWriter::writeString;
            } else {
                writerImpl = JSONWriter::writeDouble;
            }
        }
    }

    @Override
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, T object) {
        double value;
        try {
            value = propertyAccessor.getDoubleValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue) && defaultValue == null) {
            return false;
        }

        writeFieldNameJSONB(jsonWriter);
        writerImpl.accept(jsonWriter, value);
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        long features = jsonWriter.getFeatures(this.features);
        double value;
        try {
            value = propertyAccessor.getDoubleValue(object);
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
        writerImpl.accept(jsonWriter, value);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, T object) {
        double value;
        try {
            value = propertyAccessor.getDoubleValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue) && defaultValue == null) {
            return false;
        }

        writeFieldNameUTF16(jsonWriter);
        writerImpl.accept(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        writerImpl.accept(jsonWriter, propertyAccessor.getDoubleValue(object));
    }
}
