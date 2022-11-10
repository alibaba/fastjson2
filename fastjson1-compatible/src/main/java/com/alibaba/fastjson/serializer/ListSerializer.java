package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson2.JSONWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ListSerializer
        implements ObjectSerializer {
    public static final ListSerializer instance = new ListSerializer();

    private ListSerializer() {
    }

    public final void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
            throws IOException {
        JSONWriter out = serializer.out.raw;

        if (object == null) {
            out.writeArrayNull();
            return;
        }

        List<?> list = (List<?>) object;
        out.write(list);
    }

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
        JSONWriter out = serializer.out.raw;

        if (object == null) {
            out.writeArrayNull();
            return;
        }

        List<?> list = (List<?>) object;
        out.write(list);
    }
}
