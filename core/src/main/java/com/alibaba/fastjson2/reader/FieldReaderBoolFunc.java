package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderBoolFunc<T, V>
        extends FieldReader<T> {
    final BiConsumer<T, V> function;

    FieldReaderBoolFunc(String fieldName, Class<V> fieldClass, int ordinal, String format, Locale locale, Object defaultValue, JSONSchema schema, Method method, BiConsumer<T, V> function) {
        super(fieldName, fieldClass, fieldClass, ordinal, 0, format, locale, defaultValue, schema, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Object value) {
        Boolean booleanValue = TypeUtils.toBoolean(value);
        function.accept(object, (V) booleanValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Boolean fieldValue = jsonReader.readBool();
        function.accept(object, (V) fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }
}
