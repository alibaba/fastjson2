package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

final class FieldWriterListFunc<T> extends FieldWriterList<T> {
    final Method method;
    final Function<T, List> function;

    FieldWriterListFunc(
            String fieldName
            , int ordinal
            , long features
            , String format
            , Type itemType
            , Method method
            , Function<T, List> function
            , Type fieldType
            , Class fieldClass
    ) {
        super(fieldName, itemType, ordinal, features, format, fieldType, fieldClass);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

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
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            jsonWriter.writeArrayNull();
            return true;
        }

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        writeFieldName(jsonWriter);

        if (jsonWriter.isJSONB()) {
            int size = list.size();
            jsonWriter.startArray(size);

            for (int i = 0; i < size; i++) {
                Object item = list.get(i);
                if (item == null) {
                    jsonWriter.writeNull();
                    continue;
                }
                Class<?> itemClass = item.getClass();
                ObjectWriter itemObjectWriter;
                if (itemClass == previousClass) {
                    itemObjectWriter = previousObjectWriter;
                } else {
                    itemObjectWriter = getItemWriter(jsonWriter, itemClass);
                    previousClass = itemClass;
                    previousObjectWriter = itemObjectWriter;
                }

                itemObjectWriter.write(jsonWriter, item);
            }
            return true;
        }

        jsonWriter.startArray();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                itemObjectWriter = getItemWriter(jsonWriter, itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
            }

            itemObjectWriter.write(jsonWriter, item);
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

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        if (jsonWriter.isJSONB()) {
            int size = list.size();
            jsonWriter.startArray(size);

            for (int i = 0; i < size; i++) {
                Object item = list.get(i);
                if (item == null) {
                    jsonWriter.writeNull();
                    continue;
                }
                Class<?> itemClass = item.getClass();
                ObjectWriter itemObjectWriter;
                if (itemClass == previousClass) {
                    itemObjectWriter = previousObjectWriter;
                } else {
                    itemObjectWriter = getItemWriter(jsonWriter, itemClass);
                    previousClass = itemClass;
                    previousObjectWriter = itemObjectWriter;
                }

                itemObjectWriter.write(jsonWriter, item);
            }
            return;
        }

        jsonWriter.startArray();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                itemObjectWriter = getItemWriter(jsonWriter, itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
            }

            itemObjectWriter.write(jsonWriter, item);
        }
        jsonWriter.endArray();
    }
}
