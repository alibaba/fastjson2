package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.IntFunction;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

public final class ObjectReaderImplFromInt<T>
        extends ObjectReaderPrimitive<T> {
    final IntFunction<T> creator;

    public ObjectReaderImplFromInt(Class<T> objectClass, IntFunction creator) {
        super(objectClass);
        this.creator = creator;
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        if (objectClass == AtomicInteger.class && jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            if (typeHash != 7576651708426282938L) { // Fnv.hashCode64("AtomicInteger")
                String typeName = jsonReader.getString();
                throw new JSONException(jsonReader.info(typeName));
            }
        }

        return creator.apply(
                jsonReader.readInt32Value()
        );
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return creator.apply(
                jsonReader.readInt32Value()
        );
    }
}
