package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.text.DecimalFormat;

final class ObjectWriterImplFloatValueArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplFloatValueArray INSTANCE = new ObjectWriterImplFloatValueArray(null);
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[F");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[F");

    private final DecimalFormat format;

    public ObjectWriterImplFloatValueArray(DecimalFormat format) {
        this.format = format;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        jsonWriter.writeFloat((float[]) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (format == null) {
            jsonWriter.writeFloat((float[]) object);
        } else {
            jsonWriter.writeFloat((float[]) object, format);
        }
    }
}
