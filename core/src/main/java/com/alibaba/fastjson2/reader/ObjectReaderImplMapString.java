package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

final class ObjectReaderImplMapString
        extends ObjectReaderImplMapTyped {
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

        JSONReader.Context context = jsonReader.getContext();
        Map<String, Object> object
                = instanceType == HashMap.class
                ? new HashMap<>()
                : (Map) createInstance(context.getFeatures() | features);

        for (; ; ) {
            if (jsonReader.nextIfMatch('}')) {
                break;
            }

            String name = jsonReader.readFieldName();
            String value = jsonReader.readString();
            Object origin = object.put(name, value);
            if (origin != null) {
                long contextFeatures = features | context.getFeatures();
                if ((contextFeatures & JSONReader.Feature.DuplicateKeyValueAsArray.mask) != 0) {
                    if (origin instanceof Collection) {
                        ((Collection) origin).add(value);
                        object.put(name, value);
                    } else {
                        JSONArray array = JSONArray.of(origin, value);
                        object.put(name, array);
                    }
                }
            }
        }

        jsonReader.nextIfMatch(',');

        return object;
    }
}
