package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.alibaba.fastjson2.JSONWriter.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;

class FieldWriterInt64<T>
        extends FieldWriter<T> {
    final boolean browserCompatible;
    final boolean toString;
    final NameValueLongWriter<JSONWriterUTF8> utf8Value;
    final NameValueLongWriter<JSONWriterUTF8> utf8NameValue;
    final NameValueLongWriter<JSONWriterUTF16> utf16Value;
    final NameValueLongWriter<JSONWriterJSONB> jsonbValue;

    FieldWriterInt64(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Class fieldClass,
            Field field,
            Method method,
            Object function
    ) {
        super(name, ordinal, features, format, null, label, fieldClass, fieldClass, field, method, function);
        browserCompatible = (features & JSONWriter.Feature.BrowserCompatible.mask) != 0;
        toString = (features & WriteNonStringValueAsString.mask) != 0
                || "string".equals(format);

        NameValueLongWriter<JSONWriterUTF8> utf8NameValue = null;
        if (toString) {
            utf8Value = (w, v, f) -> w.writeString(v);
            utf16Value = (w, v, f) -> w.writeString(v);
            jsonbValue = (w, v, f) -> w.writeString(v);
        } else if (format != null) {
            utf8Value = (w, v, f) -> w.writeString(String.format(format, v));
            utf16Value = (w, v, f) -> w.writeString(String.format(format, v));
            jsonbValue = (w, v, f) -> w.writeString(String.format(format, v));
        } else {
            utf8Value = JSONWriterUTF8::writeInt64;
            utf16Value = JSONWriterUTF16::writeInt64;
            jsonbValue = JSONWriterJSONB::writeInt64;

            if (defaultValue == null) {
                utf8NameValue = (w, v, f) -> {
                    long features2 = w.getFeatures() | this.features;
                    if (v == 0 && (features2 & MASK_NOT_WRITE_DEFAULT_VALUE) != 0) {
                        return;
                    }
                    w.writeInt64(fieldNameUTF8(features2), v, features2);
                };
            }
        }
        if (utf8NameValue == null) {
            utf8NameValue = (w, v, f) -> {
                long features2 = w.getFeatures() | this.features;
                if (v == 0 && (features2 & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
                    return;
                }
                w.writeNameRaw(fieldNameUTF8(features2));
                utf8Value.write(w, v, features2);
            };
        }
        this.utf8NameValue = utf8NameValue;
    }

    @Override
    public final void writeInt64JSONB(JSONWriterJSONB jsonWriter, long value) {
        long features = jsonWriter.getFeatures() | this.features;
        if (value == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
            return;
        }
        writeFieldName(jsonWriter);
        jsonbValue.write(jsonWriter, value, features);
    }

    @Override
    public final void writeInt64UTF8(JSONWriterUTF8 jsonWriter, long value) {
        utf8NameValue.write(jsonWriter, value, jsonWriter.getFeatures() | this.features);
    }

    @Override
    public final void writeInt64UTF16(JSONWriterUTF16 jsonWriter, long value) {
        long features = jsonWriter.getFeatures() | this.features;
        if (value == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
            return;
        }
        jsonWriter.writeNameRaw(fieldNameUTF16(features));
        utf16Value.write(jsonWriter, value, features);
    }

    private static final long MASK_WRITE_NULLS = MASK_WRITE_MAP_NULL_VALUE | MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO;

    @Override
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Long value;
        try {
            value = (Long) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & MASK_WRITE_NULLS) == 0) {
                return false;
            }
            writeFieldNameJSONB(jsonWriter);
            jsonWriter.writeNumberNull();
            return true;
        }

        writeInt64JSONB(jsonWriter, value);
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Long value;
        try {
            value = (Long) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & MASK_WRITE_NULLS) == 0) {
                return false;
            }
            jsonWriter.writeNameRaw(fieldNameUTF8(jsonWriter.getFeatures(features)));
            jsonWriter.writeNumberNull();
            return true;
        }

        writeInt64UTF8(jsonWriter, value);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Long value;
        try {
            value = (Long) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & MASK_WRITE_NULLS) == 0) {
                return false;
            }
            jsonWriter.writeNameRaw(fieldNameUTF16(jsonWriter.getFeatures(features)));
            jsonWriter.writeNumberNull();
            return true;
        }

        writeInt64UTF16(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Long value = (Long) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        jsonWriter.writeInt64(value);
    }
}
