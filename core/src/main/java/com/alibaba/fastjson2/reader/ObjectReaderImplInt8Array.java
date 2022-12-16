package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

class ObjectReaderImplInt8Array
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplInt8Array INSTANCE = new ObjectReaderImplInt8Array(null);

    final String format;

    public ObjectReaderImplInt8Array(String format) {
        super(Byte[].class);
        this.format = format;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            Byte[] values = new Byte[16];
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

                Integer i = jsonReader.readInt32();
                values[size++] = i == null ? null : i.byteValue();
            }
            jsonReader.nextIfMatch(',');

            return Arrays.copyOf(values, size);
        }

        if (jsonReader.current() == 'x') {
            return jsonReader.readBinary();
        }

        if (jsonReader.isString()) {
            String strVal = jsonReader.readString();
            if (strVal.isEmpty()) {
                return null;
            }

            if ("base64".equals(format)) {
                return Base64.getDecoder().decode(strVal);
            }

            if ("gzip,base64".equals(format) || "gzip".equals(format)) {
                byte[] bytes = Base64.getDecoder().decode(strVal);

                GZIPInputStream gzipIn = null;
                try {
                    gzipIn = new GZIPInputStream(new ByteArrayInputStream(bytes));

                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    for (; ; ) {
                        byte[] buf = new byte[1024];
                        int len = gzipIn.read(buf);
                        if (len == -1) {
                            break;
                        }
                        if (len > 0) {
                            byteOut.write(buf, 0, len);
                        }
                    }
                    return byteOut.toByteArray();
                } catch (IOException ex) {
                    throw new JSONException(jsonReader.info("unzip bytes error."), ex);
                }
            }
        }

        throw new JSONException(jsonReader.info("TODO"));
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        Byte[] array = new Byte[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            Integer integer = jsonReader.readInt32();
            array[i] = integer == null ? null : integer.byteValue();
        }
        return array;
    }

    @Override
    public Object createInstance(Collection collection) {
        Byte[] array = new Byte[collection.size()];
        int i = 0;
        for (Object item : collection) {
            Byte value;
            if (item == null) {
                value = null;
            } else if (item instanceof Number) {
                value = ((Number) item).byteValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), Byte.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to Byte " + item.getClass());
                }
                value = (Byte) typeConvert.apply(item);
            }
            array[i++] = value;
        }
        return array;
    }
}
