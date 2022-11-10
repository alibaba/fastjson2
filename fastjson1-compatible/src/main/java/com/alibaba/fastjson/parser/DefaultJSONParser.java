package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONToken;
import com.alibaba.fastjson2.JSONReader;

import java.io.Closeable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class DefaultJSONParser
        implements Closeable {
    private final JSONReader reader;
    private final ParserConfig config;
    private final JSONScanner lexer;

    public DefaultJSONParser(String text) {
        this(JSONReader.of(text), ParserConfig.global);
    }

    public DefaultJSONParser(final Object input, final JSONLexer lexer, final ParserConfig config) {
        this.lexer = (JSONScanner) lexer;
        this.reader = lexer.getReader();
        this.config = config;
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

    public void parseArray(Type type, Collection array) {
        reader.readArray(array, type);
    }

    public void parseArray(Class<?> clazz, @SuppressWarnings("rawtypes") Collection array) {
        reader.readArray(array, clazz);
    }

    public Object[] parseArray(Type[] types) {
        return reader.readArray(types);
    }

    public final void parseArray(final Collection array) {
        reader.readArray(array, Object.class);
    }

    public <T> T parseObject(Class<T> clazz) {
        return reader.read(clazz);
    }

    public <T> T parseObject(Type type) {
        return reader.read(type);
    }

    public void parseObject(Object object) {
        reader.readObject(object);
    }

    @Deprecated
    public Object parse(Object fieldName) {
        return reader.readAny();
    }

    @Deprecated
    public void handleResovleTask(Object value) {
        reader.handleResolveTasks(value);
    }

    public void handleResolveTasks(Object value) {
        reader.handleResolveTasks(value);
    }

    public final void accept(final int token) {
        char expect;
        switch (token) {
            case JSONToken.DOT:
                expect = '.';
                break;
            case JSONToken.LBRACE:
                expect = '{';
                break;
            case JSONToken.RBRACE:
                expect = '}';
                break;
            case JSONToken.LBRACKET:
                expect = '[';
                break;
            case JSONToken.RBRACKET:
                expect = ']';
                break;
            case JSONToken.LPAREN:
                expect = '(';
                break;
            case JSONToken.RPAREN:
                expect = ')';
                break;
            case JSONToken.COMMA:
                expect = ',';
                break;
            case JSONToken.COLON:
                expect = ':';
                break;
            case JSONToken.TRUE:
                if (reader.nextIfMatchIdent('t', 'r', 'u', 'e')) {
                    throw new JSONException(
                            "syntax error, expect true, actual " + reader.current()
                    );
                }
                return;
            case JSONToken.FALSE:
                if (reader.nextIfMatchIdent('f', 'a', 'l', 's', 'e')) {
                    throw new JSONException(
                            "syntax error, expect false, actual " + reader.current()
                    );
                }
                return;
            case JSONToken.NULL:
                if (reader.nextIfNull()) {
                    throw new JSONException(
                            "syntax error, expect false, actual " + reader.current()
                    );
                }
                return;
            case JSONToken.SET:
                if (reader.nextIfSet()) {
                    throw new JSONException(
                            "syntax error, expect set, actual " + reader.current()
                    );
                }
                return;
            default:
                throw new JSONException("not support accept token " + JSONToken.name(token));
        }

        if (reader.nextIfMatch(expect)) {
            throw new JSONException(
                    "syntax error, expect " + JSONToken.name(token) + ", actual " + reader.current()
            );
        }
    }

    public JSONObject parseObject() {
        if (reader.nextIfNull()) {
            return null;
        }

        JSONObject object = new JSONObject(lexer.isOrderedField());
        reader.read(object, 0L);
        return object;
    }

    public void config(Feature feature, boolean state) {
        lexer.config(feature, state);
    }

    @Override
    public void close() {
        this.reader.close();
    }
}
