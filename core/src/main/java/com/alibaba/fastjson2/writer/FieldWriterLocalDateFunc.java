package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.function.Function;

final class FieldWriterLocalDateFunc<T>
        extends FieldWriterObjectFinal<T> {
    final Function function;

    FieldWriterLocalDateFunc(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Method method,
            Function function
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.apply(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        LocalDate localDate = (LocalDate) function.apply(object);
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
}
