package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;
import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_DEFAULT_VALUE;

class FieldWriterInt32<T>
        extends FieldWriter<T> {
    final boolean toString;
    final NameValueIntWriter<JSONWriterUTF8> utf8Value;
    final NameValueIntWriter<JSONWriterUTF16> utf16Value;
    final NameValueIntWriter<JSONWriterUTF8> utf8NameValue;

    public FieldWriterInt32(
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
        toString = (features & WriteNonStringValueAsString.mask) != 0
                || "string".equals(format);

        NameValueIntWriter<JSONWriterUTF8> utf8NameValue = null;
        if (toString) {
            utf8Value = (w, v, f) -> w.writeString(v);
            utf16Value = (w, v, f) -> w.writeString(v);
        } else if (format != null) {
            utf8Value = (w, v, f) -> w.writeString(String.format(format, v));
            utf16Value = (w, v, f) -> w.writeString(String.format(format, v));
        } else {
            utf8Value = (w, v, f) -> w.writeInt32(v);
            utf16Value = (w, v, f) -> w.writeInt32(v);
            utf8NameValue = (w, v, f) -> {
                long features2 = w.getFeatures() | this.features;
                if (v == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
                    return;
                }
                w.writeInt32(fieldNameUTF8(features2), v, features2);
            };
        }
        if (utf8NameValue == null) {
            utf8NameValue = (w, v, f) -> {
                long features2 = w.getFeatures() | this.features;
                if (v == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
                    return;
                }
                w.writeNameRaw(fieldNameUTF8(features2));
                utf8Value.write(w, v, features2);
            };
        }
        this.utf8NameValue = utf8NameValue;
    }

    @Override
    public final void writeInt32JSONB(JSONWriterJSONB jsonWriter, int value) {
        long features = jsonWriter.getFeatures() | this.features;
        if (value == 0 && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
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
    public final void writeInt32UTF8(JSONWriterUTF8 jsonWriter, int value) {
        utf8NameValue.write(jsonWriter, value, jsonWriter.getFeatures() | this.features);
    }

    @Override
    public final void writeInt32UTF16(JSONWriterUTF16 jsonWriter, int value) {
        long features = jsonWriter.getFeatures() | this.features;
        if (value == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
            return;
        }
        jsonWriter.writeNameRaw(fieldNameUTF16(features));
        utf16Value.write(jsonWriter, value, features);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Integer value;
        try {
            value = (Integer) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
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
        Integer value = (Integer) getFieldValue(object);

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
