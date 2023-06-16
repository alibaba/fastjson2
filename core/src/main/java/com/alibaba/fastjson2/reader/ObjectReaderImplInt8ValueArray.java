package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONReader.Feature.Base64StringAsByteArray;

class ObjectReaderImplInt8ValueArray
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplInt8ValueArray INSTANCE = new ObjectReaderImplInt8ValueArray(null);
    static final long HASH_TYPE = Fnv.hashCode64("[B");

    final String format;
    final Function<byte[], Object> builder;
    final long features;

    ObjectReaderImplInt8ValueArray(String format) {
        super(byte[].class);
        this.format = format;
        this.builder = null;
        this.features = 0;
    }

    ObjectReaderImplInt8ValueArray(Function<byte[], Object> builder, String format) {
        super(byte[].class);
        this.format = format;
        this.features = "base64".equals(format) ? Base64StringAsByteArray.mask : 0;
        this.builder = builder;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfArrayStart()) {
            byte[] values = new byte[16];
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

                values[size++] = (byte) jsonReader.readInt32Value();
            }
            jsonReader.nextIfMatch(',');

            byte[] bytes = Arrays.copyOf(values, size);
            if (builder != null) {
                return builder.apply(bytes);
            }
            return bytes;
        }

        if (jsonReader.isString()) {
            byte[] bytes;
            if ((jsonReader.features(this.features | features) & Base64StringAsByteArray.mask) != 0) {
                String str = jsonReader.readString();
                bytes = Base64.getDecoder().decode(str);
            } else {
                bytes = jsonReader.readBinary();
            }

            if (builder != null) {
                return builder.apply(bytes);
            }
            return bytes;
        }

        throw new JSONException(jsonReader.info("TODO"));
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHashCode = jsonReader.readTypeHashCode();
            if (typeHashCode != HASH_TYPE && typeHashCode != ObjectReaderImplInt8Array.HASH_TYPE) {
                throw new JSONException("not support autoType : " + jsonReader.getString());
            }
        }

        byte[] bytes;
        if (jsonReader.isBinary()) {
            bytes = jsonReader.readBinary();
        } else if (jsonReader.isString()) {
            String str = jsonReader.readString();
            bytes = Base64.getDecoder().decode(str);
        } else {
            int entryCnt = jsonReader.startArray();
            if (entryCnt == -1) {
                return null;
            }
            bytes = new byte[entryCnt];
            for (int i = 0; i < entryCnt; i++) {
                bytes[i] = (byte) jsonReader.readInt32Value();
            }
        }
        if (builder != null) {
            return builder.apply(bytes);
        }
        return bytes;
    }

    @Override
    public Object createInstance(Collection collection) {
        byte[] bytes = new byte[collection.size()];
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
            bytes[i++] = value;
        }

        if (builder != null) {
            return builder.apply(bytes);
        }
        return bytes;
    }
}
