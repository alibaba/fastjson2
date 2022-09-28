package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.lang.reflect.Type;

public class ParserConfig {
    public static ParserConfig global = new ParserConfig();

    public static ParserConfig getGlobalInstance() {
        return global;
    }

    private ObjectReaderProvider provider;

    public ObjectReaderProvider getProvider() {
        ObjectReaderProvider provider = this.provider;
        if (provider == null) {
            provider = JSONFactory.getDefaultObjectReaderProvider();
        }
        return provider;
    }

    public void putDeserializer(Type type, ObjectDeserializer deserializer) {
        getProvider().register(type, deserializer);
    }

    public Class<?> checkAutoType(Class type) {
        return JSONFactory.getDefaultObjectReaderProvider().checkAutoType(type.getName(), null, 0);
    }
}
