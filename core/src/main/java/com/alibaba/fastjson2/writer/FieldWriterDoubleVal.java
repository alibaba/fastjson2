package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.ObjDoubleConsumer;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

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
    public boolean write(JSONWriter jsonWriter, T object) {
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

        writeFieldName(jsonWriter);
        writerImpl.accept(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        writerImpl.accept(jsonWriter, propertyAccessor.getDoubleValue(object));
    }
}
