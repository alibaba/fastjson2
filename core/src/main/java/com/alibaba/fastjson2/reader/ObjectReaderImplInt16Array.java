package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

class ObjectReaderImplInt16Array
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplInt16Array INSTANCE = new ObjectReaderImplInt16Array();

    ObjectReaderImplInt16Array() {
        super(Short[].class);
    }

    @Override
    public Object createInstance(Collection collection) {
        Short[] array = new Short[collection.size()];
        int i = 0;
        for (Object item : collection) {
            Short value;
            if (item == null) {
                value = null;
            } else if (item instanceof Number) {
                value = ((Number) item).shortValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), Short.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to Short " + item.getClass());
                }
                value = (Short) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        return array;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            Short[] values = new Short[16];
            int size = 0;
            for (; ; ) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                int minCapacity = size + 1;
                if (minCapacity - values.length > 0) {
                    int oldCapacity = values.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }

                    values = Arrays.copyOf(values, newCapacity);
                }

                Integer i = jsonReader.readInt32();
                values[size++] = i == null ? 0 : i.shortValue();
            }
            jsonReader.nextIfMatch(',');

            return Arrays.copyOf(values, size);
        }

        if (jsonReader.isString()) {
            String str = jsonReader.readString();
            if (str.isEmpty()) {
                return null;
            }

            throw new JSONException(jsonReader.info("not support input " + str));
        }

        throw new JSONException(jsonReader.info("TODO"));
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        Short[] array = new Short[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            Integer integer = jsonReader.readInt32();
            array[i] = integer == null ? null : integer.shortValue();
        }
        return array;
    }
}
