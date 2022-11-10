package com.alibaba.fastjson.parser;

import com.alibaba.fastjson2.JSONReader;

public class JSONScanner
        extends JSONLexerBase {
    private final JSONReader reader;

    public JSONScanner(JSONReader reader) {
        this.reader = reader;
    }

    public JSONScanner(String str) {
        this.reader = JSONReader.of(str);
    }

    @Override
    public JSONReader getReader() {
        return reader;
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
}
