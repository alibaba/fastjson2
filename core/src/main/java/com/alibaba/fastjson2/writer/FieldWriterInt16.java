package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

abstract class FieldWriterInt16<T>
        extends FieldWriter<T> {
    byte[][] utf8ValueCache;
    char[][] utf16ValueCache;
    volatile byte[][] jsonbValueCache;

    FieldWriterInt16(
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

    protected void writeInt16(JSONWriter jsonWriter, short value) {
        boolean writeNonStringValueAsString = (jsonWriter.getFeatures() & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
        if (writeNonStringValueAsString) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(Short.toString(value));
            return;
        }

        if (jsonWriter.utf8) {
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
        } else if (jsonWriter.utf16) {
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
        } else if (jsonWriter.jsonb) {
            if (value >= -1 && value < 1039) {
                byte[] bytes = null;
                if (jsonbValueCache == null) {
                    jsonbValueCache = new byte[1040][];
                } else {
                    bytes = jsonbValueCache[value + 1];
                }

                if (bytes == null) {
                    if (nameJSONB == null) {
                        nameJSONB = JSONB.toBytes(fieldName);
                    }
                    byte[] valueBytes = JSONB.toBytes(value);

                    bytes = Arrays.copyOf(nameJSONB, nameJSONB.length + valueBytes.length);
                    System.arraycopy(valueBytes, 0, bytes, nameJSONB.length, valueBytes.length);
                    jsonbValueCache[value + 1] = bytes;
                }
                jsonWriter.writeRaw(bytes);
                return;
            }
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeInt32(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Short value;
        try {
            value = (Short) getFieldValue(object);
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

        writeInt16(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Short value = (Short) getFieldValue(object);
        if (value == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt32(value.shortValue());
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        return ObjectWriterImplInt16.INSTANCE;
    }
}
