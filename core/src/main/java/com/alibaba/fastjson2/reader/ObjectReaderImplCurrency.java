package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.util.Currency;

final class ObjectReaderImplCurrency extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplCurrency INSTANCE = new ObjectReaderImplCurrency();

    static final long TYPE_HASH = Fnv.hashCode64("Currency");
    static final long TYPE_HASH_FULL = Fnv.hashCode64("java.util.Currency");

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.getType() == JSONB.Constants.BC_TYPED_ANY) {
            jsonReader.next();
            long typeHash = jsonReader.readTypeHashCode();
            if (typeHash != TYPE_HASH && typeHash != TYPE_HASH_FULL) {
                throw new JSONException("currency not support input autoTypeClass " + jsonReader.getString());
            }
        }

        String strVal = jsonReader.readString();
        if (strVal == null || strVal.isEmpty()) {
            return null;
        }
        return Currency.getInstance(strVal);
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        String strVal = jsonReader.readString();
        if (strVal == null || strVal.isEmpty()) {
            return null;
        }
        return Currency.getInstance(strVal);
    }
}
