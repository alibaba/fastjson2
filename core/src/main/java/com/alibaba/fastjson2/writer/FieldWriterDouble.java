package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.*;

class FieldWriterDouble<T>
        extends FieldWriter<T> {
    FieldWriterDouble(
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

    public double getFieldValueDouble(T object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }
        return propertyAccessor.getDoubleValue(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Double value = (Double) propertyAccessor.getObject(object);
        if (value == null) {
            jsonWriter.writeNumberNull();
        } else {
            double doubleValue = value;
            if (decimalFormat != null) {
                jsonWriter.writeDouble(doubleValue, decimalFormat);
            } else {
                jsonWriter.writeDouble(doubleValue);
            }
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Double value;
        try {
            value = (Double) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeFloatNull(jsonWriter);
        }

        writeDoubleValue(jsonWriter, value, features);
        return true;
    }

    protected final void writeDoubleValue(JSONWriter jsonWriter, Double value, long features) {
        double doubleValue = value;
        if (doubleValue == 0.0 && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return;
        }

        writeFieldName(jsonWriter);

        if (decimalFormat != null) {
            jsonWriter.writeDouble(doubleValue, decimalFormat);
        } else {
            boolean writeNonStringValueAsString = (features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
            if (writeNonStringValueAsString) {
                jsonWriter.writeString(Double.toString(doubleValue));
            } else {
                jsonWriter.writeDouble(doubleValue);
            }
        }
    }
}
