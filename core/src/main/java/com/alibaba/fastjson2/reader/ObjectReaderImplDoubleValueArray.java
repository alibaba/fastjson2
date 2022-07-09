package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

class ObjectReaderImplDoubleValueArray
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplDoubleValueArray INSTANCE = new ObjectReaderImplDoubleValueArray();

    @Override
    public Class getObjectClass() {
        return double[].class;
    }
    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            double[] values = new double[16];
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

                values[size++] = jsonReader.readDoubleValue();
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
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        double[] array = new double[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = jsonReader.readDoubleValue();
        }
        return array;
    }

    @Override
    public Object createInstance(Collection collection) {
        double[] array = new double[collection.size()];
        int i = 0;
        for (Object item : collection) {
            double value;
            if (item == null) {
                value = 0;
            } else if (item instanceof Number) {
                value = ((Number) item).doubleValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), double.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to double " + item.getClass());
                }
                value = (Double) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        return array;
    }
}
