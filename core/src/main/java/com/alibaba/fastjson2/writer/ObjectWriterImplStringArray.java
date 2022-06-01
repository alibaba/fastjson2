package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

final class ObjectWriterImplStringArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final byte[] TYPE_NAME_BYTES = JSONB.toBytes("[String");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[String");

    static final ObjectWriterImplStringArray INSTANCE = new ObjectWriterImplStringArray();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        String[] list = (String[]) object;

        jsonWriter.startArray();
        for (int i = 0; i < list.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            String item = list[i];
            if (item == null) {
                if (jsonWriter.isEnabled(JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) {
                    jsonWriter.writeString("");
                } else {
                    jsonWriter.writeNull();
                }
                continue;
            }
            jsonWriter.writeString(item);
        }
        jsonWriter.endArray();
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            jsonWriter.writeTypeName(TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        String[] list = (String[]) object;

        jsonWriter.startArray(list.length);
        for (int i = 0; i < list.length; i++) {
            String item = list[i];
            if (item == null) {
                if (jsonWriter.isEnabled(JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) {
                    jsonWriter.writeString("");
                } else {
                    jsonWriter.writeNull();
                }
                continue;
            }
            jsonWriter.writeString(item);
        }
    }
}
