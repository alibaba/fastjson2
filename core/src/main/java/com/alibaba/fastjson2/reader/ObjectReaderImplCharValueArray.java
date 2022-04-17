package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.util.Arrays;

final class ObjectReaderImplCharValueArray extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplCharValueArray INSTANCE = new ObjectReaderImplCharValueArray();

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.current() == '"') {
            String str = jsonReader.readString();
            return str.toCharArray();
        }

        if (jsonReader.nextIfMatch('[')) {
            char[] values = new char[16];
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

                if (jsonReader.isInt()) {
                    values[size++] = (char) jsonReader.readInt32Value();
                    continue;
                }
                String str = jsonReader.readString();
                values[size++] = (str == null) ? '\0' : str.charAt(0);
            }
            jsonReader.nextIfMatch(',');

            return Arrays.copyOf(values, size);
        }

        throw new JSONException("TODO");
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.isString()) {
            String str = jsonReader.readString();
            return str.toCharArray();
        }

        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        char[] array = new char[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            if (jsonReader.isInt()) {
                array[i] = (char) jsonReader.readInt32Value();
            } else {
                array[i] = jsonReader.readString().charAt(0);
            }
        }
        return array;
    }
}
