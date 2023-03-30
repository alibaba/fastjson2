package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.function.Function;

final class ObjectWriterImplCharValueArray
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplCharValueArray INSTANCE = new ObjectWriterImplCharValueArray(null);
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[C");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[C");

    private final Function<Object, char[]> function;

    public ObjectWriterImplCharValueArray(Function<Object, char[]> function) {
        this.function = function;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        char[] chars;
        if (function != null && object != null) {
            chars = function.apply(object);
        } else {
            chars = (char[]) object;
        }

        jsonWriter.writeString(chars, 0, chars.length);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        char[] chars;
        if (function != null && object != null) {
            chars = function.apply(object);
        } else {
            chars = (char[]) object;
        }

        if (jsonWriter.utf16) {
            jsonWriter.writeString(chars, 0, chars.length);
        } else {
            jsonWriter.writeString(new String(chars));
        }
    }
}
