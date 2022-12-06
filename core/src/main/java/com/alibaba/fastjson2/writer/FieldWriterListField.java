package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;

final class FieldWriterListField<T>
        extends FieldWriterList<T> {
    protected FieldWriterListField(
            String fieldName,
            Type itemType,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field
    ) {
        super(fieldName, itemType, ordinal, features, format, label, fieldType, fieldClass, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        List value = (List) getFieldValue(object);

        JSONWriter.Context context = jsonWriter.context;

        if (value == null) {
            long features = this.features | context.getFeatures();
            if ((features & (WriteNulls.mask | NullAsDefaultValue.mask | WriteNullListAsEmpty.mask)) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeArrayNull();
                return true;
            } else {
                return false;
            }
        }

        String refPath = jsonWriter.setPath(this, value);
        if (refPath != null) {
            writeFieldName(jsonWriter);
            jsonWriter.writeReference(refPath);
            jsonWriter.popPath(value);
            return true;
        }

        if (itemType == String.class) {
            writeListStr(jsonWriter, true, value);
        } else {
            writeList(jsonWriter, true, value);
        }
        jsonWriter.popPath(value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        List value = (List) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        boolean refDetect = jsonWriter.isRefDetect();

        if (refDetect) {
            String refPath = jsonWriter.setPath(fieldName, value);
            if (refPath != null) {
                jsonWriter.writeReference(refPath);
                jsonWriter.popPath(value);
                return;
            }
        }

        writeList(jsonWriter, false, value);

        if (refDetect) {
            jsonWriter.popPath(value);
        }
    }
}
