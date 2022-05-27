package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

final class FieldWriterListStrFunc<T>
        extends FieldWriterImpl<T> {
    final Method method;
    final Function<T, List> function;

    protected FieldWriterListStrFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Method method,
            Function<T, List> function,
            Type fieldType,
            Class fieldClass
    ) {
        super(fieldName, ordinal, features, format, null, fieldType, fieldClass);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        List list;
        try {
            list = function.apply(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (list == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeArrayNull();
                return true;
            } else {
                return false;
            }
        }

        writeFieldName(jsonWriter);

        if (jsonWriter.isJSONB()) {
            int size = list.size();
            jsonWriter.startArray(size);

            for (int i = 0; i < size; i++) {
                String item = (String) list.get(i);
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

            String item = (String) list.get(i);
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
        List list = function.apply(object);
        if (list == null) {
            jsonWriter.writeNull();
            return;
        }

        if (jsonWriter.isJSONB()) {
            int size = list.size();
            jsonWriter.startArray(size);

            for (int i = 0; i < size; i++) {
                String item = (String) list.get(i);
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

            String item = (String) list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            jsonWriter.writeString(item);
        }
        jsonWriter.endArray();
    }
}
