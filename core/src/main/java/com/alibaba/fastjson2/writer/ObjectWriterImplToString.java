package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

public class ObjectWriterImplToString
        extends ObjectWriterBaseModule.PrimitiveImpl {
    public static final ObjectWriterImplToString INSTANCE = new ObjectWriterImplToString(false);
    public static final ObjectWriterImplToString DIRECT = new ObjectWriterImplToString(true);

    private final boolean direct;

    public ObjectWriterImplToString(boolean direct) {
        this.direct = direct;
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        String str = object.toString();
        if (direct) {
            jsonWriter.writeRaw(str);
        } else {
            jsonWriter.writeString(str);
        }
    }
}
