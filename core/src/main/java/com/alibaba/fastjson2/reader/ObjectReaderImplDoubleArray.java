package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

final class ObjectReaderImplDoubleArray
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplDoubleArray INSTANCE = new ObjectReaderImplDoubleArray();

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            Double[] values = new Double[16];
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

                values[size++] = jsonReader.readDouble();
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
        Double[] array = new Double[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = jsonReader.readDouble();
        }
        return array;
    }

    @Override
    public Object createInstance(Collection collection) {
        Double[] array = new Double[collection.size()];
        int i = 0;
        for (Object item : collection) {
            Double value;
            if (item == null) {
                value = null;
            } else if (item instanceof Number) {
                value = ((Number) item).doubleValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), Double.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to Double " + item.getClass());
                }
                value = (Double) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        return array;
    }
}
