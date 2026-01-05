package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_MAP_NULL_VALUE;

final class FieldWriterOffsetDateTime<T>
        extends FieldWriterObjectFinal<T> {
    FieldWriterOffsetDateTime(
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
            Function function
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, function);
        this.initObjectWriter = ObjectWriterImplOffsetDateTime.INSTANCE;
    }

    @Override
    public Object getFieldValue(Object object) {
        return propertyAccessor.getObject(object);
    }

    @Override
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        OffsetDateTime dateTime = (OffsetDateTime) function.apply(object);
        if (dateTime == null) {
            if ((features & MASK_WRITE_MAP_NULL_VALUE) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        writeFieldNameJSONB(jsonWriter);
        jsonWriter.writeOffsetDateTime(dateTime);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        OffsetDateTime dateTime = (OffsetDateTime) propertyAccessor.getObject(object);
        if (dateTime == null) {
            if ((features & MASK_WRITE_MAP_NULL_VALUE) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        jsonWriter.writeNameRaw(fieldNameUTF16(features));
        jsonWriter.writeOffsetDateTime(dateTime);
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        OffsetDateTime dateTime = (OffsetDateTime) propertyAccessor.getObject(object);
        if (dateTime == null) {
            if ((features & MASK_WRITE_MAP_NULL_VALUE) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        jsonWriter.writeNameRaw(fieldNameUTF8(jsonWriter.getFeatures(features)));
        jsonWriter.writeOffsetDateTime(dateTime);
        return true;
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
