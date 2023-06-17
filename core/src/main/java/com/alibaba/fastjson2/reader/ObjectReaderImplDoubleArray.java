package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

final class ObjectReaderImplDoubleArray
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplDoubleArray INSTANCE = new ObjectReaderImplDoubleArray();
    static final long HASH_TYPE = Fnv.hashCode64("[Double");

    ObjectReaderImplDoubleArray() {
        super(Double[].class);
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfArrayStart()) {
            Double[] values = new Double[16];
            int size = 0;
            while (!jsonReader.nextIfArrayEnd()) {
                if (jsonReader.isEnd()) {
                    throw new JSONException(jsonReader.info("input end"));
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
            jsonReader.nextIfComma();

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
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHashCode = jsonReader.readTypeHashCode();
            if (typeHashCode != HASH_TYPE) {
                throw new JSONException("not support autoType : " + jsonReader.getString());
            }
        }

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
