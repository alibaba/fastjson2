package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.MultiType;

import java.lang.reflect.Type;
import java.util.Collection;

final class ObjectArrayReaderMultiType
        implements ObjectReader {
    final Type[] types;
    final ObjectReader[] readers;

    ObjectArrayReaderMultiType(MultiType multiType) {
        Type[] types = new Type[multiType.size()];
        for (int i = 0; i < multiType.size(); i++) {
            types[i] = multiType.getType(i);
        }
        this.types = types;
        this.readers = new ObjectReader[types.length];
    }

    ObjectReader getObjectReader(JSONReader jsonReader, int index) {
        ObjectReader objectReader = readers[index];
        if (objectReader == null) {
            Type type = types[index];
            objectReader = jsonReader.getObjectReader(type);
            readers[index] = objectReader;
        }
        return objectReader;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, 0);
        }

        if (jsonReader.nextIfNullOrEmptyString()) {
            return null;
        }

        Object[] values = new Object[types.length];
        if (jsonReader.nextIfMatch('[')) {
            for (int i = 0; ; ++i) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                Object value;

                if (jsonReader.isReference()) {
                    String reference = jsonReader.readReference();
                    if ("..".equals(reference)) {
                        value = values;
                    } else {
                        value = null;
                        jsonReader.addResolveTask(values, i, JSONPath.of(reference));
                    }
                } else {
                    ObjectReader objectReader = getObjectReader(jsonReader, i);
                    value = objectReader.readObject(jsonReader, types[i], i, features);
                }
                values[i] = value;

                jsonReader.nextIfMatch(',');
            }
            jsonReader.nextIfMatch(',');

            return values;
        }

        throw new JSONException(jsonReader.info("TODO"));
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }

        Object[] values = new Object[types.length];
        for (int i = 0; i < entryCnt; ++i) {
            Object value;

            if (jsonReader.isReference()) {
                String reference = jsonReader.readReference();
                if ("..".equals(reference)) {
                    value = values;
                } else {
                    value = null;
                    jsonReader.addResolveTask(values, i, JSONPath.of(reference));
                }
            } else {
                ObjectReader objectReader = getObjectReader(jsonReader, i);
                value = objectReader.readObject(jsonReader, types[i], i, features);
            }

            values[i] = value;
        }
        return values;
    }

    @Override
    public Object createInstance(Collection collection) {
        Object[] array = new Object[types.length];
        return array;
    }
}
