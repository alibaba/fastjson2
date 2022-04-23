package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

public interface FieldReaderObject<T, V> extends FieldReader<T> {
    ObjectReader<V> getFieldObjectReader(JSONReader.Context context);

    @Override
    default void readFieldValue(JSONReader jsonReader, T object) {
        accept(object,
                getFieldObjectReader(jsonReader.getContext())
                        .readObject(jsonReader, 0));
    }
}
