package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class MapDeserializer
        implements ObjectDeserializer {
    public static final MapDeserializer instance = new MapDeserializer();

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return (T) parser.getRawReader().read(Map.class);
    }

    public static Map parseMap(
            DefaultJSONParser parser,
            Map<String, Object> map,
            Type valueType,
            Object fieldName,
            int features
    ) {
        ParameterizedType parameterizedType = new ParameterizedTypeImpl(Map.class, String.class, valueType);
        JSONReader reader = parser.getLexer().getReader();
        ObjectReader objectReader = reader.getObjectReader(parameterizedType);
        Map<String, Object> object = (Map<String, Object>) objectReader.readObject(reader, parameterizedType, fieldName, 0L);
        map.putAll(object);
        return map;
    }

    public static Map parseMap(DefaultJSONParser parser, Map<String, Object> map, Type valueType, Object fieldName) {
        return parseMap(parser, map, valueType, fieldName, 0);
    }

    public static Object parseMap(
            DefaultJSONParser parser,
            Map<Object, Object> map,
            Type keyType,
            Type valueType,
            Object fieldName
    ) {
        JSONReader jsonReader = parser.getRawReader();
        jsonReader.read(map, keyType, valueType, 0L);
        return map;
    }
}
