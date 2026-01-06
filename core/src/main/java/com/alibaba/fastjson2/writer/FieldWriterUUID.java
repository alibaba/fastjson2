package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_MAP_NULL_VALUE;

final class FieldWriterUUID<T>
        extends FieldWriterObjectFinal<T> {
    FieldWriterUUID(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Function function
    ) {
        super(name, ordinal, features, format, null, label, fieldType, fieldClass, field, method, function);
    }

    @Override
    public Object getFieldValue(Object object) {
        return propertyAccessor.getObject(object);
    }

    @Override
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, T object) {
        UUID uuid = (UUID) propertyAccessor.getObject(object);
        if (uuid == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldNameJSONB(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }
        writeFieldNameJSONB(jsonWriter);
        jsonWriter.writeUUID(uuid);
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        UUID uuid = (UUID) propertyAccessor.getObject(object);
        long features = this.features | jsonWriter.getFeatures();
        if (uuid == null) {
            if ((features & MASK_WRITE_MAP_NULL_VALUE) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }
        jsonWriter.writeUUID(fieldNameUTF8(jsonWriter.getFeatures(features)), uuid);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, T object) {
        UUID uuid = (UUID) propertyAccessor.getObject(object);
        if (uuid == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldNameUTF16(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }
        writeFieldNameUTF16(jsonWriter);
        jsonWriter.writeUUID(uuid);
        return true;
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
