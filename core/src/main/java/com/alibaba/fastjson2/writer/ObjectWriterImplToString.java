package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

public class ObjectWriterImplToString
        extends ObjectWriterBaseModule.PrimitiveImpl {
    public static final ObjectWriterImplToString INSTANCE = new ObjectWriterImplToString();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeString(
                object.toString());
    }
}
