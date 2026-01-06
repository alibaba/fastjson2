package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;
import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_ERROR_GETTER;
import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_DEFAULT_VALUE;

final class FieldWriterDoubleVal<T>
        extends FieldWriter<T> {
    private final NameValueDoubleWriter<JSONWriterUTF8> nameValueUTF8;
    private final NameValueDoubleWriter<JSONWriterUTF16> nameValueUTF16;
    private final NameValueDoubleWriter<JSONWriterJSONB> nameValueJSONB;

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
            nameValueUTF8 = (w, v, f) -> {
                writeFieldNameUTF8(w);
                w.writeDouble(v, decimalFormat);
            };
            nameValueUTF16 = (w, v, f) -> {
                writeFieldNameUTF16(w);
                w.writeDouble(v, decimalFormat);
            };
            nameValueJSONB = (w, v, f) -> {
                writeFieldNameJSONB(w);
                w.writeDouble(v, decimalFormat);
            };
        } else {
            if ((features & WriteNonStringValueAsString.mask) != 0) {
                nameValueUTF8 = (w, v, f) -> {
                    writeFieldNameUTF8(w);
                    w.writeString(v);
                };
                nameValueUTF16 = (w, v, f) -> {
                    writeFieldNameUTF16(w);
                    w.writeString(v);
                };
                nameValueJSONB = (w, v, f) -> {
                    writeFieldNameJSONB(w);
                    w.writeString(v);
                };
            } else {
                nameValueUTF8 = (w, v, f) -> {
                    long features2 = w.getFeatures() | this.features | f;
                    w.writeDouble(fieldNameUTF8(w.getFeatures(features2)), v, features2);
                };
                nameValueUTF16 = (w, v, f) -> {
                    writeFieldNameUTF16(w);
                    w.writeDouble(v);
                };
                nameValueJSONB = (w, v, f) -> {
                    writeFieldNameJSONB(w);
                    w.writeDouble(v);
                };
            }
        }
    }

    @Override
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, T object) {
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

        nameValueJSONB.write(jsonWriter, value, features);
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

        nameValueUTF8.write(jsonWriter, value, features);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, T object) {
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

        nameValueUTF16.write(jsonWriter, value, features);
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
