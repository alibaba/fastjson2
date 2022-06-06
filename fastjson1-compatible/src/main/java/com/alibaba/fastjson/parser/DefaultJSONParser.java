package com.alibaba.fastjson.parser;

import com.alibaba.fastjson2.JSONReader;

import java.io.Closeable;
import java.lang.reflect.Type;
import java.util.List;

public class DefaultJSONParser
        implements Closeable {
    private final JSONReader reader;
    private final ParserConfig config;
    private final JSONScanner lexer;

    public DefaultJSONParser(String text) {
        this(JSONReader.of(text), ParserConfig.global);
    }

    public ParserConfig getConfig() {
        return config;
    }

    public DefaultJSONParser(String text, ParserConfig config) {
        this(JSONReader.of(text), config);
    }

    public DefaultJSONParser(JSONReader reader, ParserConfig config) {
        this.reader = reader;
        this.config = config;
        this.lexer = new JSONScanner(this.reader);
    }

    public JSONLexer getLexer() {
        return lexer;
    }

    public JSONReader getRawReader() {
        return reader;
    }

    public Object parse() {
        return reader.readAny();
    }

    public <T> List<T> parseArray(Class<T> clazz) {
        return reader.readArray(clazz);
    }

    public <T> T parseObject(Class<T> clazz) {
        return reader.read(clazz);
    }

    public <T> T parseObject(Type type) {
        return reader.read(type);
    }

    @Override
    public void close() {
        this.reader.close();
    }
}
