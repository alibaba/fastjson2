package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

import static com.alibaba.fastjson2.JSONB.Constants.BC_TYPED_ANY;

public final class ObjectReaderImplInt32Array
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplInt32Array INSTANCE = new ObjectReaderImplInt32Array();
    public static final long HASH_TYPE = Fnv.hashCode64("[Integer");

    ObjectReaderImplInt32Array() {
        super(Integer[].class);
    }

    @Override
    public Object createInstance(Collection collection) {
        Integer[] array = new Integer[collection.size()];
        int i = 0;
        for (Object item : collection) {
            Integer value;
            if (item == null) {
                value = null;
            } else if (item instanceof Number) {
                value = ((Number) item).intValue();
            } else {
                Function typeConvert = JSONFactory.defaultObjectReaderProvider.getTypeConvert(item.getClass(), Integer.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to Integer " + item.getClass());
                }
                value = (Integer) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        return array;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfArrayStart()) {
            Integer[] values = new Integer[16];
            int size = 0;
            for (; ; ) {
                if (jsonReader.nextIfArrayEnd()) {
                    break;
                }

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

                values[size++] = jsonReader.readInt32();
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
        if (jsonReader.nextIfMatch(BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            if (typeHash != HASH_TYPE
                    && typeHash != ObjectReaderImplInt32ValueArray.HASH_TYPE
            ) {
                throw new JSONException(jsonReader.info("not support type " + jsonReader.getString()));
            }
        }

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
