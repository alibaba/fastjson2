package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

class ObjectReaderImplFloatValueArray
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplFloatValueArray INSTANCE = new ObjectReaderImplFloatValueArray();

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            float[] values = new float[16];
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

                values[size++] = jsonReader.readFloatValue();
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
        float[] array = new float[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = jsonReader.readFloatValue();
        }
        return array;
    }

    @Override
    public Object createInstance(Collection collection) {
        float[] array = new float[collection.size()];
        int i = 0;
        for (Object item : collection) {
            float value;
            if (item == null) {
                value = 0;
            } else if (item instanceof Number) {
                value = ((Number) item).floatValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), float.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to float " + item.getClass());
                }
                value = (Float) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        return array;
    }
}
