package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_MAP_NULL_VALUE;

final class FieldWriterLocalDate<T>
        extends FieldWriterObjectFinal<T> {
    final NameValueWriter<JSONWriterUTF8, LocalDate> nameValueUTF8;
    final NameValueWriter<JSONWriterUTF16, LocalDate> nameValueUTF16;

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
        objectWriter = ObjectWriterImplLocalDate.of(format, locale);
        nameValueUTF8 = (jsonWriter, value, features2) -> {
            jsonWriter.writeNameRaw(fieldNameUTF8(features));
            objectWriter.write(jsonWriter, value, name, fieldType, features2);
        };
        nameValueUTF16 = (jsonWriter, value, features2) -> {
            jsonWriter.writeNameRaw(fieldNameUTF16(features));
            objectWriter.write(jsonWriter, value, name, fieldType, features2);
        };
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
        jsonWriter.writeLocalDate(localDate);
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        LocalDate localDate = (LocalDate) propertyAccessor.getObject(object);
        if (localDate == null) {
            if ((features & MASK_WRITE_MAP_NULL_VALUE) != 0) {
                jsonWriter.writeNameRaw(nameNullUTF8);
                return true;
            } else {
                return false;
            }
        }

        nameValueUTF8.write(jsonWriter, localDate, features);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        LocalDate localDate = (LocalDate) propertyAccessor.getObject(object);
        if (localDate == null) {
            if ((features & MASK_WRITE_MAP_NULL_VALUE) != 0) {
                jsonWriter.writeNameRaw(nameNullUTF16);
                return true;
            } else {
                return false;
            }
        }

        nameValueUTF16.write(jsonWriter, localDate, features);
        return true;
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
