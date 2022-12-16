package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

abstract class FieldWriterInt8<T>
        extends FieldWriter<T> {
    final byte[][] utf8ValueCache = new byte[256][];
    final char[][] utf16ValueCache = new char[256][];

    FieldWriterInt8(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Class fieldClass,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, label, fieldClass, fieldClass, field, method);
    }

    protected void writeInt8(JSONWriter jsonWriter, byte value) {
        boolean writeNonStringValueAsString = (jsonWriter.getFeatures() & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
        if (writeNonStringValueAsString) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(Byte.toString(value));
            return;
        }

        if (jsonWriter.utf8) {
            byte[] bytes = utf8ValueCache[value + 128];
            if (bytes == null) {
                int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
                byte[] original = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + size);
                bytes = Arrays.copyOf(original, original.length);
                IOUtils.getChars(value, bytes.length, bytes);
                utf8ValueCache[value + 128] = bytes;
            }
            jsonWriter.writeNameRaw(bytes);
            return;
        }
        if (jsonWriter.utf16) {
            char[] bytes = utf16ValueCache[value + 128];
            if (bytes == null) {
                int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
                char[] original = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + size);
                bytes = Arrays.copyOf(original, original.length);
                IOUtils.getChars(value, bytes.length, bytes);
                utf16ValueCache[value + 128] = bytes;
            }
            jsonWriter.writeNameRaw(bytes);
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeInt32(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Byte value;
        try {
            value = (Byte) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            jsonWriter.writeNumberNull();
            return true;
        }

        writeInt8(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Byte value = (Byte) getFieldValue(object);
        if (value == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt32(value.byteValue());
    }
}
