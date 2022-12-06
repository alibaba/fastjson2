package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;

final class FieldWriterListFunc<T>
        extends FieldWriterList<T> {
    final Function<T, List> function;

    FieldWriterListFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Type itemType,
            Method method,
            Function<T, List> function,
            Type fieldType,
            Class fieldClass
    ) {
        super(fieldName, itemType, ordinal, features, format, label, fieldType, fieldClass, null, method);
        this.function = function;
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
            if ((features & (WriteNulls.mask | NullAsDefaultValue.mask | WriteNullListAsEmpty.mask)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            jsonWriter.writeArrayNull();
            return true;
        }

        if ((features & NotWriteEmptyArray.mask) != 0 && list.isEmpty()) {
            return false;
        }

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        writeFieldName(jsonWriter);

        if (jsonWriter.jsonb) {
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

                itemObjectWriter.writeJSONB(jsonWriter, item, null, itemType, 0);
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

        if (jsonWriter.jsonb) {
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
