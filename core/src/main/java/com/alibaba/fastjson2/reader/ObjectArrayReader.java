package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

import static com.alibaba.fastjson2.JSONB.Constants.*;

public final class ObjectArrayReader
        extends ObjectReaderPrimitive {
    public static final ObjectArrayReader INSTANCE = new ObjectArrayReader();
    public static final long TYPE_HASH_CODE = Fnv.hashCode64("[O");

    public ObjectArrayReader() {
        super(Object[].class);
    }

    @Override
    public Object[] createInstance(Collection collection) {
        Object[] array = new Object[collection.size()];
        int i = 0;
        for (Object item : collection) {
            array[i++] = item;
        }
        return array;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            Object[] values = new Object[16];
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

                Object value;
                char ch = jsonReader.current();
                switch (ch) {
                    case '"':
                        value = jsonReader.readString();
                        break;
                    case '+':
                    case '-':
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '.':
                        value = jsonReader.readNumber();
                        break;
                    case 'n':
                        jsonReader.readNull();
                        value = null;
                        break;
                    case 't':
                    case 'f':
                        value = jsonReader.readBoolValue();
                        break;
                    case '{':
                        value = jsonReader.readObject();
                        break;
                    case '[':
                        value = jsonReader.readArray();
                        break;
                    default:
                        throw new JSONException(jsonReader.info());
                }
                values[size++] = value;
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
        if (jsonReader.getType() == BC_TYPED_ANY) {
            ObjectReader autoTypeObjectReader = jsonReader.checkAutoType(Object[].class, TYPE_HASH_CODE, features);
            if (autoTypeObjectReader != this) {
                return autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
            }
        }

        int itemCnt = jsonReader.startArray();
        if (itemCnt == -1) {
            return null;
        }
        Object[] array = new Object[itemCnt];
        for (int i = 0; i < itemCnt; i++) {
            byte type = jsonReader.getType();

            Object value;
            ObjectReader autoTypeValueReader;
            if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_UTF16BE) {
                value = jsonReader.readString();
            } else if (type == BC_TYPED_ANY) {
                autoTypeValueReader = jsonReader.checkAutoType(Object.class, 0, features);
                value = autoTypeValueReader.readJSONBObject(jsonReader, null, null, features);
            } else if (type == BC_NULL) {
                jsonReader.next();
                value = null;
            } else if (type == BC_TRUE) {
                jsonReader.next();
                value = Boolean.TRUE;
            } else if (type == BC_FALSE) {
                jsonReader.next();
                value = Boolean.FALSE;
            } else if (type == BC_INT64) {
                value = jsonReader.readInt64Value();
            } else {
                value = jsonReader.readAny();
            }
            array[i] = value;
        }
        return array;
    }
}
