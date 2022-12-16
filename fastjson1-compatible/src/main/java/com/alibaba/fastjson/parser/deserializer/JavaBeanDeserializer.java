package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.Type;

public class JavaBeanDeserializer
        implements ObjectDeserializer {
    final ObjectReader objectReader;

    public JavaBeanDeserializer(ParserConfig config, Class<?> clazz, Type type) {
        if (type == null) {
            type = clazz;
        }

        if (config == null) {
            config = ParserConfig.global;
        }

        objectReader = config.getProvider().getObjectReader(type);
    }

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return (T) objectReader.readObject(parser.getRawReader(), type, fieldName, 0L);
    }
}
