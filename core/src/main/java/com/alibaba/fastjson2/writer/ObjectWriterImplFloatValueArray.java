package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.function.Function;

final class ObjectWriterImplFloatValueArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplFloatValueArray INSTANCE = new ObjectWriterImplFloatValueArray(null, null);
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[F");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[F");

    private final DecimalFormat format;

    private final Function<Object, float[]> function;

    public ObjectWriterImplFloatValueArray(DecimalFormat format) {
        this.format = format;
        this.function = null;
    }

    public ObjectWriterImplFloatValueArray(Function<Object, float[]> function, DecimalFormat format) {
        this.function = function;
        this.format = format;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        float[] array;
        if (function != null && object != null) {
            array = function.apply(object);
        } else {
            array = (float[]) object;
        }

        jsonWriter.writeFloat(array);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        float[] array;
        if (function != null && object != null) {
            array = function.apply(object);
        } else {
            array = (float[]) object;
        }

        if (format == null) {
            jsonWriter.writeFloat(array);
        } else {
            jsonWriter.writeFloat(array, format);
        }
    }
}
