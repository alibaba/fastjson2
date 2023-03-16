package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

abstract class FieldWriterBoolean
        extends FieldWriter {
    volatile byte[] utf8ValueTrue;
    volatile byte[] utf8ValueFalse;
    volatile byte[] utf8Value1;
    volatile byte[] utf8Value0;
    volatile char[] utf16ValueTrue;
    volatile char[] utf16ValueFalse;
    volatile char[] utf16Value1;
    volatile char[] utf16Value0;

    FieldWriterBoolean(
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
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        Boolean value = (Boolean) getFieldValue(object);
        if (value == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeBool(value);
    }

    @Override
    public void writeBool(JSONWriter jsonWriter, boolean value) {
        long features = jsonWriter.getFeatures() | this.features;
        boolean writeNonStringValueAsString = (features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
        if (writeNonStringValueAsString) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(value ? "true" : "false");
            return;
        }

        if (jsonWriter.utf8) {
            byte[] bytes;
            if (value) {
                if ((features & JSONWriter.Feature.WriteBooleanAsNumber.mask) != 0) {
                    if (utf8Value1 == null) {
                        bytes = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + 1);
                        bytes[nameWithColonUTF8.length] = '1';
                        utf8Value1 = bytes;
                    } else {
                        bytes = utf8Value1;
                    }
                } else {
                    if (utf8ValueTrue == null) {
                        bytes = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + 4);
                        bytes[nameWithColonUTF8.length] = 't';
                        bytes[nameWithColonUTF8.length + 1] = 'r';
                        bytes[nameWithColonUTF8.length + 2] = 'u';
                        bytes[nameWithColonUTF8.length + 3] = 'e';
                        utf8ValueTrue = bytes;
                    } else {
                        bytes = utf8ValueTrue;
                    }
                }
            } else {
                if ((features & JSONWriter.Feature.WriteBooleanAsNumber.mask) != 0) {
                    if (utf8Value0 == null) {
                        bytes = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + 1);
                        bytes[nameWithColonUTF8.length] = '0';
                        utf8Value0 = bytes;
                    } else {
                        bytes = utf8Value0;
                    }
                } else {
                    if (utf8ValueFalse == null) {
                        bytes = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + 5);
                        bytes[nameWithColonUTF8.length] = 'f';
                        bytes[nameWithColonUTF8.length + 1] = 'a';
                        bytes[nameWithColonUTF8.length + 2] = 'l';
                        bytes[nameWithColonUTF8.length + 3] = 's';
                        bytes[nameWithColonUTF8.length + 4] = 'e';
                        utf8ValueFalse = bytes;
                    } else {
                        bytes = utf8ValueFalse;
                    }
                }
            }
            jsonWriter.writeNameRaw(bytes);
            return;
        }

        if (jsonWriter.utf16) {
            char[] chars;
            if (value) {
                if ((features & JSONWriter.Feature.WriteBooleanAsNumber.mask) != 0) {
                    if (utf16Value1 == null) {
                        chars = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + 1);
                        chars[nameWithColonUTF16.length] = '1';
                        utf16Value1 = chars;
                    } else {
                        chars = utf16Value1;
                    }
                } else {
                    if (utf16ValueTrue == null) {
                        chars = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + 4);
                        chars[nameWithColonUTF16.length] = 't';
                        chars[nameWithColonUTF16.length + 1] = 'r';
                        chars[nameWithColonUTF16.length + 2] = 'u';
                        chars[nameWithColonUTF16.length + 3] = 'e';
                        utf16ValueTrue = chars;
                    } else {
                        chars = utf16ValueTrue;
                    }
                }
            } else {
                if ((features & JSONWriter.Feature.WriteBooleanAsNumber.mask) != 0) {
                    if (utf16Value0 == null) {
                        chars = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + 1);
                        chars[nameWithColonUTF16.length] = '0';
                        utf16Value0 = chars;
                    } else {
                        chars = utf16Value0;
                    }
                } else {
                    if (utf16ValueFalse == null) {
                        chars = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + 5);
                        chars[nameWithColonUTF16.length] = 'f';
                        chars[nameWithColonUTF16.length + 1] = 'a';
                        chars[nameWithColonUTF16.length + 2] = 'l';
                        chars[nameWithColonUTF16.length + 3] = 's';
                        chars[nameWithColonUTF16.length + 4] = 'e';
                        utf16ValueFalse = chars;
                    } else {
                        chars = utf16ValueFalse;
                    }
                }
            }
            jsonWriter.writeNameRaw(chars);
            return;
        }
        writeFieldName(jsonWriter);
        jsonWriter.writeBool(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        Boolean value;
        try {
            value = (Boolean) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullBooleanAsFalse.mask)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            jsonWriter.writeBooleanNull();
            return true;
        }

        writeBool(jsonWriter, value);
        return true;
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        return ObjectWriterImplBoolean.INSTANCE;
    }
}
