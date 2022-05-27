package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.IOUtils;

import java.util.Arrays;

abstract class FieldWriterInt64<T>
        extends FieldWriterImpl<T> {
    volatile byte[][] utf8ValueCache;
    volatile char[][] utf16ValueCache;
    final boolean browserCompatible;

    FieldWriterInt64(String name, int ordinal, long features, String format, String label, Class fieldClass) {
        super(name, ordinal, features, format, label, fieldClass, fieldClass);
        browserCompatible = (features & JSONWriter.Feature.BrowserCompatible.mask) != 0;
    }

    @Override
    public void writeInt64(JSONWriter jsonWriter, long value) {
        boolean writeNonStringValueAsString = (jsonWriter.getFeatures() & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;

        if (jsonWriter.isUTF8() && !writeNonStringValueAsString) {
            if (value >= -1 && value < 1039) {
                byte[] bytes = null;
                if (utf8ValueCache == null) {
                    utf8ValueCache = new byte[1040][];
                } else {
                    bytes = utf8ValueCache[(int) value + 1];
                }

                if (bytes == null) {
                    int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
                    byte[] original = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + size);
                    bytes = Arrays.copyOf(original, original.length);
                    IOUtils.getChars(value, bytes.length, bytes);
                    utf8ValueCache[(int) value + 1] = bytes;
                }
                jsonWriter.writeNameRaw(bytes);
                return;
            }
        } else if (jsonWriter.isUTF16() && !writeNonStringValueAsString) {
            if (value >= -1 && value < 1039) {
                char[] chars = null;
                if (utf16ValueCache == null) {
                    utf16ValueCache = new char[1040][];
                } else {
                    chars = utf16ValueCache[(int) value + 1];
                }

                if (chars == null) {
                    int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
                    char[] original = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + size);
                    chars = Arrays.copyOf(original, original.length);
                    IOUtils.getChars(value, chars.length, chars);
                    utf16ValueCache[(int) value + 1] = chars;
                }
                jsonWriter.writeNameRaw(chars);
                return;
            }
        }

        writeFieldName(jsonWriter);
        if (browserCompatible
                && !jsonWriter.isJSONB()
                && (value > 9007199254740991L || value < -9007199254740991L)) {
            jsonWriter.writeString(Long.toString(value));
        } else {
            jsonWriter.writeInt64(value);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Long value;
        try {
            value = (Long) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            jsonWriter.writeNumberNull();
            return true;
        }

        writeInt64(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Long value = (Long) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeInt64(value);
    }
}
