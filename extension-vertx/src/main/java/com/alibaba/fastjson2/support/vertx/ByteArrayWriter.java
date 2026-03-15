package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;

public class ByteArrayWriter implements ObjectWriter<byte[]> {
    public static final ByteArrayWriter INSTANCE = new ByteArrayWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeBinary((byte[]) object);
    }
}
