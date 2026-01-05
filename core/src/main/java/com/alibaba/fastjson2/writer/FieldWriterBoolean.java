package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

class FieldWriterBoolean
        extends FieldWriter {
    final byte[] utf8ValueTrue;
    final byte[] utf8ValueFalse;
    final byte[] utf8Value1;
    final byte[] utf8Value0;
    final char[] utf16ValueTrue;
    final char[] utf16ValueFalse;
    final char[] utf16Value1;
    final char[] utf16Value0;

    FieldWriterBoolean(
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
        {
            byte[] bytes = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + 4);
            bytes[nameWithColonUTF8.length] = 't';
            bytes[nameWithColonUTF8.length + 1] = 'r';
            bytes[nameWithColonUTF8.length + 2] = 'u';
            bytes[nameWithColonUTF8.length + 3] = 'e';
            utf8ValueTrue = bytes;
        }
        {
            byte[] bytes = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + 5);
            bytes[nameWithColonUTF8.length] = 'f';
            bytes[nameWithColonUTF8.length + 1] = 'a';
            bytes[nameWithColonUTF8.length + 2] = 'l';
            bytes[nameWithColonUTF8.length + 3] = 's';
            bytes[nameWithColonUTF8.length + 4] = 'e';
            utf8ValueFalse = bytes;
        }
        {
            byte[] bytes = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + 1);
            bytes[nameWithColonUTF8.length] = '1';
            utf8Value1 = bytes;
        }
        {
            byte[] bytes = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + 1);
            bytes[nameWithColonUTF8.length] = '0';
            utf8Value0 = bytes;
        }

        {
            char[] chars = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + 4);
            chars[nameWithColonUTF16.length] = 't';
            chars[nameWithColonUTF16.length + 1] = 'r';
            chars[nameWithColonUTF16.length + 2] = 'u';
            chars[nameWithColonUTF16.length + 3] = 'e';
            utf16ValueTrue = chars;
        }
        {
            char[] chars = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + 5);
            chars[nameWithColonUTF16.length] = 'f';
            chars[nameWithColonUTF16.length + 1] = 'a';
            chars[nameWithColonUTF16.length + 2] = 'l';
            chars[nameWithColonUTF16.length + 3] = 's';
            chars[nameWithColonUTF16.length + 4] = 'e';
            utf16ValueFalse = chars;
        }
        {
            char[] chars = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + 1);
            chars[nameWithColonUTF16.length] = '1';
            utf16Value1 = chars;
        }
        {
            char[] chars = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + 1);
            chars[nameWithColonUTF16.length] = '0';
            utf16Value0 = chars;
        }
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
    public final void writeBoolUTF8(JSONWriterUTF8 jsonWriter, boolean value) {
        long features = jsonWriter.getFeatures(this.features);
        if (!value && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return;
        }
        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
            writeFieldNameUTF8(jsonWriter);
            jsonWriter.writeString(value ? "true" : "false");
            return;
        }

        jsonWriter.writeNameRaw(
                (features & JSONWriter.Feature.WriteBooleanAsNumber.mask) != 0
                        ? (value ? utf8Value1 : utf8Value0)
                        : (value ? utf8ValueTrue : utf8ValueFalse)
        );
    }

    @Override
    public final void writeBoolUTF16(JSONWriterUTF16 jsonWriter, boolean value) {
        long features = jsonWriter.getFeatures(this.features);
        if (!value && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return;
        }
        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
            writeFieldNameUTF16(jsonWriter);
            jsonWriter.writeString(value ? "true" : "false");
            return;
        }

        jsonWriter.writeNameRaw(
                (features & JSONWriter.Feature.WriteBooleanAsNumber.mask) != 0
                        ? (value ? utf16Value1 : utf16Value0)
                        : (value ? utf16ValueTrue : utf16ValueFalse)
        );
    }

    @Override
    public final void writeBoolJSONB(JSONWriterJSONB jsonWriter, boolean value) {
        long features = jsonWriter.getFeatures(this.features);
        if (!value && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return;
        }
        if ((features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0) {
            writeFieldNameJSONB(jsonWriter);
            jsonWriter.writeString(value ? "true" : "false");
            return;
        }

        writeFieldNameJSONB(jsonWriter);
        jsonWriter.writeBool(value);
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, Object object) {
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
            writeFieldNameUTF8(jsonWriter);
            if ((features & JSONWriter.Feature.WriteNullBooleanAsFalse.mask) != 0) {
                jsonWriter.writeBool(false);
            } else {
                jsonWriter.writeBooleanNull();
            }
            return true;
        }

        if (fieldClass == boolean.class
                && !value
                && (jsonWriter.getFeatures(features) & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0
        ) {
            return false;
        }

        writeBoolUTF8(jsonWriter, value);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, Object object) {
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
            writeFieldNameUTF16(jsonWriter);
            if ((features & JSONWriter.Feature.WriteNullBooleanAsFalse.mask) != 0) {
                jsonWriter.writeBool(false);
            } else {
                jsonWriter.writeBooleanNull();
            }
            return true;
        }

        if (fieldClass == boolean.class
                && !value
                && (jsonWriter.getFeatures(features) & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0
        ) {
            return false;
        }

        writeBoolUTF16(jsonWriter, value);
        return true;
    }

    @Override
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, Object object) {
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
            writeFieldNameJSONB(jsonWriter);
            if ((features & JSONWriter.Feature.WriteNullBooleanAsFalse.mask) != 0) {
                jsonWriter.writeBool(false);
            } else {
                jsonWriter.writeBooleanNull();
            }
            return true;
        }

        if (fieldClass == boolean.class
                && !value
                && (jsonWriter.getFeatures(features) & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0
        ) {
            return false;
        }

        writeBoolJSONB(jsonWriter, value);
        return true;
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (valueClass == fieldClass) {
            return ObjectWriterImplBoolean.INSTANCE;
        }
        return jsonWriter.getObjectWriter(valueClass);
    }
}
