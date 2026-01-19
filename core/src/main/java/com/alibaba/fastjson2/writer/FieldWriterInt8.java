package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.*;

class FieldWriterInt8<T>
        extends FieldWriter<T> {
    FieldWriterInt8(
            String name,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Object function
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, function);
    }

    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
    }

    public byte getFieldValueByte(T object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }
        return propertyAccessor.getByteValue(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Byte value = (Byte) propertyAccessor.getObject(object);
        if (value == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt32(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Byte value;
        try {
            value = (Byte) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeIntNull(jsonWriter);
        }

        writeInt8(jsonWriter, value);
        return true;
    }

    protected final boolean writeInt8(JSONWriter jsonWriter, byte value) {
        long features = jsonWriter.getFeatures(this.features);
        if (value == 0 && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return false;
        }
        boolean writeNonStringValueAsString = (features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
        writeFieldName(jsonWriter);
        if (writeNonStringValueAsString) {
            jsonWriter.writeString(Byte.toString(value));
        } else {
            jsonWriter.writeInt8(value);
        }
        return true;
    }
}
