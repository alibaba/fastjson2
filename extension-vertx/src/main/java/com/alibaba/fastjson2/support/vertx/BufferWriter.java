package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import io.vertx.core.buffer.Buffer;

import java.lang.reflect.Type;
import java.util.Base64;

public class BufferWriter implements ObjectWriter<Buffer> {
    public static final BufferWriter INSTANCE = new BufferWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Buffer buffer = (Buffer) object;
        String base64Str = Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.getBytes()); // 无填充的 Base64
        jsonWriter.writeString(base64Str);
    }
}
