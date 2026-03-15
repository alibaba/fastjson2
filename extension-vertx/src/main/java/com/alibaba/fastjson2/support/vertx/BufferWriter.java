package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import io.vertx.core.buffer.Buffer;

import java.lang.reflect.Type;

public class BufferWriter implements ObjectWriter<Buffer> {
    public static final BufferWriter INSTANCE = new BufferWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Buffer buffer = (Buffer) object;
        jsonWriter.writeBinary(buffer.getBytes());
    }
}
