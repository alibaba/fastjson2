package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;

public final class ObjectReaderImplFromLong<T>
        extends ObjectReaderPrimitive<T> {
    final LongFunction<T> creator;

    public ObjectReaderImplFromLong(Class<T> objectClass, LongFunction creator) {
        super(objectClass);
        this.creator = creator;
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        if (objectClass == AtomicLong.class && jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            if (typeHash != -1591858996898070466L) { // Fnv.hashCode64("AtomicLong")
                String typeName = jsonReader.getString();
                throw new JSONException(jsonReader.info(typeName));
            }
        }

        return creator.apply(
                jsonReader.readInt64Value()
        );
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return creator.apply(
                jsonReader.readInt64Value()
        );
    }
}
