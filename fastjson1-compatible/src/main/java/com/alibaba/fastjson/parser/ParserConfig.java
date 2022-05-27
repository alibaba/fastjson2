package com.alibaba.fastjson.parser;

import com.alibaba.fastjson2.JSONFactory;

public class ParserConfig {
    public static ParserConfig global = new ParserConfig();

    public static ParserConfig getGlobalInstance() {
        return global;
    }

    public Class<?> checkAutoType(Class type) {
        return JSONFactory.getDefaultObjectReaderProvider().checkAutoType(type.getName(), null, 0);
    }
}
