package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;

import java.lang.reflect.Type;
import java.util.Map;

public class MapDeserializer
        implements ObjectDeserializer {
    public static MapDeserializer instance = new MapDeserializer();

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return (T) parser.getRawReader().read(Map.class);
    }
}
