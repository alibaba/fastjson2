package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderListFunc<T, V>
        extends FieldReaderImpl<T>
        implements FieldReaderList<T, Object> {
    final BiConsumer<T, V> function;
    final Type itemType;
    final Class itemClass;
    final long itemClassHash;
    private ObjectReader itemObjectReader;

    FieldReaderListFunc(
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            int ordinal,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            BiConsumer<T, V> function
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, 0, format, locale, defaultValue, schema, method, null);
        this.function = function;
        if (fieldType instanceof ParameterizedType) {
            itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
        } else {
            itemType = null;
        }
        this.itemClass = TypeUtils.getClass(itemType);
        this.itemClassHash = this.itemClass == null ? 0 : Fnv.hashCode64(itemClass.getName());
    }

    @Override
    public ObjectReader getItemObjectReader(JSONReader.Context ctx) {
        if (itemObjectReader != null) {
            return itemObjectReader;
        }
        return itemObjectReader = ctx.getObjectReader(itemType);
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, (V) value);
    }

    @Override
    public Type getItemType() {
        return itemType;
    }

    @Override
    public Class getItemClass() {
        return itemClass;
    }

    @Override
    public long getItemClassHash() {
        return itemClassHash;
    }
}
