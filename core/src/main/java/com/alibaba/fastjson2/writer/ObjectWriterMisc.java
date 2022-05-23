package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterMisc
        implements ObjectWriter {
    static final ObjectWriterMisc INSTANCE = new ObjectWriterMisc();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Class<?> objectClass = object.getClass();
        String objectClassName = objectClass.getName();
        switch (objectClassName) {
            case "net.sf.json.JSONNull":
                jsonWriter.writeNull();
                break;
            default:
                throw new JSONException("not support class : " + objectClassName);
        }
    }
}
