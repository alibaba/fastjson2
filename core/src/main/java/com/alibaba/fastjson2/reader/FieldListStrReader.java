package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

interface FieldListStrReader<T> extends FieldReader<T> {

    @Override
    default Type getFieldType() {
        return List.class;
    }

    @Override
    default Class getFieldClass() {
        return List.class;
    }

    @Override
    default Type getItemType() {
        return String.class;
    }

    default List<String> createList() {
        return new ArrayList<>();
    }

    default ObjectReader<T> getItemConsumer(JSONReader.Context ctx) {
        return ctx.getObjectReader(
                getItemType());
    }

    @Override
    default void readFieldValue(JSONReader reader, T object) {
        if (reader.isJSONB()) {
            int entryCnt = reader.startArray();

            String[] array = new String[entryCnt];
            for (int i = 0; i < entryCnt; ++i) {
                array[i] = reader.readString();
            }
            List list = Arrays.asList(array);
            accept(object, list);
            return;
        }

        if (reader.current() == '[') {
            List list = createList();
            reader.startArray();
            for (; ; ) {
                if (reader.nextIfMatch(']')) {
                    break;
                }

                list.add(
                        reader.readString()
                );

                if (reader.nextIfMatch(',')) {

                }
            }
            accept(object, list);

            reader.nextIfMatch(',');
        }
    }
}
