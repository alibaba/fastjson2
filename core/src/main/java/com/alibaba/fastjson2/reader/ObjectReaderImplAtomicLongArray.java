package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongArray;

final class ObjectReaderImplAtomicLongArray extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplAtomicLongArray INSTANCE = new ObjectReaderImplAtomicLongArray();

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            List<Long> values = new ArrayList<>();
            for (; ; ) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }
                values.add(jsonReader.readInt64());
            }
            jsonReader.nextIfMatch(',');

            AtomicLongArray array = new AtomicLongArray(values.size());
            for (int i = 0; i < values.size(); i++) {
                Long value = values.get(i);
                if (value == null) {
                    continue;
                }
                array.set(i, value);
            }
            return array;
        }


        throw new JSONException("TODO");
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        AtomicLongArray array = new AtomicLongArray(entryCnt);
        for (int i = 0; i < entryCnt; i++) {
            Long value = jsonReader.readInt64();
            if (value == null) {
                continue;
            }
            array.set(i, value);
        }
        return array;
    }
}
