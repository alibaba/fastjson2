package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import io.vertx.core.buffer.Buffer;

import java.lang.reflect.Type;

public class BufferReader implements ObjectReader<Buffer> {

    public static final BufferReader INSTANCE = new BufferReader();

    @Override
    public Buffer readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return Buffer.buffer(jsonReader.readBase64());
    }
}
