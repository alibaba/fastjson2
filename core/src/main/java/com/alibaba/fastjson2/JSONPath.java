package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT_END;
import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT;

public abstract class JSONPath {
    JSONReader.Context readerContext;
    JSONWriter.Context writerContext;
    final String path;

    JSONPath(String path) {
        this.path = path;
    }

    public boolean isPrevious() {
        return false;
    }

    @Override
    public final String toString() {
        return path;
    }

    public static Object extract(String json, String path) {
        JSONReader jsonReader = JSONReader.of(json);
        JSONPath jsonPath = JSONPath.of(path);
        return jsonPath.extract(jsonReader);
    }

    public static Object eval(Object rootObject, String path) {
        return JSONPath.of(path).eval(rootObject);
    }

    public static void set(Object rootObject, String path, Object value) {
        JSONPath.of(path).set(rootObject, value);
    }

    public static Map<String, Object> paths(Object javaObject) {
        Map<Object, String> values = new IdentityHashMap<>();
        Map<String, Object> paths = new HashMap<>();

        JSONPath.of("").paths(values, paths, "/", javaObject);
        return paths;
    }

    void paths(Map<Object, String> values, Map<String, Object> paths, String parent, Object javaObject) {
        if (javaObject == null) {
            return;
        }

        String p = values.put(javaObject, parent);
        if (p != null) {
            Class<?> type = javaObject.getClass();
            boolean basicType = type == String.class
                    || type == Boolean.class
                    || type == Character.class
                    || type == UUID.class
                    || type.isEnum()
                    || javaObject instanceof Number
                    || javaObject instanceof Date;

            if (!basicType) {
                return;
            }
        }

        paths.put(parent, javaObject);

        if (javaObject instanceof Map) {
            Map map = (Map) javaObject;

            for (Object entryObj : map.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                Object key = entry.getKey();

                if (key instanceof String) {
                    String path = parent.equals("/") ? "/" + key : parent + "/" + key;
                    paths(values, paths, path, entry.getValue());
                }
            }
            return;
        }

        if (javaObject instanceof Collection) {
            Collection collection = (Collection) javaObject;

            int i = 0;
            for (Object item : collection) {
                String path = parent.equals("/") ? "/" + i : parent + "/" + i;
                paths(values, paths, path, item);
                ++i;
            }

            return;
        }

        Class<?> clazz = javaObject.getClass();

        if (clazz.isArray()) {
            int len = Array.getLength(javaObject);

            for (int i = 0; i < len; ++i) {
                Object item = Array.get(javaObject, i);

                String path = parent.equals("/") ? "/" + i : parent + "/" + i;
                paths(values, paths, path, item);
            }

            return;
        }

        if (ObjectWriterProvider.isPrimitiveOrEnum(clazz)) {
            return;
        }

        ObjectWriter serializer = getWriterContext().getObjectWriter(clazz);
        if (serializer instanceof ObjectWriterAdapter) {
            ObjectWriterAdapter javaBeanSerializer = (ObjectWriterAdapter) serializer;

            try {
                Map<String, Object> fieldValues = javaBeanSerializer.toMap(javaObject);
                for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
                    String key = entry.getKey();

                    if (key != null) {
                        String path = parent.equals("/") ? "/" + key : parent + "/" + key;
                        paths(values, paths, path, entry.getValue());
                    }
                }
            } catch (Exception e) {
                throw new JSONException("toJSON error", e);
            }
        }
    }

    public abstract boolean isRef();

    public void arrayAdd(Object root, Object... values) {
        Object result = eval(root);
        if (result instanceof Collection) {
            Collection collection = (Collection) result;
            for (Object value : values) {
                collection.add(value);
            }
        }
    }

    public abstract boolean contains(Object rootObject);

    public abstract Object eval(Object rootObject);

    public abstract Object extract(JSONReader jsonReader);

    public abstract String extractScalar(JSONReader jsonReader);

    public JSONReader.Context getReaderContext() {
        if (readerContext == null) {
            readerContext = JSONFactory.createReadContext();
        }
        return readerContext;
    }

    public JSONPath setReaderContext(JSONReader.Context context) {
        this.readerContext = context;
        return this;
    }

    public JSONWriter.Context getWriterContext() {
        if (writerContext == null) {
            writerContext = JSONFactory.createWriteContext();
        }
        return writerContext;
    }

    public JSONPath setWriterContext(JSONWriter.Context writerContext) {
        this.writerContext = writerContext;
        return this;
    }

    public abstract void set(Object rootObject, Object value);

    public abstract void setInt(Object rootObject, int value);

    public abstract void setLong(Object rootObject, long value);

    public abstract boolean remove(Object rootObject);

    public void extract(JSONReader jsonReader, ValueConsumer consumer) {
        Object object = extract(jsonReader);
        if (object == null) {
            consumer.acceptNull();
            return;
        }

        if (object instanceof Number) {
            consumer.accept((Number) object);
            return;
        }

        if (object instanceof String) {
            consumer.accept((String) object);
            return;
        }

        if (object instanceof Boolean) {
            consumer.accept((Boolean) object);
            return;
        }

        if (object instanceof Map) {
            consumer.accept((Map) object);
            return;
        }

        if (object instanceof List) {
            consumer.accept((List) object);
            return;
        }

        throw new JSONException("TODO : " + object.getClass());
    }

    public void extractScalar(JSONReader jsonReader, ValueConsumer consumer) {
        Object object = extractScalar(jsonReader);
        if (object == null) {
            consumer.acceptNull();
            return;
        }

        String str = object.toString();
        consumer.accept(str);
    }

    public Long extractInt64(JSONReader jsonReader) {
        long value = extractInt64Value(jsonReader);
        if (jsonReader.wasNull) {
            return null;
        }
        return value;
    }

    public long extractInt64Value(JSONReader jsonReader) {
        Object object = extract(jsonReader);
        if (object == null) {
            jsonReader.wasNull = true;
            return 0L;
        }

        if (object instanceof Number) {
            return ((Number) object).longValue();
        }

        java.util.function.Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(object.getClass(), long.class);
        if (typeConvert == null) {
            throw new JSONException("can not convert to long : " + object);
        }
        Object converted = typeConvert.apply(object);
        return ((Long) converted).longValue();
    }

    public Integer extractInt32(JSONReader jsonReader) {
        int intValue = extractInt32Value(jsonReader);
        if (jsonReader.wasNull) {
            return null;
        }
        return intValue;
    }

    public int extractInt32Value(JSONReader jsonReader) {
        Object object = extract(jsonReader);
        if (object == null) {
            jsonReader.wasNull = true;
            return 0;
        }
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        java.util.function.Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(object.getClass(), int.class);
        if (typeConvert == null) {
            throw new JSONException("can not convert to int : " + object);
        }
        Object converted = typeConvert.apply(object);
        return ((Integer) converted).intValue();
    }

    public static JSONPath of(String path) {
        if (path.equals("#-1")) {
            return PreviousPath.INSTANCE;
        }

        JSONReader jsonReader = JSONReader.of(path);
        List<Segment> segments = new ArrayList<>();

        while (jsonReader.current() != JSONReader.EOI) {
            if (jsonReader.current() == '$') {
                jsonReader.next();
                if (jsonReader.current() == '.') {
                    jsonReader.next();
                    if (jsonReader.current() == '*') {
                        jsonReader.next();
                        segments.add(AllSegment.INSTANCE);
                    } else if (jsonReader.current() == '.') {
                        jsonReader.next();
                        long hashCode = jsonReader.readFieldNameHashCodeUnquote();
                        String name = jsonReader.getFieldName();
                        segments.add(
                                new CycleNameSegment(name, hashCode));
                    } else {
                        long hashCode = jsonReader.readFieldNameHashCodeUnquote();
                        String name = jsonReader.getFieldName();
                        Segment segment;
                        if (jsonReader.ch == '(') {
                            switch (name) {
                                case "length":
                                case "size":
                                    segment = LengthSegment.INSTANCE;
                                    break;
                                case "keys":
                                    segment = KeysSegment.INSTANCE;
                                    break;
                                case "min":
                                    segment = MinSegment.INSTANCE;
                                    break;
                                case "max":
                                    segment = MaxSegment.INSTANCE;
                                    break;
                                case "type":
                                    segment = TypeSegment.INSTANCE;
                                    break;
                                case "floor":
                                    segment = FloorSegment.INSTANCE;
                                    break;
                                default:
                                    throw new JSONException("not support syntax, path : " + path);

                            }
                            jsonReader.next();
                            if (jsonReader.ch != ')') {
                                throw new JSONException("not support syntax, path : " + path);
                            }
                        } else {
                            segment = new NameSegment(name, hashCode);
                        }
                        segments.add(segment);
                    }
                }
                continue;
            }

            if (jsonReader.current() == '.') {
                jsonReader.next();
                if (jsonReader.current() == '*') {
                    jsonReader.next();
                    segments.add(AllSegment.INSTANCE);
                    continue;
                } else if (jsonReader.current() == '.') {
                    jsonReader.next();
                    long hashCode = jsonReader.readFieldNameHashCodeUnquote();
                    String name = jsonReader.getFieldName();
                    segments.add(
                            new CycleNameSegment(name, hashCode));
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
                    if (isNum) {
                        try {
                            int index = Integer.parseInt(name);
                            segments.add(
                                    IndexSegment.of(index));
                            continue;
                        } catch (NumberFormatException ignored) {

                        }
                    }

                    Segment segment;
                    if (jsonReader.ch == '(') {
                        switch (name) {
                            case "length":
                            case "size":
                                segment = LengthSegment.INSTANCE;
                                break;
                            case "keys":
                                segment = KeysSegment.INSTANCE;
                                break;
                            case "min":
                                segment = MinSegment.INSTANCE;
                                break;
                            case "max":
                                segment = MaxSegment.INSTANCE;
                                break;
                            case "type":
                                segment = TypeSegment.INSTANCE;
                                break;
                            case "floor":
                                segment = FloorSegment.INSTANCE;
                                break;
                            default:
                                throw new JSONException("not support syntax, path : " + path);

                        }
                        jsonReader.next();
                        if (jsonReader.ch != ')') {
                            throw new JSONException("not support syntax, path : " + path);
                        }
                    } else {
                        segment = new NameSegment(name, hashCode);
                    }
                    segments.add(segment);
                }
                continue;
            }

            if (jsonReader.ch == '[') {
                jsonReader.next();

                do {
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
                            Segment segment;
                            if (jsonReader.ch == ':') {
                                jsonReader.next();
                                if (jsonReader.ch == ']') {
                                    segment = new RangeIndexSegment(index, index >= 0 ? Integer.MAX_VALUE : 0);
                                } else {
                                    int end = jsonReader.readInt32Value();
                                    segment = new RangeIndexSegment(index, end);
                                }
                            } else if (jsonReader.isNumber()) {
                                List<Integer> list = new ArrayList<>();
                                list.add(index);
                                while (jsonReader.isNumber()) {
                                    index = jsonReader.readInt32Value();
                                    list.add(index);
                                }
                                int[] indics = new int[list.size()];
                                for (int i = 0; i < list.size(); i++) {
                                    indics[i] = list.get(i);
                                }
                                segment = new MultiIndexSegment(indics);
                            } else {
                                segment = IndexSegment.of(index);
                            }
                            segments.add(segment);
                            break;
                        }
                        case '*':
                            jsonReader.next();
                            segments.add(AllSegment.INSTANCE);
                            break;
                        case ':': {
                            jsonReader.next();
                            int end = jsonReader.ch == ']' ? 0 : jsonReader.readInt32Value();
                            RangeIndexSegment segment;
                            if (end > 0) {
                                segment = new RangeIndexSegment(0, end);
                            } else {
                                segment = new RangeIndexSegment(Integer.MIN_VALUE, end);
                            }
                            segments.add(segment);
                            break;
                        }
                        case '"':
                        case '\'':
                            String name = jsonReader.readString();
                            if (jsonReader.current() == ']') {
                                segments.add(new NameSegment(name, Fnv.hashCode64(name)));
                            } else if (jsonReader.isString()) {
                                List<String> names = new ArrayList<>();
                                names.add(name);
                                do {
                                    names.add(jsonReader.readString());
                                } while (jsonReader.isString());
                                String[] nameArray = new String[names.size()];
                                names.toArray(nameArray);
                                segments.add(new MultiNameSegment(nameArray));
                            } else {
                                throw new JSONException("TODO : " + jsonReader.current());
                            }
                            break;
                        case '?':
                            jsonReader.next();
                            segments.add(parseFilter(jsonReader));
                            break;
                        case 'r':
                            String fieldName = jsonReader.readFieldNameUnquote();
                            if (fieldName.equals("randomIndex")) {
                                if (!jsonReader.nextIfMatch('(')
                                        || !jsonReader.nextIfMatch(')')
                                        || !jsonReader.nextIfMatch(']')) {
                                    throw new JSONException("not support : " + fieldName);
                                }
                                segments.add(RandomIndexSegment.INSTANCE);
                                break;
                            }
                            throw new JSONException("not support : " + fieldName);
                        default:
                            throw new JSONException("TODO : " + jsonReader.current());
                    }

                } while (jsonReader.ch != ']' && jsonReader.ch != JSONReader.EOI);

                if (jsonReader.ch == ']') {
                    jsonReader.next();
                    continue;
                }
            }

            char ch = jsonReader.ch;
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
                long hashCode = jsonReader.readFieldNameHashCodeUnquote();
                String name = jsonReader.getFieldName();
                Segment segment;
                if (jsonReader.ch == '(') {
                    switch (name) {
                        case "length":
                        case "size":
                            segment = LengthSegment.INSTANCE;
                            break;
                        case "keys":
                            segment = KeysSegment.INSTANCE;
                            break;
                        case "min":
                            segment = MinSegment.INSTANCE;
                            break;
                        case "max":
                            segment = MaxSegment.INSTANCE;
                            break;
                        case "type":
                            segment = TypeSegment.INSTANCE;
                            break;
                        case "floor":
                            segment = FloorSegment.INSTANCE;
                            break;
                        default:
                            throw new JSONException("not support syntax, path : " + path);

                    }
                    jsonReader.next();
                    if (jsonReader.ch != ')') {
                        throw new JSONException("not support syntax, path : " + path);
                    }
                } else {
                    segment = new NameSegment(name, hashCode);
                }
                segments.add(segment);
                continue;
            }

            break;
        }

        if (segments.size() == 1) {
            Segment segment = segments.get(0);
            if (segment instanceof NameSegment) {
                return new SingleNamePath(path, (NameSegment) segment);
            }

            return new SingleSegmentPath(segment, path);
        }

        return new MultiSegmentPath(path, segments);
    }

    static Segment parseFilter(JSONReader jsonReader) {
        boolean parentheses = jsonReader.nextIfMatch('(');

        if (jsonReader.ch == '@') {
            jsonReader.next();
            if (jsonReader.ch != '.') {
                Operator operator = parseOperator(jsonReader);

                Segment segment = null;
                if (jsonReader.isNumber()) {
                    Number number = jsonReader.readNumber();
                    if (number instanceof Integer || number instanceof Long) {
                        segment = new NameIntOpSegment(null, 0, null, operator, number.longValue());
                    }
                }

                if (segment != null) {
                    if (parentheses) {
                        if (!jsonReader.nextIfMatch(')')) {
                            throw new JSONException("syntax error, " + jsonReader.ch);
                        }
                    }
                    return segment;
                }

                throw new JSONException("syntax error, " + jsonReader.ch);
            }

            jsonReader.next();
            long hashCode = jsonReader.readFieldNameHashCodeUnquote();
            String fieldName = jsonReader.getFieldName();


            if (parentheses) {
                if (jsonReader.nextIfMatch(')')) {
                    NameExistsFilter segment = new NameExistsFilter(fieldName, hashCode);
                    return segment;
                }
            }

            Function function = null;
            if (jsonReader.ch == '(') {
                jsonReader.next();
                if (!jsonReader.nextIfMatch(')')) {
                    throw new JSONException("syntax error, function " + fieldName);
                }
                switch (fieldName) {
                    case "type":
                        fieldName = null;
                        hashCode = 0;
                        function = TypeFunction.INSTANCE;
                        break;
                    case "size":
                        fieldName = null;
                        hashCode = 0;
                        function = SizeFunction.INSTANCE;
                        break;
                    default:
                        throw new JSONException("syntax error, function not support " + fieldName);
                }
            }

            Operator operator = parseOperator(jsonReader);

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

                    Segment segment = new NameRLikeSegment(fieldName, hashCode, pattern, operator == Operator.NOT_RLIKE);
                    if (!jsonReader.nextIfMatch(')')) {
                        throw new JSONException("syntax error, " + jsonReader.ch);
                    }

                    return segment;
                }
                case IN:
                case NOT_IN: {
                    if (jsonReader.ch != '(') {
                        throw new JSONException("syntax error, " + jsonReader.ch);
                    }
                    jsonReader.next();

                    Segment segment;
                    if (jsonReader.isString()) {
                        List<String> list = new ArrayList<>();
                        while (jsonReader.isString()) {
                            list.add(jsonReader.readString());
                        }
                        String[] strArray = new String[list.size()];
                        list.toArray(strArray);
                        segment = new NameStringInSegment(fieldName, hashCode, strArray, operator == Operator.NOT_IN);
                    } else if (jsonReader.isNumber()) {
                        List<Number> list = new ArrayList<>();
                        while (jsonReader.isNumber()) {
                            list.add(jsonReader.readNumber());
                        }
                        long[] values = new long[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            values[i] = list.get(i).longValue();
                        }
                        segment = new NameIntInSegment(fieldName, hashCode, function, values, operator == Operator.NOT_IN);
                    } else {
                        throw new JSONException("syntax error, " + jsonReader.ch);
                    }

                    if (!jsonReader.nextIfMatch(')')) {
                        throw new JSONException("syntax error, " + jsonReader.ch);
                    }
                    if (!jsonReader.nextIfMatch(')')) {
                        throw new JSONException("syntax error, " + jsonReader.ch);
                    }

                    return segment;
                }
                case BETWEEN:
                case NOT_BETWEEN: {
                    Segment segment;
                    if (jsonReader.isNumber()) {
                        Number begin = jsonReader.readNumber();
                        String and = jsonReader.readFieldNameUnquote();
                        if (!"and".equalsIgnoreCase(and)) {
                            throw new JSONException("syntax error, " + and);
                        }
                        Number end = jsonReader.readNumber();
                        segment = new NameIntBetweenSegment(fieldName, hashCode, begin.longValue(), end.longValue(), operator == Operator.NOT_BETWEEN);
                    } else {
                        throw new JSONException("syntax error, " + jsonReader.ch);
                    }

                    if (parentheses) {
                        if (!jsonReader.nextIfMatch(')')) {
                            throw new JSONException("syntax error, " + jsonReader.ch);
                        }
                    }

                    return segment;
                }
                default:
                    break;
            }

            Segment segment = null;
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
                        segment = new NameIntOpSegment(fieldName, hashCode, function, operator, number.longValue());
                    } else if (number instanceof BigDecimal) {
                        segment = new NameDecimalOpSegment(fieldName, hashCode, operator, (BigDecimal) number);
                    } else {
                        throw new JSONException("TODO : " + number);
                    }
                    break;
                }
                case '"':
                case '\'': {
                    String strVal = jsonReader.readString();
                    int p0 = strVal.indexOf('%');
                    if (p0 == -1) {
                        if (operator == Operator.LIKE) {
                            operator = Operator.EQ;
                        } else if (operator == Operator.NOT_LIKE) {
                            operator = Operator.NE;
                        }
                    }

                    if (operator == Operator.LIKE || operator == Operator.NOT_LIKE) {
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
                        segment = new NameMatchFilter(
                                fieldName
                                , hashCode
                                , startsWithValue
                                , endsWithValue,
                                containsValues
                                , operator == Operator.NOT_LIKE
                        );
                    } else {
                        segment = new NameStringOpSegment(fieldName, hashCode, function, operator, strVal);
                    }
                    break;
                }
                case 't': {
                    String ident = jsonReader.readFieldNameUnquote();
                    if (ident.equalsIgnoreCase("true")) {
                        segment = new NameIntOpSegment(fieldName, hashCode, function, operator, 1);
                        break;
                    }
                    break;
                }
                case 'f': {
                    String ident = jsonReader.readFieldNameUnquote();
                    if (ident.equalsIgnoreCase("false")) {
                        segment = new NameIntOpSegment(fieldName, hashCode, function, operator, 0);
                        break;
                    }
                    break;
                }
                default:
                    throw new JSONException("TODO : " + jsonReader.ch);
            }

            if (jsonReader.ch == '&' || jsonReader.ch == '|' || jsonReader.ch == 'a' || jsonReader.ch == 'o') {
                segment = parseFilterRest(segment, jsonReader);
            }

            if (parentheses) {
                if (!jsonReader.nextIfMatch(')')) {
                    throw new JSONException("TODO : " + jsonReader.ch);
                }
            }

            return segment;
        } else {
            throw new JSONException("TODO : " + jsonReader.ch);
        }
    }

    static Segment parseFilterRest(Segment segment, JSONReader jsonReader) {
        boolean and;
        switch (jsonReader.ch) {
            case '&':
                jsonReader.next();
                if (!jsonReader.nextIfMatch('&')) {
                    throw new JSONException("syntx error, " + jsonReader.ch);
                }
                and = true;
                break;
            case '|':
                jsonReader.next();
                if (!jsonReader.nextIfMatch('|')) {
                    throw new JSONException("syntx error, " + jsonReader.ch);
                }
                and = false;
                break;
            case 'a':
            case 'A': {
                String fieldName = jsonReader.readFieldNameUnquote();
                if (!fieldName.equalsIgnoreCase("and")) {
                    throw new JSONException("syntax error : " + fieldName);
                }
                and = true;
                break;
            }
            case 'o':
            case 'O': {
                String fieldName = jsonReader.readFieldNameUnquote();
                if (!fieldName.equalsIgnoreCase("or")) {
                    throw new JSONException("syntax error : " + fieldName);
                }
                and = false;
                break;
            }
            default:
                throw new JSONException("TODO : " + jsonReader.ch);
        }

        Segment right = parseFilter(jsonReader);
        if (segment instanceof GroupFilter) {
            GroupFilter group = (GroupFilter) segment;
            if (group.and == and) {
                group.filters.add((FilterSegment) right);
                return group;
            }
        }
        List<FilterSegment> filters = new ArrayList<>();
        filters.add((FilterSegment) segment);
        filters.add((FilterSegment) right);
        return new GroupFilter(filters, and);
    }

    static Operator parseOperator(JSONReader jsonReader) {
        Operator operator;
        switch (jsonReader.ch) {
            case '<':
                jsonReader.next();
                if (jsonReader.ch == '=') {
                    jsonReader.next();
                    operator = Operator.LE;
                } else if (jsonReader.ch == '>') {
                    jsonReader.next();
                    operator = Operator.NE;
                } else {
                    operator = Operator.LT;
                }
                break;
            case '=':
                jsonReader.next();
                if (jsonReader.ch == '~') {
                    jsonReader.next();
                    operator = Operator.REG_MATCH;
                } else if (jsonReader.ch == '=') {
                    jsonReader.next();
                    operator = Operator.EQ
                    ;
                } else {
                    operator = Operator.EQ;
                }
                break;
            case '!':
                jsonReader.next();
                if (jsonReader.ch == '=') {
                    jsonReader.next();
                    operator = Operator.NE;
                } else {
                    throw new JSONException("not support operator : !" + jsonReader.ch);
                }
                break;
            case '>':
                jsonReader.next();
                if (jsonReader.ch == '=') {
                    jsonReader.next();
                    operator = Operator.GE;
                } else {
                    operator = Operator.GT;
                }
                break;
            case 'l':
            case 'L': {
                jsonReader.readFieldNameHashCodeUnquote();
                String fieldName = jsonReader.getFieldName();
                if (fieldName.equalsIgnoreCase("like")) {
                    operator = Operator.LIKE;
                } else {
                    throw new JSONException("not support operator : " + fieldName);
                }
                break;
            }
            case 'n':
            case 'N': {
                jsonReader.readFieldNameHashCodeUnquote();
                String fieldName = jsonReader.getFieldName();

                if (fieldName.equalsIgnoreCase("nin")) {
                    operator = Operator.NOT_IN;
                    break;
                }

                if (!fieldName.equalsIgnoreCase("not")) {
                    throw new JSONException("not support operator : " + fieldName);
                }

                jsonReader.readFieldNameHashCodeUnquote();
                fieldName = jsonReader.getFieldName();
                if (fieldName.equalsIgnoreCase("like")) {
                    operator = Operator.NOT_LIKE;
                } else if (fieldName.equalsIgnoreCase("rlike")) {
                    operator = Operator.NOT_RLIKE;
                } else if (fieldName.equalsIgnoreCase("in")) {
                    operator = Operator.NOT_IN;
                } else if (fieldName.equalsIgnoreCase("between")) {
                    operator = Operator.NOT_BETWEEN;
                } else {
                    throw new JSONException("not support operator : " + fieldName);
                }
                break;
            }
            case 'i':
            case 'I': {
                jsonReader.readFieldNameHashCodeUnquote();
                String fieldName = jsonReader.getFieldName();
                if (fieldName.equalsIgnoreCase("in")) {
                    operator = Operator.IN;
                } else {
                    throw new JSONException("not support operator : " + fieldName);
                }
                break;
            }
            case 'r':
            case 'R': {
                jsonReader.readFieldNameHashCodeUnquote();
                String fieldName = jsonReader.getFieldName();
                if (fieldName.equalsIgnoreCase("rlike")) {
                    operator = Operator.RLIKE;
                } else {
                    throw new JSONException("not support operator : " + fieldName);
                }
                break;
            }
            case 'b':
            case 'B': {
                jsonReader.readFieldNameHashCodeUnquote();
                String fieldName = jsonReader.getFieldName();
                if (fieldName.equalsIgnoreCase("between")) {
                    operator = Operator.BETWEEN;
                } else {
                    throw new JSONException("not support operator : " + fieldName);
                }
                break;
            }
            default: {
                jsonReader.readFieldNameHashCodeUnquote();
                throw new JSONException("not support operator : " + jsonReader.getFieldName());
            }
        }
        return operator;
    }

    enum Operator {
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE,
        LIKE,
        NOT_LIKE,
        RLIKE,
        NOT_RLIKE,
        IN,
        NOT_IN,
        BETWEEN,
        NOT_BETWEEN,
        AND,
        OR,
        REG_MATCH
    }

    static abstract class FilterSegment extends Segment implements EvalSegment {
        abstract boolean apply(Context ctx, Object object);
    }

    interface EvalSegment {

    }

    static final class NameIntOpSegment extends NameFilter {
        final Operator operator;
        final long value;

        public NameIntOpSegment(String name, long nameHashCode, Function expr, Operator operator, long value) {
            super(name, nameHashCode, expr);
            this.operator = operator;
            this.value = value;
        }

        @Override
        public boolean apply(Object fieldValue) {
            boolean objInt = fieldValue instanceof Boolean
                    || fieldValue instanceof Byte
                    || fieldValue instanceof Short
                    || fieldValue instanceof Integer
                    || fieldValue instanceof Long;
            if (objInt) {
                long fieldValueInt;
                if (fieldValue instanceof Boolean) {
                    fieldValueInt = (Boolean) fieldValue ? 1 : 0;
                } else {
                    fieldValueInt = ((Number) fieldValue).longValue();
                }

                switch (operator) {
                    case LT:
                        return fieldValueInt < this.value;
                    case LE:
                        return fieldValueInt <= this.value;
                    case EQ:
                        return fieldValueInt == this.value;
                    case NE:
                        return fieldValueInt != this.value;
                    case GT:
                        return fieldValueInt > this.value;
                    case GE:
                        return fieldValueInt >= this.value;
                    default:
                        throw new UnsupportedOperationException();
                }
            }

            int cmp;
            if (fieldValue instanceof BigDecimal) {
                cmp = ((BigDecimal) fieldValue)
                        .compareTo(
                                BigDecimal.valueOf(value));
            } else if (fieldValue instanceof BigInteger) {
                cmp = ((BigInteger) fieldValue)
                        .compareTo(
                                BigInteger.valueOf(value));
            } else if (fieldValue instanceof Float) {
                cmp = ((Float) fieldValue)
                        .compareTo(
                                Float.valueOf(value));
            } else if (fieldValue instanceof Double) {
                cmp = ((Double) fieldValue)
                        .compareTo(
                                Double.valueOf(value));
            } else {
                throw new UnsupportedOperationException();
            }

            switch (operator) {
                case LT:
                    return cmp < 0;
                case LE:
                    return cmp <= 0;
                case EQ:
                    return cmp == 0;
                case NE:
                    return cmp != 0;
                case GT:
                    return cmp > 0;
                case GE:
                    return cmp >= 0;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    static final class NameDecimalOpSegment extends NameFilter {
        final Operator operator;
        final BigDecimal value;

        public NameDecimalOpSegment(String name, long nameHashCode, Operator operator, BigDecimal value) {
            super(name, nameHashCode);
            this.operator = operator;
            this.value = value;
        }

        @Override
        public boolean apply(Object fieldValue) {
            if (fieldValue == null) {
                return false;
            }

            BigDecimal fieldValueDecimal;

            if (fieldValue instanceof Boolean) {
                fieldValueDecimal = (Boolean) fieldValue ? BigDecimal.ONE : BigDecimal.ZERO;
            } else if (fieldValue instanceof Byte
                    || fieldValue instanceof Short
                    || fieldValue instanceof Integer
                    || fieldValue instanceof Long) {
                fieldValueDecimal = BigDecimal.valueOf(
                        ((Number) fieldValue).longValue()
                );
            } else if (fieldValue instanceof BigDecimal) {
                fieldValueDecimal = ((BigDecimal) fieldValue);
            } else if (fieldValue instanceof BigInteger) {
                fieldValueDecimal = new BigDecimal((BigInteger) fieldValue);
            } else {
                throw new UnsupportedOperationException();
            }

            int cmp = fieldValueDecimal.compareTo(value);
            switch (operator) {
                case LT:
                    return cmp < 0;
                case LE:
                    return cmp <= 0;
                case EQ:
                    return cmp == 0;
                case NE:
                    return cmp != 0;
                case GT:
                    return cmp > 0;
                case GE:
                    return cmp >= 0;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    static final class NameRLikeSegment extends NameFilter {
        final Pattern pattern;
        final boolean not;

        public NameRLikeSegment(String fieldName, long fieldNameNameHash, Pattern pattern, boolean not) {
            super(fieldName, fieldNameNameHash);
            this.pattern = pattern;
            this.not = not;
        }

        @Override
        boolean apply(Object fieldValue) {
            String strPropertyValue = fieldValue.toString();
            Matcher m = pattern.matcher(strPropertyValue);
            boolean match = m.matches();

            if (not) {
                match = !match;
            }

            return match;
        }
    }

    static final class GroupFilter extends Segment implements EvalSegment {
        final boolean and;
        final List<FilterSegment> filters;

        public GroupFilter(List<FilterSegment> filters, boolean and) {
            this.and = and;
            this.filters = filters;
        }

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            if (ctx.parent == null) {
                ctx.root = jsonReader.readAny();
            }
            eval(ctx);
        }

        @Override
        public void eval(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                JSONArray array = new JSONArray(list.size());
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    boolean match = and;
                    for (FilterSegment filter : filters) {
                        boolean result = filter.apply(ctx, item);
                        if (and) {
                            if (!result) {
                                match = false;
                                break;
                            }
                        } else {
                            if (result) {
                                match = true;
                                break;
                            }
                        }
                    }
                    if (match) {
                        array.add(item);
                    }
                }
                ctx.value = array;
                ctx.eval = true;
                return;
            }

            boolean match = and;
            for (FilterSegment filter : filters) {
                boolean result = filter.apply(ctx, object);
                if (and) {
                    if (!result) {
                        match = false;
                        break;
                    }
                } else {
                    if (result) {
                        match = true;
                        break;
                    }
                }
            }
            if (match) {
                ctx.value = object;
            }
            ctx.eval = true;
        }
    }

    static final class NameIntInSegment extends NameFilter {
        private final long[] values;
        private final boolean not;

        public NameIntInSegment(String fieldName, long fieldNameNameHash, Function expr, long[] values, boolean not) {
            super(fieldName, fieldNameNameHash, expr);
            this.values = values;
            this.not = not;
        }

        @Override
        public boolean apply(Object fieldValue) {
            if (fieldValue instanceof Byte
                    || fieldValue instanceof Short
                    || fieldValue instanceof Integer
                    || fieldValue instanceof Long
            ) {
                long fieldValueLong = ((Number) fieldValue).longValue();

                for (long value : values) {
                    if (value == fieldValueLong) {
                        return !not;
                    }
                }

                return not;
            }

            if (fieldValue instanceof Float
                    || fieldValue instanceof Double
            ) {
                double fieldValueDouble = ((Number) fieldValue).doubleValue();

                for (long value : values) {
                    if (value == fieldValueDouble) {
                        return !not;
                    }
                }

                return not;
            }

            if (fieldValue instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) fieldValue;
                long longValue = decimal.longValue();
                for (long value : values) {
                    if (value == longValue && decimal.equals(BigDecimal.valueOf(value))) {
                        return !not;
                    }
                }

                return not;
            }

            if (fieldValue instanceof BigInteger) {
                BigInteger bigiInt = (BigInteger) fieldValue;
                long longValue = bigiInt.longValue();
                for (long value : values) {
                    if (value == longValue && bigiInt.equals(BigInteger.valueOf(value))) {
                        return !not;
                    }
                }

                return not;
            }

            return not;
        }
    }

    static final class NameIntBetweenSegment extends NameFilter {
        private final long begin;
        private final long end;
        private final boolean not;

        public NameIntBetweenSegment(String fieldName, long fieldNameNameHash, long begin, long end, boolean not) {
            super(fieldName, fieldNameNameHash);
            this.begin = begin;
            this.end = end;
            this.not = not;
        }

        @Override
        public boolean apply(Object fieldValue) {
            if (fieldValue instanceof Byte
                    || fieldValue instanceof Short
                    || fieldValue instanceof Integer
                    || fieldValue instanceof Long
            ) {
                long fieldValueLong = ((Number) fieldValue).longValue();

                if (fieldValueLong >= begin && fieldValueLong <= end) {
                    return !not;
                }

                return not;
            }

            if (fieldValue instanceof Float
                    || fieldValue instanceof Double
            ) {
                double fieldValueDouble = ((Number) fieldValue).doubleValue();

                if (fieldValueDouble >= begin && fieldValueDouble <= end) {
                    return !not;
                }

                return not;
            }

            if (fieldValue instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) fieldValue;
                int cmpBegin = decimal.compareTo(BigDecimal.valueOf(begin));
                int cmpEnd = decimal.compareTo(BigDecimal.valueOf(end));

                if (cmpBegin >= 0 && cmpEnd <= 0) {
                    return !not;
                }

                return not;
            }

            if (fieldValue instanceof BigInteger) {
                BigInteger bigInt = (BigInteger) fieldValue;
                int cmpBegin = bigInt.compareTo(BigInteger.valueOf(begin));
                int cmpEnd = bigInt.compareTo(BigInteger.valueOf(end));

                if (cmpBegin >= 0 && cmpEnd <= 0) {
                    return !not;
                }

                return not;
            }

            return not;
        }
    }

    static final class TypeFunction implements Function {
        static final TypeFunction INSTANCE = new TypeFunction();

        @Override
        public Object apply(Object object) {
            return type(object);
        }
    }

    static final class SizeFunction implements Function {
        static final SizeFunction INSTANCE = new SizeFunction();

        @Override
        public Object apply(Object value) {
            if (value == null) {
                return -1;
            }

            if (value instanceof Collection) {
                return ((Collection<?>) value).size();
            }

            if (value.getClass().isArray()) {
                return Array.getLength(value);
            }

            if (value instanceof Map) {
                return ((Map) value).size();
            }

            return -1;
        }
    }

    static abstract class NameFilter extends FilterSegment {
        final String fieldName;
        final long fieldNameNameHash;
        final Function function;

        public NameFilter(String fieldName, long fieldNameNameHash) {
            this.fieldName = fieldName;
            this.fieldNameNameHash = fieldNameNameHash;
            this.function = null;
        }

        public NameFilter(String fieldName, long fieldNameNameHash, Function function) {
            this.fieldName = fieldName;
            this.fieldNameNameHash = fieldNameNameHash;
            this.function = function;
        }

        abstract boolean apply(Object fieldValue);

        @Override
        public final void accept(JSONReader jsonReader, Context ctx) {
            if (ctx.parent == null) {
                ctx.root = jsonReader.readAny();
            }
            eval(ctx);
        }

        @Override
        public boolean remove(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                for (int i = list.size() - 1; i >= 0; i--) {
                    Object item = list.get(i);
                    if (apply(ctx, item)) {
                        list.remove(i);
                    }
                }
                return true;
            }

            throw new JSONException("UnsupportedOperation " + getClass());
        }

        @Override
        public final void eval(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                JSONArray array = new JSONArray(list.size());
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    if (apply(ctx, item)) {
                        array.add(item);
                    }
                }
                ctx.value = array;
                ctx.eval = true;
                return;
            }

            if (object instanceof Object[]) {
                Object[] list = (Object[]) object;
                JSONArray array = new JSONArray(list.length);
                for (Object item : list) {
                    if (apply(ctx, item)) {
                        array.add(item);
                    }
                }
                ctx.value = array;
                ctx.eval = true;
                return;
            }

            if (apply(ctx, object)) {
                ctx.value = object;
                ctx.eval = true;
            }
        }

        @Override
        public final boolean apply(Context ctx, Object object) {
            if (object == null) {
                return false;
            }

            if (object instanceof Map) {
                Object fieldValue = fieldName == null ? object : ((Map<?, ?>) object).get(fieldName);
                if (fieldValue == null) {
                    return false;
                }

                if (function != null) {
                    fieldValue = function.apply(fieldValue);
                }

                return apply(fieldValue);
            }

            ObjectWriter objectWriter = ctx.path.getWriterContext().getObjectWriter(object.getClass());
            if (objectWriter instanceof ObjectWriterAdapter) {
                FieldWriter fieldWriter = objectWriter.getFieldWriter(fieldNameNameHash);
                Object fieldValue = fieldWriter.getFieldValue(object);

                if (function != null) {
                    fieldValue = function.apply(fieldValue);
                }

                return apply(fieldValue);
            }

            if (function != null) {
                Object fieldValue = function.apply(object);
                return apply(fieldValue);
            }

            if (fieldName == null) {
                return apply(object);
            }

            return false;
        }
    }

    static final class NameStringOpSegment extends NameFilter {
        final Operator operator;
        final String value;

        public NameStringOpSegment(
                String fieldName
                , long fieldNameNameHash
                , Function expr
                , Operator operator, String value
        ) {
            super(fieldName, fieldNameNameHash, expr);
            this.operator = operator;
            this.value = value;
        }

        @Override
        public boolean apply(Object fieldValue) {
            if (!(fieldValue instanceof String)) {
                return false;
            }

            int cmp = ((String) fieldValue).compareTo(this.value);

            switch (operator) {
                case LT:
                    return cmp < 0;
                case LE:
                    return cmp <= 0;
                case EQ:
                    return cmp == 0;
                case NE:
                    return cmp != 0;
                case GT:
                    return cmp > 0;
                case GE:
                    return cmp >= 0;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    static final class NameStringInSegment extends NameFilter {
        private final String[] values;
        private final boolean not;

        public NameStringInSegment(String fieldName, long fieldNameNameHash, String[] values, boolean not) {
            super(fieldName, fieldNameNameHash);
            this.values = values;
            this.not = not;
        }

        @Override
        public boolean apply(Object fieldValue) {
            for (String value : values) {
                if (value == fieldValue) {
                    return !not;
                } else if (value != null && value.equals(fieldValue)) {
                    return !not;
                }
            }

            return not;
        }
    }

    static final class NameMatchFilter extends NameFilter {
        final String startsWithValue;
        final String endsWithValue;
        final String[] containsValues;
        final int minLength;
        final boolean not;

        public NameMatchFilter(
                String fieldName,
                long fieldNameNameHash,
                String startsWithValue,
                String endsWithValue,
                String[] containsValues,
                boolean not) {
            super(fieldName, fieldNameNameHash);
            this.startsWithValue = startsWithValue;
            this.endsWithValue = endsWithValue;
            this.containsValues = containsValues;
            this.not = not;

            int len = 0;
            if (startsWithValue != null) {
                len += startsWithValue.length();
            }

            if (endsWithValue != null) {
                len += endsWithValue.length();
            }

            if (containsValues != null) {
                for (String item : containsValues) {
                    len += item.length();
                }
            }

            this.minLength = len;
        }

        @Override
        boolean apply(Object arg) {
            if (!(arg instanceof String)) {
                return false;
            }

            String fieldValue = (String) arg;
            if (fieldValue.length() < minLength) {
                return not;
            }

            int start = 0;
            if (startsWithValue != null) {
                if (!fieldValue.startsWith(startsWithValue)) {
                    return not;
                }
                start += startsWithValue.length();
            }

            if (containsValues != null) {
                for (String containsValue : containsValues) {
                    int index = fieldValue.indexOf(containsValue, start);
                    if (index == -1) {
                        return not;
                    }
                    start = index + containsValue.length();
                }
            }

            if (endsWithValue != null) {
                if (!fieldValue.endsWith(endsWithValue)) {
                    return not;
                }
            }

            return !not;
        }
    }

    static final class NameExistsFilter extends FilterSegment {
        final String name;
        final long nameHashCode;

        public NameExistsFilter(String name, long nameHashCode) {
            this.name = name;
            this.nameHashCode = nameHashCode;
        }

        @Override
        public void eval(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            JSONArray array = new JSONArray();
            if (object instanceof List) {
                List list = (List) object;
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    if (item instanceof Map) {
                        if (((Map) item).containsKey(name)) {
                            array.add(item);
                        }
                    }
                }
                ctx.value = array;
                return;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            eval(ctx);
        }

        @Override
        public String toString() {
            return '?' + name;
        }

        @Override
        public boolean apply(Context ctx, Object object) {
            throw new UnsupportedOperationException();
        }
    }

    static final class PreviousPath extends JSONPath {
        static final PreviousPath INSTANCE = new PreviousPath("#-1");

        PreviousPath(String path) {
            super(path);
        }

        @Override
        public boolean isRef() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPrevious() {
            return true;
        }

        @Override
        public boolean contains(Object rootObject) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object eval(Object rootObject) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object extract(JSONReader jsonReader) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String extractScalar(JSONReader jsonReader) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Object rootObject, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setInt(Object rootObject, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLong(Object rootObject, long value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object rootObject) {
            throw new UnsupportedOperationException();
        }
    }

    static final class SingleNamePath extends JSONPath {
        final long nameHashCode;
        final String name;

        public SingleNamePath(String path, NameSegment segment) {
            super(path);
            this.name = segment.name;
            this.nameHashCode = segment.nameHashCode;
        }

        @Override
        public Object eval(Object root) {
            if (root instanceof Map) {
                Map map = (Map) root;
                Object value = map.get(name);
                if (value == null) {
                    boolean isNum = IOUtils.isNumber(this.name);
                    Long longValue = null;

                    for (Object o : map.entrySet()) {
                        Map.Entry entry = (Map.Entry) o;
                        Object entryKey = entry.getKey();
                        if (entryKey instanceof Enum && ((Enum<?>) entryKey).name().equals(this.name)) {
                            value = entry.getValue();
                            break;
                        } else if (entryKey instanceof Long) {
                            if (longValue == null && isNum) {
                                longValue = Long.parseLong(this.name);
                            }
                            if (entryKey.equals(longValue)) {
                                value = entry.getValue();
                                break;
                            }
                        }
                    }
                }
                return value;
            }

            JSONWriter.Context writerContext = getWriterContext();
            ObjectWriter objectWriter = writerContext.getObjectWriter(root.getClass());
            if (objectWriter == null) {
                return null;
            }

            FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
            if (fieldWriter == null) {
                return null;
            }

            return fieldWriter.getFieldValue(root);
        }

        @Override
        public boolean remove(Object root) {
            if (root == null) {
                return false;
            }

            if (root instanceof Map) {
                return ((Map<?, ?>) root).remove(name) != null;
            }

            ObjectReaderProvider provider = getReaderContext().getProvider();

            ObjectReader objectReader = provider.getObjectReader(root.getClass());
            if (objectReader == null) {
                return false;
            }

            FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);
            if (fieldReader == null) {
                return false;
            }

            try {
                fieldReader.accept(root, null);
            } catch (Exception ignored) {
                return false;
            }
            return true;
        }

        @Override
        public boolean isRef() {
            return true;
        }

        @Override
        public boolean contains(Object root) {
            if (root instanceof Map) {
                return ((Map) root).get(name) != null;
            }

            ObjectWriterProvider provider = getWriterContext().getProvider();

            ObjectWriter objectWriter = provider.getObjectWriter(root.getClass());
            if (objectWriter == null) {
                return false;
            }

            FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
            if (fieldWriter == null) {
                return false;
            }

            return fieldWriter.getFieldValue(root) != null;
        }

        @Override
        public void set(Object rootObject, Object value) {
            if (rootObject instanceof Map) {
                ((Map) rootObject).put(name, value);
                return;
            }
            ObjectReaderProvider provider = getReaderContext().getProvider();
            ObjectReader objectReader = provider.getObjectReader(rootObject.getClass());
            FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);

            if (value != null) {
                Class<?> valueClass = value.getClass();
                Class fieldClass = fieldReader.getFieldClass();
                if (valueClass != fieldClass) {
                    java.util.function.Function typeConvert = provider.getTypeConvert(valueClass, fieldClass);
                    if (typeConvert != null) {
                        value = typeConvert.apply(value);
                    }
                }
            }
            fieldReader.accept(rootObject, value);
        }

        @Override
        public void setInt(Object rootObject, int value) {
            if (rootObject instanceof Map) {
                ((Map) rootObject).put(name, value);
                return;
            }
            ObjectReaderProvider provider = getReaderContext().getProvider();
            ObjectReader objectReader = provider.getObjectReader(rootObject.getClass());
            objectReader.setFieldValue(rootObject, name, nameHashCode, value);
        }

        @Override
        public void setLong(Object rootObject, long value) {
            if (rootObject instanceof Map) {
                ((Map) rootObject).put(name, value);
                return;
            }
            ObjectReaderProvider provider = getReaderContext().getProvider();
            ObjectReader objectReader = provider.getObjectReader(rootObject.getClass());
            objectReader.setFieldValue(rootObject, name, nameHashCode, value);
        }

        @Override
        public Object extract(JSONReader jsonReader) {
            if (jsonReader.isJSONB()) {
                if (jsonReader.isObject()) {
                    jsonReader.nextIfObjectStart();
                    while (!jsonReader.nextIfObjectEnd()) {
                        long nameHashCode = jsonReader.readFieldNameHashCode();
                        if (nameHashCode == 0) {
                            continue;
                        }

                        boolean match = nameHashCode == this.nameHashCode;
                        if (!match && (!jsonReader.isObject()) && !jsonReader.isArray()) {
                            jsonReader.skipValue();
                            continue;
                        }

                        if (jsonReader.isNumber()) {
                            return jsonReader.readNumber();
                        }

                        throw new JSONException("TODO");
                    }
                }
                return null;
            }

            if (jsonReader.nextIfObjectStart()) {
                while (!jsonReader.nextIfObjectEnd()) {

                    long nameHashCode = jsonReader.readFieldNameHashCode();
                    boolean match = nameHashCode == this.nameHashCode;

                    char ch = jsonReader.ch;
                    if (!match) {
                        jsonReader.skipValue();
                        continue;
                    }

                    Object val;
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
                        case '9':
                            return jsonReader.readNumber();
                        case '[':
                            return jsonReader.readArray();
                        case '{':
                            return jsonReader.readObject();
                        case '"':
                        case '\'':
                            val = jsonReader.readString(); //
                            break;
                        case 't':
                        case 'f':
                            val = jsonReader.readBoolValue();
                            break;
                        case 'n':
                            jsonReader.readNull();
                            val = null;
                            break;
                        default:
                            throw new JSONException("TODO : " + jsonReader.ch);
                    }
                    return val;
                }
            }
            return null;
        }

        @Override
        public String extractScalar(JSONReader jsonReader) {
            if (jsonReader.nextIfObjectStart()) {
                for (; ; ) {
                    if (jsonReader.ch == '}') {
                        jsonReader.next();
                        break;
                    }

                    long nameHashCode = jsonReader.readFieldNameHashCode();

                    boolean match = nameHashCode == this.nameHashCode;
                    char ch = jsonReader.ch;
                    if (!match && ch != '{' && ch != '[') {
                        jsonReader.skipValue();
                        continue;
                    }

                    Object val;
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
                        case '9':
                            val = jsonReader.readNumber();
                            break;
                        case '[':
                            val = jsonReader.readArray();
                            break;
                        case '{':
                            val = jsonReader.readObject();
                            break;
                        case '"':
                        case '\'':
                            val = jsonReader.readString(); //
                            break;
                        case 't':
                        case 'f':
                            val = jsonReader.readBoolValue();
                            break;
                        case 'n':
                            jsonReader.readNull();
                            val = null;
                            break;
                        default:
                            throw new JSONException("TODO : " + jsonReader.ch);
                    }

                    return JSON.toJSONString(val);
                }
            }
            return null;
        }

        @Override
        public long extractInt64Value(JSONReader jsonReader) {
            if (jsonReader.nextIfObjectStart()) {
                _for:
                for (; ; ) {
                    if (jsonReader.ch == '}') {
                        jsonReader.wasNull = true;
                        return 0;
                    }

                    long nameHashCode = jsonReader.readFieldNameHashCode();

                    boolean match = nameHashCode == this.nameHashCode;
                    if (!match) {
                        jsonReader.skipValue();
                        continue;
                    }

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
                        case '9':
                            return jsonReader.readInt64Value();
                        case '[':
                        case '{':
                            Map object = jsonReader.readObject();
                            return jsonReader.toLong(object);
                        case '"':
                        case '\'':
                            String str = jsonReader.readString();
                            return Long.parseLong(str);
                        case 't':
                        case 'f':
                            boolean booleanValue = jsonReader.readBoolValue();
                            return booleanValue ? 1L : 0L;
                        case 'n':
                            jsonReader.readNull();
                            jsonReader.wasNull = true;
                            return 0;
                        case ']':
                            jsonReader.next();
                            break _for;
                        default:
                            throw new JSONException("TODO : " + jsonReader.ch);
                    }
                }
            }

            jsonReader.wasNull = true;
            return 0;
        }

        @Override
        public int extractInt32Value(JSONReader jsonReader) {
            if (jsonReader.nextIfObjectStart()) {
                _for:
                for (; ; ) {
                    if (jsonReader.ch == '}') {
                        jsonReader.wasNull = true;
                        return 0;
                    }

                    long nameHashCode = jsonReader.readFieldNameHashCode();

                    boolean match = nameHashCode == this.nameHashCode;
                    if (!match) {
                        jsonReader.skipValue();
                        continue;
                    }

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
                        case '9':
                            return jsonReader.readInt32Value();
                        case '"':
                        case '\'':
                            String str = jsonReader.readString(); //
                            return Integer.parseInt(str);
                        case 't':
                        case 'f':
                            boolean booleanValue = jsonReader.readBoolValue();
                            return booleanValue ? 1 : 0;
                        case 'n':
                            jsonReader.readNull();
                            jsonReader.wasNull = true;
                            return 0;
                        case ']':
                            jsonReader.next();
                            break _for;
                        default:
                            throw new JSONException("TODO : " + jsonReader.ch);
                    }
                }
            }

            jsonReader.wasNull = true;
            return 0;
        }

        @Override
        public void extractScalar(JSONReader jsonReader, ValueConsumer consumer) {
            if (jsonReader.nextIfObjectStart()) {
                _for:
                for (; ; ) {
                    if (jsonReader.ch == '}') {
                        consumer.acceptNull();
                        return;
                    }

                    long nameHashCode = jsonReader.readFieldNameHashCode();

                    boolean match = nameHashCode == this.nameHashCode;
                    if (!match) {
                        jsonReader.skipValue();
                        continue;
                    }

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
                            jsonReader.readNumber(consumer, false);
                            return;
                        }
                        case '[': {
                            List array = jsonReader.readArray();
                            consumer.accept(array);
                            return;
                        }
                        case '{': {
                            Map object = jsonReader.readObject();
                            consumer.accept(object);
                            return;
                        }
                        case '"':
                        case '\'': {
                            jsonReader.readString(consumer, false);
                            return;
                        }
                        case 't':
                        case 'f': {
                            consumer.accept(
                                    jsonReader.readBoolValue()
                            );
                            return;
                        }
                        case 'n':
                            jsonReader.readNull();
                            consumer.acceptNull();
                            return;
                        case ']':
                            jsonReader.next();
                            break _for;
                        default:
                            throw new JSONException("TODO : " + jsonReader.ch);
                    }
                }
            }

            consumer.acceptNull();
        }

        @Override
        public void extract(JSONReader jsonReader, ValueConsumer consumer) {
            if (jsonReader.nextIfObjectStart()) {
                for (; ; ) {
                    if (jsonReader.ch == '}') {
                        consumer.acceptNull();
                        return;
                    }

                    long nameHashCode = jsonReader.readFieldNameHashCode();

                    boolean match = nameHashCode == this.nameHashCode;
                    if (!match) {
                        jsonReader.skipValue();
                        continue;
                    }

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
                            jsonReader.readNumber(consumer, true);
                            return;
                        }
                        case '[': {
                            List array = jsonReader.readArray();
                            consumer.accept(array);
                            return;
                        }
                        case '{': {
                            Map object = jsonReader.readObject();
                            consumer.accept(object);
                            return;
                        }
                        case '"':
                        case '\'': {
                            jsonReader.readString(consumer, true);
                            return;
                        }
                        case 't':
                        case 'f': {
                            consumer.accept(
                                    jsonReader.readBoolValue()
                            );
                            return;
                        }
                        case 'n':
                            jsonReader.readNull();
                            consumer.acceptNull();
                            return;
                        default:
                            throw new JSONException("TODO : " + jsonReader.ch);
                    }
                }
            }

            consumer.acceptNull();
        }
    }

    static final class SingleSegmentPath extends JSONPath {
        final Segment segment;
        final boolean ref;

        public SingleSegmentPath(Segment segment, String path) {
            super(path);
            this.segment = segment;
            this.ref = segment instanceof IndexSegment || segment instanceof NameSegment;
        }

        @Override
        public boolean remove(Object root) {
            Context ctx = new Context(this, null, segment, null);
            ctx.root = root;
            return segment.remove(ctx);
        }

        @Override
        public boolean contains(Object root) {
            Context ctx = new Context(this, null, segment, null);
            ctx.root = root;
            return segment.contains(ctx);
        }

        @Override
        public boolean isRef() {
            return ref;
        }

        @Override
        public Object eval(Object root) {
            Context ctx = new Context(this, null, segment, null);
            ctx.root = root;
            segment.eval(ctx);
            return ctx.value;
        }

        @Override
        public void set(Object root, Object value) {
            Context ctx = new Context(this, null, segment, null);
            ctx.root = root;
            segment.set(ctx, value);
        }

        @Override
        public void setInt(Object root, int value) {
            Context ctx = new Context(this, null, segment, null);
            ctx.root = root;
            segment.setInt(ctx, value);
        }

        @Override
        public void setLong(Object root, long value) {
            Context ctx = new Context(this, null, segment, null);
            ctx.root = root;
            segment.setLong(ctx, value);
        }

        @Override
        public Object extract(JSONReader jsonReader) {
            Context ctx = new Context(this, null, segment, null);
            segment.accept(jsonReader, ctx);
            return ctx.value;
        }

        @Override
        public String extractScalar(JSONReader jsonReader) {
            Context ctx = new Context(this, null, segment, null);
            segment.accept(jsonReader, ctx);
            return JSON.toJSONString(ctx.value);
        }
    }

    static final class MultiSegmentPath extends JSONPath {
        final List<Segment> segments;
        final boolean ref;

        private MultiSegmentPath(String path, List<Segment> segments) {
            super(path);
            this.segments = segments;

            boolean ref = true;
            for (int i = 0, l = segments.size(); i < l; i++) {
                Segment segment = segments.get(i);
                if (segment instanceof IndexSegment || segment instanceof NameSegment) {
                    continue;
                }
                ref = false;
                break;
            }
            this.ref = ref;
        }

        @Override
        public boolean remove(Object root) {
            Context ctx = null;

            int size = segments.size();
            if (size == 0) {
                return false;
            }

            for (int i = 0; i < size; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;
                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }
                ctx = new Context(this, ctx, segment, nextSegment);
                if (i == 0) {
                    ctx.root = root;
                }

                if (i == size - 1) {
                    return segment.remove(ctx);
                }
                segment.eval(ctx);

                if (ctx.value == null) {
                    return false;
                }
            }

            return false;
        }

        @Override
        public boolean contains(Object root) {
            Context ctx = null;

            int size = segments.size();
            if (size == 0) {
                return root != null;
            }

            for (int i = 0; i < size; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;
                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }
                ctx = new Context(this, ctx, segment, nextSegment);
                if (i == 0) {
                    ctx.root = root;
                }

                if (i == size - 1) {
                    return segment.contains(ctx);
                }
                segment.eval(ctx);
            }

            return false;
        }

        @Override
        public boolean isRef() {
            return ref;
        }

        @Override
        public Object eval(Object root) {
            Context ctx = null;

            int size = segments.size();
            if (size == 0) {
                return root;
            }

            for (int i = 0; i < size; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;
                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }
                ctx = new Context(this, ctx, segment, nextSegment);
                if (i == 0) {
                    ctx.root = root;
                }

                segment.eval(ctx);
            }
            return ctx.value;
        }

        @Override
        public void set(Object root, Object value) {
            Context ctx = null;
            int size = segments.size();
            for (int i = 0; i < size - 1; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;
                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }
                ctx = new Context(this, ctx, segment, nextSegment);
                if (i == 0) {
                    ctx.root = root;
                }

                segment.eval(ctx);
            }
            ctx = new Context(this, ctx, segments.get(0), null);
            ctx.root = root;

            Segment segment = segments.get(size - 1);
            segment.set(ctx, value);
        }

        @Override
        public void setInt(Object rootObject, int value) {
            set(rootObject, value);
        }

        @Override
        public void setLong(Object rootObject, long value) {
            set(rootObject, value);
        }

        @Override
        public Object extract(JSONReader jsonReader) {
            if (jsonReader == null) {
                return null;
            }

            int size = segments.size();
            if (size == 0) {
                return null;
            }

            Context ctx = null;
            for (int i = 0; i < size; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;

                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }

                ctx = new Context(this, ctx, segment, nextSegment);
                segment.accept(jsonReader, ctx);
            }

            return ctx.value;
        }

        @Override
        public String extractScalar(JSONReader jsonReader) {
            int size = segments.size();
            if (size == 0) {
                return null;
            }

            Context ctx = null;
            for (int i = 0; i < size; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;

                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }

                ctx = new Context(this, ctx, segment, nextSegment);
                segment.accept(jsonReader, ctx);
            }

            return JSON.toJSONString(ctx.value);
        }
    }

    static final class Context {
        final JSONPath path;
        final Context parent;
        final Segment current;
        final Segment next;
        Object root;
        Object value;

        boolean eval = false;

        Context(JSONPath path, Context parent, Segment current, Segment next) {
            this.path = path;
            this.current = current;
            this.next = next;
            this.parent = parent;
        }
    }

    static abstract class Segment {
        public abstract void accept(JSONReader jsonReader, Context ctx);

        public abstract void eval(Context context);

        public boolean contains(Context context) {
            eval(context);
            return context.value != null;
        }

        public boolean remove(Context context) {
            throw new JSONException("UnsupportedOperation " + getClass());
        }

        public void set(Context context, Object value) {
            throw new JSONException("UnsupportedOperation " + getClass());
        }

        public void setInt(Context context, int value) {
            set(context, Integer.valueOf(value));
        }

        public void setLong(Context context, long value) {
            set(context, Long.valueOf(value));
        }
    }

    static final class KeysSegment extends Segment implements EvalSegment {
        static final KeysSegment INSTANCE = new KeysSegment();

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            if (jsonReader.isObject()) {
                jsonReader.next();
                JSONArray array = new JSONArray();
                while (!jsonReader.nextIfObjectEnd()) {
                    String fieldName = jsonReader.readFieldName();
                    array.add(fieldName);
                    jsonReader.skipValue();
                }
                ctx.value = array;
            }
        }

        @Override
        public void eval(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;
            if (object instanceof Map) {
                ctx.value = new JSONArray(((Map<?, ?>) object).keySet());
                return;
            }

            throw new JSONException("TODO");
        }
    }

    static final class LengthSegment extends Segment implements EvalSegment {
        static final LengthSegment INSTANCE = new LengthSegment();

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            if (ctx.parent == null) {
                ctx.root = jsonReader.readAny();
                ctx.eval = true;
            }
            eval(ctx);
        }

        @Override
        public void eval(Context ctx) {
            Object value = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (value == null) {
                return;
            }

            if (value instanceof Collection) {
                ctx.value = ((Collection<?>) value).size();
            } else if (value.getClass().isArray()) {
                ctx.value = Array.getLength(value);
            } else if (value instanceof Map) {
                ctx.value = ((Map<?, ?>) value).size();
            } else if (value instanceof String) {
                ctx.value = ((String) value).length();
            }
        }
    }

    static final class FloorSegment extends Segment implements EvalSegment {
        static final FloorSegment INSTANCE = new FloorSegment();

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            if (ctx.parent == null) {
                ctx.root = jsonReader.readAny();
                ctx.eval = true;
            }
            eval(ctx);
        }

        @Override
        public void eval(Context ctx) {
            Object value = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (value instanceof Double) {
                value = Math.floor((Double) value);
            } else if (value instanceof Float) {
                value = Math.floor((Float) value);
            } else if (value instanceof BigDecimal) {
                value = ((BigDecimal) value).setScale(0, RoundingMode.FLOOR);
            }

            if (value instanceof List) {
                List list = (List) value;
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    if (item instanceof Double) {
                        list.set(i, Math.floor((Double) item));
                    } else if (item instanceof Float) {
                        list.set(i, Math.floor((Float) item));
                    } else if (item instanceof BigDecimal) {
                        list.set(i, ((BigDecimal) item).setScale(0, RoundingMode.FLOOR));
                    }
                }
            }
            ctx.value = value;
        }
    }

    static final class TypeSegment extends Segment implements EvalSegment {
        static final TypeSegment INSTANCE = new TypeSegment();

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            if (ctx.parent == null) {
                ctx.root = jsonReader.readAny();
                ctx.eval = true;
            }
            eval(ctx);
        }

        @Override
        public void eval(Context ctx) {
            Object value = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            ctx.value = type(value);
        }
    }

    static String type(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Collection) {
            return "array";
        }

        if (value instanceof Number) {
            return "number";
        }

        if (value instanceof Boolean) {
            return "boolean";
        }

        if (value instanceof String
                || value instanceof UUID
                || value instanceof Enum) {
            return "string";
        }

        return "object";
    }

    static final class MinSegment extends Segment implements EvalSegment {
        static final MinSegment INSTANCE = new MinSegment();

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            eval(ctx);
        }

        @Override
        public void eval(Context ctx) {
            Object value = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (value == null) {
                return;
            }

            Object min = null;
            if (value instanceof Collection) {
                for (Object next : (Collection) value) {
                    if (next == null) {
                        continue;
                    }

                    if (min == null) {
                        min = next;
                    } else if (TypeUtils.compare(min, next) > 0) {
                        min = next;
                    }
                }
            } else if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                for (Object item : array) {
                    if (item == null) {
                        continue;
                    }

                    if (min == null) {
                        min = item;
                    } else if (TypeUtils.compare(min, item) > 0) {
                        min = item;
                    }
                }
            } else {
                throw new UnsupportedOperationException();
            }

            ctx.value = min;
            ctx.eval = true;
        }
    }

    static final class MaxSegment extends Segment implements EvalSegment {
        static final MaxSegment INSTANCE = new MaxSegment();

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            eval(ctx);
        }

        @Override
        public void eval(Context ctx) {
            Object value = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (value == null) {
                return;
            }

            Object max = null;
            if (value instanceof Collection) {
                for (Object next : (Collection) value) {
                    if (next == null) {
                        continue;
                    }

                    if (max == null) {
                        max = next;
                    } else if (TypeUtils.compare(max, next) < 0) {
                        max = next;
                    }
                }
            } else if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                for (Object item : array) {
                    if (item == null) {
                        continue;
                    }

                    if (max == null) {
                        max = item;
                    } else if (TypeUtils.compare(max, item) < 0) {
                        max = item;
                    }
                }
            } else {
                throw new UnsupportedOperationException();
            }

            ctx.value = max;
            ctx.eval = true;
        }
    }

    static final class NameSegment extends Segment {
        static final long HASH_NAME = Fnv.hashCode64("name");
        static final long HASH_ORDINAL = Fnv.hashCode64("ordinal");

        final String name;
        final long nameHashCode;

        public NameSegment(String name, long nameHashCode) {
            this.name = name;
            this.nameHashCode = nameHashCode;
        }

        @Override
        public boolean remove(Context context) {
            set(context, null);
            return context.eval = true;
        }

        @Override
        public boolean contains(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (object == null) {
                return false;
            }

            if (object instanceof Map) {
                return ((Map<?, ?>) object).get(name) != null;
            }

            if (object instanceof Collection) {
                for (Object item : (Collection) object) {
                    if (item == null) {
                        continue;
                    }

                    if (item instanceof Map) {
                        if (((Map<?, ?>) item).get(name) != null) {
                            return true;
                        }
                    }

                    ObjectWriter<?> objectWriter = ctx.path
                            .getWriterContext()
                            .getObjectWriter(item.getClass());
                    if (objectWriter instanceof ObjectWriterAdapter) {
                        FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                        if (fieldWriter != null) {
                            if (fieldWriter.getFieldValue(item) != null) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                for (Object item : array) {
                    if (item == null) {
                        continue;
                    }

                    if (item instanceof Map) {
                        if (((Map) item).get(name) != null) {
                            return true;
                        }
                    }

                    ObjectWriter<?> objectWriter = ctx.path
                            .getWriterContext()
                            .getObjectWriter(item.getClass());
                    if (objectWriter instanceof ObjectWriterAdapter) {
                        FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                        if (fieldWriter != null) {
                            if (fieldWriter.getFieldValue(item) != null) {
                                return true;
                            }
                        }
                    }
                }
            }

            ObjectWriter<?> objectWriter = ctx.path
                    .getWriterContext()
                    .getObjectWriter(object.getClass());
            if (objectWriter instanceof ObjectWriterAdapter) {
                FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                if (fieldWriter != null) {
                    return fieldWriter.getFieldValue(object) != null;
                }
            }

            return false;
        }

        @Override
        public void eval(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (object == null) {
                return;
            }

            if (object instanceof Map) {
                Map map = (Map) object;
                Object value = map.get(name);
                if (value == null) {
                    boolean isNum = IOUtils.isNumber(this.name);
                    Long longValue = null;

                    for (Object o : map.entrySet()) {
                        Map.Entry entry = (Map.Entry) o;
                        Object entryKey = entry.getKey();
                        if (entryKey instanceof Enum && ((Enum<?>) entryKey).name().equals(this.name)) {
                            value = entry.getValue();
                            break;
                        } else if (entryKey instanceof Long) {
                            if (longValue == null && isNum) {
                                longValue = Long.parseLong(this.name);
                            }
                            if (entryKey.equals(longValue)) {
                                value = entry.getValue();
                                break;
                            }
                        }
                    }
                }

                ctx.value = value;
                return;
            }

            if (object instanceof Collection) {
                Collection<?> collection = (Collection<?>) object;
                int size = collection.size();
                Collection values = null; // = new JSONArray(collection.size());
                for (Object item : collection) {
                    if (item instanceof Map) {
                        Object val = ((Map<?, ?>) item).get(name);
                        if (val instanceof Collection) {
                            if (size == 1) {
                                values = (Collection) val;
                            } else {
                                if (values == null) {
                                    values = new JSONArray(size);
                                }
                                values.addAll((Collection) val);
                            }
                        } else {
                            if (values == null) {
                                values = new JSONArray(size);
                            }
                            values.add(val);
                        }
                    }
                }
                ctx.value = values;
                return;
            }

            JSONWriter.Context writerContext = ctx.path.getWriterContext();
            ObjectWriter<?> objectWriter = writerContext.getObjectWriter(object.getClass());
            if (objectWriter instanceof ObjectWriterAdapter) {
                FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                if (fieldWriter != null) {
                    ctx.value = fieldWriter.getFieldValue(object);
                }

                return;
            }

            if (nameHashCode == HASH_NAME && object instanceof Enum) {
                ctx.value = ((Enum<?>) object).name();
                return;
            }

            if (nameHashCode == HASH_ORDINAL && object instanceof Enum) {
                ctx.value = ((Enum<?>) object).ordinal();
                return;
            }

            if (object instanceof String) {
                String str = (String) object;
                if (!str.isEmpty() && str.charAt(0) == '{') {
                    ctx.value =
                            JSONPath.of("$." + name)
                                    .extract(
                                            JSONReader.of(str));
                    return;
                }
            }

            throw new JSONException("not support : " + object.getClass());
        }

        @Override
        public void set(Context context, Object value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof Map) {
                ((Map) object).put(name, value);
                return;
            }

            ObjectReaderProvider provider = context.path.getReaderContext().getProvider();
            ObjectReader objectReader = provider.getObjectReader(object.getClass());
            FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);
            if (fieldReader == null) {
                return;
            }

            if (value != null) {
                Class<?> valueClass = value.getClass();
                Class fieldClass = fieldReader.getFieldClass();
                if (valueClass != fieldClass) {
                    java.util.function.Function typeConvert = provider.getTypeConvert(valueClass, fieldClass);
                    if (typeConvert != null) {
                        value = typeConvert.apply(value);
                    }
                }
            }
            fieldReader.accept(object, value);
        }

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            if (ctx.parent != null
                    && (ctx.parent.eval
                    || ctx.parent.current instanceof FilterSegment
                    || ctx.parent.current instanceof MultiIndexSegment)
            ) {
                eval(ctx);
                return;
            }

            if (jsonReader.isJSONB()) {
                if (jsonReader.nextIfObjectStart()) {
                    for (int i = 0; ; ++i) {
                        if (jsonReader.nextIfObjectEnd()) {
                            break;
                        }

                        long nameHashCode = jsonReader.readFieldNameHashCode();
                        if (nameHashCode == 0) {
                            continue;
                        }
                        boolean match = nameHashCode == this.nameHashCode;
                        if (!match) {
                            jsonReader.skipValue();
                            continue;
                        }

                        if (jsonReader.isArray() || jsonReader.isObject()) {
                            if (ctx.next != null) {
                                break;
                            }
                        }

                        ctx.value = jsonReader.readAny();
                        ctx.eval = true;
                        break;
                    }
                    return;
                } else if (jsonReader.isArray()
                        && ctx.parent != null
                        && ctx.parent.current instanceof AllSegment) {
                    List values = new JSONArray();
                    int itemCnt = jsonReader.startArray();
                    for (int i = 0; i < itemCnt; i++) {
                        if (jsonReader.nextIfMatch(BC_OBJECT)) {
                            for (int j = 0; ; j++) {
                                if (jsonReader.nextIfMatch(BC_OBJECT_END)) {
                                    break;
                                }

                                long nameHashCode = jsonReader.readFieldNameHashCode();
                                boolean match = nameHashCode == this.nameHashCode;

                                if (!match) {
                                    jsonReader.skipValue();
                                    continue;
                                }

                                if (jsonReader.isArray() || jsonReader.isObject()) {
                                    if (ctx.next != null) {
                                        break;
                                    }
                                }

                                values.add(jsonReader.readAny());
                            }
                        } else {
                            jsonReader.skipValue();
                        }
                    }

                    ctx.value = values;
                    ctx.eval = true;
                    return;
                }

                throw new JSONException("TODO");
            }

            if (jsonReader.nextIfObjectStart()) {
                if (jsonReader.ch == '}') {
                    jsonReader.next();
                    // return object;
                }

                _for:
                for (; ; ) {
                    if (jsonReader.nextIfObjectEnd()) {
                        jsonReader.next();
                        break;
                    }

                    long nameHashCode = jsonReader.readFieldNameHashCode();
                    boolean match = nameHashCode == this.nameHashCode;

                    if (!match) {
                        jsonReader.skipValue();
                        if (jsonReader.ch == ',') {
                            jsonReader.next();
                        }
                        continue;
                    }

                    Object val;
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
                        case '9':
                            jsonReader.readNumber0();
                            val = jsonReader.getNumber();
                            break;
                        case '[':
                            if (ctx.next != null && !(ctx.next instanceof EvalSegment)) {
                                break _for;
                            }
                            val = jsonReader.readArray();
                            break;
                        case '{':
                            if (ctx.next != null && !(ctx.next instanceof EvalSegment)) {
                                break _for;
                            }
                            val = jsonReader.readObject();
                            break;
                        case '"':
                        case '\'':
                            val = jsonReader.readString();
                            break;
                        case 't':
                        case 'f':
                            val = jsonReader.readBoolValue();
                            break;
                        case 'n':
                            jsonReader.readNull();
                            val = null;
                            break;
                        default:
                            throw new JSONException("TODO : " + jsonReader.ch);
                    }

                    ctx.value = val;
                    break;
                }
            } else if (jsonReader.ch == '[' && ctx.parent != null && ctx.parent.current instanceof AllSegment) {
                jsonReader.next();
                List values = new JSONArray();
                while (jsonReader.ch != JSONReader.EOI) {
                    if (jsonReader.ch == ']') {
                        jsonReader.next();
                        break;
                    }
                    if (jsonReader.ch == '{') {
                        jsonReader.next();

                        _for:
                        for (; ; ) {
                            if (jsonReader.ch == '}') {
                                jsonReader.next();
                                break;
                            }

                            long nameHashCode = jsonReader.readFieldNameHashCode();
                            boolean match = nameHashCode == this.nameHashCode;

                            if (!match) {
                                jsonReader.skipValue();
                                if (jsonReader.ch == ',') {
                                    jsonReader.next();
                                }
                                continue;
                            }

                            Object val;
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
                                case '9':
                                case '.':
                                    jsonReader.readNumber0();
                                    val = jsonReader.getNumber();
                                    break;
                                case '[':
                                    if (ctx.next != null) {
                                        break _for;
                                    }
                                    val = jsonReader.readArray();
                                    break;
                                case '{':
                                    if (ctx.next != null) {
                                        break _for;
                                    }
                                    val = jsonReader.readObject();
                                    break;
                                case '"':
                                case '\'':
                                    val = jsonReader.readString();
                                    break;
                                case 't':
                                case 'f':
                                    val = jsonReader.readBoolValue();
                                    break;
                                case 'n':
                                    jsonReader.readNull();
                                    val = null;
                                    break;
                                default:
                                    throw new JSONException("TODO : " + jsonReader.ch);
                            }
                            values.add(val);
                        }
                    } else {
                        jsonReader.skipValue();
                    }

                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                    }
                }

                ctx.value = values;
            }/* else if (jsonReader.ch == JSONReader.EOI) {
                return;
            }*/
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static final class CycleNameSegment extends Segment {
        final String name;
        final long nameHashCode;

        public CycleNameSegment(String name, long nameHashCode) {
            this.name = name;
            this.nameHashCode = nameHashCode;
        }

        @Override
        public String toString() {
            return ".." + name;
        }

        @Override
        public boolean remove(Context context) {
            set(context, null);
            return context.eval = true;
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            List values = new JSONArray();

            MapLoop action = new MapLoop(context, values);
            if (object instanceof Map) {
                Map map = (Map) object;
                map.forEach(action);
            } else if (object instanceof Collection) {
                ((Collection<?>) object).forEach(action);
            } else if (object != null) {
                ObjectWriter<?> objectWriter = context.path
                        .getWriterContext()
                        .getObjectWriter(object.getClass());
                if (objectWriter instanceof ObjectWriterAdapter) {
                    action.accept(object);
                }
            }


            if (values.size() == 1 && values.get(0) instanceof Collection) {
                context.value = values.get(0);
            } else {
                context.value = values;
            }
            context.eval = true;
        }

        @Override
        public void set(Context context, Object value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            LoopSet action = new LoopSet(context, value);
            action.accept(object);
        }

        class MapLoop implements BiConsumer, Consumer {
            final Context context;
            final List values;

            public MapLoop(Context context, List values) {
                this.context = context;
                this.values = values;
            }

            @Override
            public void accept(Object key, Object value) {
                if (name.equals(key)) {
                    values.add(value);
                    return;
                }

                if (value instanceof Map) {
                    ((Map<?, ?>) value).forEach(this);
                } else if (value instanceof List) {
                    ((List<?>) value).forEach(this);
                }
            }

            @Override
            public void accept(Object value) {
                if (value == null) {
                    return;
                }

                if (value instanceof Map) {
                    ((Map<?, ?>) value).forEach(this);
                } else if (value instanceof List) {
                    ((List<?>) value).forEach(this);
                } else {
                    ObjectWriter<?> objectWriter = context.path
                            .getWriterContext()
                            .getObjectWriter(value.getClass());
                    if (objectWriter instanceof ObjectWriterAdapter) {
                        FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                        if (fieldWriter != null) {
                            Object fieldValue = fieldWriter.getFieldValue(value);
                            if (fieldValue != null) {
                                values.add(fieldValue);
                            }
                            return;
                        }

                        for (int i = 0; i < objectWriter.getFieldWriters().size(); i++) {
                            fieldWriter = objectWriter.getFieldWriters().get(i);
                            Object fieldValue = fieldWriter.getFieldValue(value);
                            accept(fieldValue);
                        }

                        return;
                    }
                }
            }
        }

        class LoopSet {
            final Context context;
            final Object value;

            public LoopSet(Context context, Object value) {
                this.context = context;
                this.value = value;
            }

            public void accept(Object object) {
                if (object instanceof Map) {
                    for (Map.Entry entry : (Iterable<Map.Entry>) ((Map) object).entrySet()) {
                        if (name.equals(entry.getKey())) {
                            entry.setValue(value);
                            context.eval = true;
                        } else {
                            Object entryValue = entry.getValue();
                            if (entryValue != null) {
                                accept(entryValue);
                            }
                        }
                    }
                } else if (object instanceof Collection) {
                    for (Object item : ((List<?>) object)) {
                        accept(item);
                    }
                } else {
                    Class<?> entryValueClass = object.getClass();
                    ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(entryValueClass);
                    if (objectReader instanceof ObjectReaderBean) {
                        FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);
                        if (fieldReader != null) {
                            fieldReader.accept(object, value);
                            context.eval = true;
                            return;
                        }
                    }

                    ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(entryValueClass);
                    List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
                    for (FieldWriter fieldWriter : fieldWriters) {
                        Object fieldValue = fieldWriter.getFieldValue(object);
                        accept(fieldValue);
                    }
                }
            }
        }

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            List values = new JSONArray();
            accept(jsonReader, ctx, values);
            ctx.value = values;
            ctx.eval = true;
        }

        public void accept(JSONReader jsonReader, Context ctx, List<Object> values) {
            if (jsonReader.isJSONB()) {
                if (jsonReader.nextIfMatch(BC_OBJECT)) {
                    while (!jsonReader.nextIfMatch(BC_OBJECT_END)) {
                        long nameHashCode = jsonReader.readFieldNameHashCode();
                        if (nameHashCode == 0) {
                            continue;
                        }

                        boolean match = nameHashCode == this.nameHashCode;
                        if (match) {
                            if (jsonReader.isArray()) {
                                values.addAll(jsonReader.readArray());
                            } else {
                                values.add(jsonReader.readAny());
                            }
                        } else if (jsonReader.isObject() || jsonReader.isArray()) {
                            accept(jsonReader, ctx, values);
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    return;
                }

                if (jsonReader.isArray()) {
                    int itemCnt = jsonReader.startArray();
                    for (int i = 0; i < itemCnt; i++) {
                        if (jsonReader.isObject() || jsonReader.isArray()) {
                            accept(jsonReader, ctx, values);
                            continue;
                        }

                        jsonReader.skipValue();
                    }
                } else {
                    jsonReader.skipValue();
                }
                return;
            }

            if (jsonReader.ch == '{') {
                jsonReader.next();
                _for:
                for (; ; ) {
                    if (jsonReader.ch == '}') {
                        jsonReader.next();
                        break;
                    }

                    long nameHashCode = jsonReader.readFieldNameHashCode();

                    boolean match = nameHashCode == this.nameHashCode;
                    char ch = jsonReader.ch;
                    if (!match && ch != '{' && ch != '[') {
                        jsonReader.skipValue();
                        continue;
                    }

                    Object val;
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
                        case '9':
                            jsonReader.readNumber0();
                            val = jsonReader.getNumber();
                            break;
                        case '[':
                        case '{':
                            if (match) {
                                val = ch == '[' ? jsonReader.readArray() : jsonReader.readObject();
                                break;
                            }
                            accept(jsonReader, ctx, values);
                            continue _for;
                        case '"':
                        case '\'':
                            val = jsonReader.readString();
                            break;
                        case 't':
                        case 'f':
                            val = jsonReader.readBoolValue();
                            break;
                        case 'n':
                            jsonReader.readNull();
                            val = null;
                            break;
                        default:
                            throw new JSONException("TODO : " + jsonReader.ch);
                    }
                    if (val instanceof Collection) {
                        values.addAll((Collection) val);
                    } else {
                        values.add(val);
                    }

                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                    }
                }

                if (jsonReader.ch == ',') {
                    jsonReader.next();
                }
            } else if (jsonReader.ch == '[') {
                jsonReader.next();

                for (; ; ) {
                    if (jsonReader.ch == ']') {
                        jsonReader.next();
                        break;
                    }

                    if (jsonReader.ch == '{' || jsonReader.ch == '[') {
                        accept(jsonReader, ctx, values);
                    } else {
                        jsonReader.skipValue();
                    }

                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                        break;
                    }
                }

                if (jsonReader.ch == ',') {
                    jsonReader.next();
                }
            } else {
                jsonReader.skipValue();
            }
        }
    }

    static final class IndexSegment extends Segment {
        static final IndexSegment ZERO = new IndexSegment(0);
        static final IndexSegment ONE = new IndexSegment(1);
        static final IndexSegment TWO = new IndexSegment(2);

        final int index;

        public IndexSegment(int index) {
            this.index = index;
        }

        static IndexSegment of(int index) {
            if (index == 0) {
                return ZERO;
            }
            if (index == 1) {
                return ONE;
            }
            if (index == 2) {
                return TWO;
            }
            return new IndexSegment(index);
        }

        @Override
        public void eval(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (object == null) {
                ctx.eval = true;
                return;
            }

            if (object instanceof java.util.List) {
                List list = (List) object;
                if (index >= 0) {
                    if (index < list.size()) {
                        ctx.value = list.get(index);
                    }
                } else {
                    int itemIndex = list.size() + this.index;
                    if (itemIndex >= 0) {
                        ctx.value = list.get(itemIndex);
                    }
                }
                ctx.eval = true;
                return;
            }

            if ((object instanceof SortedSet || object instanceof LinkedHashSet)
                    || (index == 0 && object instanceof Collection && ((Collection<?>) object).size() == 1)
            ) {
                Collection collection = (Collection) object;
                int i = 0;
                for (Iterator it = collection.iterator(); it.hasNext(); ++i) {
                    Object item = it.next();
                    if (i == index) {
                        ctx.value = item;
                        break;
                    }
                }
                ctx.eval = true;
                return;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                if (index >= 0) {
                    if (index < array.length) {
                        ctx.value = array[index];
                    }
                } else {
                    int itemIndex = array.length + this.index;
                    if (itemIndex >= 0) {
                        ctx.value = array[itemIndex];
                    }
                }
                ctx.eval = true;
                return;
            }

            Class objectClass = object.getClass();
            if (objectClass.isArray()) {
                int length = Array.getLength(object);
                if (index >= 0) {
                    if (index < length) {
                        ctx.value = Array.get(object, index);
                    }
                } else {
                    int itemIndex = length + this.index;
                    if (itemIndex >= 0) {
                        ctx.value = Array.get(object, itemIndex);
                    }
                }
                ctx.eval = true;
                return;
            }

            if (Map.class.isAssignableFrom(objectClass)) {
                Map map = (Map) object;
                Object value = map.get(index);
                if (value == null) {
                    value = map.get(Integer.toString(index));
                }

                if (value == null) {
                    int size = map.size();
                    Iterator it = map.entrySet().iterator();
                    if (size == 1 || map instanceof LinkedHashMap || map instanceof SortedMap) {
                        for (int i = 0; i <= index && i < size && it.hasNext(); ++i) {
                            Map.Entry entry = (Map.Entry) it.next();
                            Object entryKey = entry.getKey();
                            Object entryValue = entry.getValue();
                            if (entryKey instanceof Long) {
                                if (entryKey.equals(Long.valueOf(index))) {
                                    value = entryValue;
                                    break;
                                }
                            } else {
                                if (i == index) {
                                    value = entryValue;
                                }
                            }
                        }
                    } else {
                        for (int i = 0; i <= index && i < map.size() && it.hasNext(); ++i) {
                            Map.Entry entry = (Map.Entry) it.next();
                            Object entryKey = entry.getKey();
                            Object entryValue = entry.getValue();
                            if (entryKey instanceof Long) {
                                if (entryKey.equals(Long.valueOf(index))) {
                                    value = entryValue;
                                    break;
                                }
                            }
                        }
                    }
                }
                ctx.value = value;
                ctx.eval = true;
                return;
            }

            throw new JSONException("jsonpath not support operate : " + ctx.path + ", objectClass" + objectClass.getName());
        }

        @Override
        public void set(Context ctx, Object value) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (object instanceof java.util.List) {
                List list = (List) object;
                if (index >= 0) {
                    if (index < list.size()) {
                        list.set(index, value);
                    }
                } else {
                    int itemIndex = list.size() + this.index;
                    if (itemIndex >= 0) {
                        list.set(itemIndex, value);
                    }
                }
                return;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                if (index >= 0) {
                    array[index] = value;
                } else {
                    array[array.length + index] = value;
                }
                return;
            }

            if (object != null && object.getClass().isArray()) {
                int length = Array.getLength(object);
                if (index >= 0) {
                    if (index < length) {
                        Array.set(object, index, value);
                    }
                } else {
                    int arrayIndex = length + index;
                    if (arrayIndex >= 0) {
                        Array.set(object, arrayIndex, value);
                    }
                }
                return;
            }

            throw new JSONException("UnsupportedOperation");
        }

        @Override
        public void setInt(Context ctx, int value) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;
            if (object instanceof int[]) {
                int[] array = (int[]) object;
                if (index >= 0) {
                    if (index < array.length) {
                        array[index] = value;
                    }
                } else {
                    int arrayIndex = array.length + index;
                    if (arrayIndex >= 0) {
                        array[arrayIndex] = value;
                    }
                }
                return;
            }

            if (object instanceof long[]) {
                long[] array = (long[]) object;
                if (index >= 0) {
                    if (index < array.length) {
                        array[index] = value;
                    }
                } else {
                    int arrayIndex = array.length + index;
                    if (arrayIndex >= 0) {
                        array[arrayIndex] = value;
                    }
                }
                return;
            }

            set(ctx, value);
        }

        @Override
        public void setLong(Context ctx, long value) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;
            if (object instanceof int[]) {
                int[] array = (int[]) object;
                if (index >= 0) {
                    if (index < array.length) {
                        array[index] = (int) value;
                    }
                } else {
                    int arrayIndex = array.length + index;
                    if (arrayIndex >= 0) {
                        array[arrayIndex] = (int) value;
                    }
                }
                return;
            }

            if (object instanceof long[]) {
                long[] array = (long[]) object;
                if (index >= 0) {
                    if (index < array.length) {
                        array[index] = value;
                    }
                } else {
                    int arrayIndex = array.length + index;
                    if (arrayIndex >= 0) {
                        array[arrayIndex] = value;
                    }
                }
                return;
            }

            set(ctx, value);
        }

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            if (ctx.parent != null
                    && (ctx.parent.eval
                    || (ctx.parent.current instanceof CycleNameSegment && ctx.next == null))
            ) {
                eval(ctx);
                return;
            }

            if (jsonReader.isJSONB()) {
                int itemCnt = jsonReader.startArray();
                for (int i = 0; i < itemCnt; i++) {
                    boolean match = index == i;
                    if (!match) {
                        jsonReader.skipValue();
                        continue;
                    }

                    if (jsonReader.isArray() || jsonReader.isObject()) {
                        if (ctx.next != null) {
                            break;
                        }
                    }

                    ctx.value = jsonReader.readAny();
                    ctx.eval = true;
                    break;
                }
                return;
            }

            jsonReader.next();
            _for:
            for (int i = 0; jsonReader.ch != JSONReader.EOI; ++i) {
                if (jsonReader.ch == ']') {
                    jsonReader.next();
                    break;
                }

                boolean match = index == i;

                if (!match) {
                    jsonReader.skipValue();
                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                    }
                    continue;
                }

                Object val;
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
                    case '9':
                    case '.':
                        jsonReader.readNumber0();
                        val = jsonReader.getNumber();
                        break;
                    case '[':
                        if (ctx.next != null && !(ctx.next instanceof EvalSegment)) {
                            break _for;
                        }
                        val = jsonReader.readArray();
                        break;
                    case '{':
                        if (ctx.next != null && !(ctx.next instanceof EvalSegment)) {
                            break _for;
                        }
                        val = jsonReader.readObject();
                        break;
                    case '"':
                    case '\'':
                        val = jsonReader.readString();
                        break;
                    case 't':
                    case 'f':
                        val = jsonReader.readBoolValue();
                        break;
                    case 'n':
                        jsonReader.readNull();
                        val = null;
                        break;
                    default:
                        throw new JSONException("TODO : " + jsonReader.ch);
                }

                ctx.value = val;
            }
        }

        @Override
        public String toString() {
            int size = (index < 0) ? IOUtils.stringSize(-index) + 1 : IOUtils.stringSize(index);
            byte[] bytes = new byte[size + 2];
            bytes[0] = '[';
            IOUtils.getChars(index, bytes.length - 1, bytes);
            bytes[bytes.length - 1] = ']';

            String str;
            if (JDKUtils.UNSAFE_ASCII_CREATOR != null) {
                str = JDKUtils.UNSAFE_ASCII_CREATOR.apply(bytes);
            } else {
                str = new String(bytes, StandardCharsets.US_ASCII);
            }
            return str;
        }
    }

    static final class RandomIndexSegment extends Segment {
        public static final RandomIndexSegment INSTANCE = new RandomIndexSegment();

        Random random = new Random();

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            throw new JSONException("not support");
        }

        @Override
        public void eval(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            if (object instanceof java.util.List) {
                List list = (List) object;
                if (list.isEmpty()) {
                    return;
                }

                int randomIndex = Math.abs(random.nextInt()) % list.size();
                ctx.value = list.get(randomIndex);
                ctx.eval = true;
                return;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                if (array.length == 0) {
                    return;
                }

                int randomIndex = random.nextInt() % array.length;
                ctx.value = array[randomIndex];
                ctx.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }
    }

    static final class RangeIndexSegment extends Segment {
        final int begin;
        final int end;

        public RangeIndexSegment(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void eval(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            List result = new JSONArray();

            if (object instanceof java.util.List) {
                List list = (List) object;
                for (int i = 0, size = list.size(); i < size; i++) {
                    boolean match;
                    if (begin >= 0) {
                        match = i >= begin && i < end;
                    } else {
                        int ni = i - size;
                        match = ni >= begin && ni < end;
                    }
                    if (match) {
                        result.add(list.get(i));
                    }
                }
                ctx.value = result;
                ctx.eval = true;
                return;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                for (int i = 0; i < array.length; i++) {
                    boolean match = i >= begin && i <= end
                            || i - array.length > begin && i - array.length <= end;
                    if (match) {
                        result.add(array[i]);
                    }
                }
                ctx.value = result;
                ctx.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            if (ctx.parent != null
                    && (ctx.parent.eval
                    || (ctx.parent.current instanceof CycleNameSegment && ctx.next == null)
            )
            ) {
                eval(ctx);
                return;
            }

            if (jsonReader.isJSONB()) {
                JSONArray array = new JSONArray();

                {
                    int itemCnt = jsonReader.startArray();
                    for (int i = 0; i < itemCnt; i++) {
                        boolean match = begin < 0 || i >= begin && i < end;

                        if (!match) {
                            jsonReader.skipValue();
                            continue;
                        }

                        array.add(
                                jsonReader.readAny());
                    }
                }

                if (begin < 0) {
                    for (int size = array.size(), i = size - 1; i >= 0; i--) {
                        int ni = i - size;
                        if (ni < begin || ni >= end) {
                            array.remove(i);
                        }
                    }
                }

                ctx.value = array;
                ctx.eval = true;
                return;
            }

            JSONArray array = new JSONArray();
            jsonReader.next();
            for (int i = 0; jsonReader.ch != JSONReader.EOI; ++i) {
                if (jsonReader.ch == ']') {
                    jsonReader.next();
                    break;
                }

                boolean match = begin < 0 || i >= begin && i < end;

                if (!match) {
                    jsonReader.skipValue();
                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                    }
                    continue;
                }

                Object val;
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
                    case '9':
                    case '.':
                        jsonReader.readNumber0();
                        val = jsonReader.getNumber();
                        break;
                    case '[':
                        val = jsonReader.readArray();
                        break;
                    case '{':
                        val = jsonReader.readObject();
                        break;
                    case '"':
                    case '\'':
                        val = jsonReader.readString();
                        break;
                    case 't':
                    case 'f':
                        val = jsonReader.readBoolValue();
                        break;
                    case 'n':
                        jsonReader.readNull();
                        val = null;
                        break;
                    default:
                        throw new JSONException("TODO : " + jsonReader.ch);
                }

                array.add(val);
            }

            if (begin < 0) {
                for (int size = array.size(), i = size - 1; i >= 0; i--) {
                    int ni = i - size;
                    if (ni < begin || ni >= end) {
                        array.remove(i);
                    }
                }
            }
            ctx.value = array;
            ctx.eval = true;
        }
    }

    static final class MultiIndexSegment extends Segment {
        final int[] indexes;

        public MultiIndexSegment(int[] indexes) {
            Arrays.sort(indexes);
            this.indexes = indexes;
        }

        @Override
        public void eval(Context ctx) {
            Object object = ctx.parent == null
                    ? ctx.root
                    : ctx.parent.value;

            List result = new JSONArray();

            if (object instanceof java.util.List) {
                List list = (List) object;
                for (int i = 0, l = list.size(); i < l; i++) {
                    boolean match = Arrays.binarySearch(indexes, i) >= 0
                            || Arrays.binarySearch(indexes, i - l) >= 0;
                    if (match) {
                        result.add(list.get(i));
                    }
                }
                ctx.value = result;
                return;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                for (int i = 0; i < array.length; i++) {
                    boolean match = Arrays.binarySearch(indexes, i) >= 0
                            || Arrays.binarySearch(indexes, i - array.length) >= 0;
                    if (match) {
                        result.add(array[i]);
                    }
                }
                ctx.value = result;
                return;
            }

            throw new JSONException("TODO");
        }

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            if (ctx.parent != null
                    && ctx.parent.current instanceof CycleNameSegment
                    && ctx.next == null) {
                eval(ctx);
                return;
            }

            if (jsonReader.isJSONB()) {
                JSONArray array = new JSONArray();
                int itemCnt = jsonReader.startArray();
                for (int i = 0; i < itemCnt; i++) {
                    boolean match = Arrays.binarySearch(indexes, i) >= 0;
                    if (!match) {
                        jsonReader.skipValue();
                        continue;
                    }

                    array.add(jsonReader.readAny());
                }
                ctx.value = array;
                return;
            }

            JSONArray array = new JSONArray();

            jsonReader.next();
            for (int i = 0; jsonReader.ch != JSONReader.EOI; ++i) {
                if (jsonReader.ch == ']') {
                    jsonReader.next();
                    break;
                }

                boolean match = Arrays.binarySearch(indexes, i) >= 0;

                if (!match) {
                    jsonReader.skipValue();
                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                    }
                    continue;
                }

                Object val;
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
                    case '9':
                    case '.':
                        jsonReader.readNumber0();
                        val = jsonReader.getNumber();
                        break;
                    case '[':
                        val = jsonReader.readArray();
                        break;
                    case '{':
                        val = jsonReader.readObject();
                        break;
                    case '"':
                    case '\'':
                        val = jsonReader.readString();
                        break;
                    case 't':
                    case 'f':
                        val = jsonReader.readBoolValue();
                        break;
                    case 'n':
                        jsonReader.readNull();
                        val = null;
                        break;
                    default:
                        throw new JSONException("TODO : " + jsonReader.ch);
                }

                array.add(val);
            }
            ctx.value = array;
        }
    }

    static final class MultiNameSegment extends Segment {
        final String[] names;
        final long[] nameHashCodes;
        final Set<String> nameSet;

        public MultiNameSegment(String[] names) {
            this.names = names;
            this.nameHashCodes = new long[names.length];
            this.nameSet = new HashSet<>();
            for (int i = 0; i < names.length; i++) {
                nameHashCodes[i] = Fnv.hashCode64(names[i]);
                nameSet.add(names[i]);
            }

        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof Map) {
                Map map = (Map) object;
                JSONArray array = new JSONArray(names.length);
                for (String name : names) {
                    Object value = map.get(name);
                    array.add(value);
                }
                context.value = array;
                return;
            }

            if (object instanceof Collection) {
                // skip
                context.value = object;
                return;
            }

            ObjectWriterProvider provider = context.path.getWriterContext().getProvider();
            ObjectWriter objectWriter = provider.getObjectWriter(object.getClass());

            JSONArray array = new JSONArray(names.length);
            for (int i = 0; i < names.length; i++) {
                FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCodes[i]);
                Object fieldValue = null;
                if (fieldWriter != null) {
                    fieldValue = fieldWriter.getFieldValue(object);
                }
                array.add(fieldValue);
            }
            context.value = array;
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (context.parent != null
                    && (context.parent.eval
                    || context.parent.current instanceof FilterSegment
                    || context.parent.current instanceof MultiIndexSegment)
            ) {
                eval(context);
                return;
            }

            Object object = jsonReader.readAny();
            if (object instanceof Map) {
                Map map = (Map) object;
                JSONArray array = new JSONArray(names.length);
                for (String name : names) {
                    Object value = map.get(name);
                    array.add(value);
                }
                context.value = array;
                return;
            }

            if (object instanceof Collection) {
                // skip
                context.value = object;
                return;
            }

            ObjectWriterProvider provider = context.path.getWriterContext().getProvider();
            ObjectWriter objectWriter = provider.getObjectWriter(object.getClass());

            JSONArray array = new JSONArray(names.length);
            for (int i = 0; i < names.length; i++) {
                FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCodes[i]);
                Object fieldValue = null;
                if (fieldWriter != null) {
                    fieldValue = fieldWriter.getFieldValue(object);
                }
                array.add(fieldValue);
            }
            context.value = array;
            return;
        }
    }

    static final class AllSegment extends Segment {
        static final AllSegment INSTANCE = new AllSegment();

        public AllSegment() {
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof Map) {
                Map map = (Map) object;
                JSONArray array = new JSONArray(map.size());
                for (Object value : map.values()) {
                    if (value instanceof Collection) {
                        array.addAll((Collection) value);
                    } else {
                        array.add(value);
                    }
                }
                context.value = array;
                context.eval = true;
                return;
            }

            if (object instanceof Collection) {
                // skip
                context.value = object;
                context.eval = true;
                return;
            }

            ObjectWriterProvider provider = context.path.getWriterContext().getProvider();
            ObjectWriter objectWriter = provider.getObjectWriter(object.getClass());
            List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
            int size = fieldWriters.size();
            JSONArray array = new JSONArray(size);
            for (int i = 0; i < size; i++) {
                Object fieldValue = fieldWriters.get(i).getFieldValue(object);
                array.add(fieldValue);
            }
            context.value = array;
            context.eval = true;
        }

        @Override
        public void accept(JSONReader jsonReader, Context ctx) {
            if (ctx.parent != null && ctx.parent.eval) {
                eval(ctx);
                return;
            }

            List<Object> values = new JSONArray();

            if (jsonReader.isJSONB()) {
                if (jsonReader.nextIfMatch(BC_OBJECT)) {
                    while (!jsonReader.nextIfMatch(BC_OBJECT_END)) {
                        if (jsonReader.skipName()) {
                            Object val = jsonReader.readAny();

                            if (val instanceof Collection) {
                                values.addAll((Collection) val);
                            } else {
                                values.add(val);
                            }
                        }
                    }

                    ctx.value = values;
                    return;
                }

                if (jsonReader.isArray() && ctx.next != null) {
                    // skip
                    return;
                }

                throw new JSONException("TODO");
            }

            if (jsonReader.nextIfMatch('{')) {
                _for:
                for (; ; ) {
                    if (jsonReader.ch == '}') {
                        jsonReader.next();
                        break;
                    }

                    jsonReader.skipName();
                    Object val;
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
                        case '9':
                            jsonReader.readNumber0();
                            val = jsonReader.getNumber();
                            break;
                        case '[':
                            val = jsonReader.readArray();
                            break;
                        case '{':
                            val = jsonReader.readObject();
                            break;
                        case '"':
                        case '\'':
                            val = jsonReader.readString();
                            break;
                        case 't':
                        case 'f':
                            val = jsonReader.readBoolValue();
                            break;
                        case 'n':
                            jsonReader.readNull();
                            val = null;
                            break;
                        case ']':
                            jsonReader.next();
                            break _for;
                        default:
                            throw new JSONException("TODO : " + jsonReader.ch);
                    }

                    if (val instanceof Collection) {
                        values.addAll((Collection) val);
                    } else {
                        values.add(val);
                    }

                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                    }
                }
                ctx.value = values;
                return;
            }

            if (jsonReader.ch == '[') {
                // skip
                if (ctx.next != null) {
                    return;
                }

                jsonReader.next();;
                for (; ; ) {
                    if (jsonReader.ch == ']') {
                        jsonReader.next();
                        break;
                    }
                    Object value = jsonReader.readAny();
                    values.add(value);
                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                    }
                }
                ctx.value = values;
                return;
            }

            throw new JSONException("TODO");
        }
    }
}
