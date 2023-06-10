package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Arrays;

final class ObjectReaderImplCharValueArray
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplCharValueArray INSTANCE = new ObjectReaderImplCharValueArray(null);
    static final long TYPE_HASH = Fnv.hashCode64("[C");

    final Function<char[], Object> builder;

    public ObjectReaderImplCharValueArray(Function<char[], Object> builder) {
        super(char[].class);
        this.builder = builder;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.current() == '"') {
            String str = jsonReader.readString();
            char[] chars = str.toCharArray();
            if (builder != null) {
                return builder.apply(chars);
            }
            return chars;
        }

        if (jsonReader.nextIfArrayStart()) {
            char[] values = new char[16];
            int size = 0;
            for (; ; ) {
                if (jsonReader.nextIfArrayEnd()) {
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

                if (jsonReader.isInt()) {
                    values[size++] = (char) jsonReader.readInt32Value();
                    continue;
                }
                String str = jsonReader.readString();
                values[size++] = (str == null) ? '\0' : str.charAt(0);
            }
            jsonReader.nextIfComma();

            char[] chars = Arrays.copyOf(values, size);
            if (builder != null) {
                return builder.apply(chars);
            }
            return chars;
        }

        throw new JSONException(jsonReader.info("TODO"));
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHashCode = jsonReader.readTypeHashCode();
            if (typeHashCode != TYPE_HASH) {
                throw new JSONException("not support autoType : " + jsonReader.getString());
            }
        }

        if (jsonReader.isString()) {
            String str = jsonReader.readString();
            return str.toCharArray();
        }

        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }
        char[] chars = new char[entryCnt];
        for (int i = 0; i < entryCnt; i++) {
            if (jsonReader.isInt()) {
                chars[i] = (char) jsonReader.readInt32Value();
            } else {
                chars[i] = jsonReader.readString().charAt(0);
            }
        }
        if (builder != null) {
            return builder.apply(chars);
        }
        return chars;
    }
}
