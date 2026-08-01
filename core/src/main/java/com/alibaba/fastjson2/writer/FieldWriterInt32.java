package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.*;

class FieldWriterInt32<T>
        extends FieldWriter<T> {
    final boolean toString;

    FieldWriterInt32(
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
        toString = (features & MASK_WRITE_NON_STRING_VALUE_AS_STRING) != 0
                || "string".equals(format);
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return propertyAccessor.getObject(object);
        } catch (Throwable e) {
            throw errorOnGet(e);
        }
    }

    public int getFieldValueInt(T object) {
        if (object == null) {
            throw new JSONException("field.get error, ".concat(fieldName));
        }
        return propertyAccessor.getIntValue(object);
    }

    @Override
    public final void writeInt32(JSONWriter jsonWriter, int value) {
        long features = jsonWriter.getFeatures() | this.features;
        if (value == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
            return;
        }
        if (toString) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(Integer.toString(value));
            return;
        }
        writeFieldName(jsonWriter);
        if (format != null) {
            jsonWriter.writeInt32(value, format);
        } else {
            jsonWriter.writeInt32(value);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Integer value;
        try {
            value = (Integer) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeIntNull(jsonWriter);
        }

        writeInt32(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Integer value = (Integer) propertyAccessor.getObject(object);

        if (value == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        jsonWriter.writeInt32(value);
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (valueClass == this.fieldClass) {
            return ObjectWriterImplInt32.INSTANCE;
        }

        return jsonWriter.getObjectWriter(valueClass);
    }
}
