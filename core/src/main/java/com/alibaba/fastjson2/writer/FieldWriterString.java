package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.IgnoreErrorGetter;
import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_ERROR_GETTER;

final class FieldWriterString<T>
        extends FieldWriter<T> {
    FieldWriterString(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Function function
    ) {
        super(name, ordinal, features, format, null, label, fieldType, fieldClass, field, method, function);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        String value = (String) propertyAccessor.getObject(object);

        if (trim && value != null) {
            value = value.trim();
        }

        if (symbol && jsonWriter.jsonb) {
            jsonWriter.writeSymbol(value);
        } else {
            if (raw) {
                jsonWriter.writeRaw(value);
            } else {
                jsonWriter.writeString(value);
            }
        }
    }

    @Override
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, T object) {
        long features = jsonWriter.getFeatures(this.features);
        String value;
        try {
            value = (String) propertyAccessor.getObject(object);
        } catch (Exception error) {
            if ((features & IgnoreErrorGetter.mask) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) == 0
                    || (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0) {
                return false;
            }

            writeFieldNameJSONB(jsonWriter);
            if ((features & (JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) != 0) {
                jsonWriter.writeString("");
            } else {
                jsonWriter.writeNull();
            }
            return true;
        }

        if (trim) {
            value = value.trim();
        }

        if (value.isEmpty() && (features & JSONWriter.Feature.IgnoreEmpty.mask) != 0) {
            return false;
        }

        writeFieldNameJSONB(jsonWriter);

        if (symbol) {
            jsonWriter.writeSymbol(value);
        } else {
            if (raw) {
                jsonWriter.writeRaw(value);
            } else {
                jsonWriter.writeString(value);
            }
        }
        return true;
    }

    private static final long MASK_WRITE_NULL = JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask;
    private static final long MASK_NOT_WRITE_DEFAULT_VALUE = JSONWriter.Feature.NotWriteDefaultValue.mask;
    private static final long MASK_NULL_AS_DEFALT = JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask;
    private static final long MASK_IGNORE_EMPTY = JSONWriter.Feature.IgnoreEmpty.mask;

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        long features = jsonWriter.getFeatures(this.features);
        String value;
        try {
            value = (String) propertyAccessor.getObject(object);
        } catch (Exception error) {
            if ((features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & MASK_WRITE_NULL) == 0
                    || (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0) {
                return false;
            }

            if ((features & MASK_NULL_AS_DEFALT) != 0) {
                value = "";
            }
        }

        if (trim && value != null) {
            value = value.trim();
        }

        if (value != null && value.isEmpty() && (features & MASK_IGNORE_EMPTY) != 0) {
            return false;
        }

        jsonWriter.writeNameRaw(fieldNameUTF8(features));

        if (raw) {
            jsonWriter.writeRaw(value == null ? "null" : value);
        } else {
            jsonWriter.writeString(value);
        }
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, T object) {
        long features = jsonWriter.getFeatures(this.features);
        String value;
        try {
            value = (String) propertyAccessor.getObject(object);
        } catch (Exception error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & MASK_WRITE_NULL) == 0
                    || (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0) {
                return false;
            }

            if ((features & MASK_NULL_AS_DEFALT) != 0) {
                value = "";
            }
        }

        if (trim && value != null) {
            value = value.trim();
        }

        if (value != null && value.isEmpty() && (features & MASK_IGNORE_EMPTY) != 0) {
            return false;
        }

        jsonWriter.writeNameRaw(fieldNameUTF16(features));

        if (raw) {
            jsonWriter.writeRaw(value == null ? "null" : value);
        } else {
            jsonWriter.writeString(value);
        }
        return true;
    }
}
