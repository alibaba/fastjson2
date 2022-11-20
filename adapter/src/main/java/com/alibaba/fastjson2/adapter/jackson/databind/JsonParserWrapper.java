package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import com.alibaba.fastjson2.adapter.jackson.core.JsonToken;
import com.alibaba.fastjson2.adapter.jackson.core.ObjectCodec;

import java.io.IOException;
import java.math.BigDecimal;

public class JsonParserWrapper
        extends JsonParser {
    private final JSONReader jsonReader;

    public JsonParserWrapper(JSONReader jsonReader) {
        this.jsonReader = jsonReader;
    }

    @Override
    public ObjectCodec getCodec() {
        return new ObjectCodecWrapper();
    }

    @Override
    public JSONReader getRaw() {
        return jsonReader;
    }

    public boolean isClosed() {
        return jsonReader.isEnd();
    }

    public JsonToken nextToken() throws IOException {
        JsonToken token = null;
        char current = jsonReader.current();
        switch (current) {
            case '{':
                token = JsonToken.START_OBJECT;
                jsonReader.next();
                break;
            case '}':
                token = JsonToken.END_OBJECT;
                jsonReader.next();
                break;
            case '[':
                token = JsonToken.START_ARRAY;
                jsonReader.next();
                break;
            case ']':
                token = JsonToken.END_ARRAY;
                jsonReader.next();
                break;
            case '"':
                jsonReader.readString();
                if (jsonReader.current() == ':') {
                    jsonReader.next();
                    token = JsonToken.FIELD_NAME;
                } else {
                    token = JsonToken.VALUE_STRING;
                }
                break;
            case '.':
            case '+':
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                Number number = jsonReader.readNumber();
                if (number instanceof Double || number instanceof Float || number instanceof BigDecimal) {
                    token = JsonToken.VALUE_NUMBER_FLOAT;
                } else {
                    token = JsonToken.VALUE_NUMBER_INT;
                }
                break;
            case 'n':
                if (jsonReader.nextIfNull()) {
                    token = JsonToken.VALUE_NULL;
                } else {
                    throw new IOException("TODO");
                }
                break;
            case 't':
                if (!jsonReader.nextIfMatchIdent('t', 'r', 'u', 'e')) {
                    throw new IOException("TODO");
                }
                token = JsonToken.VALUE_TRUE;
                break;
            case 'f':
                if (!jsonReader.nextIfMatchIdent('f', 'a', 'l', 's', 'e')) {
                    throw new IOException("TODO");
                }
                token = JsonToken.VALUE_FALSE;
                break;
            default:
                throw new IOException("TODO");
        }

        return token;
    }
}
