package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson2.JSONReader;

import java.io.IOException;
import java.lang.reflect.Type;

public class StringCodec
        implements ObjectSerializer {
    public static final StringCodec instance = new StringCodec();
    public static <T> T deserialze(DefaultJSONParser parser) {
        return (T) parser.getRawReader().readString();
    }

    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        JSONReader reader = parser.getRawReader();
        String str = reader.readString();

        if (clazz == StringBuffer.class) {
            return (T) new StringBuffer(str);
        }

        if (clazz == StringBuilder.class) {
            return (T) new StringBuilder(str);
        }

        return (T) str;
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        out.writeString((String) object);
    }
}
