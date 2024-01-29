package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.function.Function;

final class ObjectWriterImplInt32ValueArray
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplInt32ValueArray INSTANCE = new ObjectWriterImplInt32ValueArray(null);
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[I");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[I");

    private final Function<Object, int[]> function;

    public ObjectWriterImplInt32ValueArray(Function<Object, int[]> function) {
        this.function = function;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        int[] array;
        if (function != null) {
            array = function.apply(object);
        } else {
            array = (int[]) object;
        }

        jsonWriter.writeInt32(array);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        int[] array;
        if (function != null) {
            array = function.apply(object);
        } else {
            array = (int[]) object;
        }

        jsonWriter.writeInt32(array);
    }
}
