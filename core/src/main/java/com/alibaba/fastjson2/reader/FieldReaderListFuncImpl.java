package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

class FieldReaderListFuncImpl<T, V>
        extends FieldReaderList<T, V> {
    final Supplier<List<V>> listCreator;
    final ObjectReader<V> itemObjectReader;

    public FieldReaderListFuncImpl(
            Supplier<List<V>> listCreator,
            ObjectReader<V> itemObjectReader,
            BiConsumer<T, List<V>> function,
            Type itemType, String fieldName) {
        super(fieldName, List.class, List.class, itemType, TypeUtils.getClass(itemType), 0, 0, null, null, null, null, null, null, function);
        this.listCreator = listCreator;
        this.itemObjectReader = itemObjectReader;
    }

    @Override
    public Collection<V> createList(JSONReader.Context context) {
        return listCreator.get();
    }

    @Override
    public void accept(T object, Object list) {
        function.accept(object, list);
    }
}
