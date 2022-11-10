package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;

final class ObjectDeserializerWrapper
        implements ObjectDeserializer {
    private final ObjectReader raw;

    ObjectDeserializerWrapper(ObjectReader raw) {
        this.raw = raw;
    }

    @Deprecated
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return (T) raw.readObject(parser.getRawReader(), type, fieldName, 0);
    }
}
