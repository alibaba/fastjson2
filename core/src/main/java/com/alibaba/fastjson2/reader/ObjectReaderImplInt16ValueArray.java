package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

class ObjectReaderImplInt16ValueArray
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplInt16ValueArray INSTANCE = new ObjectReaderImplInt16ValueArray();

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            short[] values = new short[16];
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

                values[size++] = (short) jsonReader.readInt32Value();
            }

            jsonReader.nextIfMatch(',');

            return Arrays.copyOf(values, size);
        }

        throw new JSONException(jsonReader.info("TODO"));
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        short[] array = new short[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = (short) jsonReader.readInt32Value();
        }
        return array;
    }

    @Override
    public Object createInstance(Collection collection) {
        short[] array = new short[collection.size()];
        int i = 0;
        for (Object item : collection) {
            short value;
            if (item == null) {
                value = 0;
            } else if (item instanceof Number) {
                value = ((Number) item).shortValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), short.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to short " + item.getClass());
                }
                value = (Short) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        return array;
    }
}
