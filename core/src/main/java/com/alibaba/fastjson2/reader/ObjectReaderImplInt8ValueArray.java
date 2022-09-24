package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.function.Function;

class ObjectReaderImplInt8ValueArray
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplInt8ValueArray INSTANCE = new ObjectReaderImplInt8ValueArray(null);

    final String format;

    ObjectReaderImplInt8ValueArray(String format) {
        super(byte[].class);
        this.format = format;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            byte[] values = new byte[16];
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

                values[size++] = (byte) jsonReader.readInt32Value();
            }
            jsonReader.nextIfMatch(',');

            return Arrays.copyOf(values, size);
        }

        if (jsonReader.isString()) {
            if ((jsonReader.features(features) & JSONReader.Feature.Base64StringAsByteArray.mask) != 0) {
                String str = jsonReader.readString();
                return Base64.getDecoder().decode(str);
            }

            return jsonReader.readBinary();
        }

        throw new JSONException(jsonReader.info("TODO"));
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isBinary()) {
            return jsonReader.readBinary();
        }

        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        byte[] array = new byte[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            array[i] = (byte) jsonReader.readInt32Value();
        }
        return array;
    }

    @Override
    public Object createInstance(Collection collection) {
        byte[] array = new byte[collection.size()];
        int i = 0;
        for (Object item : collection) {
            byte value;
            if (item == null) {
                value = 0;
            } else if (item instanceof Number) {
                value = ((Number) item).byteValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), byte.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to byte " + item.getClass());
                }
                value = (Byte) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        return array;
    }
}
