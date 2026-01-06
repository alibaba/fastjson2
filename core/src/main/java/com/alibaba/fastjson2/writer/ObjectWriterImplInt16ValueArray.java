package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

final class ObjectWriterImplInt16ValueArray
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplInt16ValueArray INSTANCE = new ObjectWriterImplInt16ValueArray(null);
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[S");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[S");

    private final Function<Object, short[]> function;

    public ObjectWriterImplInt16ValueArray(Function<Object, short[]> function) {
        this.function = function;
    }

    @Override
    public void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        short[] shorts;
        if (function != null && object != null) {
            shorts = function.apply(object);
        } else {
            shorts = (short[]) object;
        }

        boolean writeAsString = (features & WriteNonStringValueAsString.mask) != 0;
        if (writeAsString) {
            jsonWriter.writeString(shorts);
            return;
        }

        jsonWriter.writeInt16(shorts);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        short[] shorts;
        if (function != null) {
            shorts = function.apply(object);
        } else {
            shorts = (short[]) object;
        }

        boolean writeAsString = (features & WriteNonStringValueAsString.mask) != 0;
        if (writeAsString) {
            jsonWriter.writeString(shorts);
            return;
        }

        jsonWriter.writeInt16(shorts);
    }
}
