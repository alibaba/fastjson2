package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.util.Arrays;

final class ObjectReaderImplInt64ValueArray extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplInt64ValueArray INSTANCE = new ObjectReaderImplInt64ValueArray();

    static final long HASH_TYPE = Fnv.hashCode64("[J");

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, features);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            long[] values = new long[16];
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

                values[size++] = jsonReader.readInt64Value();
            }
            jsonReader.nextIfMatch(',');

            return Arrays.copyOf(values, size);
        }


        throw new JSONException("TODO");
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            if (typeHash != HASH_TYPE) {
                throw new JSONException("not support " + jsonReader.getString());
            }
        }

        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        long[] array = new long[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = jsonReader.readInt64Value();
        }
        return array;
    }
}
