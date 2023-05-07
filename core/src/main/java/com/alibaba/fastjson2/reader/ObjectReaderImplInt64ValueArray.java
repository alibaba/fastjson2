package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Function;

public final class ObjectReaderImplInt64ValueArray
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplInt64ValueArray INSTANCE = new ObjectReaderImplInt64ValueArray(long[].class, null);

    public static final long HASH_TYPE = Fnv.hashCode64("[J");

    final Function<long[], Object> builder;

    ObjectReaderImplInt64ValueArray(Class objectClass, Function<long[], Object> builder) {
        super(objectClass);
        this.builder = builder;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        long[] array = jsonReader.readInt64ValueArray();
        if (array != null && builder != null) {
            return builder.apply(array);
        }
        return array;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        long[] array = jsonReader.readInt64ValueArray();
        if (array != null && builder != null) {
            return builder.apply(array);
        }
        return array;
    }

    @Override
    public Object createInstance(Collection collection) {
        long[] array = new long[collection.size()];
        int i = 0;
        for (Object item : collection) {
            long value;
            if (item == null) {
                value = 0;
            } else if (item instanceof Number) {
                value = ((Number) item).longValue();
            } else {
                Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(item.getClass(), long.class);
                if (typeConvert == null) {
                    throw new JSONException("can not cast to long " + item.getClass());
                }
                value = (Long) typeConvert.apply(item);
            }
            array[i++] = value;
        }

        if (builder != null) {
            return builder.apply(array);
        }
        return array;
    }
}
