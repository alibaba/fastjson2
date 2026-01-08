package com.alibaba.fastjson2.writer;

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
    final NameValueWriter<JSONWriterUTF8, OffsetDateTime> nameValueUTF8;
    final NameValueWriter<JSONWriterUTF16, OffsetDateTime> nameValueUTF16;
    final NameValueWriter<JSONWriterJSONB, OffsetDateTime> nameValueJSONB;

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
        this.objectWriter = ObjectWriterImplOffsetDateTime.of(format, locale);

        if (objectWriter != ObjectWriterImplOffsetDateTime.INSTANCE) {
            nameValueUTF8 = (jsonWriter, value, features2) -> {
                jsonWriter.writeNameRaw(fieldNameUTF8(features));
                objectWriter.write(jsonWriter, value, name, fieldType, features2);
            };
            nameValueUTF16 = (jsonWriter, value, features2) -> {
                jsonWriter.writeNameRaw(fieldNameUTF16(features));
                objectWriter.write(jsonWriter, value, name, fieldType, features2);
            };
            nameValueJSONB = (jsonWriter, value, features2) -> {
                writeFieldNameJSONB(jsonWriter);
                objectWriter.write(jsonWriter, value, name, fieldType, features2);
            };
        } else {
            nameValueUTF8 = (jsonWriter, value, features2) -> {
                jsonWriter.writeOffsetDateTime(fieldNameUTF8(features), value);
            };
            nameValueUTF16 = (jsonWriter, value, features2) -> {
                jsonWriter.writeNameRaw(fieldNameUTF16(features));
                jsonWriter.writeOffsetDateTime(value);
            };
            nameValueJSONB = (jsonWriter, value, features2) -> {
                writeFieldNameJSONB(jsonWriter);
                jsonWriter.writeOffsetDateTime(value);
            };
        }
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

        nameValueJSONB.write(jsonWriter, dateTime, features);
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

        nameValueUTF16.write(jsonWriter, dateTime, features);
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        OffsetDateTime dateTime = (OffsetDateTime) propertyAccessor.getObject(object);
        if (dateTime == null) {
            if ((features & MASK_WRITE_MAP_NULL_VALUE) != 0) {
                jsonWriter.writeNameRaw(nameNullUTF8);
                return true;
            } else {
                return false;
            }
        }

        nameValueUTF8.write(jsonWriter, dateTime, features);
        return true;
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
