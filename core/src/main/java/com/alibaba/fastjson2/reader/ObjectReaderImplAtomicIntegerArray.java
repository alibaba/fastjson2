package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

class ObjectReaderImplAtomicIntegerArray
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplAtomicIntegerArray INSTANCE = new ObjectReaderImplAtomicIntegerArray();

    @Override
    public Class getObjectClass() {
        return AtomicIntegerArray.class;
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            List<Integer> values = new ArrayList<>();
            for (; ; ) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }
                values.add(jsonReader.readInt32());
            }
            jsonReader.nextIfMatch(',');

            AtomicIntegerArray array = new AtomicIntegerArray(values.size());
            for (int i = 0; i < values.size(); i++) {
                Integer value = values.get(i);
                if (value == null) {
                    continue;
                }
                array.set(i, value);
            }
            return array;
        }

        throw new JSONException(jsonReader.info("TODO"));
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        AtomicIntegerArray array = new AtomicIntegerArray(entryCnt);
        for (int i = 0; i < entryCnt; i++) {
            Integer value = jsonReader.readInt32();
            if (value == null) {
                continue;
            }
            array.set(i, value);
        }
        return array;
    }
}
