package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.Currency;

final class ObjectWriterImplCurrency
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplCurrency INSTANCE = new ObjectWriterImplCurrency(null);
    static final ObjectWriterImplCurrency INSTANCE_FOR_FIELD = new ObjectWriterImplCurrency(null);

    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes(TypeUtils.getTypeName(Currency.class));
    static final long JSONB_TYPE_HASH = Fnv.hashCode64(TypeUtils.getTypeName(Currency.class));

    final Class defineClass;

    ObjectWriterImplCurrency(Class defineClass) {
        this.defineClass = defineClass;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Currency currency = (Currency) object;
        if (jsonWriter.isWriteTypeInfo(currency) && defineClass == null) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        jsonWriter.writeString(currency.getCurrencyCode());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.jsonb) {
            writeJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Currency currency = (Currency) object;
        jsonWriter.writeString(currency.getCurrencyCode());
    }
}
