package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.Feature;
import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_ERROR_GETTER;

class FieldWriterInt16<T>
        extends FieldWriter<T> {
    FieldWriterInt16(
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

    public short getFieldValueShort(T object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }
        return propertyAccessor.getShortValue(object);
    }

    protected final void writeInt16(JSONWriter jsonWriter, short value) {
        long features = jsonWriter.getFeatures(this.features);
        if (value == 0 && (features & Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return;
        }
        boolean writeNonStringValueAsString = (features & Feature.WriteNonStringValueAsString.mask) != 0;
        if (writeNonStringValueAsString) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(Short.toString(value));
            return;
        }
        writeFieldName(jsonWriter);
        jsonWriter.writeInt16(value);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Short value = (Short) propertyAccessor.getObject(object);
        if (value == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt32(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Short value;
        try {
            value = (Short) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeIntNull(jsonWriter);
        }

        writeInt16(jsonWriter, value);
        return true;
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (valueClass == fieldClass) {
            return ObjectWriterImplInt16.INSTANCE;
        }
        return jsonWriter.getObjectWriter(valueClass);
    }
}
