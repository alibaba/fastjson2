package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    protected JSONPath(String path, long features) {
        this.path = path;
        this.features = features;
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

    protected JSONReader.Context createContext() {
        return JSONFactory.createReadContext();
    }

    public Object extract(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }

        try (JSONReader jsonReader = JSONReader.of(jsonStr, createContext())) {
            return extract(jsonReader);
        }
    }

    public Object extract(byte[] jsonBytes) {
        if (jsonBytes == null) {
            return null;
        }

        try (JSONReader jsonReader = JSONReader.of(jsonBytes, createContext())) {
            return extract(jsonReader);
        }
    }

    public Object extract(byte[] jsonBytes, int off, int len, Charset charset) {
        if (jsonBytes == null) {
            return null;
        }

        try (JSONReader jsonReader = JSONReader.of(jsonBytes, off, len, charset, createContext())) {
            return extract(jsonReader);
        }
    }

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
                new JSONPathFunction.BiFunctionAdapter(callback)
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

    public static JSONPath of(String path, Type type) {
        JSONPath jsonPath = of(path);
        return JSONPathTyped.of(jsonPath, type);
    }

    /**
     * create multi-path jsonpath
     *
     * @param paths jsonpath array
     * @param types item types
     * @since 2.0.20
     */
    public static JSONPath of(String[] paths, Type[] types) {
        return of(paths, types, null, null, (ZoneId) null);
    }

    /**
     * create multi-path jsonpath
     *
     * @param paths jsonpath array
     * @param types item types
     * @since 2.0.20
     */
    public static JSONPath of(String[] paths, Type[] types, JSONReader.Feature... features) {
        return of(paths, types, null, null, null, features);
    }

    /**
     * create multi-path jsonpath
     *
     * @param paths jsonpath array
     * @param types item types
     * @param formats item format
     * @param zoneId zonedId
     * @param features parse use JSONReader.Features
     * @since 2.0.20
     */
    public static JSONPath of(
            String[] paths,
            Type[] types,
            String[] formats,
            long[] pathFeatures,
            ZoneId zoneId,
            JSONReader.Feature... features
    ) {
        if (paths.length == 0) {
            throw new JSONException("illegal paths, not support 0 length");
        }

        if (types == null) {
            types = new Type[paths.length];
            Arrays.fill(types, Object.class);
        }

        if (types.length != paths.length) {
            throw new JSONException("types.length not equals paths.length");
        }

        JSONPath[] jsonPaths = new JSONPath[paths.length];
        for (int i = 0; i < paths.length; i++) {
            jsonPaths[i] = of(paths[i]);
        }

        boolean allSingleName = true, allSinglePositiveIndex = true;
        boolean allTwoName = true, allTwoIndexPositive = true;
        boolean allThreeName = true;
        for (JSONPath path : jsonPaths) {
            if (allSingleName && !(path instanceof JSONPathSingleName)) {
                allSingleName = false;
            }

            if (allSinglePositiveIndex) {
                if (!(path instanceof JSONPathSingleIndex)
                        || ((JSONPathSingleIndex) path).index < 0) {
                    allSinglePositiveIndex = false;
                }
            }

            if (allTwoName) {
                if (path instanceof JSONPathTwoSegment) {
                    JSONPathTwoSegment two = (JSONPathTwoSegment) path;
                    if (!(two.second instanceof JSONPathSegmentName)) {
                        allTwoName = false;
                    }
                } else {
                    allTwoName = false;
                }
            }

            if (allTwoIndexPositive) {
                if (path instanceof JSONPathTwoSegment) {
                    JSONPathTwoSegment two = (JSONPathTwoSegment) path;
                    if (!(two.second instanceof JSONPathSegmentIndex) || ((JSONPathSegmentIndex) two.second).index < 0) {
                        allTwoIndexPositive = false;
                    }
                } else {
                    allTwoIndexPositive = false;
                }
            }

            if (allThreeName) {
                if (path instanceof JSONPathMulti) {
                    JSONPathMulti multi = (JSONPathMulti) path;
                    if (multi.segments.size() == 3) {
                        JSONPathSegment three = multi.segments.get(2);
                        if (!(three instanceof JSONPathSegmentName)) {
                            allThreeName = false;
                        }
                    } else {
                        allThreeName = false;
                    }
                } else {
                    allThreeName = false;
                }
            }
        }

        long featuresValue = JSONReader.Feature.of(features);

        if (allSingleName) {
            return new JSONPathTypedMultiNames(
                    jsonPaths,
                    null,
                    jsonPaths,
                    types,
                    formats,
                    pathFeatures,
                    zoneId,
                    featuresValue
            );
        }

        if (allSinglePositiveIndex) {
            return new JSONPathTypedMultiIndexes(jsonPaths, null, jsonPaths, types, formats, pathFeatures, zoneId, featuresValue);
        }

        if (allTwoName || allTwoIndexPositive) {
            boolean samePrefix = true;
            JSONPathSegment first0 = ((JSONPathTwoSegment) jsonPaths[0]).first;
            for (int i = 1; i < jsonPaths.length; i++) {
                JSONPathTwoSegment two = (JSONPathTwoSegment) jsonPaths[i];
                if (!first0.equals(two.first)) {
                    samePrefix = false;
                    break;
                }
            }

            if (samePrefix) {
                JSONPath firstPath = jsonPaths[0];

                if (allTwoName) {
                    JSONPathSingleName[] names = new JSONPathSingleName[jsonPaths.length];
                    for (int i = 0; i < jsonPaths.length; i++) {
                        JSONPathTwoSegment two = (JSONPathTwoSegment) jsonPaths[i];
                        JSONPathSegmentName name = (JSONPathSegmentName) two.second;
                        names[i] = new JSONPathSingleName("$." + name, name);
                    }

                    String prefixPath = firstPath.path.substring(0, firstPath.path.length() - names[0].name.length() - 1);
                    if (first0 instanceof JSONPathSegmentName) {
                        JSONPathSegmentName name = (JSONPathSegmentName) first0;
                        JSONPath prefix = new JSONPathSingleName(prefixPath, name);

                        return new JSONPathTypedMultiNamesPrefixName1(
                                jsonPaths,
                                prefix,
                                names,
                                types,
                                formats,
                                pathFeatures,
                                zoneId,
                                featuresValue
                        );
                    } else if (first0 instanceof JSONPathSegmentIndex) {
                        JSONPathSegmentIndex first0Index = ((JSONPathSegmentIndex) first0);
                        if (first0Index.index >= 0) {
                            JSONPathSingleIndex prefix = new JSONPathSingleIndex(prefixPath, first0Index);
                            return new JSONPathTypedMultiNamesPrefixIndex1(
                                    jsonPaths,
                                    prefix,
                                    names,
                                    types,
                                    formats,
                                    pathFeatures,
                                    zoneId,
                                    featuresValue
                            );
                        }
                    }
                } else if (allTwoIndexPositive) {
                    JSONPathSingleIndex[] indexes = new JSONPathSingleIndex[jsonPaths.length];
                    for (int i = 0; i < jsonPaths.length; i++) {
                        JSONPathTwoSegment two = (JSONPathTwoSegment) jsonPaths[i];
                        JSONPathSegmentIndex name = (JSONPathSegmentIndex) two.second;
                        indexes[i] = new JSONPathSingleIndex("$" + name, name);
                    }

                    JSONPath prefix = null;
                    if (first0 instanceof JSONPathSegmentName) {
                        JSONPathSegmentName name = (JSONPathSegmentName) first0;
                        prefix = new JSONPathSingleName("$." + name.name, name);
                    } else if (first0 instanceof JSONPathSegmentIndex) {
                        JSONPathSegmentIndex index = (JSONPathSegmentIndex) first0;
                        prefix = new JSONPathSingleIndex("$[" + index.index + "]", index);
                    }

                    if (prefix != null) {
                        return new JSONPathTypedMultiIndexes(
                                jsonPaths,
                                prefix,
                                indexes,
                                types,
                                formats,
                                pathFeatures,
                                zoneId,
                                featuresValue
                        );
                    }
                }
            }
        } else if (allThreeName) {
            boolean samePrefix = true;
            JSONPathSegment first0 = ((JSONPathMulti) jsonPaths[0]).segments.get(0);
            JSONPathSegment first1 = ((JSONPathMulti) jsonPaths[0]).segments.get(1);
            for (int i = 1; i < jsonPaths.length; i++) {
                JSONPathMulti multi = (JSONPathMulti) jsonPaths[i];
                if (!first0.equals(multi.segments.get(0))) {
                    samePrefix = false;
                    break;
                }
                if (!first1.equals(multi.segments.get(1))) {
                    samePrefix = false;
                    break;
                }
            }

            if (samePrefix) {
                JSONPathSingleName[] names = new JSONPathSingleName[jsonPaths.length];
                for (int i = 0; i < jsonPaths.length; i++) {
                    JSONPathMulti multi = (JSONPathMulti) jsonPaths[i];
                    JSONPathSegmentName name = (JSONPathSegmentName) multi.segments.get(2);
                    names[i] = new JSONPathSingleName("$." + name, name);
                }

                JSONPath firstPath = jsonPaths[0];
                String prefixPath = firstPath.path.substring(0, firstPath.path.length() - names[0].name.length() - 1);
                JSONPathTwoSegment prefix = new JSONPathTwoSegment(prefixPath, first0, first1);

                if (first0 instanceof JSONPathSegmentName && first1 instanceof JSONPathSegmentName) {
                    return new JSONPathTypedMultiNamesPrefixName2(
                            jsonPaths,
                            prefix,
                            names,
                            types,
                            formats,
                            pathFeatures,
                            zoneId,
                            featuresValue
                    );
                }

                return new JSONPathTypedMultiNames(
                        jsonPaths,
                        prefix,
                        names,
                        types,
                        formats,
                        pathFeatures,
                        zoneId,
                        featuresValue
                );
            }
        }

        return new JSONPathTypedMulti(jsonPaths, types, formats, pathFeatures, zoneId, featuresValue);
    }

    public static JSONPath of(String path, Feature... features) {
        if ("#-1".equals(path)) {
            return PreviousPath.INSTANCE;
        }

        return new JSONPathParser(path)
                .parse(features);
    }

    static JSONPathFilter.Operator parseOperator(JSONReader jsonReader) {
        JSONPathFilter.Operator operator;
        switch (jsonReader.ch) {
            case '<':
                jsonReader.next();
                if (jsonReader.ch == '=') {
                    jsonReader.next();
                    operator = JSONPathFilter.Operator.LE;
                } else if (jsonReader.ch == '>') {
                    jsonReader.next();
                    operator = JSONPathFilter.Operator.NE;
                } else {
                    operator = JSONPathFilter.Operator.LT;
                }
                break;
            case '=':
                jsonReader.next();
                if (jsonReader.ch == '~') {
                    jsonReader.next();
                    operator = JSONPathFilter.Operator.REG_MATCH;
                } else if (jsonReader.ch == '=') {
                    jsonReader.next();
                    operator = JSONPathFilter.Operator.EQ;
                } else {
                    operator = JSONPathFilter.Operator.EQ;
                }
                break;
            case '!':
                jsonReader.next();
                if (jsonReader.ch == '=') {
                    jsonReader.next();
                    operator = JSONPathFilter.Operator.NE;
                } else {
                    throw new JSONException("not support operator : !" + jsonReader.ch);
                }
                break;
            case '>':
                jsonReader.next();
                if (jsonReader.ch == '=') {
                    jsonReader.next();
                    operator = JSONPathFilter.Operator.GE;
                } else {
                    operator = JSONPathFilter.Operator.GT;
                }
                break;
            case 'l':
            case 'L': {
                jsonReader.readFieldNameHashCodeUnquote();
                String fieldName = jsonReader.getFieldName();
                if ("like".equalsIgnoreCase(fieldName)) {
                    operator = JSONPathFilter.Operator.LIKE;
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
                    operator = JSONPathFilter.Operator.NOT_IN;
                    break;
                }

                if (!"not".equalsIgnoreCase(fieldName)) {
                    throw new JSONException("not support operator : " + fieldName);
                }

                jsonReader.readFieldNameHashCodeUnquote();
                fieldName = jsonReader.getFieldName();
                if ("like".equalsIgnoreCase(fieldName)) {
                    operator = JSONPathFilter.Operator.NOT_LIKE;
                } else if ("rlike".equalsIgnoreCase(fieldName)) {
                    operator = JSONPathFilter.Operator.NOT_RLIKE;
                } else if ("in".equalsIgnoreCase(fieldName)) {
                    operator = JSONPathFilter.Operator.NOT_IN;
                } else if ("between".equalsIgnoreCase(fieldName)) {
                    operator = JSONPathFilter.Operator.NOT_BETWEEN;
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
                    operator = JSONPathFilter.Operator.IN;
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
                    operator = JSONPathFilter.Operator.RLIKE;
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
                    operator = JSONPathFilter.Operator.BETWEEN;
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
                operator = JSONPathFilter.Operator.STARTS_WITH;
                break;
            }
            case 'e':
            case 'E':
                jsonReader.readFieldNameHashCodeUnquote();
                String fieldName = jsonReader.getFieldName();
                if (!"ends".equalsIgnoreCase(fieldName)) {
                    throw new JSONException("not support operator : " + fieldName);
                }

                jsonReader.readFieldNameHashCodeUnquote();
                fieldName = jsonReader.getFieldName();
                if (!"with".equalsIgnoreCase(fieldName)) {
                    throw new JSONException("not support operator : " + fieldName);
                }
                operator = JSONPathFilter.Operator.ENDS_WITH;
                break;
            default: {
                jsonReader.readFieldNameHashCodeUnquote();
                throw new JSONException("not support operator : " + jsonReader.getFieldName());
            }
        }
        return operator;
    }

    static final class PreviousPath
            extends JSONPath {
        static final PreviousPath INSTANCE = new PreviousPath("#-1");

        PreviousPath(String path) {
            super(path);
        }

        @Override
        public boolean isRef() {
            throw new JSONException("unsupported operation");
        }

        @Override
        public boolean isPrevious() {
            return true;
        }

        @Override
        public boolean contains(Object rootObject) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public Object eval(Object rootObject) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public Object extract(JSONReader jsonReader) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public String extractScalar(JSONReader jsonReader) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public void set(Object rootObject, Object value) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public void set(Object rootObject, Object value, JSONReader.Feature... readerFeatures) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public void setCallback(Object rootObject, BiFunction callback) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public void setInt(Object rootObject, int value) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public void setLong(Object rootObject, long value) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public boolean remove(Object rootObject) {
            throw new JSONException("unsupported operation");
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
            throw new JSONException("unsupported operation");
        }

        @Override
        public void set(Object object, Object value, JSONReader.Feature... readerFeatures) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public void setCallback(Object object, BiFunction callback) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public void setInt(Object object, int value) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public void setLong(Object object, long value) {
            throw new JSONException("unsupported operation");
        }

        @Override
        public boolean remove(Object object) {
            return false;
        }
    }

    static final class Context {
        final JSONPath path;
        final Context parent;
        final JSONPathSegment current;
        final JSONPathSegment next;
        final long readerFeatures;
        Object root;
        Object value;

        boolean eval;

        Context(JSONPath path, Context parent, JSONPathSegment current, JSONPathSegment next, long readerFeatures) {
            this.path = path;
            this.current = current;
            this.next = next;
            this.parent = parent;
            this.readerFeatures = readerFeatures;
        }
    }

    static class Sequence {
        final List values;

        public Sequence(List values) {
            this.values = values;
        }
    }

    public enum Feature {
        AlwaysReturnList(1),
        NullOnError(1 << 1);
        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }
}
