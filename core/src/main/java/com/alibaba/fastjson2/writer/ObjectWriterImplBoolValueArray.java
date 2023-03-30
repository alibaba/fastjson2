package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.function.Function;

class ObjectWriterImplBoolValueArray
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplBoolValueArray INSTANCE = new ObjectWriterImplBoolValueArray(null);
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[Z");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[Z");

    private final Function<Object, boolean[]> function;

    public ObjectWriterImplBoolValueArray(Function<Object, boolean[]> function) {
        this.function = function;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        boolean[] array;
        if (function != null && object != null) {
            array = function.apply(object);
        } else {
            array = (boolean[]) object;
        }
        jsonWriter.writeBool(array);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        boolean[] array;
        if (function != null && object != null) {
            array = function.apply(object);
        } else {
            array = (boolean[]) object;
        }
        jsonWriter.writeBool(array);
    }
}
