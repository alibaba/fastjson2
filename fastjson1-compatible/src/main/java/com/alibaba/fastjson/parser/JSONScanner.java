package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.math.BigDecimal;

public class JSONScanner
        extends JSONLexerBase {
    private final JSONReader reader;
    private boolean orderedField;

    protected int token;
    private String strVal;

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

    @Override
    public String stringVal() {
        return strVal;
    }

    public BigDecimal decimalValue() {
        return reader.getBigDecimal();
    }

    public int token() {
        return token;
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
            case UseBigDecimal:
                rawFeature = JSONReader.Feature.UseBigDecimalForDoubles;
                not = true;
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

    public boolean isEnabled(Feature feature) {
        JSONReader.Feature rawFeature = null;

        switch (feature) {
            case AllowUnQuotedFieldNames:
                rawFeature = JSONReader.Feature.AllowUnQuotedFieldNames;
                break;
            case SupportArrayToBean:
                rawFeature = JSONReader.Feature.SupportArrayToBean;
                break;
            case DisableFieldSmartMatch:
                return !reader.isEnabled(JSONReader.Feature.SupportSmartMatch);
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
            case UseBigDecimal:
                return !reader.isEnabled(JSONReader.Feature.UseBigDecimalForDoubles);
            default:
                break;
        }

        if (rawFeature == null) {
            return true;
        }

        return reader.isEnabled(rawFeature);
    }

    @Override
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
        strVal = null;
        char ch = reader.current();
        switch (ch) {
            case '[':
                reader.next();
                token = JSONToken.LBRACKET;
                return;
            case ']':
                reader.next();
                token = JSONToken.RBRACKET;
                return;
            case '{':
                reader.next();
                token = JSONToken.LBRACE;
                return;
            case '}':
                reader.next();
                token = JSONToken.RBRACE;
                return;
            case ':':
                reader.next();
                token = JSONToken.COLON;
                return;
            case '"':
            case '\'':
                strVal = reader.readString();
                token = JSONToken.LITERAL_STRING;
                return;
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
            case '-':
            case '+':
                Number number = reader.readNumber();
                if (number instanceof BigDecimal || number instanceof Float || number instanceof Double) {
                    token = JSONToken.LITERAL_FLOAT;
                } else {
                    token = JSONToken.LITERAL_INT;
                }
                return;
            case 't':
            case 'f':
                boolean boolValue = reader.readBoolValue();
                token = boolValue ? JSONToken.TRUE : JSONToken.FALSE;
                return;
            case 'n':
                reader.readNull();
                token = JSONToken.NULL;
                return;
            case EOI:
                token = JSONToken.EOF;
                return;
            default:
                break;
        }

        if (reader.nextIfNull()) {
            return;
        }

        throw new JSONException("not support operation");
    }

    @Override
    public char getCurrent() {
        return reader.current();
    }

    @Override
    public final void nextToken(int expect) {
        nextToken();
    }

    @Override
    public boolean isEOF() {
        return reader.isEnd();
    }
}
