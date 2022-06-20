package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

final class ObjectReaderImplFloatArray
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplFloatArray INSTANCE = new ObjectReaderImplFloatArray();

    @Override
    public Class getObjectClass() {
        return Float[].class;
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            Float[] values = new Float[16];
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

                values[size++] = jsonReader.readFloat();
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
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        Float[] array = new Float[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = jsonReader.readFloat();
        }
        return array;
    }

    @Override
    public Object createInstance(Collection collection) {
        Float[] array = new Float[collection.size()];
        int i = 0;
        for (Object item : collection) {
            Float value;
            if (item == null) {
                value = null;
            } else if (item instanceof Number) {
                value = ((Number) item).floatValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), Float.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to Float " + item.getClass());
                }
                value = (Float) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        return array;
    }
}
