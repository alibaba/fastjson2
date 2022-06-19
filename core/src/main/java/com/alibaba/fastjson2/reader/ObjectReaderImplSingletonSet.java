package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.util.Collections;

class ObjectReaderImplSingletonSet
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final Class TYPE = Collections.singleton(1).getClass();

    @Override
    public Class getObjectClass() {
        return TYPE;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt != 1) {
            throw new JSONException(jsonReader.info("input not singleton"));
        }
        Object value = jsonReader.read(Object.class);
        return Collections.singleton(value);
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        jsonReader.nextIfMatch('[');
        Object value = jsonReader.readAny();

        if (jsonReader.nextIfMatch(']')) {
            jsonReader.nextIfMatch(',');
        } else {
            throw new JSONException(jsonReader.info("input not singleton"));
        }
        return Collections.singleton(value);
    }
}
