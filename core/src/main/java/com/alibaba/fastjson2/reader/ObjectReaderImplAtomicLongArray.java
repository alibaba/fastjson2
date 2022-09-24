package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongArray;

final class ObjectReaderImplAtomicLongArray
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplAtomicLongArray INSTANCE = new ObjectReaderImplAtomicLongArray();

    public ObjectReaderImplAtomicLongArray() {
        super(AtomicLongArray.class);
    }

    @Override
    public Object createInstance(Collection collection) {
        AtomicLongArray array = new AtomicLongArray(collection.size());
        int index = 0;
        for (Object item : collection) {
            array.set(index++, TypeUtils.toLongValue(item));
        }
        return array;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
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

        throw new JSONException(jsonReader.info("TODO"));
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
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
