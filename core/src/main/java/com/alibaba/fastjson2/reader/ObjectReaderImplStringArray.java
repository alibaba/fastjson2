package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Collection;

public final class ObjectReaderImplStringArray
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplStringArray INSTANCE = new ObjectReaderImplStringArray();
    public static final long HASH_TYPE = Fnv.hashCode64("[String");

    ObjectReaderImplStringArray() {
        super(Long[].class);
    }

    @Override
    public Object createInstance(Collection collection) {
        String[] array = new String[collection.size()];
        int i = 0;
        for (Object item : collection) {
            String value;
            if (item == null) {
                value = null;
            } else if (item instanceof String) {
                value = (String) item;
            } else {
                value = item.toString();
            }
            array[i++] = value;
        }
        return array;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readStringArray();
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readStringArray();
    }
}
