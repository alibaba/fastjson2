package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.AbstractMap;

class ObjectReaderImplMapEntry
        extends ObjectReaderBaseModule.PrimitiveImpl {
    final Type keyType;
    final Type valueType;

    volatile ObjectReader keyReader;
    volatile ObjectReader valueReader;

    public ObjectReaderImplMapEntry(Type keyType, Type valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt != 2) {
            throw new JSONException(jsonReader.info("entryCnt must be 2, but " + entryCnt));
        }
        Object key;
        if (keyType == null) {
            key = jsonReader.readAny();
        } else {
            if (keyReader == null) {
                keyReader = jsonReader.getObjectReader(keyType);
            }
            key = keyReader.readObject(jsonReader, features);
        }

        Object value;
        if (valueType == null) {
            value = jsonReader.readAny();
        } else {
            if (valueReader == null) {
                valueReader = jsonReader.getObjectReader(valueType);
            }
            value = valueReader.readObject(jsonReader, features);
        }

        return new AbstractMap.SimpleEntry(key, value);
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        jsonReader.nextIfMatch('{');
        Object key = jsonReader.readAny();
        jsonReader.nextIfMatch(':');

        Object value;
        if (valueType == null) {
            value = jsonReader.readAny();
        } else {
            if (valueReader == null) {
                valueReader = jsonReader.getObjectReader(valueType);
            }
            value = valueReader.readObject(jsonReader, features);
        }

        jsonReader.nextIfMatch('}');
        jsonReader.nextIfMatch(',');
        return new AbstractMap.SimpleEntry(key, value);
    }
}
