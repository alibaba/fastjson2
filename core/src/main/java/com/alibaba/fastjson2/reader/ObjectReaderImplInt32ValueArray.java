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

public final class ObjectReaderImplInt32ValueArray
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplInt32ValueArray INSTANCE = new ObjectReaderImplInt32ValueArray(int[].class, null);
    public static final long HASH_TYPE = Fnv.hashCode64("[I");

    final Function<int[], Object> builder;

    ObjectReaderImplInt32ValueArray(Class objectClass, Function<int[], Object> builder) {
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
            int[] values = new int[16];
            int size = 0;
            while (!jsonReader.nextIfMatch(']')) {
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

                values[size++] = jsonReader.readInt32Value();
            }
            jsonReader.nextIfMatch(',');

            int[] array = Arrays.copyOf(values, size);
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
            long typeHashCode = jsonReader.readTypeHashCode();
            if (typeHashCode != HASH_TYPE && typeHashCode != ObjectReaderImplInt32Array.HASH_TYPE) {
                throw new JSONException("not support autoType : " + jsonReader.getString());
            }
        }

        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        int[] array = new int[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = jsonReader.readInt32Value();
        }

        if (builder != null) {
            return builder.apply(array);
        }

        return array;
    }

    @Override
    public Object createInstance(Collection collection) {
        int[] array = new int[collection.size()];
        int i = 0;
        for (Object item : collection) {
            int value;
            if (item == null) {
                value = 0;
            } else if (item instanceof Number) {
                value = ((Number) item).intValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), int.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to int " + item.getClass());
                }
                value = (Integer) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        if (builder != null) {
            return builder.apply(array);
        }
        return array;
    }
}
