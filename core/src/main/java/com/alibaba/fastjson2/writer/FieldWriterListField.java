package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

final class FieldWriterListField<T> extends FieldWriterList<T> {
    final Field field;

    protected FieldWriterListField(
            String fieldName
            , Type itemType
            , int ordinal
            , long features
            , String format
            , Type fieldType
            , Class fieldClass
            , Field field
    ) {
        super(fieldName, itemType, ordinal, features, format, fieldType, fieldClass);
        this.field = field;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + name, e);
        }
    }

//    public boolean writeJSONBTable(JSONWriter jsonWriter, List objects) {
//        if (itemObjectWriter == null) {
//            itemObjectWriter = jsonWriter.getObjectWriter(itemType, itemClass);
//        }
//
//        return itemObjectWriter.writeJSONBTable(jsonWriter, objects, getFieldName(), getFieldType(), getItemClass(), getFeatures());
//    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        List value = (List) getFieldValue(object);

        JSONWriter.Context context = jsonWriter.getContext();

        if (value == null) {
            long features = this.features | context.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeArrayNull();
                return true;
            } else {
                return false;
            }
        }

        String refPath = jsonWriter.setPath(name, value);
        if (refPath != null) {
            writeFieldName(jsonWriter);
            jsonWriter.writeReference(refPath);
            jsonWriter.popPath(value);
            return true;
        }

        writeList(jsonWriter, true, value);
        jsonWriter.popPath(value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        List value = (List) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        boolean refDetect = jsonWriter.isRefDetect();

        if (refDetect) {
            String refPath = jsonWriter.setPath(name, value);
            if (refPath != null) {
                jsonWriter.writeReference(refPath);
                jsonWriter.popPath(value);
                return;
            }
        }

        writeList(jsonWriter, false, value);

        if (refDetect) {
            jsonWriter.popPath(value);
        }
    }
}
