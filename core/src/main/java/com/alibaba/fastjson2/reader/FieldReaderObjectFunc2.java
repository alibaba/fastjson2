package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;

final class FieldReaderObjectFunc2<T, U> implements FieldReaderObject<T, U> {
    final ObjectReader<U> fieldObjectReader;
    final BiConsumer<T, U> function;
    final Type fieldType;
    final Class fieldClass;
    final String fieldName;

    public FieldReaderObjectFunc2(
            ObjectReader<U> fieldObjectReader
            , BiConsumer<T, U> function
            , Type fieldType
            , String fieldName) {

        this.fieldObjectReader = fieldObjectReader;
        this.function = function;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.fieldClass = TypeUtils.getMapping(fieldType);
    }

    @Override
    public ObjectReader getFieldObjectReader(JSONReader.Context context) {
        return fieldObjectReader;
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object, (U) value);
    }

    @Override
    public Type getFieldType() {
        return fieldType;
    }

    @Override
    public Class getFieldClass() {
        return fieldClass;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }
}
