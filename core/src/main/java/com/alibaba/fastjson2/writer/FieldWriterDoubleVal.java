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

final class FieldWriterDoubleVal<T>
        extends FieldWriter<T> {
    private final ObjDoubleConsumer<JSONWriterUTF8> valueUTF8;
    private final ObjDoubleConsumer<JSONWriterUTF16> valueUTF16;
    private final ObjDoubleConsumer<JSONWriterJSONB> valueJSONB;
    private final ObjDoubleConsumer<JSONWriterUTF8> nameValueUTF8;

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
            valueUTF8 = (w, v) -> w.writeDouble(v, decimalFormat);
            valueUTF16 = (w, v) -> w.writeDouble(v, decimalFormat);
            valueJSONB = (w, v) -> w.writeDouble(v, decimalFormat);
            nameValueUTF8 = (w, v) -> {
                writeFieldNameUTF8(w);
                w.writeDouble(v, decimalFormat);
            };
        } else {
            if ((features & WriteNonStringValueAsString.mask) != 0) {
                valueUTF8 = JSONWriterUTF8::writeString;
                valueUTF16 = JSONWriterUTF16::writeString;
                valueJSONB = JSONWriterJSONB::writeString;
                nameValueUTF8 = (w, v) -> {
                    writeFieldNameUTF8(w);
                    w.writeString(v);
                };
            } else {
                valueUTF8 = JSONWriterUTF8::writeDouble;
                valueUTF16 = JSONWriterUTF16::writeDouble;
                valueJSONB = JSONWriterJSONB::writeDouble;
                nameValueUTF8 = (w, v) -> {
                    long features2 = w.getFeatures(this.features);
                    w.writeDouble(fieldNameUTF8(w.getFeatures(features2)), v, features2);
                };
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
        valueJSONB.accept(jsonWriter, value);
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

        nameValueUTF8.accept(jsonWriter, value);
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
        valueUTF16.accept(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        double value = propertyAccessor.getDoubleValue(object);
        if (decimalFormat != null) {
            jsonWriter.writeDouble(value, decimalFormat);
        } else {
            jsonWriter.writeDouble(value);
        }
    }
}
