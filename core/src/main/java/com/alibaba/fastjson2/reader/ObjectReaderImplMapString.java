package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.util.HashMap;
import java.util.Map;

final class ObjectReaderImplMapString extends ObjectReaderImplMapTyped {
    public ObjectReaderImplMapString(Class mapType, Class instanceType, long features) {
        super(mapType, instanceType, null, String.class, features, null);
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return this.readJSONBObject(jsonReader, features);
        }

        boolean match = jsonReader.nextIfMatch('{');
        if (!match) {
            if (jsonReader.current() == '[') {
                jsonReader.next();
                if (jsonReader.current() == '{') {
                    Object arrayItem = readObject(jsonReader, features);
                    if (jsonReader.nextIfMatch(']')) {
                        jsonReader.nextIfMatch(',');
                        return arrayItem;
                    }
                }
                throw new JSONException("expect '{', but '['");
            }
        }

        Map<String, Object> object
                = instanceType == HashMap.class
                ? new HashMap<>()
                : (Map) createInstance(jsonReader.getContext().getFeatures() | features);

        for (; ; ) {
            if (jsonReader.nextIfMatch('}')) {
                break;
            }

            String name = jsonReader.readFieldName();
            String value = jsonReader.readString();
            object.put(name, value);
        }

        jsonReader.nextIfMatch(',');

        return object;
    }
}
