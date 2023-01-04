package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjBoolConsumer;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;

final class FieldReaderBoolValFunc<T>
        extends FieldReader<T> {
    final ObjBoolConsumer<T> function;

    public FieldReaderBoolValFunc(String fieldName, int ordinal, JSONSchema schema, Method method, ObjBoolConsumer<T> function) {
        super(fieldName, boolean.class, boolean.class, ordinal, 0, null, null, null, schema, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Object value) {
        boolean booleanValue = TypeUtils.toBooleanValue(value);
        function.accept(object, booleanValue);
    }

    @Override
    public void accept(T object, boolean value) {
        function.accept(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        function.accept(object,
                jsonReader.readBoolValue());
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }
}
