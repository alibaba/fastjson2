package com.alibaba.fastjson.parser;

import java.lang.reflect.Type;

public interface ObjectDeserializer {
    @Deprecated
    <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName);

    @Deprecated
    default int getFastMatchToken() {
        return 0;
    }
}
