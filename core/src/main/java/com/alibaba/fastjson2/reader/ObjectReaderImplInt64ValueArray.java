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

final class ObjectReaderImplInt64ValueArray
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplInt64ValueArray INSTANCE = new ObjectReaderImplInt64ValueArray(long[].class, null);

    static final long HASH_TYPE = Fnv.hashCode64("[J");

    final Function<long[], Object> builder;

    ObjectReaderImplInt64ValueArray(Class objectClass, Function<long[], Object> builder) {
        super(objectClass);
        this.builder = builder;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, features);
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

                values[size++] = jsonReader.readInt64Value();
            }
            jsonReader.nextIfMatch(',');

            long[] array = Arrays.copyOf(values, size);
            if (builder != null) {
                return builder.apply(array);
            }
            return array;
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
            long typeHash = jsonReader.readTypeHashCode();
            if (typeHash != HASH_TYPE
                    && typeHash != ObjectReaderImplInt64Array.HASH_TYPE
                    && typeHash != ObjectReaderImplInt32Array.HASH_TYPE
                    && typeHash != ObjectReaderImplInt32ValueArray.HASH_TYPE
            ) {
                throw new JSONException(jsonReader.info("not support " + jsonReader.getString()));
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

        if (builder != null) {
            return builder.apply(array);
        }
        return array;
    }

    @Override
    public Object createInstance(Collection collection) {
        long[] array = new long[collection.size()];
        int i = 0;
        for (Object item : collection) {
            long value;
            if (item == null) {
                value = 0;
            } else if (item instanceof Number) {
                value = ((Number) item).longValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), long.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to long " + item.getClass());
                }
                value = (Long) typeConvert.apply(item);
            }
            array[i++] = value;
        }

        if (builder != null) {
            return builder.apply(array);
        }
        return array;
    }
}
