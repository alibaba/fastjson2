package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Function;

final class FieldWriterLocalDate<T>
        extends FieldWriterObjectFinal<T> {
    FieldWriterLocalDate(
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
    }

    @Override
    public Object getFieldValue(Object object) {
        return propertyAccessor.getObject(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        LocalDate localDate = (LocalDate) propertyAccessor.getObject(object);
        if (localDate == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        writeFieldName(jsonWriter);

        if (objectWriter == null) {
            objectWriter = getObjectWriter(jsonWriter, LocalDate.class);
        }

        if (objectWriter != ObjectWriterImplLocalDate.INSTANCE) {
            objectWriter.write(jsonWriter, localDate, fieldName, fieldClass, features);
        } else {
            jsonWriter.writeLocalDate(localDate);
        }
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        LocalDate localDate = (LocalDate) propertyAccessor.getObject(object);
        if (localDate == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldNameUTF8(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        writeFieldNameUTF8(jsonWriter);

        if (objectWriter == null) {
            objectWriter = getObjectWriter(jsonWriter, LocalDate.class);
        }

        if (objectWriter != ObjectWriterImplLocalDate.INSTANCE) {
            objectWriter.write(jsonWriter, localDate, fieldName, fieldClass, features);
        } else {
            jsonWriter.writeLocalDate(localDate);
        }
        return true;
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
