package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

class FieldReaderListFuncImpl<T, V>
        implements FieldReaderList<T, V> {

    final Supplier<List<V>> listCreator;
    final ObjectReader<V> itemObjectReader;
    final BiConsumer<T, List<V>> function;
    final Type itemType;
    final String fieldName;

    public FieldReaderListFuncImpl(
            Supplier<List<V>> listCreator
            , ObjectReader<V> itemObjectReader
            , BiConsumer<T, List<V>> function
            , Type itemType
            , String fieldName) {

        this.listCreator = listCreator;
        this.itemObjectReader = itemObjectReader;
        this.function = function;
        this.itemType = itemType;
        this.fieldName = fieldName;
    }

    @Override
    public List<V> createList() {
        return listCreator.get();
    }

    @Override
    public ObjectReader<V> getItemObjectReader(JSONReader.Context ctx) {
        return itemObjectReader;
    }

    @Override
    public void accept(T object, Object list) {
        function.accept(object, (List) list);
    }

    @Override
    public Type getItemType() {
        return itemType;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }
}
