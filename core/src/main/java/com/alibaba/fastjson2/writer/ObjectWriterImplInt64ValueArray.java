package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.function.Function;

import static com.alibaba.fastjson2.writer.ObjectWriterProvider.TYPE_INT64_MASK;

final class ObjectWriterImplInt64ValueArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInt64ValueArray INSTANCE = new ObjectWriterImplInt64ValueArray(null);

    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[J");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[J");

    private final Function<Object, long[]> function;

    public ObjectWriterImplInt64ValueArray(Function<Object, long[]> function) {
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

        long[] array;
        if (function != null && object != null) {
            array = function.apply(object);
        } else {
            array = (long[]) object;
        }

        jsonWriter.writeInt64(array);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        ObjectWriterProvider provider = jsonWriter.context.provider;
        ObjectWriter objectWriter = null;
        if ((provider.userDefineMask & TYPE_INT64_MASK) != 0) {
            objectWriter = jsonWriter.context.getObjectWriter(Long.class);
        }

        long[] array;
        if (function != null && object != null) {
            array = function.apply(object);
        } else {
            array = (long[]) object;
        }

        if (objectWriter == null || objectWriter == ObjectWriterImplInt32.INSTANCE) {
            jsonWriter.writeInt64(array);
            return;
        }

        jsonWriter.startArray();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }
            objectWriter.write(jsonWriter, array[i], i, long.class, features);
        }
        jsonWriter.endArray();
    }
}
