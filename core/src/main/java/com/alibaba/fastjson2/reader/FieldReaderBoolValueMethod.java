package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;

final class FieldReaderBoolValueMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderBoolValueMethod(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Boolean defaultValue,
            JSONSchema schema,
            Method method
    ) {
        super(fieldName, boolean.class, boolean.class, ordinal, features, format, null, defaultValue, schema, method, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        boolean fieldValue = jsonReader.readBoolValue();
        propertyAccessor.setBoolean(object, fieldValue);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        boolean fieldValue = jsonReader.readBoolValue();
        propertyAccessor.setBoolean(object, fieldValue);
    }

    @Override
    public void accept(T object, Object value) {
        boolean booleanValue = TypeUtils.toBooleanValue(value);
        propertyAccessor.setBoolean(object, booleanValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }
}
