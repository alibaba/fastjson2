package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.*;

class FieldWriterBool<T>
        extends FieldWriter<T> {
    final byte[] utf8ValueTrue;
    final byte[] utf8ValueFalse;
    final byte[] utf8Value1;
    final byte[] utf8Value0;
    final char[] utf16ValueTrue;
    final char[] utf16ValueFalse;
    final char[] utf16Value1;
    final char[] utf16Value0;

    FieldWriterBool(
            String name,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Object function
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, function);

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
    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
    }

    public boolean getFieldValueBoolean(T object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }
        return (Boolean) propertyAccessor.getObject(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Boolean value = (Boolean) propertyAccessor.getObject(object);
        if (value == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeBool(value);
    }

    @Override
    public final void writeBool(JSONWriter jsonWriter, boolean value) {
        long features = jsonWriter.getFeatures(this.features);
        if (!value && (features & Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return;
        }
        if ((features & Feature.WriteNonStringValueAsString.mask) != 0) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(value ? "true" : "false");
            return;
        }

        if (jsonWriter.utf8) {
            jsonWriter.writeNameRaw(
                    (features & Feature.WriteBooleanAsNumber.mask) != 0
                            ? (value ? utf8Value1 : utf8Value0)
                            : (value ? utf8ValueTrue : utf8ValueFalse)
            );
            return;
        }

        if (jsonWriter.utf16) {
            jsonWriter.writeNameRaw(
                    (features & Feature.WriteBooleanAsNumber.mask) != 0
                            ? (value ? utf16Value1 : utf16Value0)
                            : (value ? utf16ValueTrue : utf16ValueFalse)
            );
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeBool(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Boolean value;
        try {
            value = (Boolean) propertyAccessor.getObject(object);
        } catch (RuntimeException e) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw errorOnGet(e);
        }

        if (value == null) {
            if ((features & (MASK_WRITE_MAP_NULL_VALUE | MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_BOOLEAN_AS_FALSE)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            if ((features & MASK_WRITE_NULL_BOOLEAN_AS_FALSE) != 0) {
                jsonWriter.writeBool(false);
            } else {
                jsonWriter.writeBooleanNull();
            }
            return true;
        }

        writeBool(jsonWriter, value);
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
