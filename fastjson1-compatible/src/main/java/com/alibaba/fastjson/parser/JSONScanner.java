package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson2.JSONReader;

public class JSONScanner
        extends JSONLexerBase {
    private final JSONReader reader;
    private boolean orderedField;

    public JSONScanner(JSONReader reader) {
        this.reader = reader;
    }

    public JSONScanner(String str) {
        this.reader = JSONReader.of(str);
    }

    public JSONScanner(String str, int features) {
        this.reader = JSONReader.of(str, JSON.createReadContext(features));
    }

    @Override
    public JSONReader getReader() {
        return reader;
    }

    public boolean isOrderedField() {
        return orderedField;
    }

    public String stringVal() {
        return reader.getString();
    }

    public void config(Feature feature, boolean state) {
        JSONReader.Feature rawFeature = null;

        boolean not = false;
        switch (feature) {
            case AllowUnQuotedFieldNames:
                rawFeature = JSONReader.Feature.AllowUnQuotedFieldNames;
                break;
            case SupportArrayToBean:
                rawFeature = JSONReader.Feature.SupportArrayToBean;
                break;
            case DisableFieldSmartMatch:
                rawFeature = JSONReader.Feature.SupportSmartMatch;
                not = true;
                break;
            case SupportAutoType:
                rawFeature = JSONReader.Feature.SupportAutoType;
                break;
            case NonStringKeyAsString:
                rawFeature = JSONReader.Feature.NonStringKeyAsString;
                break;
            case ErrorOnEnumNotMatch:
                rawFeature = JSONReader.Feature.ErrorOnEnumNotMatch;
                break;
            case SupportClassForName:
                rawFeature = JSONReader.Feature.SupportClassForName;
                break;
            case ErrorOnNotSupportAutoType:
                rawFeature = JSONReader.Feature.ErrorOnNotSupportAutoType;
                break;
            case UseNativeJavaObject:
                rawFeature = JSONReader.Feature.UseNativeObject;
                break;
            case OrderedField:
                orderedField = state;
                break;
            default:
                break;
        }

        if (rawFeature == null) {
            return;
        }

        if (not) {
            state = !state;
        }

        JSONReader.Context context = reader.getContext();
        context.config(rawFeature, state);
    }

    public boolean isBlankInput() {
        return reader.isEnd();
    }

    @Override
    public int intValue() {
        return reader.getInt32Value();
    }

    @Override
    public long longValue() {
        return reader.getInt64Value();
    }

    public final void nextToken() {
        char ch = reader.current();
        switch (ch) {
            case '[':
            case ']':
            case '{':
            case '}':
            case ':':
                reader.next();
                break;
            default:
                break;
        }

        if (reader.nextIfNull()) {
            return;
        }

        throw new JSONException("not support operation");
    }

    public final void nextToken(int expect) {
        boolean match = true;
        switch (expect) {
            case JSONToken.COLON:
                match = reader.nextIfMatch(':');
                break;
            case JSONToken.LBRACE:
                match = reader.nextIfMatch('{');
                break;
            case JSONToken.LBRACKET:
                match = reader.nextIfMatch('[');
                break;
            case JSONToken.RBRACE:
                match = reader.nextIfMatch('}');
                break;
            case JSONToken.RBRACKET:
                match = reader.nextIfMatch(']');
                break;
            case JSONToken.SET:
                match = reader.nextIfSet();
                break;
            case JSONToken.NULL:
                match = reader.nextIfNull();
                break;
            default:
                throw new JSONException("not support operation");
        }

        if (!match) {
            throw new JSONException("not support operation");
        }
    }
}
