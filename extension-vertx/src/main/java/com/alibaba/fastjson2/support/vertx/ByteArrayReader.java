package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;

public class ByteArrayReader implements ObjectReader<byte[]> {
    public static final ByteArrayReader INSTANCE = new ByteArrayReader();

    @Override
    public byte[] readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return jsonReader.readBinary();
    }
}
