package com.alibaba.fastjson2;

import com.alibaba.fastjson2.function.impl.ToDouble;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT;
import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT_END;
import static com.alibaba.fastjson2.JSONReader.EOI;
import static com.alibaba.fastjson2.util.JDKUtils.LATIN1;
import static com.alibaba.fastjson2.util.JDKUtils.STRING_CREATOR_JDK11;

public abstract class JSONPath {
    static final JSONReader.Context PARSE_CONTEXT = JSONFactory.createReadContext();

    JSONReader.Context readerContext;
    JSONWriter.Context writerContext;
    final String path;
    final long features;

    protected JSONPath(String path, Feature... features) {
        this.path = path;
        long featuresValue = 0;
        for (Feature feature : features) {
            featuresValue |= feature.mask;
        }
        this.features = featuresValue;
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

    public static Object extract(String json, String path, Feature... features) {
        JSONReader jsonReader = JSONReader.of(json);
        JSONPath jsonPath = JSONPath.of(path, features);
        return jsonPath.extract(jsonReader);
    }

    public static Object eval(String str, String path) {
        return extract(str, path);
    }

    public static Object eval(Object rootObject, String path) {
        return JSONPath.of(path)
                .eval(rootObject);
    }

    public static String set(String json, String path, Object value) {
        Object object = JSON.parse(json);
        JSONPath.of(path)
                .set(object, value);
        return JSON.toJSONString(object);
    }

    public static boolean contains(Object rootObject, String path) {
        if (rootObject == null) {
            return false;
        }

        JSONPath jsonPath = JSONPath.of(path);
        return jsonPath.contains(rootObject);
    }

    public static Object set(Object rootObject, String path, Object value) {
        JSONPath.of(path)
                .set(rootObject, value);

        return rootObject;
    }

    public static Object setCallback(Object rootObject, String path, Function callback) {
        JSONPath.of(path)
                .setCallback(rootObject, callback);

        return rootObject;
    }

    public static Object setCallback(Object rootObject, String path, BiFunction callback) {
        JSONPath.of(path)
                .setCallback(rootObject, callback);

        return rootObject;
    }

    public static String remove(String json, String path) {
        Object object = JSON.parse(json);

        JSONPath.of(path)
                .remove(object);

        return JSON.toJSONString(object);
    }

    public static void remove(Object rootObject, String path) {
        JSONPath.of(path)
                .remove(rootObject);
    }

    public static Map<String, Object> paths(Object javaObject) {
        Map<Object, String> values = new IdentityHashMap<>();
        Map<String, Object> paths = new HashMap<>();

        RootPath.INSTANCE.paths(values, paths, "$", javaObject);
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
                    || javaObject instanceof Enum
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
                    String path = parent + "." + key;
                    paths(values, paths, path, entry.getValue());
                }
            }
            return;
        }

        if (javaObject instanceof Collection) {
            Collection collection = (Collection) javaObject;

            int i = 0;
            for (Object item : collection) {
                String path = parent + "[" + i + "]";
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

                String path = parent + "[" + i + "]";
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
                        String path = parent + "." + key;
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
        if (result == null) {
            set(root, JSONArray.of(values));
            return;
        }

        if (result instanceof Collection) {
            Collection collection = (Collection) result;
            for (Object value : values) {
                collection.add(value);
            }
        }
    }

    public abstract boolean contains(Object object);

    public abstract Object eval(Object object);

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

    public abstract void set(Object object, Object value);

    public abstract void set(Object object, Object value, JSONReader.Feature... readerFeatures);

    public void setCallback(Object object, Function callback) {
        setCallback(
                object,
                new BiFunctionAdapter(callback)
        );
    }

    public abstract void setCallback(Object object, BiFunction callback);

    public abstract void setInt(Object object, int value);

    public abstract void setLong(Object object, long value);

    public abstract boolean remove(Object object);

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

    @Deprecated
    public static JSONPath compile(String path) {
        return of(path);
    }

    public static JSONPath compile(String strPath, Class objectClass) {
        JSONPath path = of(strPath);
        JSONFactory.JSONPathCompiler compiler = JSONFactory.getDefaultJSONPathCompiler();
        return compiler.compile(objectClass, path);
    }

    public static JSONPath of(String path) {
        if ("#-1".equals(path)) {
            return PreviousPath.INSTANCE;
        }

        return new JSONPathParser(path)
                .parse();
    }

    public static JSONPath of(String path, Feature... features) {
        if ("#-1".equals(path)) {
            return PreviousPath.INSTANCE;
        }

        return new JSONPathParser(path)
                .parse(features);
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
                    operator = Operator.EQ;
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
                if ("like".equalsIgnoreCase(fieldName)) {
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

                if ("nin".equalsIgnoreCase(fieldName)) {
                    operator = Operator.NOT_IN;
                    break;
                }

                if (!"not".equalsIgnoreCase(fieldName)) {
                    throw new JSONException("not support operator : " + fieldName);
                }

                jsonReader.readFieldNameHashCodeUnquote();
                fieldName = jsonReader.getFieldName();
                if ("like".equalsIgnoreCase(fieldName)) {
                    operator = Operator.NOT_LIKE;
                } else if ("rlike".equalsIgnoreCase(fieldName)) {
                    operator = Operator.NOT_RLIKE;
                } else if ("in".equalsIgnoreCase(fieldName)) {
                    operator = Operator.NOT_IN;
                } else if ("between".equalsIgnoreCase(fieldName)) {
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
                if ("in".equalsIgnoreCase(fieldName)) {
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
                if ("rlike".equalsIgnoreCase(fieldName)) {
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
                if ("between".equalsIgnoreCase(fieldName)) {
                    operator = Operator.BETWEEN;
                } else {
                    throw new JSONException("not support operator : " + fieldName);
                }
                break;
            }
            case 's':
            case 'S': {
                jsonReader.readFieldNameHashCodeUnquote();
                String fieldName = jsonReader.getFieldName();
                if (!"starts".equalsIgnoreCase(fieldName)) {
                    throw new JSONException("not support operator : " + fieldName);
                }

                jsonReader.readFieldNameHashCodeUnquote();
                fieldName = jsonReader.getFieldName();
                if (!"with".equalsIgnoreCase(fieldName)) {
                    throw new JSONException("not support operator : " + fieldName);
                }
                operator = Operator.STARTS_WITH;
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
        REG_MATCH,
        STARTS_WITH
    }

    abstract static class FilterSegment
            extends Segment
            implements EvalSegment {
        abstract boolean apply(Context context, Object object);
    }

    interface EvalSegment {
    }

    static final class NameIntOpSegment
            extends NameFilter {
        final Operator operator;
        final long value;

        public NameIntOpSegment(
                String name,
                long nameHashCode,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                Function expr,
                Operator operator,
                long value) {
            super(name, nameHashCode, fieldName2, fieldNameNameHash2, expr);
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
            } else if (fieldValue instanceof String) {
                String fieldValueStr = (String) fieldValue;
                if (IOUtils.isNumber(fieldValueStr)) {
                    try {
                        cmp = Long.valueOf(Long.parseLong(fieldValueStr)).compareTo(Long.valueOf(value));
                    } catch (Exception ignored) {
                        cmp = fieldValueStr.compareTo(Long.toString(value));
                    }
                } else {
                    cmp = fieldValueStr.compareTo(Long.toString(value));
                }
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

        @Override
        public void set(Context context, Object value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    if (apply(context, item)) {
                        list.set(i, value);
                    }
                }
                return;
            }

            throw new JSONException("UnsupportedOperation ");
        }
    }

    static final class NameDecimalOpSegment
            extends NameFilter {
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

    static final class NameRLikeSegment
            extends NameFilter {
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

    static final class StartsWithSegment
            extends NameFilter {
        final String prefix;

        public StartsWithSegment(String fieldName, long fieldNameNameHash, String prefix) {
            super(fieldName, fieldNameNameHash);
            this.prefix = prefix;
        }

        @Override
        boolean apply(Object fieldValue) {
            String propertyValue = fieldValue.toString();
            return propertyValue != null && propertyValue.startsWith(prefix);
        }
    }

    static final class GroupFilter
            extends Segment
            implements EvalSegment {
        final boolean and;
        final List<FilterSegment> filters;

        public GroupFilter(List<FilterSegment> filters, boolean and) {
            this.and = and;
            this.filters = filters;
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (context.parent == null) {
                context.root = jsonReader.readAny();
            }
            eval(context);
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                JSONArray array = new JSONArray(list.size());
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    boolean match = and;
                    for (FilterSegment filter : filters) {
                        boolean result = filter.apply(context, item);
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
                context.value = array;
                context.eval = true;
                return;
            }

            boolean match = and;
            for (FilterSegment filter : filters) {
                boolean result = filter.apply(context, object);
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
                context.value = object;
            }
            context.eval = true;
        }
    }

    static final class NameIntInSegment
            extends NameFilter {
        private final long[] values;
        private final boolean not;

        public NameIntInSegment(
                String fieldName,
                long fieldNameNameHash,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                Function expr,
                long[] values,
                boolean not) {
            super(fieldName, fieldNameNameHash, fieldName2, fieldNameNameHash2, expr);
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

    static final class NameIntBetweenSegment
            extends NameFilter {
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

    static final class TypeFunction
            implements Function {
        static final TypeFunction INSTANCE = new TypeFunction();

        @Override
        public Object apply(Object object) {
            return type(object);
        }
    }

    static final class SizeFunction
            implements Function {
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

            if (value instanceof Sequence) {
                return ((Sequence) value).values.size();
            }

            return 1;
        }
    }

    abstract static class NameFilter
            extends FilterSegment {
        final String fieldName;
        final long fieldNameNameHash;

        final String[] fieldName2;
        final long[] fieldNameNameHash2;

        final Function function;

        public NameFilter(String fieldName, long fieldNameNameHash) {
            this.fieldName = fieldName;
            this.fieldNameNameHash = fieldNameNameHash;
            this.fieldName2 = null;
            this.fieldNameNameHash2 = null;
            this.function = null;
        }

        public NameFilter(String fieldName,
                          long fieldNameNameHash,
                          String[] fieldName2,
                          long[] fieldNameNameHash2,
                          Function function) {
            this.fieldName = fieldName;
            this.fieldNameNameHash = fieldNameNameHash;
            this.fieldName2 = fieldName2;
            this.fieldNameNameHash2 = fieldNameNameHash2;
            this.function = function;
        }

        abstract boolean apply(Object fieldValue);

        @Override
        public final void accept(JSONReader jsonReader, Context context) {
            if (context.parent == null) {
                context.root = jsonReader.readAny();
            }
            eval(context);
        }

        @Override
        public boolean remove(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                for (int i = list.size() - 1; i >= 0; i--) {
                    Object item = list.get(i);
                    if (apply(context, item)) {
                        list.remove(i);
                    }
                }
                return true;
            }

            throw new JSONException("UnsupportedOperation " + getClass());
        }

        @Override
        public final void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof List) {
                List list = (List) object;
                JSONArray array = new JSONArray(list.size());
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    if (apply(context, item)) {
                        array.add(item);
                    }
                }
                context.value = array;
                context.eval = true;
                return;
            }

            if (object instanceof Object[]) {
                Object[] list = (Object[]) object;
                JSONArray array = new JSONArray(list.length);
                for (Object item : list) {
                    if (apply(context, item)) {
                        array.add(item);
                    }
                }
                context.value = array;
                context.eval = true;
                return;
            }

            if (object instanceof Sequence) {
                Sequence sequence = (Sequence) object;
                JSONArray array = new JSONArray();
                for (Object value : sequence.values) {
                    if (value instanceof Collection) {
                        for (Object valueItem : ((Collection<?>) value)) {
                            if (apply(context, valueItem)) {
                                array.add(valueItem);
                            }
                        }
                    } else {
                        if (apply(context, value)) {
                            array.add(value);
                        }
                    }
                }
                context.value = array;
                context.eval = true;
                return;
            }

            if (apply(context, object)) {
                context.value = object;
                context.eval = true;
            }
        }

        @Override
        public final boolean apply(Context context, Object object) {
            if (object == null) {
                return false;
            }

            JSONWriter.Context writerContext = context.path.getWriterContext();

            if (object instanceof Map) {
                Object fieldValue = fieldName == null ? object : ((Map<?, ?>) object).get(fieldName);
                if (fieldValue == null) {
                    return false;
                }

                if (fieldName2 != null) {
                    for (int i = 0; i < fieldName2.length; i++) {
                        String name = fieldName2[i];
                        if (fieldValue instanceof Map) {
                            fieldValue = ((Map) fieldValue).get(name);
                        } else {
                            ObjectWriter objectWriter2 = writerContext.getObjectWriter(fieldValue.getClass());
                            if (objectWriter2 instanceof ObjectWriterAdapter) {
                                FieldWriter fieldWriter2 = objectWriter2.getFieldWriter(fieldNameNameHash2[i]);
                                if (fieldWriter2 == null) {
                                    return false;
                                }
                                fieldValue = fieldWriter2.getFieldValue(fieldValue);
                            } else {
                                return false;
                            }
                        }

                        if (fieldValue == null) {
                            return false;
                        }
                    }
                }

                if (function != null) {
                    fieldValue = function.apply(fieldValue);
                }

                return apply(fieldValue);
            }

            ObjectWriter objectWriter = writerContext.getObjectWriter(object.getClass());
            if (objectWriter instanceof ObjectWriterAdapter) {
                FieldWriter fieldWriter = objectWriter.getFieldWriter(fieldNameNameHash);
                Object fieldValue = fieldWriter.getFieldValue(object);

                if (fieldValue == null) {
                    return false;
                }

                if (fieldName2 != null) {
                    for (int i = 0; i < fieldName2.length; i++) {
                        String name = fieldName2[i];
                        if (fieldValue instanceof Map) {
                            fieldValue = ((Map) fieldValue).get(name);
                        } else {
                            ObjectWriter objectWriter2 = writerContext.getObjectWriter(fieldValue.getClass());
                            if (objectWriter2 instanceof ObjectWriterAdapter) {
                                FieldWriter fieldWriter2 = objectWriter2.getFieldWriter(fieldNameNameHash2[i]);
                                if (fieldWriter2 == null) {
                                    return false;
                                }
                                fieldValue = fieldWriter2.getFieldValue(fieldValue);
                            } else {
                                return false;
                            }
                        }

                        if (fieldValue == null) {
                            return false;
                        }
                    }
                }

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

    static final class NameStringOpSegment
            extends NameFilter {
        final Operator operator;
        final String value;

        public NameStringOpSegment(
                String fieldName,
                long fieldNameNameHash,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                Function expr,
                Operator operator,
                String value
        ) {
            super(fieldName, fieldNameNameHash, fieldName2, fieldNameNameHash2, expr);
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

    static final class NameArrayOpSegment
            extends NameFilter {
        final Operator operator;
        final JSONArray array;

        public NameArrayOpSegment(
                String fieldName,
                long fieldNameNameHash,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                Function function,
                Operator operator,
                JSONArray array
        ) {
            super(fieldName, fieldNameNameHash, fieldName2, fieldNameNameHash2, function);
            this.operator = operator;
            this.array = array;
        }

        @Override
        boolean apply(Object fieldValue) {
            switch (operator) {
                case EQ:
                    return array.equals(fieldValue);
                default:
                    throw new JSONException("not support operator : " + operator);
            }
        }
    }

    static final class NameObjectOpSegment
            extends NameFilter {
        final Operator operator;
        final JSONObject object;

        public NameObjectOpSegment(
                String fieldName,
                long fieldNameNameHash,
                String[] fieldName2,
                long[] fieldNameNameHash2,
                Function function,
                Operator operator,
                JSONObject object) {
            super(fieldName, fieldNameNameHash, fieldName2, fieldNameNameHash2, function);
            this.operator = operator;
            this.object = object;
        }

        @Override
        boolean apply(Object fieldValue) {
            switch (operator) {
                case EQ:
                    return object.equals(fieldValue);
                default:
                    throw new JSONException("not support operator : " + operator);
            }
        }
    }

    static final class NameStringInSegment
            extends NameFilter {
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

    static final class NameMatchFilter
            extends NameFilter {
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

    static final class NameExistsFilter
            extends FilterSegment {
        final String name;
        final long nameHashCode;

        public NameExistsFilter(String name, long nameHashCode) {
            this.name = name;
            this.nameHashCode = nameHashCode;
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

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
                context.value = array;
                return;
            }

            if (object instanceof Map) {
                Map map = (Map) object;
                Object value = map.get(name);
                context.value = value != null ? object : null;
                return;
            }

            if (object instanceof Sequence) {
                List list = ((Sequence) object).values;
                for (int i = 0, l = list.size(); i < l; i++) {
                    Object item = list.get(i);
                    if (item instanceof Map) {
                        if (((Map) item).containsKey(name)) {
                            array.add(item);
                        }
                    }
                }
                context.value = new Sequence(array);
                return;
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            eval(context);
        }

        @Override
        public String toString() {
            return '?' + name;
        }

        @Override
        public boolean apply(Context context, Object object) {
            throw new UnsupportedOperationException();
        }
    }

    static final class PreviousPath
            extends JSONPath {
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
        public void set(Object rootObject, Object value, JSONReader.Feature... readerFeatures) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCallback(Object rootObject, BiFunction callback) {
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

    static final class SingleNamePath
            extends JSONPath {
        final long nameHashCode;
        final String name;

        public SingleNamePath(String path, NameSegment segment, Feature... features) {
            super(path, features);
            this.name = segment.name;
            this.nameHashCode = segment.nameHashCode;
        }

        @Override
        public Object eval(Object root) {
            Object value;
            if (root instanceof Map) {
                Map map = (Map) root;
                value = map.get(name);
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
            } else {
                JSONWriter.Context writerContext = getWriterContext();
                ObjectWriter objectWriter = writerContext.getObjectWriter(root.getClass());
                if (objectWriter == null) {
                    return null;
                }

                FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                if (fieldWriter == null) {
                    return null;
                }

                value = fieldWriter.getFieldValue(root);
            }

            if ((features & Feature.AlwaysReturnList.mask) != 0) {
                if (value == null) {
                    value = new JSONArray();
                } else if (!(value instanceof List)) {
                    value = JSONArray.of(value);
                }
            }
            return value;
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
                return ((Map) root).containsKey(name);
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
                Map map = (Map) rootObject;
                map.put(name, value);
                return;
            }
            ObjectReaderProvider provider = getReaderContext().getProvider();
            ObjectReader objectReader = provider.getObjectReader(rootObject.getClass());
            FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);

            if (fieldReader != null) {
                if (value != null) {
                    Class<?> valueClass = value.getClass();
                    Class fieldClass = fieldReader.fieldClass;
                    if (valueClass != fieldClass) {
                        java.util.function.Function typeConvert = provider.getTypeConvert(valueClass, fieldClass);
                        if (typeConvert != null) {
                            value = typeConvert.apply(value);
                        }
                    }
                }
                fieldReader.accept(rootObject, value);
            } else if (objectReader instanceof ObjectReaderBean) {
                ((ObjectReaderBean) objectReader).acceptExtra(rootObject, name, value);
            }
        }

        @Override
        public void set(Object rootObject, Object value, JSONReader.Feature... readerFeatures) {
            if (rootObject instanceof Map) {
                Map map = (Map) rootObject;
                Object origin = map.put(name, value);
                if (origin != null) {
                    boolean duplicateKeyValueAsArray = false;
                    for (JSONReader.Feature feature : readerFeatures) {
                        if (feature == JSONReader.Feature.DuplicateKeyValueAsArray) {
                            duplicateKeyValueAsArray = true;
                            break;
                        }
                    }

                    if (duplicateKeyValueAsArray) {
                        if (origin instanceof Collection) {
                            ((Collection) origin).add(value);
                            map.put(name, value);
                        } else {
                            JSONArray array = JSONArray.of(origin, value);
                            map.put(name, array);
                        }
                    }
                }
                return;
            }
            ObjectReaderProvider provider = getReaderContext().getProvider();
            ObjectReader objectReader = provider.getObjectReader(rootObject.getClass());
            FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);

            if (value != null) {
                Class<?> valueClass = value.getClass();
                Class fieldClass = fieldReader.fieldClass;
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
        public void setCallback(Object object, BiFunction callback) {
            if (object instanceof Map) {
                Map map = (Map) object;
                Object originValue = map.get(name);
                if (originValue != null || map.containsKey(name)) {
                    map.put(name, callback.apply(map, originValue));
                }
                return;
            }

            Class<?> objectClass = object.getClass();

            if (readerContext == null) {
                readerContext = JSONFactory.createReadContext();
            }
            FieldReader fieldReader = readerContext.provider
                    .getObjectReader(objectClass)
                    .getFieldReader(nameHashCode);

            if (writerContext == null) {
                writerContext = JSONFactory.createWriteContext();
            }
            FieldWriter fieldWriter = writerContext.provider
                    .getObjectWriter(objectClass)
                    .getFieldWriter(nameHashCode);

            if (fieldReader != null && fieldWriter != null) {
                Object fieldValue = fieldWriter.getFieldValue(object);
                Object value = callback.apply(object, fieldValue);
                fieldReader.accept(object, value);
            }
        }

        @Override
        public void setInt(Object obejct, int value) {
            if (obejct instanceof Map) {
                ((Map) obejct).put(name, value);
                return;
            }
            ObjectReaderProvider provider = getReaderContext().getProvider();
            ObjectReader objectReader = provider.getObjectReader(obejct.getClass());
            objectReader.setFieldValue(obejct, name, nameHashCode, value);
        }

        @Override
        public void setLong(Object object, long value) {
            if (object instanceof Map) {
                ((Map) object).put(name, value);
                return;
            }
            ObjectReaderProvider provider = getReaderContext().getProvider();
            ObjectReader objectReader = provider.getObjectReader(object.getClass());
            objectReader.setFieldValue(object, name, nameHashCode, value);
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

                    if ((features & Feature.AlwaysReturnList.mask) != 0) {
                        if (val == null) {
                            val = new JSONArray();
                        } else if (!(val instanceof List)) {
                            val = JSONArray.of(val);
                        }
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

    static final class RootPath
            extends JSONPath {
        static final RootPath INSTANCE = new RootPath();

        protected RootPath() {
            super("$");
        }

        @Override
        public boolean isRef() {
            return true;
        }

        @Override
        public boolean contains(Object object) {
            return false;
        }

        @Override
        public Object eval(Object object) {
            return object;
        }

        @Override
        public Object extract(JSONReader jsonReader) {
            if (jsonReader == null) {
                return null;
            }
            return jsonReader.readAny();
        }

        @Override
        public String extractScalar(JSONReader jsonReader) {
            Object any = jsonReader.readAny();
            return JSON.toJSONString(any);
        }

        @Override
        public void set(Object object, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Object object, Object value, JSONReader.Feature... readerFeatures) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCallback(Object object, BiFunction callback) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setInt(Object object, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLong(Object object, long value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object object) {
            return false;
        }
    }

    static final class SingleSegmentPath
            extends JSONPath {
        final Segment segment;
        final boolean ref;

        public SingleSegmentPath(Segment segment, String path) {
            super(path);
            this.segment = segment;
            this.ref = segment instanceof IndexSegment || segment instanceof NameSegment;
        }

        @Override
        public boolean remove(Object root) {
            Context context = new Context(this, null, segment, null, 0);
            context.root = root;
            return segment.remove(context);
        }

        @Override
        public boolean contains(Object root) {
            Context context = new Context(this, null, segment, null, 0);
            context.root = root;
            return segment.contains(context);
        }

        @Override
        public boolean isRef() {
            return ref;
        }

        @Override
        public Object eval(Object root) {
            Context context = new Context(this, null, segment, null, 0);
            context.root = root;
            segment.eval(context);
            return context.value;
        }

        @Override
        public void set(Object root, Object value) {
            Context context = new Context(this, null, segment, null, 0);
            context.root = root;
            segment.set(context, value);
        }

        @Override
        public void set(Object root, Object value, JSONReader.Feature... readerFeatures) {
            Context context = new Context(this, null, segment, null, 0);
            context.root = root;
            segment.set(context, value);
        }

        @Override
        public void setCallback(Object root, BiFunction callback) {
            Context context = new Context(this, null, segment, null, 0);
            context.root = root;
            segment.setCallback(context, callback);
        }

        @Override
        public void setInt(Object root, int value) {
            Context context = new Context(this, null, segment, null, 0);
            context.root = root;
            segment.setInt(context, value);
        }

        @Override
        public void setLong(Object root, long value) {
            Context context = new Context(this, null, segment, null, 0);
            context.root = root;
            segment.setLong(context, value);
        }

        @Override
        public Object extract(JSONReader jsonReader) {
            Context context = new Context(this, null, segment, null, 0);
            if (segment instanceof EvalSegment) {
                context.root = jsonReader.readAny();
                segment.eval(context);
            } else {
                segment.accept(jsonReader, context);
            }
            return context.value;
        }

        @Override
        public String extractScalar(JSONReader jsonReader) {
            Context context = new Context(this, null, segment, null, 0);
            segment.accept(jsonReader, context);
            return JSON.toJSONString(context.value);
        }
    }

    static class TwoSegmentPath
            extends JSONPath {
        final Segment first;
        final Segment second;
        final boolean ref;

        public TwoSegmentPath(String path, Segment first, Segment second, Feature... features) {
            super(path, features);
            this.first = first;
            this.second = second;
            this.ref = (first instanceof IndexSegment || first instanceof NameSegment)
                    && (second instanceof IndexSegment || second instanceof NameSegment);
        }

        @Override
        public boolean remove(Object root) {
            Context context0 = new Context(this, null, first, second, 0);
            context0.root = root;
            first.eval(context0);
            if (context0.value == null) {
                return false;
            }

            Context context1 = new Context(this, context0, second, null, 0);
            return second.remove(context1);
        }

        @Override
        public boolean contains(Object root) {
            Context context0 = new Context(this, null, first, second, 0);
            context0.root = root;
            first.eval(context0);
            if (context0.value == null) {
                return false;
            }

            Context context1 = new Context(this, context0, second, null, 0);
            return second.contains(context1);
        }

        @Override
        public boolean isRef() {
            return ref;
        }

        @Override
        public Object eval(Object root) {
            Context context0 = new Context(this, null, first, second, 0);
            context0.root = root;
            first.eval(context0);
            if (context0.value == null) {
                return null;
            }

            Context context1 = new Context(this, context0, second, null, 0);
            second.eval(context1);
            Object contextValue = context1.value;
            if ((features & Feature.AlwaysReturnList.mask) != 0) {
                if (contextValue == null) {
                    contextValue = new JSONArray();
                } else if (!(contextValue instanceof List)) {
                    contextValue = JSONArray.of(contextValue);
                }
            }
            return contextValue;
        }

        @Override
        public void set(Object root, Object value) {
            Context context0 = new Context(this, null, first, second, 0);
            context0.root = root;
            first.eval(context0);
            if (context0.value == null) {
                Object emptyValue;
                if (second instanceof IndexSegment) {
                    emptyValue = new JSONArray();
                } else if (second instanceof NameSegment) {
                    emptyValue = new JSONObject();
                } else {
                    return;
                }

                context0.value = emptyValue;
                if (root instanceof Map && first instanceof NameSegment) {
                    ((Map) root).put(((NameSegment) first).name, emptyValue);
                } else if (root instanceof List && first instanceof IndexSegment) {
                    ((List) root).set(((IndexSegment) first).index, emptyValue);
                } else if (root != null) {
                    Class<?> parentObjectClass = root.getClass();
                    JSONReader.Context readerContext = getReaderContext();
                    ObjectReader<?> objectReader = readerContext.getObjectReader(parentObjectClass);
                    if (first instanceof NameSegment) {
                        FieldReader fieldReader = objectReader.getFieldReader(((NameSegment) first).nameHashCode);
                        if (fieldReader != null) {
                            ObjectReader fieldObjectReader = fieldReader.getObjectReader(readerContext);
                            Object fieldValue = fieldObjectReader.createInstance();
                            fieldReader.accept(root, fieldValue);
                            context0.value = fieldValue;
                        }
                    }
                }
            }

            Context context1 = new Context(this, context0, second, null, 0);
            second.set(context1, value);
        }

        @Override
        public void set(Object root, Object value, JSONReader.Feature... readerFeatures) {
            long features = 0;
            for (JSONReader.Feature feature : readerFeatures) {
                features |= feature.mask;
            }

            Context context0 = new Context(this, null, first, second, features);
            context0.root = root;
            first.eval(context0);
            if (context0.value == null) {
                return;
            }

            Context context1 = new Context(this, context0, second, null, features);
            second.set(context1, value);
        }

        @Override
        public void setCallback(Object root, BiFunction callback) {
            Context context0 = new Context(this, null, first, second, 0);
            context0.root = root;
            first.eval(context0);
            if (context0.value == null) {
                return;
            }

            Context context1 = new Context(this, context0, second, null, 0);
            second.setCallback(context1, callback);
        }

        @Override
        public void setInt(Object root, int value) {
            Context context0 = new Context(this, null, first, second, 0);
            context0.root = root;
            first.eval(context0);
            if (context0.value == null) {
                return;
            }

            Context context1 = new Context(this, context0, second, null, 0);
            second.setInt(context1, value);
        }

        @Override
        public void setLong(Object root, long value) {
            Context context0 = new Context(this, null, first, second, 0);
            context0.root = root;
            first.eval(context0);
            if (context0.value == null) {
                return;
            }

            Context context1 = new Context(this, context0, second, null, 0);
            second.setLong(context1, value);
        }

        @Override
        public Object extract(JSONReader jsonReader) {
            if (jsonReader == null) {
                return null;
            }

            Context context0 = new Context(this, null, first, second, 0);
            first.accept(jsonReader, context0);

            Context context1 = new Context(this, context0, second, null, 0);

            if (context0.eval) {
                second.eval(context1);
            } else {
                second.accept(jsonReader, context1);
            }

            return context1.value;
        }

        @Override
        public String extractScalar(JSONReader jsonReader) {
            Context context0 = new Context(this, null, first, second, 0);
            first.accept(jsonReader, context0);

            Context context1 = new Context(this, context0, second, null, 0);
            second.accept(jsonReader, context1);

            return JSON.toJSONString(context1.value);
        }
    }

    static final class MultiSegmentPath
            extends JSONPath {
        final List<Segment> segments;
        final boolean ref;

        private MultiSegmentPath(String path, List<Segment> segments, Feature... features) {
            super(path, features);
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
            Context context = null;

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
                context = new Context(this, context, segment, nextSegment, 0);
                if (i == 0) {
                    context.root = root;
                }

                if (i == size - 1) {
                    return segment.remove(context);
                }
                segment.eval(context);

                if (context.value == null) {
                    return false;
                }
            }

            return false;
        }

        @Override
        public boolean contains(Object root) {
            Context context = null;

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
                context = new Context(this, context, segment, nextSegment, 0);
                if (i == 0) {
                    context.root = root;
                }

                if (i == size - 1) {
                    return segment.contains(context);
                }
                segment.eval(context);
            }

            return false;
        }

        @Override
        public boolean isRef() {
            return ref;
        }

        @Override
        public Object eval(Object root) {
            Context context = null;

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
                context = new Context(this, context, segment, nextSegment, 0);
                if (i == 0) {
                    context.root = root;
                }

                segment.eval(context);
            }

            Object contextValue = context.value;
            if ((context.path.features & Feature.AlwaysReturnList.mask) != 0) {
                if (contextValue == null) {
                    contextValue = new JSONArray();
                } else if (!(contextValue instanceof List)) {
                    contextValue = JSONArray.of(contextValue);
                }
            }
            return contextValue;
        }

        @Override
        public void set(Object root, Object value) {
            Context context = null;
            int size = segments.size();
            for (int i = 0; i < size - 1; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;
                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }
                context = new Context(this, context, segment, nextSegment, 0L);
                if (i == 0) {
                    context.root = root;
                }

                segment.eval(context);
                if (context.value == null && nextSegment != null) {
                    if (value == null) {
                        return;
                    }

                    Object parentObject;
                    if (i == 0) {
                        parentObject = root;
                    } else {
                        parentObject = context.parent.value;
                    }

                    Object emptyValue;
                    if (nextSegment instanceof IndexSegment) {
                        emptyValue = new JSONArray();
                    } else if (nextSegment instanceof NameSegment) {
                        emptyValue = new JSONObject();
                    } else {
                        return;
                    }
                    context.value = emptyValue;

                    if (parentObject instanceof Map && segment instanceof NameSegment) {
                        ((Map) parentObject).put(((NameSegment) segment).name, emptyValue);
                    } else if (parentObject instanceof List && segment instanceof IndexSegment) {
                        List list = (List) parentObject;
                        int index = ((IndexSegment) segment).index;
                        if (index == list.size()) {
                            list.add(emptyValue);
                        } else {
                            list.set(index, emptyValue);
                        }
                    } else if (parentObject != null) {
                        Class<?> parentObjectClass = parentObject.getClass();
                        JSONReader.Context readerContext = getReaderContext();
                        ObjectReader<?> objectReader = readerContext.getObjectReader(parentObjectClass);
                        if (segment instanceof NameSegment) {
                            FieldReader fieldReader = objectReader.getFieldReader(((NameSegment) segment).nameHashCode);
                            if (fieldReader != null) {
                                ObjectReader fieldObjectReader = fieldReader.getObjectReader(readerContext);
                                Object fieldValue = fieldObjectReader.createInstance();
                                fieldReader.accept(parentObject, fieldValue);
                                context.value = fieldValue;
                            }
                        }
                    }
                }
            }
            context = new Context(this, context, segments.get(0), null, 0L);
            context.root = root;

            Segment segment = segments.get(size - 1);
            segment.set(context, value);
        }

        @Override
        public void set(Object root, Object value, JSONReader.Feature... readerFeatures) {
            long features = 0;
            for (JSONReader.Feature feature : readerFeatures) {
                features |= feature.mask;
            }

            Context context = null;
            int size = segments.size();
            for (int i = 0; i < size - 1; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;
                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }
                context = new Context(this, context, segment, nextSegment, features);
                if (i == 0) {
                    context.root = root;
                }

                segment.eval(context);
            }
            context = new Context(this, context, segments.get(0), null, features);
            context.root = root;

            Segment segment = segments.get(size - 1);
            segment.set(context, value);
        }

        @Override
        public void setCallback(Object root, BiFunction callback) {
            Context context = null;
            int size = segments.size();
            for (int i = 0; i < size - 1; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;
                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }
                context = new Context(this, context, segment, nextSegment, 0);
                if (i == 0) {
                    context.root = root;
                }

                segment.eval(context);
            }
            context = new Context(this, context, segments.get(0), null, 0);
            context.root = root;

            Segment segment = segments.get(size - 1);
            segment.setCallback(context, callback);
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

            boolean eval = false;
            Context context = null;
            for (int i = 0; i < size; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;

                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }

                context = new Context(this, context, segment, nextSegment, 0);
                if (eval) {
                    segment.eval(context);
                } else {
                    segment.accept(jsonReader, context);
                }

                if (context.eval) {
                    eval = true;
                    if (context.value == null) {
                        break;
                    }
                }
            }

            Object value = context.value;
            if (value instanceof Sequence) {
                value = ((Sequence) value).values;
            }
            return value;
        }

        @Override
        public String extractScalar(JSONReader jsonReader) {
            int size = segments.size();
            if (size == 0) {
                return null;
            }

            boolean eval = false;
            Context context = null;
            for (int i = 0; i < size; i++) {
                Segment segment = segments.get(i);
                Segment nextSegment = null;

                int nextIndex = i + 1;
                if (nextIndex < size) {
                    nextSegment = segments.get(nextIndex);
                }

                context = new Context(this, context, segment, nextSegment, 0);
                if (eval) {
                    segment.eval(context);
                } else {
                    segment.accept(jsonReader, context);
                }

                if (context.eval) {
                    eval = true;
                    if (context.value == null) {
                        break;
                    }
                }
            }

            return JSON.toJSONString(context.value);
        }
    }

    static final class Context {
        final JSONPath path;
        final Context parent;
        final Segment current;
        final Segment next;
        final long readerFeatures;
        Object root;
        Object value;

        boolean eval;

        Context(JSONPath path, Context parent, Segment current, Segment next, long readerFeatures) {
            this.path = path;
            this.current = current;
            this.next = next;
            this.parent = parent;
            this.readerFeatures = readerFeatures;
        }
    }

    abstract static class Segment {
        public abstract void accept(JSONReader jsonReader, Context context);

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

        public void setCallback(Context context, BiFunction callback) {
            throw new JSONException("UnsupportedOperation " + getClass());
        }

        public void setInt(Context context, int value) {
            set(context, Integer.valueOf(value));
        }

        public void setLong(Context context, long value) {
            set(context, Long.valueOf(value));
        }
    }

    static final class EntrySetSegment
            extends Segment
            implements EvalSegment {
        static final EntrySetSegment INSTANCE = new EntrySetSegment();

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (jsonReader.isObject()) {
                jsonReader.next();
                JSONArray array = new JSONArray();
                while (!jsonReader.nextIfObjectEnd()) {
                    String fieldName = jsonReader.readFieldName();
                    Object value = jsonReader.readAny();
                    array.add(
                            JSONObject.of("key", fieldName, "value", value)
                    );
                }
                context.value = array;
                return;
            }
            throw new JSONException("TODO");
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;
            if (object instanceof Map) {
                Map map = (Map) object;
                JSONArray array = new JSONArray(map.size());
                for (Iterator<Map.Entry> it = ((Map) object).entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = it.next();
                    array.add(
                            JSONObject.of("key", entry.getKey(), "value", entry.getValue())
                    );
                }
                context.value = array;
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }
    }

    static final class KeysSegment
            extends Segment
            implements EvalSegment {
        static final KeysSegment INSTANCE = new KeysSegment();

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (jsonReader.isObject()) {
                jsonReader.next();
                JSONArray array = new JSONArray();
                while (!jsonReader.nextIfObjectEnd()) {
                    String fieldName = jsonReader.readFieldName();
                    array.add(fieldName);
                    jsonReader.skipValue();
                }
                context.value = array;
                return;
            }
            throw new JSONException("TODO");
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;
            if (object instanceof Map) {
                context.value = new JSONArray(((Map<?, ?>) object).keySet());
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }
    }

    static final class ValuesSegment
            extends Segment
            implements EvalSegment {
        static final ValuesSegment INSTANCE = new ValuesSegment();

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            eval(context);
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object == null) {
                context.value = null;
                context.eval = true;
                return;
            }

            if (object instanceof Map) {
                context.value = new JSONArray(((Map<?, ?>) object).values());
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }
    }

    static final class LengthSegment
            extends Segment
            implements EvalSegment {
        static final LengthSegment INSTANCE = new LengthSegment();

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (context.parent == null) {
                context.root = jsonReader.readAny();
                context.eval = true;
            }
            eval(context);
        }

        @Override
        public void eval(Context context) {
            Object value = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (value == null) {
                return;
            }

            int length = 1;
            if (value instanceof Collection) {
                length = ((Collection<?>) value).size();
            } else if (value.getClass().isArray()) {
                length = Array.getLength(value);
            } else if (value instanceof Map) {
                length = ((Map<?, ?>) value).size();
            } else if (value instanceof String) {
                length = ((String) value).length();
            } else if (value instanceof Sequence) {
                length = ((Sequence) value).values.size();
            }
            context.value = length;
        }
    }

    static FunctionSegment FUNCTION_TYPE = new FunctionSegment(JSONPath::type);
    static FunctionSegment FUNCTION_DOUBLE = new FunctionSegment(new ToDouble(null));
    static FunctionSegment FUNCTION_FLOOR = new FunctionSegment(JSONPath::floor);
    static FunctionSegment FUNCTION_CEIL = new FunctionSegment(JSONPath::ceil);
    static FunctionSegment FUNCTION_ABS = new FunctionSegment(JSONPath::abs);
    static FunctionSegment FUNCTION_NEGATIVE = new FunctionSegment(JSONPath::negative);
    static FunctionSegment FUNCTION_EXISTS = new FunctionSegment(JSONPath::exists);

    static final class FunctionSegment
            extends Segment
            implements EvalSegment {
        final Function function;

        public FunctionSegment(Function function) {
            this.function = function;
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (context.parent == null) {
                context.root = jsonReader.readAny();
                context.eval = true;
            }
            eval(context);
        }

        @Override
        public void eval(Context context) {
            Object value = context.parent == null
                    ? context.root
                    : context.parent.value;

            context.value = function.apply(value);
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

    static Object floor(Object value) {
        if (value instanceof Double) {
            return Math.floor((Double) value);
        }

        if (value instanceof Float) {
            return Math.floor((Float) value);
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(0, RoundingMode.FLOOR);
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

        return value;
    }

    static Object ceil(Object value) {
        if (value instanceof Double) {
            return Math.ceil((Double) value);
        }

        if (value instanceof Float) {
            return Math.ceil((Float) value);
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(0, RoundingMode.CEILING);
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

        return value;
    }

    static Object exists(Object value) {
        return value != null;
    }

    static Object negative(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return -((Integer) value).intValue();
        }

        if (value instanceof Long) {
            return -((Long) value).longValue();
        }

        if (value instanceof Byte) {
            return -((Byte) value).byteValue();
        }

        if (value instanceof Short) {
            return -((Short) value).shortValue();
        }

        if (value instanceof Double) {
            return -((Double) value).doubleValue();
        }

        if (value instanceof Float) {
            return -((Float) value).floatValue();
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).negate();
        }

        if (value instanceof BigInteger) {
            return ((BigInteger) value).negate();
        }

        if (value instanceof List) {
            List list = (List) value;
            JSONArray values = new JSONArray(list.size());
            for (int i = 0, l = list.size(); i < l; i++) {
                Object item = list.get(i);
                Object negativeItem;
                if (item instanceof Double) {
                    negativeItem = -((Double) item).doubleValue();
                } else if (item instanceof Float) {
                    negativeItem = -((Float) item).floatValue();
                } else if (item instanceof BigDecimal) {
                    negativeItem = ((BigDecimal) item).negate();
                } else if (item instanceof BigInteger) {
                    negativeItem = ((BigInteger) item).negate();
                } else {
                    negativeItem = item;
                }
                values.add(negativeItem);
            }
            return values;
        }

        throw new JSONException("abs not support " + value);
    }

    static Object abs(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return Math.abs((Integer) value);
        }

        if (value instanceof Long) {
            return Math.abs((Long) value);
        }

        if (value instanceof Byte) {
            return Math.abs((Byte) value);
        }

        if (value instanceof Short) {
            return Math.abs((Short) value);
        }

        if (value instanceof Double) {
            return Math.abs((Double) value);
        }

        if (value instanceof Float) {
            return Math.abs((Float) value);
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).abs();
        }

        if (value instanceof BigInteger) {
            return ((BigInteger) value).abs();
        }

        if (value instanceof List) {
            List list = (List) value;

            JSONArray values = new JSONArray(list.size());
            for (int i = 0, l = list.size(); i < l; i++) {
                Object item = list.get(i);
                Object absItem;
                if (item instanceof Double) {
                    absItem = Math.abs((Double) item);
                } else if (item instanceof Float) {
                    absItem = Math.abs((Float) item);
                } else if (item instanceof BigDecimal) {
                    absItem = ((BigDecimal) item).abs();
                } else if (item instanceof BigInteger) {
                    absItem = ((BigInteger) item).abs();
                } else {
                    absItem = item;
                }
                values.add(absItem);
            }
            return values;
        }

        throw new JSONException("abs not support " + value);
    }

    static final class MinSegment
            extends Segment
            implements EvalSegment {
        static final MinSegment INSTANCE = new MinSegment();

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            eval(context);
        }

        @Override
        public void eval(Context context) {
            Object value = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (value == null) {
                return;
            }

            Object min = null;
            if (value instanceof Collection) {
                for (Object item : (Collection) value) {
                    if (item == null) {
                        continue;
                    }

                    if (min == null) {
                        min = item;
                    } else if (TypeUtils.compare(min, item) > 0) {
                        min = item;
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
            } else if (value instanceof Sequence) {
                for (Object item : ((Sequence) value).values) {
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

            context.value = min;
            context.eval = true;
        }
    }

    static final class MaxSegment
            extends Segment
            implements EvalSegment {
        static final MaxSegment INSTANCE = new MaxSegment();

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            eval(context);
        }

        @Override
        public void eval(Context context) {
            Object value = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (value == null) {
                return;
            }

            Object max = null;
            if (value instanceof Collection) {
                for (Object item : (Collection) value) {
                    if (item == null) {
                        continue;
                    }

                    if (max == null) {
                        max = item;
                    } else if (TypeUtils.compare(max, item) < 0) {
                        max = item;
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
            } else if (value instanceof Sequence) {
                for (Object item : ((Sequence) value).values) {
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

            context.value = max;
            context.eval = true;
        }
    }

    static final class SumSegment
            extends Segment
            implements EvalSegment {
        static final SumSegment INSTANCE = new SumSegment();

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            eval(context);
        }

        static Number add(Number a, Number b) {
            boolean aIsInt = a instanceof Byte || a instanceof Short || a instanceof Integer || a instanceof Long;
            boolean bIsInt = b instanceof Byte || b instanceof Short || b instanceof Integer || b instanceof Long;
            if (aIsInt && bIsInt) {
                return a.longValue() + b.longValue();
            }
            throw new JSONException("not support operation");
        }

        @Override
        public void eval(Context context) {
            Object value = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (value == null) {
                return;
            }

            Number sum = 0;
            if (value instanceof Collection) {
                for (Object item : (Collection) value) {
                    if (item == null) {
                        continue;
                    }

                    sum = add(sum, (Number) item);
                }
            } else if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                for (Object item : array) {
                    if (item == null) {
                        continue;
                    }

                    sum = add(sum, (Number) item);
                }
            } else if (value instanceof Sequence) {
                for (Object item : ((Sequence) value).values) {
                    if (item == null) {
                        continue;
                    }

                    sum = add(sum, (Number) item);
                }
            } else {
                throw new UnsupportedOperationException();
            }

            context.value = sum;
            context.eval = true;
        }
    }

    static final class SelfSegment
            extends Segment {
        static final SelfSegment INSTANCE = new SelfSegment();

        protected SelfSegment() {
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            context.value = jsonReader.readAny();
            context.eval = true;
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;
            context.value = object;
        }
    }

    static final class RootSegment
            extends Segment {
        static final RootSegment INSTANCE = new RootSegment();

        protected RootSegment() {
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (context.parent != null) {
                throw new JSONException("not support operation");
            }
            context.value = jsonReader.readAny();
            context.eval = true;
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.root;
            context.value = object;
        }
    }

    static class NameSegment
            extends Segment {
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
        public boolean contains(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object == null) {
                return false;
            }

            if (object instanceof Map) {
                return ((Map<?, ?>) object).containsKey(name);
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

                    ObjectWriter<?> objectWriter = context.path
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

            if (object instanceof Sequence) {
                Sequence sequence = (Sequence) object;
                for (Object item : sequence.values) {
                    if (item == null) {
                        continue;
                    }

                    if (item instanceof Map) {
                        if (((Map<?, ?>) item).get(name) != null) {
                            return true;
                        }
                    }

                    ObjectWriter<?> objectWriter = context.path
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

                    ObjectWriter<?> objectWriter = context.path
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

            ObjectWriter<?> objectWriter = context.path
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
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

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

                context.value = value;
                return;
            }

            if (object instanceof Collection) {
                Collection<?> collection = (Collection<?>) object;
                int size = collection.size();
                Collection values = null; // = new JSONArray(collection.size());
                for (Object item : collection) {
                    if (item instanceof Map) {
                        Object val = ((Map<?, ?>) item).get(name);
                        if (val == null) {
                            continue;
                        }
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
                context.value = values;
                return;
            }

            if (object instanceof Sequence) {
                List sequence = ((Sequence) object).values;
                JSONArray values = new JSONArray(sequence.size());
                for (int i = 0; i < sequence.size(); i++) {
                    Object item = sequence.get(i);
                    context.value = item;
                    Context itemContext = new Context(context.path, context, context.current, context.next, context.readerFeatures);
                    eval(itemContext);
                    Object val = itemContext.value;

                    if (val == null) {
                        continue;
                    }

                    if (val instanceof Collection) {
                        values.addAll((Collection) val);
                    } else {
                        values.add(val);
                    }
                }
                if (context.next != null) {
                    context.value = new Sequence(values);
                } else {
                    context.value = values;
                }
                context.eval = true;
                return;
            }

            JSONWriter.Context writerContext = context.path.getWriterContext();
            ObjectWriter<?> objectWriter = writerContext.getObjectWriter(object.getClass());
            if (objectWriter instanceof ObjectWriterAdapter) {
                FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                if (fieldWriter != null) {
                    context.value = fieldWriter.getFieldValue(object);
                }

                return;
            }

            if (nameHashCode == HASH_NAME && object instanceof Enum) {
                context.value = ((Enum<?>) object).name();
                return;
            }

            if (nameHashCode == HASH_ORDINAL && object instanceof Enum) {
                context.value = ((Enum<?>) object).ordinal();
                return;
            }

            if (object instanceof String) {
                String str = (String) object;
                if (!str.isEmpty() && str.charAt(0) == '{') {
                    context.value =
                            JSONPath.of("$." + name)
                                    .extract(
                                            JSONReader.of(str));
                    return;
                }

                context.value = null;
                return;
            }

            if (object instanceof Number || object instanceof Boolean) {
                context.value = null;
                return;
            }

            throw new JSONException("not support : " + object.getClass());
        }

        @Override
        public void set(Context context, Object value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof Map) {
                Map map = (Map) object;
                Object origin = map.put(name, value);
                if (origin != null) {
                    if ((context.readerFeatures & JSONReader.Feature.DuplicateKeyValueAsArray.mask) != 0) {
                        if (origin instanceof Collection) {
                            ((Collection) origin).add(value);
                            map.put(name, value);
                        } else {
                            JSONArray array = JSONArray.of(origin, value);
                            map.put(name, array);
                        }
                    }
                }
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
                Class fieldClass = fieldReader.fieldClass;
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
        public void setCallback(Context context, BiFunction callback) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof Map) {
                Map map = (Map) object;
                Object origin = map.get(name);
                if (origin != null) {
                    Object applyValue = callback.apply(map, origin);
                    map.put(name, applyValue);
                }
                return;
            }

            ObjectReaderProvider provider = context.path.getReaderContext().getProvider();

            ObjectReader objectReader = provider.getObjectReader(object.getClass());
            ObjectWriter objectWriter = context.path
                    .getWriterContext()
                    .getProvider()
                    .getObjectWriter(object.getClass());

            FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);
            FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
            if (fieldReader == null || fieldWriter == null) {
                return;
            }

            Object fieldValue = fieldWriter.getFieldValue(object);
            Object applyValue = callback.apply(object, fieldValue);
            fieldReader.accept(object, applyValue);
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
                            if (context.next != null) {
                                break;
                            }
                        }

                        context.value = jsonReader.readAny();
                        context.eval = true;
                        break;
                    }
                    return;
                } else if (jsonReader.isArray()
                        && context.parent != null
                        && context.parent.current instanceof AllSegment) {
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
                                    if (context.next != null) {
                                        break;
                                    }
                                }

                                values.add(jsonReader.readAny());
                            }
                        } else {
                            jsonReader.skipValue();
                        }
                    }

                    context.value = values;
                    context.eval = true;
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
                            if (context.next != null && !(context.next instanceof EvalSegment)
                                    && !(context.next instanceof NameSegment)
                                    && !(context.next instanceof AllSegment)) {
                                break _for;
                            }
                            val = jsonReader.readArray();
                            context.eval = true;
                            break;
                        case '{':
                            if (context.next != null
                                    && !(context.next instanceof EvalSegment)
                                    && !(context.next instanceof AllSegment)) {
                                break _for;
                            }
                            val = jsonReader.readObject();
                            context.eval = true;
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

                    context.value = val;
                    break;
                }
            } else if (jsonReader.ch == '[' && context.parent != null && context.parent.current instanceof AllSegment) {
                jsonReader.next();
                List values = new JSONArray();
                while (jsonReader.ch != EOI) {
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
                                    if (context.next != null) {
                                        break _for;
                                    }
                                    val = jsonReader.readArray();
                                    break;
                                case '{':
                                    if (context.next != null) {
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

                context.value = values;
            }/* else if (jsonReader.ch == JSONReader.EOI) {
                return;
            }*/
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static final class CycleNameSegment
            extends Segment {
        static final long HASH_STAR = Fnv.hashCode64("*");
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

        @Override
        public void setCallback(Context context, BiFunction callback) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            LoopCallback action = new LoopCallback(context, callback);
            action.accept(object);
        }

        class MapLoop
                implements BiConsumer, Consumer {
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
                } else if (nameHashCode == HASH_STAR) {
                    values.add(value);
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
                    } else if (nameHashCode == HASH_STAR) {
                        values.add(value);
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

        class LoopCallback {
            final Context context;
            final BiFunction callback;

            public LoopCallback(Context context, BiFunction callback) {
                this.context = context;
                this.callback = callback;
            }

            public void accept(Object object) {
                if (object instanceof Map) {
                    for (Map.Entry entry : (Iterable<Map.Entry>) ((Map) object).entrySet()) {
                        Object entryValue = entry.getValue();
                        if (name.equals(entry.getKey())) {
                            Object applyValue = callback.apply(object, entryValue);
                            entry.setValue(applyValue);
                            context.eval = true;
                        } else {
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
                    ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(entryValueClass);
                    if (objectReader instanceof ObjectReaderBean) {
                        FieldReader fieldReader = objectReader.getFieldReader(nameHashCode);
                        FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHashCode);
                        if (fieldWriter != null && fieldReader != null) {
                            Object fieldValue = fieldWriter.getFieldValue(object);
                            fieldValue = callback.apply(object, fieldValue);
                            fieldReader.accept(object, fieldValue);
                            context.eval = true;
                            return;
                        }
                    }

                    List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
                    for (FieldWriter fieldWriter : fieldWriters) {
                        Object fieldValue = fieldWriter.getFieldValue(object);
                        accept(fieldValue);
                    }
                }
            }
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            List values = new JSONArray();
            accept(jsonReader, context, values);
            context.value = values;
            context.eval = true;
        }

        public void accept(JSONReader jsonReader, Context context, List<Object> values) {
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
                            accept(jsonReader, context, values);
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
                            accept(jsonReader, context, values);
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
                            accept(jsonReader, context, values);
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
                        accept(jsonReader, context, values);
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

    static final class IndexSegment
            extends Segment {
        static final IndexSegment ZERO = new IndexSegment(0);
        static final IndexSegment ONE = new IndexSegment(1);
        static final IndexSegment TWO = new IndexSegment(2);

        static final IndexSegment LAST = new IndexSegment(-1);

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
            if (index == -1) {
                return LAST;
            }
            return new IndexSegment(index);
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object == null) {
                context.eval = true;
                return;
            }

            if (object instanceof java.util.List) {
                List list = (List) object;
                if (index >= 0) {
                    if (index < list.size()) {
                        context.value = list.get(index);
                    }
                } else {
                    int itemIndex = list.size() + this.index;
                    if (itemIndex >= 0) {
                        context.value = list.get(itemIndex);
                    }
                }
                context.eval = true;
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
                        context.value = item;
                        break;
                    }
                }
                context.eval = true;
                return;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                if (index >= 0) {
                    if (index < array.length) {
                        context.value = array[index];
                    }
                } else {
                    int itemIndex = array.length + this.index;
                    if (itemIndex >= 0) {
                        context.value = array[itemIndex];
                    }
                }
                context.eval = true;
                return;
            }

            Class objectClass = object.getClass();
            if (objectClass.isArray()) {
                int length = Array.getLength(object);
                if (index >= 0) {
                    if (index < length) {
                        context.value = Array.get(object, index);
                    }
                } else {
                    int itemIndex = length + this.index;
                    if (itemIndex >= 0) {
                        context.value = Array.get(object, itemIndex);
                    }
                }
                context.eval = true;
                return;
            }

            if (object instanceof Sequence) {
                List sequence = ((Sequence) object).values;
                JSONArray values = new JSONArray(sequence.size());
                for (int i = 0; i < sequence.size(); i++) {
                    Object item = sequence.get(i);
                    context.value = item;
                    Context itemContext = new Context(context.path, context, context.current, context.next, context.readerFeatures);
                    eval(itemContext);
                    values.add(itemContext.value);
                }
                if (context.next != null) {
                    context.value = new Sequence(values);
                } else {
                    context.value = values;
                }
                context.eval = true;
                return;
            }

            if (Map.class.isAssignableFrom(objectClass)) {
                Object value = eval((Map) object);
                context.value = value;
                context.eval = true;
                return;
            }

            // lax mode
            if (index == 0) {
                context.value = object;
                context.eval = true;
                return;
            }

            throw new JSONException("jsonpath not support operate : " + context.path + ", objectClass" + objectClass.getName());
        }

        private Object eval(Map object) {
            Map map = object;
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
            return value;
        }

        @Override
        public void set(Context context, Object value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof java.util.List) {
                List list = (List) object;
                if (index >= 0) {
                    if (index > list.size()) {
                        for (int i = list.size(); i < index; ++i) {
                            list.add(null);
                        }
                    }
                    if (index < list.size()) {
                        list.set(index, value);
                    } else if (index <= list.size()) {
                        list.add(value);
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
        public void setCallback(Context context, BiFunction callback) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof java.util.List) {
                List list = (List) object;
                if (index >= 0) {
                    if (index < list.size()) {
                        Object value = list.get(index);
                        value = callback.apply(object, value);
                        list.set(index, value);
                    }
                } else {
                    int itemIndex = list.size() + this.index;
                    if (itemIndex >= 0) {
                        Object value = list.get(index);
                        value = callback.apply(object, value);
                        list.set(itemIndex, value);
                    }
                }
                return;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                if (index >= 0) {
                    if (index < array.length) {
                        Object value = array[index];
                        value = callback.apply(object, value);
                        array[index] = value;
                    }
                } else {
                    Object value = array[index];
                    value = callback.apply(object, value);
                    array[array.length + index] = value;
                }
                return;
            }

            if (object != null && object.getClass().isArray()) {
                int length = Array.getLength(object);
                if (index >= 0) {
                    if (index < length) {
                        Object value = Array.get(object, index);
                        value = callback.apply(object, value);
                        Array.set(object, index, value);
                    }
                } else {
                    int arrayIndex = length + index;
                    if (arrayIndex >= 0) {
                        Object value = Array.get(object, index);
                        value = callback.apply(object, value);
                        Array.set(object, arrayIndex, value);
                    }
                }
                return;
            }

            throw new JSONException("UnsupportedOperation");
        }

        @Override
        public boolean remove(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof java.util.List) {
                List list = (List) object;
                if (index >= 0) {
                    if (index < list.size()) {
                        list.remove(index);
                        return true;
                    }
                } else {
                    int itemIndex = list.size() + this.index;
                    if (itemIndex >= 0) {
                        list.remove(itemIndex);
                        return true;
                    }
                }
                return false;
            }

            throw new JSONException("UnsupportedOperation");
        }

        @Override
        public void setInt(Context context, int value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;
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

            set(context, value);
        }

        @Override
        public void setLong(Context context, long value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;
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

            set(context, value);
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (context.parent != null
                    && (context.parent.eval
                    || (context.parent.current instanceof CycleNameSegment && context.next == null))
            ) {
                eval(context);
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
                        if (context.next != null) {
                            break;
                        }
                    }

                    context.value = jsonReader.readAny();
                    context.eval = true;
                    break;
                }
                return;
            }

            if (jsonReader.ch == '{') {
                Map object = jsonReader.readObject();
                context.value = eval(object);
                context.eval = true;
                return;
            }

            jsonReader.next();
            _for:
            for (int i = 0; jsonReader.ch != EOI; ++i) {
                if (jsonReader.ch == ']') {
                    jsonReader.next();
                    break;
                }

                boolean match = index == -1 || index == i;

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
                        if (context.next != null && !(context.next instanceof EvalSegment)) {
                            break _for;
                        }
                        val = jsonReader.readArray();
                        break;
                    case '{':
                        if (context.next != null && !(context.next instanceof EvalSegment)) {
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

                if (index == -1) {
                    if (jsonReader.ch == ']') {
                        context.value = val;
                    }
                } else {
                    context.value = val;
                }
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
            if (STRING_CREATOR_JDK11 != null) {
                str = STRING_CREATOR_JDK11.apply(bytes, LATIN1);
            } else {
                str = new String(bytes, StandardCharsets.US_ASCII);
            }
            return str;
        }
    }

    static final class RandomIndexSegment
            extends Segment {
        public static final RandomIndexSegment INSTANCE = new RandomIndexSegment();

        Random random;

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (context.parent != null
                    && (context.parent.eval
                    || (context.parent.current instanceof CycleNameSegment && context.next == null))) {
                eval(context);
                return;
            }

            if (jsonReader.isJSONB()) {
                JSONArray array = new JSONArray();

                {
                    int itemCnt = jsonReader.startArray();
                    for (int i = 0; i < itemCnt; i++) {
                        array.add(jsonReader.readAny());
                    }
                }

                // lazy init for graalvm
                if (random == null) {
                    random = new Random();
                }

                int index = Math.abs(random.nextInt()) % array.size();
                context.value = array.get(index);
                context.eval = true;
                return;
            }

            JSONArray array = new JSONArray();
            jsonReader.next();
            for (int i = 0; jsonReader.ch != EOI; ++i) {
                if (jsonReader.ch == ']') {
                    jsonReader.next();
                    break;
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

            // lazy init for graalvm
            if (random == null) {
                random = new Random();
            }

            int index = Math.abs(random.nextInt()) % array.size();
            context.value = array.get(index);
            context.eval = true;
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof java.util.List) {
                List list = (List) object;
                if (list.isEmpty()) {
                    return;
                }

                // lazy init for graalvm
                if (random == null) {
                    random = new Random();
                }

                int randomIndex = Math.abs(random.nextInt()) % list.size();
                context.value = list.get(randomIndex);
                context.eval = true;
                return;
            }

            if (object instanceof Object[]) {
                Object[] array = (Object[]) object;
                if (array.length == 0) {
                    return;
                }

                // lazy init for graalvm
                if (random == null) {
                    random = new Random();
                }

                int randomIndex = random.nextInt() % array.length;
                context.value = array[randomIndex];
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }
    }

    static final class RangeIndexSegment
            extends Segment {
        final int begin;
        final int end;

        public RangeIndexSegment(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

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
                context.value = result;
                context.eval = true;
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
                context.value = result;
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (context.parent != null
                    && (context.parent.eval
                    || (context.parent.current instanceof CycleNameSegment && context.next == null))
            ) {
                eval(context);
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

                context.value = array;
                context.eval = true;
                return;
            }

            JSONArray array = new JSONArray();
            jsonReader.next();
            for (int i = 0; jsonReader.ch != EOI; ++i) {
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
            context.value = array;
            context.eval = true;
        }

        @Override
        public void set(Context context, Object value) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

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
                        list.set(i, value);
                    }
                }
                return;
            }

            throw new JSONException("UnsupportedOperation " + getClass());
        }

        @Override
        public boolean remove(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof java.util.List) {
                List list = (List) object;
                int removeCount = 0;
                for (int size = list.size(), i = size - 1; i >= 0; i--) {
                    boolean match;
                    if (begin >= 0) {
                        match = i >= begin && i < end;
                    } else {
                        int ni = i - size;
                        match = ni >= begin && ni < end;
                    }
                    if (match) {
                        list.remove(i);
                        removeCount++;
                    }
                }
                return removeCount > 0;
            }

            throw new JSONException("UnsupportedOperation " + getClass());
        }
    }

    static final class MultiIndexSegment
            extends Segment {
        final int[] indexes;

        public MultiIndexSegment(int[] indexes) {
//            Arrays.sort(indexes);
            this.indexes = indexes;
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            List result = new JSONArray();

            if (object instanceof Sequence) {
                List list = ((Sequence) object).values;

                for (Object item : list) {
                    context.value = item;
                    Context itemContext = new Context(context.path, context, context.current, context.next, context.readerFeatures);
                    eval(itemContext);
                    Object value = itemContext.value;

                    if (value instanceof Collection) {
                        result.addAll((Collection) value);
                    } else {
                        result.add(value);
                    }
                }
                context.value = result;
                return;
            }

            for (int index : indexes) {
                Object value;

                if (object instanceof java.util.List) {
                    List list = (List) object;

                    if (index >= 0) {
                        if (index < list.size()) {
                            value = list.get(index);
                        } else {
                            continue;
                        }
                    } else {
                        int itemIndex = list.size() + index;
                        if (itemIndex >= 0) {
                            value = list.get(itemIndex);
                        } else {
                            continue;
                        }
                    }
                } else if (object instanceof Object[]) {
                    Object[] array = (Object[]) object;
                    if (index >= 0) {
                        if (index < array.length) {
                            value = array[index];
                        } else {
                            continue;
                        }
                    } else {
                        int itemIndex = array.length + index;
                        if (itemIndex >= 0) {
                            value = array[itemIndex];
                        } else {
                            continue;
                        }
                    }
                } else {
                    continue;
                }

                if (value instanceof Collection) {
                    result.addAll((Collection) value);
                } else {
                    result.add(value);
                }
            }
            context.value = result;
            return;
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (context.parent != null
                    && context.parent.current instanceof CycleNameSegment
                    && context.next == null) {
                eval(context);
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
                context.value = array;
                return;
            }

            JSONArray array = new JSONArray();

            jsonReader.next();
            for (int i = 0; jsonReader.ch != EOI; ++i) {
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
            context.value = array;
        }
    }

    static final class MultiNameSegment
            extends Segment {
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

    static final class AllSegment
            extends Segment {
        static final AllSegment INSTANCE = new AllSegment(false);
        static final AllSegment INSTANCE_ARRAY = new AllSegment(true);

        final boolean array;

        AllSegment(boolean array) {
            this.array = array;
        }

        @Override
        public void eval(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object == null) {
                context.value = null;
                context.eval = true;
                return;
            }

            if (object instanceof Map) {
                Map map = (Map) object;
                JSONArray array = new JSONArray(map.size());
                for (Object value : map.values()) {
                    if (this.array && value instanceof Collection) {
                        array.addAll((Collection) value);
                    } else {
                        array.add(value);
                    }
                }
                if (context.next != null) {
                    context.value = new Sequence(array);
                } else {
                    context.value = array;
                }
                context.eval = true;
                return;
            }

            if (object instanceof List) {
                List list = (List) object;
                JSONArray values = new JSONArray(list.size());
                if (context.next == null && !array) {
                    for (Object item : list) {
                        if (item instanceof Map) {
                            values.addAll(((Map<?, ?>) item).values());
                        } else {
                            values.add(item);
                        }
                    }
                    context.value = values;
                    context.eval = true;
                    return;
                }

                if (context.next != null) {
                    context.value = new Sequence(list);
                } else {
                    context.value = object;
                }
                context.eval = true;
                return;
            }

            if (object instanceof Collection) {
                // skip
                context.value = object;
                context.eval = true;
                return;
            }

            if (object instanceof Sequence) {
                List list = ((Sequence) object).values;
                JSONArray values = new JSONArray(list.size());
                if (context.next == null && !array) {
                    for (Object item : list) {
                        if (item instanceof Map) {
                            values.addAll(((Map<?, ?>) item).values());
                        } else {
                            values.add(item);
                        }
                    }
                    context.value = values;
                    context.eval = true;
                    return;
                }

                if (context.next != null) {
                    context.value = new Sequence(list);
                } else {
                    context.value = object;
                }
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
        public boolean remove(Context context) {
            Object object = context.parent == null
                    ? context.root
                    : context.parent.value;

            if (object instanceof Map) {
                ((Map<?, ?>) object).clear();
                return true;
            }

            if (object instanceof Collection) {
                ((Collection<?>) object).clear();
                return true;
            }

            throw new JSONException("UnsupportedOperation " + getClass());
        }

        @Override
        public void accept(JSONReader jsonReader, Context context) {
            if (context.parent != null && context.parent.eval) {
                eval(context);
                return;
            }

            if (jsonReader.isJSONB()) {
                List<Object> values = new JSONArray();
                if (jsonReader.nextIfMatch(BC_OBJECT)) {
                    while (!jsonReader.nextIfMatch(BC_OBJECT_END)) {
                        if (jsonReader.skipName()) {
                            Object val = jsonReader.readAny();

                            if (array && val instanceof Collection) {
                                values.addAll((Collection) val);
                            } else {
                                values.add(val);
                            }
                        }
                    }

                    context.value = values;
                    return;
                }

                if (jsonReader.isArray() && context.next != null) {
                    // skip
                    return;
                }

                throw new JSONException("TODO");
            }

            List<Object> values = new JSONArray();

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
                context.value = values;
                context.eval = true;
                return;
            }

            if (jsonReader.ch == '[') {
                jsonReader.next();
                for (; ; ) {
                    if (jsonReader.ch == ']') {
                        jsonReader.next();
                        break;
                    }
                    Object value = jsonReader.readAny();
                    if (context.next == null && value instanceof Map) {
                        values.addAll(((Map<?, ?>) value).values());
                    } else {
                        values.add(value);
                    }
                    if (jsonReader.ch == ',') {
                        jsonReader.next();
                    }
                }

                if (context.next != null) {
                    context.value = new Sequence(values);
                } else {
                    context.value = values;
                }
                context.eval = true;
                return;
            }

            throw new JSONException("TODO");
        }
    }

    static final class BiFunctionAdapter
            implements BiFunction {
        private final Function function;

        BiFunctionAdapter(Function function) {
            this.function = function;
        }

        @Override
        public Object apply(Object o1, Object o2) {
            return function.apply(o2);
        }
    }

    static class Sequence {
        final List values;

        public Sequence(List values) {
            this.values = values;
        }
    }

    static class JSONPathParser {
        final String path;
        final JSONReader jsonReader;

        boolean dollar;
        boolean lax;
        boolean strict;

        int segmentIndex;
        Segment first;
        Segment second;

        List<Segment> segments;

        boolean negative;

        public JSONPathParser(String str) {
            this.jsonReader = JSONReader.of(this.path = str, PARSE_CONTEXT);

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

        JSONPath parse(Feature... features) {
            if (dollar && jsonReader.ch == EOI) {
                if (negative) {
                    return new SingleSegmentPath(FUNCTION_NEGATIVE, path);
                } else {
                    return RootPath.INSTANCE;
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
                Segment segment;
                if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_' || ch == '@') {
                    segment = parseProperty();
                } else {
                    throw new JSONException("syntax error " + path);
                }

                if (!jsonReader.nextIfMatch(')')) {
                    throw new JSONException("syntax error " + path);
                }

                return new TwoSegmentPath(path, segment, FUNCTION_EXISTS);
            }

            while (jsonReader.ch != EOI) {
                final Segment segment;

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
                        first = RootSegment.INSTANCE;
                        segmentIndex++;
                    }
                    jsonReader.next();
                    segment = parseFilter();
                } else if (ch == '@') {
                    jsonReader.next();
                    segment = SelfSegment.INSTANCE;
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
                    second = FUNCTION_NEGATIVE;
                } else if (segmentIndex == 2) {
                    segments = new ArrayList<>();
                    segments.add(first);
                    segments.add(second);
                    segments.add(FUNCTION_NEGATIVE);
                } else {
                    segments.add(FUNCTION_NEGATIVE);
                }
                segmentIndex++;
            }

            if (segmentIndex == 1) {
                if (first instanceof NameSegment) {
                    return new SingleNamePath(path, (NameSegment) first, features);
                }

                return new SingleSegmentPath(first, path);
            }

            if (segmentIndex == 2) {
                return new TwoSegmentPath(path, first, second, features);
            }

            return new MultiSegmentPath(path, segments, features);
        }

        private Segment parseArrayAccess() {
            jsonReader.next();

            Segment segment;
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
                            segment = new RangeIndexSegment(index, index >= 0 ? Integer.MAX_VALUE : 0);
                        } else {
                            int end = jsonReader.readInt32Value();
                            segment = new RangeIndexSegment(index, end);
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
                        segment = new MultiIndexSegment(indics);
                    } else {
                        segment = IndexSegment.of(index);
                    }
                    break;
                }
                case '*':
                    jsonReader.next();
                    segment = AllSegment.INSTANCE_ARRAY;
                    break;
                case ':': {
                    jsonReader.next();
                    int end = jsonReader.ch == ']' ? 0 : jsonReader.readInt32Value();

                    if (end > 0) {
                        segment = new RangeIndexSegment(0, end);
                    } else {
                        segment = new RangeIndexSegment(Integer.MIN_VALUE, end);
                    }
                    break;
                }
                case '"':
                case '\'':
                    String name = jsonReader.readString();
                    if (jsonReader.current() == ']') {
                        segment = new NameSegment(name, Fnv.hashCode64(name));
                    } else if (jsonReader.isString()) {
                        List<String> names = new ArrayList<>();
                        names.add(name);
                        do {
                            names.add(jsonReader.readString());
                        } while (jsonReader.isString());
                        String[] nameArray = new String[names.size()];
                        names.toArray(nameArray);
                        segment = new MultiNameSegment(nameArray);
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
                        segment = RandomIndexSegment.INSTANCE;
                        break;
                    }
                    throw new JSONException("not support : " + fieldName);
                }
                case 'l': {
                    String fieldName = jsonReader.readFieldNameUnquote();
                    if ("last".equals(fieldName)) {
                        segment = IndexSegment.of(-1);
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

        private Segment parseProperty() {
            final Segment segment;
            if (jsonReader.ch == '*') {
                jsonReader.next();
                segment = AllSegment.INSTANCE;
            } else if (jsonReader.ch == '.') {
                jsonReader.next();
                if (jsonReader.ch == '*') {
                    jsonReader.next();
                    segment = new CycleNameSegment("*", Fnv.hashCode64("*"));
                } else {
                    long hashCode = jsonReader.readFieldNameHashCodeUnquote();
                    String name = jsonReader.getFieldName();
                    segment = new CycleNameSegment(name, hashCode);
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
                IndexSegment indexSegment = null;
                if (isNum) {
                    try {
                        int index = Integer.parseInt(name);
                        indexSegment = IndexSegment.of(index);
                    } catch (NumberFormatException ignored) {
                    }
                }

                if (indexSegment != null) {
                    segment = indexSegment;
                } else {
                    if (jsonReader.ch == '(') {
                        switch (name) {
                            case "length":
                            case "size":
                                segment = LengthSegment.INSTANCE;
                                break;
                            case "keys":
                                segment = KeysSegment.INSTANCE;
                                break;
                            case "values":
                                segment = ValuesSegment.INSTANCE;
                                break;
                            case "entrySet":
                                segment = EntrySetSegment.INSTANCE;
                                break;
                            case "min":
                                segment = MinSegment.INSTANCE;
                                break;
                            case "max":
                                segment = MaxSegment.INSTANCE;
                                break;
                            case "sum":
                                segment = SumSegment.INSTANCE;
                                break;
                            case "type":
                                segment = FUNCTION_TYPE;
                                break;
                            case "floor":
                                segment = FUNCTION_FLOOR;
                                break;
                            case "ceil":
                            case "ceiling":
                                segment = FUNCTION_CEIL;
                                break;
                            case "double":
                                segment = FUNCTION_DOUBLE;
                                break;
                            case "abs":
                                segment = FUNCTION_ABS;
                                break;
                            case "negative":
                                segment = FUNCTION_NEGATIVE;
                                break;
                            default:
                                throw new JSONException("not support syntax, path : " + path);
                        }
                        jsonReader.next();
                        if (!jsonReader.nextIfMatch(')')) {
                            throw new JSONException("not support syntax, path : " + path);
                        }
                    } else {
                        segment = new NameSegment(name, hashCode);
                    }
                }
            }
            return segment;
        }

        Segment parseFilterRest(Segment segment) {
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

            Segment right = parseFilter();
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

        Segment parseFilter() {
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
                            NameExistsFilter segment = new NameExistsFilter(fieldName, hashCode);
                            return segment;
                        }
                    }
                }

                throw new JSONException(jsonReader.info("jsonpath syntax error"));
            }

            boolean starts = jsonReader.nextIfMatchIdent('s', 't', 'a', 'r', 't', 's');
            if ((at && starts) || (jsonReader.ch != '.' && !JSONReader.isFirstIdentifier(jsonReader.ch))) {
                if (!at) {
                    throw new JSONException(jsonReader.info("jsonpath syntax error"));
                }

                Operator operator;
                if (starts) {
                    jsonReader.readFieldNameHashCodeUnquote();
                    String fieldName = jsonReader.getFieldName();
                    if (!"with".equalsIgnoreCase(fieldName)) {
                        throw new JSONException("not support operator : " + fieldName);
                    }
                    operator = Operator.STARTS_WITH;
                } else {
                    operator = parseOperator(jsonReader);
                }

                Segment segment = null;
                if (jsonReader.isNumber()) {
                    Number number = jsonReader.readNumber();
                    if (number instanceof Integer || number instanceof Long) {
                        segment = new NameIntOpSegment(null, 0, null, null, null, operator, number.longValue());
                    }
                } else if (jsonReader.isString()) {
                    String string = jsonReader.readString();

                    switch (operator) {
                        case STARTS_WITH:
                            segment = new StartsWithSegment(null, 0, string);
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

            long[] hashCode2 = null;
            String[] fieldName2 = null;
            while (jsonReader.ch == '.') {
                jsonReader.next();
                long hash = jsonReader.readFieldNameHashCodeUnquote();
                String str = jsonReader.getFieldName();

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
                        segment = new NameIntInSegment(fieldName, hashCode, fieldName2, hashCode2, function, values, operator == Operator.NOT_IN);
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
                        segment = new NameIntOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, number.longValue());
                    } else if (number instanceof BigDecimal) {
                        segment = new NameDecimalOpSegment(fieldName, hashCode, operator, (BigDecimal) number);
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
                                fieldName,
                                hashCode,
                                startsWithValue,
                                endsWithValue,
                                containsValues,
                                operator == Operator.NOT_LIKE
                        );
                    } else {
                        segment = new NameStringOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, strVal);
                    }
                    break;
                }
                case 't': {
                    String ident = jsonReader.readFieldNameUnquote();
                    if ("true".equalsIgnoreCase(ident)) {
                        segment = new NameIntOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, 1);
                        break;
                    }
                    break;
                }
                case 'f': {
                    String ident = jsonReader.readFieldNameUnquote();
                    if ("false".equalsIgnoreCase(ident)) {
                        segment = new NameIntOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, 0);
                        break;
                    }
                    break;
                }
                case '[': {
                    JSONArray array = jsonReader.read(JSONArray.class);
                    segment = new NameArrayOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, array);
                    break;
                }
                case '{': {
                    JSONObject object = jsonReader.read(JSONObject.class);
                    segment = new NameObjectOpSegment(fieldName, hashCode, fieldName2, hashCode2, function, operator, object);
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

    public enum Feature {
        AlwaysReturnList(1);
        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }
}
