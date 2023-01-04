package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;

final class FieldReaderObjectFunc2<T, U>
        extends FieldReader<T> {
    final ObjectReader<U> fieldObjectReader;
    final BiConsumer<T, U> function;

    public FieldReaderObjectFunc2(
            ObjectReader<U> fieldObjectReader,
            BiConsumer<T, U> function,
            Type fieldType,
            String fieldName) {
        super(fieldName, fieldType);
        this.fieldObjectReader = fieldObjectReader;
        this.function = function;
    }

    public ObjectReader getFieldObjectReader(JSONReader.Context context) {
        return fieldObjectReader;
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object, (U) value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        throw new JSONException("UnsupportedOperationException");
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.read(fieldType);
    }
}
