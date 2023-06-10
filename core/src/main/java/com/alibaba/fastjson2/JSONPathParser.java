package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson2.JSONReader.EOI;

class JSONPathParser {
    final String path;
    final JSONReader jsonReader;

    boolean dollar;
    boolean lax;
    boolean strict;

    int segmentIndex;
    JSONPathSegment first;
    JSONPathSegment second;

    List<JSONPathSegment> segments;

    boolean negative;

    public JSONPathParser(String str) {
        this.jsonReader = JSONReader.of(this.path = str, JSONPath.PARSE_CONTEXT);

        if (jsonReader.ch == 'l' && jsonReader.nextIfMatchIdent('l', 'a', 'x')) {
            lax = true;
        } else if (jsonReader.ch == 's' && jsonReader.nextIfMatchIdent('s', 't', 'r', 'i', 'c', 't')) {
            strict = true;
        }

        if (jsonReader.ch == '-') {
            jsonReader.next();
            negative = true;
        }

        if (jsonReader.ch == '$') {
            jsonReader.next();
            dollar = true;
        }
    }

    JSONPath parse() {
        if (dollar && jsonReader.ch == EOI) {
            if (negative) {
                throw new JSONException("not support '-'");
            } else {
                return JSONPath.RootPath.INSTANCE;
            }
        }

        while (jsonReader.ch != EOI) {
            final JSONPathSegment segment;

            char ch = jsonReader.ch;
            if (ch == '.') {
                jsonReader.next();
                segment = parseProperty();
            } else if (jsonReader.ch == '[') {
                segment = parseArrayAccess();
            } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
                segment = parseProperty();
            } else if (ch == '?') {
                throw new JSONException("not support filter '?'");
            } else if (ch == '@') {
                jsonReader.next();
                segment = JSONPathSegment.SelfSegment.INSTANCE;
            } else {
                throw new JSONException("not support " + ch);
            }

            if (segmentIndex == 0) {
                first = segment;
            } else if (segmentIndex == 1) {
                second = segment;
            } else if (segmentIndex == 2) {
                segments = new ArrayList<>();
                segments.add(first);
                segments.add(second);
                segments.add(segment);
            } else {
                segments.add(segment);
            }
            segmentIndex++;
        }

        if (negative) {
            throw new JSONException("not support '-'");
        }

        if (segmentIndex == 1) {
            if (first instanceof JSONPathSegmentName) {
                return new JSONPathSingleName(path, (JSONPathSegmentName) first);
            }

            if (first instanceof JSONPathSegmentIndex) {
                JSONPathSegmentIndex firstIndex = (JSONPathSegmentIndex) first;
                if (firstIndex.index >= 0) {
                    return new JSONPathSingleIndex(path, firstIndex);
                }
            }

            return new JSONPathSingle(first, path);
        }

        if (segmentIndex == 2) {
            return new JSONPathTwoSegment(path, first, second);
        }

        return new JSONPathMulti(path, segments);
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
