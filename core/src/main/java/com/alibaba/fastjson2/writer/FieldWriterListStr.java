package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_ERROR_GETTER;
import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_DEFAULT_VALUE;
import static com.alibaba.fastjson2.util.TypeUtils.toList;

public final class FieldWriterListStr<T>
        extends FieldWriter<T> {
    public FieldWriterListStr(
            String fieldName,
            Type itemType,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Function<T, List<String>> function,
            Class<?> contentAs
    ) {
        super(fieldName, ordinal, features, format, null, label, fieldType, fieldClass, field, method, function);
    }

    @Override
    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        List<String> list;
        try {
            list = toList(propertyAccessor.getObject(object));
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (list == null) {
            if ((features & (WriteNulls.mask | NullAsDefaultValue.mask | WriteNullListAsEmpty.mask)) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeArrayNull(features);
                return true;
            } else {
                return false;
            }
        }

        if ((features & NotWriteEmptyArray.mask) != 0 && list.isEmpty()) {
            return false;
        }

        writeFieldName(jsonWriter);

        if (jsonWriter.jsonb) {
            int size = list.size();
            jsonWriter.startArray(size);

            for (int i = 0; i < size; i++) {
                String item = list.get(i);
                if (item == null) {
                    jsonWriter.writeNull();
                    continue;
                }
                jsonWriter.writeString(item);
            }
            return true;
        }

        jsonWriter.startArray();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            String item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            jsonWriter.writeString(item);
        }
        jsonWriter.endArray();

        return true;
    }

    private static final long MASK_WRITE_NULL = WriteNulls.mask | NullAsDefaultValue.mask | WriteNullStringAsEmpty.mask;

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        long features = jsonWriter.getFeatures(this.features);
        List<String> list;
        try {
            list = toList(propertyAccessor.getObject(object));
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (list == null) {
            if ((features & MASK_WRITE_NULL) != 0) {
                writeFieldNameUTF8(jsonWriter);
                jsonWriter.writeArrayNull(features);
                return true;
            } else {
                return false;
            }
        }

        if ((features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && list.isEmpty()) {
            return false;
        }

        jsonWriter.writeNameRaw(fieldNameUTF8(features));

        jsonWriter.startArray();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            String item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            jsonWriter.writeString(item);
        }
        jsonWriter.endArray();

        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        List<String> list = toList(propertyAccessor.getObject(object));
        if (list == null) {
            jsonWriter.writeNull();
            return;
        }

        if (jsonWriter.jsonb) {
            int size = list.size();
            jsonWriter.startArray(size);

            for (int i = 0; i < size; i++) {
                String item = list.get(i);
                if (item == null) {
                    jsonWriter.writeNull();
                    continue;
                }
                jsonWriter.writeString(item);
            }
            return;
        }

        jsonWriter.startArray();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            String item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            jsonWriter.writeString(item);
        }
        jsonWriter.endArray();
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
