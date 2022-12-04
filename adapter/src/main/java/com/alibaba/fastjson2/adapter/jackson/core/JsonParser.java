package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.adapter.jackson.core.type.TypeReference;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectCodecWrapper;
import com.alibaba.fastjson2.adapter.jackson.databind.node.ObjectNode;
import com.alibaba.fastjson2.adapter.jackson.databind.node.TreeNodeUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;

public class JsonParser
        implements Closeable {
    protected final JSONReader jsonReader;
    protected String stringValue;
    protected Number number;
    protected JsonToken token;

    public JsonParser(JSONReader jsonReader) {
        this.jsonReader = jsonReader;
    }

    public JSONReader getJSONReader() {
        return jsonReader;
    }

    public boolean isClosed() {
        return jsonReader.isEnd();
    }

    public ObjectCodec getCodec() {
        return new ObjectCodecWrapper();
    }

    public JsonLocation getCurrentLocation() {
        // TODO getCurrentLocation
        return new JsonLocation();
    }

    public JsonToken nextValue() throws IOException {
        return nextToken();
    }

    public <T> T readValueAs(Class<T> valueType) throws IOException {
        return jsonReader.read(valueType);
    }

    public <T> T readValueAs(TypeReference<?> valueTypeRef) throws IOException {
        return jsonReader.read(valueTypeRef.getType());
    }

    public <T extends TreeNode> T readValueAsTree() throws IOException {
        if (token == JsonToken.START_OBJECT) {
            JSONObject jsonObject = new JSONObject();
            jsonReader.setTypeRedirect(true);
            jsonReader.readObject(jsonObject, 0L);
            return (T) new ObjectNode(jsonObject);
        }

        Object any = jsonReader.readAny();
        return (T) TreeNodeUtils.as(any);
    }

    public byte[] getBinaryValue() throws IOException {
        if (token == JsonToken.START_OBJECT) {
            stringValue = jsonReader.readString();
            jsonReader.nextIfMatch(':');
        } else if (stringValue == null) {
            char ch = jsonReader.current();
            if (ch == '"') {
                stringValue = jsonReader.readString();
            }
        }

        String str = stringValue;
        stringValue = null;
        return Base64.getDecoder().decode(str);
    }

    public String getValueAsString() {
        if (token == JsonToken.START_OBJECT) {
            stringValue = jsonReader.readString();
            jsonReader.nextIfMatch(':');
        } else if (stringValue == null) {
            char ch = jsonReader.current();
            if (ch == '"') {
                stringValue = jsonReader.readString();
            }
        }

        String str = stringValue;
        stringValue = null;
        return str;
    }

    public long getLongValue() {
        long value = number.longValue();
        number = null;
        return value;
    }

    public double getDoubleValue() {
        double value = number.doubleValue();
        number = null;
        return value;
    }

    public float getFloatValue() {
        float value = number.floatValue();
        number = null;
        return value;
    }

    public BigDecimal getDecimalValue() {
        BigDecimal decimal = TypeUtils.toBigDecimal(number);
        number = null;
        return decimal;
    }

    public NumberType getNumberType() throws IOException {
        throw new JSONException("TODO");
    }

    public JsonToken nextToken() throws IOException {
        char current = jsonReader.current();
        if (current == ':' || current == ',') {
            jsonReader.next();
            current = jsonReader.current();
        }

        switch (current) {
            case '{':
                token = JsonToken.START_OBJECT;
                jsonReader.next();
                break;
            case '}':
                token = JsonToken.END_OBJECT;
                jsonReader.next();
                jsonReader.nextIfMatch(',');
                break;
            case '[':
                token = JsonToken.START_ARRAY;
                jsonReader.next();
                break;
            case ']':
                token = JsonToken.END_ARRAY;
                jsonReader.next();
                jsonReader.nextIfMatch(',');
                break;
            case '"':
                stringValue = jsonReader.readString();
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
                number = jsonReader.readNumber();
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
                throw new IOException("TODO " + current);
        }

        return token;
    }

    @Override
    public void close() throws IOException {
        jsonReader.close();
    }

    public enum Feature {
        AUTO_CLOSE_SOURCE(true),
        ALLOW_COMMENTS(false),
        ALLOW_YAML_COMMENTS(false),
        ALLOW_UNQUOTED_FIELD_NAMES(false),
        ALLOW_SINGLE_QUOTES(false),
        @Deprecated
        ALLOW_UNQUOTED_CONTROL_CHARS(false),
        @Deprecated
        ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER(false),
        @Deprecated
        ALLOW_NUMERIC_LEADING_ZEROS(false),
        @Deprecated
        ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS(false),
        @Deprecated
        ALLOW_NON_NUMERIC_NUMBERS(false),
        @Deprecated
        ALLOW_MISSING_VALUES(false),
        @Deprecated
        ALLOW_TRAILING_COMMA(false),
        STRICT_DUPLICATE_DETECTION(false),
        IGNORE_UNDEFINED(false),
        INCLUDE_SOURCE_IN_LOCATION(true);

        /**
         * Whether feature is enabled or disabled by default.
         */
        private final boolean defaultState;

        private final int mask;

        /**
         * Method that calculates bit set (flags) of all features that
         * are enabled by default.
         *
         * @return Bit mask of all features that are enabled by default
         */
        public static int collectDefaults() {
            int flags = 0;
            for (Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }

        Feature(boolean defaultState) {
            mask = (1 << ordinal());
            this.defaultState = defaultState;
        }

        public boolean enabledByDefault() {
            return defaultState;
        }

        public boolean enabledIn(int flags) {
            return (flags & mask) != 0;
        }

        public int getMask() {
            return mask;
        }
    }

    public enum NumberType {
        INT, LONG, BIG_INTEGER, FLOAT, DOUBLE, BIG_DECIMAL
    }

    public boolean nextFieldName(SerializedString str) throws IOException {
        throw new JSONException("TODO");
    }

    public boolean hasCurrentToken() {
        throw new JSONException("TODO");
    }

    public JsonToken getCurrentToken() {
        throw new JSONException("TODO");
    }

    public JsonParser skipChildren() throws IOException {
        throw new JSONException("TODO");
    }

    public String getText() throws IOException {
        throw new JSONException("TODO");
    }

    public byte[] getBinaryValue(Base64Variant bv) throws IOException {
        throw new JSONException("TODO");
    }

    public JsonToken currentToken() {
        return getCurrentToken();
    }
}
