package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

class FieldReaderObjectParam<T>
        extends FieldReaderImpl<T>
        implements FieldReaderObject<T, Object> {
    final Parameter parameter;
    final String paramName;
    final long paramNameHash;
    ObjectReader fieldObjectReader;

    FieldReaderObjectParam(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            String paramName,
            Parameter parameter,
            int ordinal,
            long features,
            String format,
            JSONSchema schema) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, null, schema, null, null);
        this.paramName = paramName;
        this.paramNameHash = Fnv.hashCode64(paramName);
        this.parameter = parameter;
    }

    @Override
    public ObjectReader getInitReader() {
        return fieldObjectReader;
    }

    @Override
    public ObjectReader<Object> getFieldObjectReader(JSONReader.Context context) {
        if (fieldObjectReader == null) {
            fieldObjectReader = context
                    .getObjectReader(fieldType);
        }
        return fieldObjectReader;
    }

    @Override
    public long getFieldNameHash() {
        return paramNameHash;
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        if (fieldObjectReader == null) {
            fieldObjectReader = jsonReader
                    .getContext()
                    .getObjectReader(fieldType);
        }
        return jsonReader.isJSONB()
                ? fieldObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features)
                : fieldObjectReader.readObject(jsonReader, fieldType, fieldName, features);
    }
}
