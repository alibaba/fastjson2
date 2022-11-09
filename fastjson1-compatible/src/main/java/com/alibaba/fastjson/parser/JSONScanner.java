package com.alibaba.fastjson.parser;

import com.alibaba.fastjson2.JSONReader;

public class JSONScanner
        extends JSONLexerBase {
    private final JSONReader reader;

    public JSONScanner(JSONReader reader) {
        this.reader = reader;
    }

    @Override
    public JSONReader getReader() {
        return reader;
    }
}
