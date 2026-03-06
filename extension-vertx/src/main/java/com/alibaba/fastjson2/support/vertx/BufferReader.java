package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import io.vertx.core.buffer.Buffer;

import java.lang.reflect.Type;
import java.util.Base64;

public class BufferReader implements ObjectReader<Buffer> {
    public static final BufferReader INSTANCE = new BufferReader();

    @Override
    public Buffer readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        String base64Str = jsonReader.readString();
        return base64Str == null ? null : Buffer.buffer(Base64.getUrlDecoder().decode(base64Str));
    }
}
