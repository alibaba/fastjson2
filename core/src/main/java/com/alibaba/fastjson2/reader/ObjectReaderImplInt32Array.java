package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.util.Arrays;

final class ObjectReaderImplInt32Array extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplInt32Array INSTANCE = new ObjectReaderImplInt32Array();

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            Integer[] values = new Integer[16];
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

                values[size++] = jsonReader.readInt32();
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
        Integer[] array = new Integer[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = jsonReader.readInt32();
        }
        return array;
    }
}
