package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

final class ObjectReaderImplInt64Array extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplInt64Array INSTANCE = new ObjectReaderImplInt64Array();

    @Override
    public Class getObjectClass() {
        return Long[].class;
    }

    public Object createInstance(Collection collection) {
        Long[] array = new Long[collection.size()];
        int i = 0;
        for (Object item : collection) {
            Long value;
            if (item == null) {
                value = null;
            } else if (item instanceof Number) {
                value = ((Number) item).longValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), Long.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to Integer " + item.getClass());
                }
                value = (Long) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        return array;
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            Long[] values = new Long[16];
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

                values[size++] = jsonReader.readInt64();
            }
            jsonReader.nextIfMatch(',');

            return Arrays.copyOf(values, size);
        }

        throw new JSONException("TODO");
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        Long[] array = new Long[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = jsonReader.readInt64();
        }
        return array;
    }
}
