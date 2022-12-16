package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONWriter.Feature.UseSingleQuotes;
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

abstract class FieldWriterInt32<T>
        extends FieldWriter<T> {
    volatile byte[][] utf8ValueCache;
    volatile char[][] utf16ValueCache;
    final boolean toString;

    protected FieldWriterInt32(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, field, method);
        toString = (features & WriteNonStringValueAsString.mask) != 0
                || "string".equals(format);
    }

    @Override
    public void writeInt32(JSONWriter jsonWriter, int value) {
        if (toString) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(Integer.toString(value));
            return;
        }

        long jsonWriterFeatures = jsonWriter.getFeatures();
        boolean writeNonStringValueAsString = (jsonWriterFeatures & (WriteNonStringValueAsString.mask | UseSingleQuotes.mask)) != 0;

        if (jsonWriter.utf8 && !writeNonStringValueAsString) {
            if (value >= -1 && value < 1039) {
                byte[] bytes = null;
                if (utf8ValueCache == null) {
                    utf8ValueCache = new byte[1040][];
                } else {
                    bytes = utf8ValueCache[value + 1];
                }

                if (bytes == null) {
                    int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
                    byte[] original = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + size);
                    bytes = Arrays.copyOf(original, original.length);
                    IOUtils.getChars(value, bytes.length, bytes);
                    utf8ValueCache[value + 1] = bytes;
                }
                jsonWriter.writeNameRaw(bytes);
                return;
            }
        } else if (jsonWriter.utf16 && !writeNonStringValueAsString) {
            if (value >= -1 && value < 1039) {
                char[] chars = null;
                if (utf16ValueCache == null) {
                    utf16ValueCache = new char[1040][];
                } else {
                    chars = utf16ValueCache[value + 1];
                }

                if (chars == null) {
                    int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
                    char[] original = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + size);
                    chars = Arrays.copyOf(original, original.length);
                    IOUtils.getChars(value, chars.length, chars);
                    utf16ValueCache[value + 1] = chars;
                }
                jsonWriter.writeNameRaw(chars);
                return;
            }
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeInt32(value);
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
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullNumberAsZero.mask)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            jsonWriter.writeNumberNull();
            return true;
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
