package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ObjectReaderInterface<T>
        extends ObjectReaderAdapter<T> {
    public ObjectReaderInterface(
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            Supplier creator,
            Function buildFunction,
            FieldReader[] fieldReaders
    ) {
        super(objectClass, typeKey, typeName, features, null, creator, buildFunction, fieldReaders);
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, long features) {
        ObjectReader autoTypeReader = jsonReader.checkAutoType(this.objectClass, this.typeNameHash, this.features | features);
        if (autoTypeReader != null && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, features);
        }

        JSONObject object = jsonReader.read(JSONObject.class);
        throw new JSONException("GraalVM not support");
    }

    @Override
    public T createInstance(long features) {
        JSONObject object = new JSONObject();
        throw new JSONException("GraalVM not support");
    }

    @Override
    public T createInstance(Map map, long features) {
        JSONObject object;
        if (map instanceof JSONObject) {
            object = (JSONObject) map;
        } else {
            object = new JSONObject(map);
        }
        throw new JSONException("GraalVM not support");
    }
}
