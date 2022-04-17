package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import static com.alibaba.fastjson2.JSONB.Constants.BC_TYPED_ANY;

public final class ObjectReaderImplShort extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplShort INSTANCE = new ObjectReaderImplShort();

    public static final long HASH_TYPE = Fnv.hashCode64("S");

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfMatch(BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            if (typeHash != HASH_TYPE) {
                String typeName = jsonReader.getString();
                throw new JSONException(typeName);
            }
        }

        Integer i = jsonReader.readInt32();
        if (i == null) {
            return null;
        }
        return i.shortValue();
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        Integer i = jsonReader.readInt32();
        if (i == null) {
            return null;
        }
        return i.shortValue();
    }
}
