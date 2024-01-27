package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson2.JSONReader.EOI;

class JSONPathParser {
    final String path;
    final JSONReader jsonReader;

    final boolean dollar;

    public JSONPathParser(String str) {
        this.jsonReader = JSONReader.of(this.path = str, JSONPath.PARSE_CONTEXT);

        if (jsonReader.ch == '-') {
            throw new JSONException("not support '-'");
        }

        if (jsonReader.ch == '$') {
            jsonReader.next();
            dollar = true;
        } else {
            this.dollar = false;
        }
    }

    JSONPath parse() {
        if (dollar && jsonReader.ch == EOI) {
            return JSONPath.ROOT;
        }

        List<JSONPathSegment> segments = new ArrayList<>();
        while (jsonReader.ch != EOI) {
            final JSONPathSegment segment;

            char ch = jsonReader.ch;
            if (ch == '.') {
                jsonReader.next();
                segment = parseProperty();
            } else if (ch == '[') {
                segment = parseArrayAccess();
            } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
                segment = parseProperty();
            } else if (ch == '@') {
                jsonReader.next();
                segment = JSONPathSegment.SelfSegment.INSTANCE;
            } else {
                throw new JSONException("not support " + ch);
            }

            segments.add(segment);
        }

        return new JSONPath(path, segments, false, false);
    }

    private JSONPathSegment parseArrayAccess() {
        jsonReader.next();

        JSONPathSegment segment;
        switch (jsonReader.ch) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9': {
                int index = jsonReader.readInt32Value();
                if (jsonReader.ch == ':') {
                    throw new JSONException("not support range index ':'");
                } else if (jsonReader.isNumber()) {
                    throw new JSONException("not support");
                } else {
                    segment = JSONPathSegmentIndex.of(index);
                }
                break;
            }
            case '*':
                throw new JSONException("not support *");
            case ':': {
                throw new JSONException("not support range index ':'");
            }
            case '"':
            case '\'':
                String name = jsonReader.readString();
                if (jsonReader.current() == ']') {
                    segment = new JSONPathSegmentName(name, Fnv.hashCode64(name));
                } else if (jsonReader.isString()) {
                    throw new JSONException("not support multi name");
                } else {
                    throw new JSONException("TODO : " + jsonReader.current());
                }
                break;
            default:
                throw new JSONException("TODO : " + jsonReader.current());
        }

        if (!jsonReader.nextIfArrayEnd()) {
            throw new JSONException(jsonReader.info("jsonpath syntax error"));
        }

        return segment;
    }

    private JSONPathSegment parseProperty() {
        final JSONPathSegment segment;
        if (jsonReader.ch == '*') {
            throw new JSONException("not support *");
        } else if (jsonReader.ch == '.') {
            throw new JSONException("not support jsonpath ..");
        } else {
            long hashCode = jsonReader.readFieldNameHashCodeUnquote();
            String name = jsonReader.getFieldName();
            if (jsonReader.ch == '(') {
                throw new JSONException("not support jsonpath function");
            } else {
                segment = new JSONPathSegmentName(name, hashCode);
            }
        }
        return segment;
    }
}
