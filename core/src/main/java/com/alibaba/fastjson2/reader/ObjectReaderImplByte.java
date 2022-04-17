package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

public final class ObjectReaderImplByte extends ObjectReaderBaseModule.PrimitiveImpl<Byte> {
    static final ObjectReaderImplByte INSTANCE = new ObjectReaderImplByte();

    public static final long HASH_TYPE = Fnv.hashCode64("B");

    @Override
    public Byte readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
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
        return i.byteValue();
    }

    @Override
    public Byte readObject(JSONReader jsonReader, long features) {
        Integer i = jsonReader.readInt32();
        if (i == null) {
            return null;
        }
        return i.byteValue();
    }
}
