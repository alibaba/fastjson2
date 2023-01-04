package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;

public interface ObjectDeserializer
        extends ObjectReader {
    @Override
    default Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        DefaultJSONParser parser = new DefaultJSONParser(jsonReader, ParserConfig.global);
        return deserialze(parser, fieldType, fieldName);
    }

    <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName);

    default int getFastMatchToken() {
        return JSONLexer.UNKNOWN;
    }
}
