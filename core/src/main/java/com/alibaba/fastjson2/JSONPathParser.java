package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

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

    JSONPath parse(JSONPath.Feature... features) {
        if (dollar && jsonReader.ch == EOI) {
            if (negative) {
                return new JSONPathSingle(JSONPathFunction.FUNC_NEGATIVE, path);
            } else {
                return JSONPath.RootPath.INSTANCE;
            }
        }

        if (jsonReader.ch == 'e' && jsonReader.nextIfMatchIdent('e', 'x', 'i', 's', 't', 's')) {
            if (!jsonReader.nextIfMatch('(')) {
                throw new JSONException("syntax error " + path);
            }

            if (jsonReader.ch == '@') {
                jsonReader.next();
                if (!jsonReader.nextIfMatch('.')) {
                    throw new JSONException("syntax error " + path);
                }
            }

            char ch = jsonReader.ch;
            JSONPathSegment segment;
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_' || ch == '@') {
                segment = parseProperty();
            } else {
                throw new JSONException("syntax error " + path);
            }

            if (!jsonReader.nextIfMatch(')')) {
                throw new JSONException("syntax error " + path);
            }

            return new JSONPathTwoSegment(path, segment, JSONPathFunction.FUNC_EXISTS);
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
                if (dollar && segmentIndex == 0) {
                    first = JSONPathSegment.RootSegment.INSTANCE;
                    segmentIndex++;
                }
                jsonReader.next();
                segment = parseFilter();
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
            if (segmentIndex == 1) {
                second = JSONPathFunction.FUNC_NEGATIVE;
            } else if (segmentIndex == 2) {
                segments = new ArrayList<>();
                segments.add(first);
                segments.add(second);
                segments.add(JSONPathFunction.FUNC_NEGATIVE);
            } else {
                segments.add(JSONPathFunction.FUNC_NEGATIVE);
            }
            segmentIndex++;
        }

        if (segmentIndex == 1) {
            if (first instanceof JSONPathSegmentName) {
                return new JSONPathSingleName(path, (JSONPathSegmentName) first, features);
            }

            if (first instanceof JSONPathSegmentIndex) {
                JSONPathSegmentIndex firstIndex = (JSONPathSegmentIndex) first;
                if (firstIndex.index >= 0) {
                    return new JSONPathSingleIndex(path, firstIndex, features);
                }
            }

            return new JSONPathSingle(first, path, features);
        }

        if (segmentIndex == 2) {
            return new JSONPathTwoSegment(path, first, second, features);
        }

        return new JSONPathMulti(path, segments, features);
    }

    private JSONPathSegment parseArrayAccess() {
        jsonReader.next();

        JSONPathSegment segment;
        switch (jsonReader.ch) {
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
            case '9': {
                int index = jsonReader.readInt32Value();
                boolean last = false;
                if (jsonReader.ch == ':') {
                    jsonReader.next();
                    if (jsonReader.ch == ']') {
                        segment = new JSONPathSegment.RangeIndexSegment(index, index >= 0 ? Integer.MAX_VALUE : 0);
                    } else {
                        int end = jsonReader.readInt32Value();
                        segment = new JSONPathSegment.RangeIndexSegment(index, end);
                    }
                } else if (jsonReader.isNumber() || (last = jsonReader.nextIfMatchIdent('l', 'a', 's', 't'))) {
                    List<Integer> list = new ArrayList<>();
                    list.add(index);
                    if (last) {
                        list.add(-1);
                        jsonReader.nextIfMatch(',');
                    }

                    while (true) {
                        if (jsonReader.isNumber()) {
                            index = jsonReader.readInt32Value();
                            list.add(index);
                        } else if (jsonReader.nextIfMatchIdent('l', 'a', 's', 't')) {
                            list.add(-1);
                            jsonReader.nextIfMatch(',');
                        } else {
                            break;
                        }
                    }

                    int[] indics = new int[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        indics[i] = list.get(i);
                    }
                    segment = new JSONPathSegment.MultiIndexSegment(indics);
                } else {
                    segment = JSONPathSegmentIndex.of(index);
                }
                break;
            }
            case '*':
                jsonReader.next();
                segment = JSONPathSegment.AllSegment.INSTANCE_ARRAY;
                break;
            case ':': {
                jsonReader.next();
                int end = jsonReader.ch == ']' ? 0 : jsonReader.readInt32Value();

                if (end > 0) {
                    segment = new JSONPathSegment.RangeIndexSegment(0, end);
                } else {
                    segment = new JSONPathSegment.RangeIndexSegment(Integer.MIN_VALUE, end);
                }
                break;
            }
            case '"':
            case '\'':
                String name = jsonReader.readString();
                if (jsonReader.current() == ']') {
                    segment = new JSONPathSegmentName(name, Fnv.hashCode64(name));
                } else if (jsonReader.isString()) {
                    List<String> names = new ArrayList<>();
                    names.add(name);
                    do {
                        names.add(jsonReader.readString());
                    } while (jsonReader.isString());
                    String[] nameArray = new String[names.size()];
                    names.toArray(nameArray);
                    segment = new JSONPathSegment.MultiNameSegment(nameArray);
                } else {
                    throw new JSONException("TODO : " + jsonReader.current());
                }
                break;
            case '?':
                jsonReader.next();
                segment = parseFilter();
                break;
            case 'r': {
                String fieldName = jsonReader.readFieldNameUnquote();
                if ("randomIndex".equals(fieldName)) {
                    if (!jsonReader.nextIfMatch('(')
                            || !jsonReader.nextIfMatch(')')
                            || !(jsonReader.ch == (']'))) {
                        throw new JSONException("not support : " + fieldName);
                    }
                    segment = JSONPathSegment.RandomIndexSegment.INSTANCE;
                    break;
                }
                throw new JSONException("not support : " + fieldName);
            }
            case 'l': {
                String fieldName = jsonReader.readFieldNameUnquote();
                if ("last".equals(fieldName)) {
                    segment = JSONPathSegmentIndex.of(-1);
                } else {
                    throw new JSONException("not support : " + fieldName);
                }
                break;
            }
            default:
                throw new JSONException("TODO : " + jsonReader.current());
        }

        if (!jsonReader.nextIfMatch(']')) {
            throw new JSONException(jsonReader.info("jsonpath syntax error"));
        }

        return segment;
    }

    private JSONPathSegment parseProperty() {
        final JSONPathSegment segment;
        if (jsonReader.ch == '*') {
            jsonReader.next();
            segment = JSONPathSegment.AllSegment.INSTANCE;
        } else if (jsonReader.ch == '.') {
            jsonReader.next();
            if (jsonReader.ch == '*') {
                jsonReader.next();
                segment = new JSONPathSegment.CycleNameSegment("*", Fnv.hashCode64("*"));
            } else {
                long hashCode = jsonReader.readFieldNameHashCodeUnquote();
                String name = jsonReader.getFieldName();
                segment = new JSONPathSegment.CycleNameSegment(name, hashCode);
            }
        } else {
            boolean isNum = jsonReader.isNumber();
            long hashCode = jsonReader.readFieldNameHashCodeUnquote();
            String name = jsonReader.getFieldName();
            if (isNum) {
                if (name.length() > 9) {
                    isNum = false;
                } else {
                    for (int i = 0; i < name.length(); ++i) {
                        char ch = name.charAt(i);
                        if (ch < '0' || ch > '9') {
                            isNum = false;
                            break;
                        }
                    }
                }
            }

            if (jsonReader.ch == '(') {
                switch (name) {
                    case "length":
                    case "size":
                        segment = JSONPathSegment.LengthSegment.INSTANCE;
                        break;
                    case "keys":
                        segment = JSONPathSegment.KeysSegment.INSTANCE;
                        break;
                    case "values":
                        segment = JSONPathSegment.ValuesSegment.INSTANCE;
                        break;
                    case "entrySet":
                        segment = JSONPathSegment.EntrySetSegment.INSTANCE;
                        break;
                    case "min":
                        segment = JSONPathSegment.MinSegment.INSTANCE;
                        break;
                    case "max":
                        segment = JSONPathSegment.MaxSegment.INSTANCE;
                        break;
                    case "sum":
                        segment = JSONPathSegment.SumSegment.INSTANCE;
                        break;
                    case "type":
                        segment = JSONPathFunction.FUNC_TYPE;
                        break;
                    case "floor":
                        segment = JSONPathFunction.FUNC_FLOOR;
                        break;
                    case "ceil":
                    case "ceiling":
                        segment = JSONPathFunction.FUNC_CEIL;
                        break;
                    case "double":
                        segment = JSONPathFunction.FUNC_DOUBLE;
                        break;
                    case "abs":
                        segment = JSONPathFunction.FUNC_ABS;
                        break;
                    case "lower":
                        segment = JSONPathFunction.FUNC_LOWER;
                        break;
                    case "upper":
                        segment = JSONPathFunction.FUNC_UPPER;
                        break;
                    case "trim":
                        segment = JSONPathFunction.FUNC_TRIM;
                        break;
                    case "negative":
                        segment = JSONPathFunction.FUNC_NEGATIVE;
                        break;
                    default:
                        throw new JSONException("not support syntax, path : " + path);
                }
                jsonReader.next();
                if (!jsonReader.nextIfMatch(')')) {
                    throw new JSONException("not support syntax, path : " + path);
                }
            } else {
                segment = new JSONPathSegmentName(name, hashCode);
            }
        }
        return segment;
    }

    JSONPathSegment parseFilterRest(JSONPathSegment segment) {
        boolean and;
        switch (jsonReader.ch) {
            case '&':
                jsonReader.next();
                if (!jsonReader.nextIfMatch('&')) {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }
                and = true;
                break;
            case '|':
                jsonReader.next();
                if (!jsonReader.nextIfMatch('|')) {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }
                and = false;
                break;
            case 'a':
            case 'A': {
                String fieldName = jsonReader.readFieldNameUnquote();
                if (!"and".equalsIgnoreCase(fieldName)) {
                    throw new JSONException("syntax error : " + fieldName);
                }
                and = true;
                break;
            }
            case 'o':
            case 'O': {
                String fieldName = jsonReader.readFieldNameUnquote();
                if (!"or".equalsIgnoreCase(fieldName)) {
                    throw new JSONException("syntax error : " + fieldName);
                }
                and = false;
                break;
            }
            default:
                throw new JSONException("TODO : " + jsonReader.ch);
        }

        JSONPathSegment right = parseFilter();
        if (segment instanceof JSONPathFilter.GroupFilter) {
            JSONPathFilter.GroupFilter group = (JSONPathFilter.GroupFilter) segment;
            if (group.and == and) {
                group.filters.add((JSONPathFilter) right);
                return group;
            }
        }
        List<JSONPathFilter> filters = new ArrayList<>();
        filters.add((JSONPathFilter) segment);
        filters.add((JSONPathFilter) right);
        return new JSONPathFilter.GroupFilter(filters, and);
    }

    JSONPathSegment parseFilter() {
        boolean parentheses = jsonReader.nextIfMatch('(');

        boolean at = jsonReader.ch == '@';
        if (at) {
            jsonReader.next();
        } else if (jsonReader.nextIfMatchIdent('e', 'x', 'i', 's', 't', 's')) {
            if (!jsonReader.nextIfMatch('(')) {
                throw new JSONException(jsonReader.info("exists"));
            }

            if (jsonReader.nextIfMatch('@')) {
                if (jsonReader.nextIfMatch('.')) {
                    long hashCode = jsonReader.readFieldNameHashCodeUnquote();
                    String fieldName = jsonReader.getFieldName();

                    if (jsonReader.nextIfMatch(')')) {
                        if (parentheses) {
                            if (!jsonReader.nextIfMatch(')')) {
                                throw new JSONException(jsonReader.info("jsonpath syntax error"));
                            }
                        }
                        JSONPathFilter.NameExistsFilter segment = new JSONPathFilter.NameExistsFilter(fieldName, hashCode);
                        return segment;
                    }
                }
            }

            throw new JSONException(jsonReader.info("jsonpath syntax error"));
        }

        boolean starts = jsonReader.nextIfMatchIdent('s', 't', 'a', 'r', 't', 's');
        boolean ends = (!starts) && jsonReader.nextIfMatchIdent('e', 'n', 'd', 's');
        if ((at && (starts || ends)) || (jsonReader.ch != '.' && !JSONReader.isFirstIdentifier(jsonReader.ch))) {
            if (!at) {
                throw new JSONException(jsonReader.info("jsonpath syntax error"));
            }

            JSONPathFilter.Operator operator;
            if (starts || ends) {
                jsonReader.readFieldNameHashCodeUnquote();
                String fieldName = jsonReader.getFieldName();
                if (!"with".equalsIgnoreCase(fieldName)) {
                    throw new JSONException("not support operator : " + fieldName);
                }
                operator = starts ? JSONPathFilter.Operator.STARTS_WITH : JSONPathFilter.Operator.ENDS_WITH;
            } else {
                operator = JSONPath.parseOperator(jsonReader);
            }

            JSONPathSegment segment = null;
            if (jsonReader.isNumber()) {
                Number number = jsonReader.readNumber();
                if (number instanceof Integer || number instanceof Long) {
                    segment = new JSONPathFilter.NameIntOpSegment(null, 0, null, null, null, operator, number.longValue());
                }
            } else if (jsonReader.isString()) {
                String string = jsonReader.readString();

                switch (operator) {
                    case STARTS_WITH:
                        segment = new JSONPathFilter.StartsWithSegment(null, 0, string);
                        break;
                    case ENDS_WITH:
                        segment = new JSONPathFilter.EndsWithSegment(null, 0, string);
                        break;
                    default:
                        throw new JSONException("syntax error, " + string);
                }
            }

            while (jsonReader.ch == '&' || jsonReader.ch == '|') {
                segment = parseFilterRest(segment);
            }

            if (segment != null) {
                if (parentheses) {
                    if (!jsonReader.nextIfMatch(')')) {
                        throw new JSONException(jsonReader.info("jsonpath syntax error"));
                    }
                }
                return segment;
            }

            throw new JSONException(jsonReader.info("jsonpath syntax error"));
        }

        if (at) {
            jsonReader.next();
        }

        long hashCode = jsonReader.readFieldNameHashCodeUnquote();
        String fieldName = jsonReader.getFieldName();

        if (parentheses) {
            if (jsonReader.nextIfMatch(')')) {
                JSONPathFilter.NameExistsFilter segment = new JSONPathFilter.NameExistsFilter(fieldName, hashCode);
                return segment;
            }
        }

        String functionName = null;

        long[] hashCode2 = null;
        String[] fieldName2 = null;
        while (jsonReader.ch == '.') {
            jsonReader.next();
            long hash = jsonReader.readFieldNameHashCodeUnquote();
            String str = jsonReader.getFieldName();

            if (jsonReader.ch == '(') {
                functionName = str;
                break;
            }

            if (hashCode2 == null) {
                hashCode2 = new long[]{hash};
                fieldName2 = new String[]{str};
            } else {
                hashCode2 = Arrays.copyOf(hashCode2, hashCode2.length + 1);
                hashCode2[hashCode2.length - 1] = hash;
                fieldName2 = Arrays.copyOf(fieldName2, fieldName2.length + 1);
                fieldName2[fieldName2.length - 1] = str;
            }
        }

        JSONPathFilter.Operator operator = null;
        Function function = null;
        if (jsonReader.ch == '(') {
            if (functionName == null) {
                functionName = fieldName;
                fieldName = null;
            }

            switch (functionName) {
                case "type":
                    hashCode = 0;
                    function = JSONPathFunction.TypeFunction.INSTANCE;
                    break;
                case "size":
                    hashCode = 0;
                    function = JSONPathFunction.SizeFunction.INSTANCE;
                    break;
                case "contains":
                    hashCode = 0;
                    operator = JSONPathFilter.Operator.CONTAINS;
                    break;
                default:
                    throw new JSONException("syntax error, function not support " + fieldName);
            }

            if (function != null) {
                jsonReader.next();
                if (!jsonReader.nextIfMatch(')')) {
                    throw new JSONException("syntax error, function " + functionName);
                }
            }
        }

        if (operator == null) {
            operator = JSONPath.parseOperator(jsonReader);
        }

        switch (operator) {
            case REG_MATCH:
            case RLIKE:
            case NOT_RLIKE: {
                String regex;
                boolean ignoreCase;
                if (jsonReader.isString()) {
                    regex = jsonReader.readString();
                    ignoreCase = false;
                } else {
                    regex = jsonReader.readPattern();
                    ignoreCase = jsonReader.nextIfMatch('i');
                }

                Pattern pattern = ignoreCase
                        ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                        : Pattern.compile(regex);

                JSONPathSegment segment = new JSONPathFilter.NameRLikeSegment(fieldName, hashCode, pattern, operator == JSONPathFilter.Operator.NOT_RLIKE);
                if (!jsonReader.nextIfMatch(')')) {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }

                return segment;
            }
            case IN:
            case NOT_IN: {
                if (jsonReader.ch != '(') {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }
                jsonReader.next();

                JSONPathSegment segment;
                if (jsonReader.isString()) {
                    List<String> list = new ArrayList<>();
                    while (jsonReader.isString()) {
                        list.add(jsonReader.readString());
                    }
                    String[] strArray = new String[list.size()];
                    list.toArray(strArray);
                    segment = new JSONPathFilter.NameStringInSegment(
                            fieldName,
                            hashCode,
                            strArray,
                            operator == JSONPathFilter.Operator.NOT_IN
                    );
                } else if (jsonReader.isNumber()) {
                    List<Number> list = new ArrayList<>();
                    while (jsonReader.isNumber()) {
                        list.add(jsonReader.readNumber());
                    }
                    long[] values = new long[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        values[i] = list.get(i).longValue();
                    }
                    segment = new JSONPathFilter.NameIntInSegment(fieldName, hashCode, fieldName2, hashCode2, function, values, operator == JSONPathFilter.Operator.NOT_IN);
                } else {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }

                if (!jsonReader.nextIfMatch(')')) {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }
                if (!jsonReader.nextIfMatch(')')) {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }

                return segment;
            }
            case CONTAINS: {
                if (jsonReader.ch != '(') {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }
                jsonReader.next();

                JSONPathSegment segment;
                if (jsonReader.isString()) {
                    List<String> list = new ArrayList<>();
                    while (jsonReader.isString()) {
                        list.add(jsonReader.readString());
                    }
                    String[] strArray = new String[list.size()];
                    list.toArray(strArray);
                    segment = new JSONPathFilter.NameStringContainsSegment(
                            fieldName,
                            hashCode,
                            fieldName2,
                            hashCode2,
                            strArray,
                            operator == JSONPathFilter.Operator.NOT_CONTAINS
                    );
                } else if (jsonReader.isNumber()) {
                    List<Number> list = new ArrayList<>();
                    while (jsonReader.isNumber()) {
                        list.add(jsonReader.readNumber());
                    }
                    long[] values = new long[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        values[i] = list.get(i).longValue();
                    }
                    segment = new JSONPathFilter.NameLongContainsSegment(
                            fieldName,
                            hashCode,
                            fieldName2,
                            hashCode2,
                            values,
                            operator == JSONPathFilter.Operator.NOT_CONTAINS
                    );
                } else {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }

                if (!jsonReader.nextIfMatch(')')) {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }
                if (!jsonReader.nextIfMatch(')')) {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }

                return segment;
            }
            case BETWEEN:
            case NOT_BETWEEN: {
                JSONPathSegment segment;
                if (jsonReader.isNumber()) {
                    Number begin = jsonReader.readNumber();
                    String and = jsonReader.readFieldNameUnquote();
                    if (!"and".equalsIgnoreCase(and)) {
                        throw new JSONException("syntax error, " + and);
                    }
                    Number end = jsonReader.readNumber();
                    segment = new JSONPathFilter.NameIntBetweenSegment(fieldName, hashCode, begin.longValue(), end.longValue(), operator == JSONPathFilter.Operator.NOT_BETWEEN);
                } else {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }

                if (parentheses) {
                    if (!jsonReader.nextIfMatch(')')) {
                        throw new JSONException(jsonReader.info("jsonpath syntax error"));
                    }
                }

                return segment;
            }
            default:
                break;
        }

        JSONPathSegment segment = null;
        switch (jsonReader.ch) {
            case '-':
            case '+':
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
                Number number = jsonReader.readNumber();
                if (number instanceof Integer || number instanceof Long) {
                    segment = new JSONPathFilter.NameIntOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, number.longValue());
                } else if (number instanceof BigDecimal) {
                    segment = new JSONPathFilter.NameDecimalOpSegment(fieldName, hashCode, operator, (BigDecimal) number);
                } else {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }
                break;
            }
            case '"':
            case '\'': {
                String strVal = jsonReader.readString();
                int p0 = strVal.indexOf('%');
                if (p0 == -1) {
                    if (operator == JSONPathFilter.Operator.LIKE) {
                        operator = JSONPathFilter.Operator.EQ;
                    } else if (operator == JSONPathFilter.Operator.NOT_LIKE) {
                        operator = JSONPathFilter.Operator.NE;
                    }
                }

                if (operator == JSONPathFilter.Operator.LIKE || operator == JSONPathFilter.Operator.NOT_LIKE) {
                    String[] items = strVal.split("%");

                    String startsWithValue = null;
                    String endsWithValue = null;
                    String[] containsValues = null;
                    if (p0 == 0) {
                        if (strVal.charAt(strVal.length() - 1) == '%') {
                            containsValues = new String[items.length - 1];
                            System.arraycopy(items, 1, containsValues, 0, containsValues.length);
                        } else {
                            endsWithValue = items[items.length - 1];
                            if (items.length > 2) {
                                containsValues = new String[items.length - 2];
                                System.arraycopy(items, 1, containsValues, 0, containsValues.length);
                            }
                        }
                    } else if (strVal.charAt(strVal.length() - 1) == '%') {
                        if (items.length == 1) {
                            startsWithValue = items[0];
                        } else {
                            containsValues = items;
                        }
                    } else {
                        if (items.length == 1) {
                            startsWithValue = items[0];
                        } else if (items.length == 2) {
                            startsWithValue = items[0];
                            endsWithValue = items[1];
                        } else {
                            startsWithValue = items[0];
                            endsWithValue = items[items.length - 1];
                            containsValues = new String[items.length - 2];
                            System.arraycopy(items, 1, containsValues, 0, containsValues.length);
                        }
                    }
                    segment = new JSONPathFilter.NameMatchFilter(
                            fieldName,
                            hashCode,
                            startsWithValue,
                            endsWithValue,
                            containsValues,
                            operator == JSONPathFilter.Operator.NOT_LIKE
                    );
                } else {
                    segment = new JSONPathFilter.NameStringOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, strVal);
                }
                break;
            }
            case 't': {
                String ident = jsonReader.readFieldNameUnquote();
                if ("true".equalsIgnoreCase(ident)) {
                    segment = new JSONPathFilter.NameIntOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, 1);
                    break;
                }
                break;
            }
            case 'f': {
                String ident = jsonReader.readFieldNameUnquote();
                if ("false".equalsIgnoreCase(ident)) {
                    segment = new JSONPathFilter.NameIntOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, 0);
                    break;
                }
                break;
            }
            case '[': {
                JSONArray array = jsonReader.read(JSONArray.class);
                segment = new JSONPathFilter.NameArrayOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, array);
                break;
            }
            case '{': {
                JSONObject object = jsonReader.read(JSONObject.class);
                segment = new JSONPathFilter.NameObjectOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, object);
                break;
            }
            default:
                throw new JSONException(jsonReader.info("jsonpath syntax error"));
        }

        if (jsonReader.ch == '&' || jsonReader.ch == '|' || jsonReader.ch == 'a' || jsonReader.ch == 'o') {
            segment = parseFilterRest(segment);
        }

        if (parentheses) {
            if (!jsonReader.nextIfMatch(')')) {
                throw new JSONException(jsonReader.info("jsonpath syntax error"));
            }
        }

        return segment;
    }
}
