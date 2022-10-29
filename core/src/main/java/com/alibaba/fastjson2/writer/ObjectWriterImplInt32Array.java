package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.NullAsDefaultValue;
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNullListAsEmpty;

final class ObjectWriterImplInt32Array
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInt32Array INSTANCE = new ObjectWriterImplInt32Array();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[Integer");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[Integer");

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            if (jsonWriter.isEnabled(NullAsDefaultValue.mask | WriteNullListAsEmpty.mask)) {
                jsonWriter.startArray();
                jsonWriter.endArray();
            } else {
                jsonWriter.writeNull();
            }
            return;
        }

        Integer[] array = (Integer[]) object;

        jsonWriter.startArray();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Integer item = array[i];
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            jsonWriter.writeInt32(item);
        }
        jsonWriter.endArray();
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

        Integer[] array = (Integer[]) object;

        jsonWriter.startArray(array.length);
        for (int i = 0; i < array.length; i++) {
            Integer item = array[i];
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            jsonWriter.writeInt32(item);
        }
    }
}
