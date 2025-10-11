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

/**
 * Represents a JSONPath expression for querying and manipulating JSON data.
 *
 * <p>JSONPath is a query language for JSON, similar to XPath for XML.
 * It allows you to select and extract data from a JSON document.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Parse JSON string and extract value using JSONPath
 * String json = "{\"name\":\"John\", \"age\":30, \"city\":\"New York\"}";
 * Object result = JSONPath.extract(json, "$.name");
 * // result = "John"
 *
 * // Evaluate JSONPath on an object
 * Map<String, Object> map = new HashMap<>();
 * map.put("name", "John");
 * map.put("age", 30);
 * Object result = JSONPath.eval(map, "$.name");
 * // result = "John"
 *
 * // Set value using JSONPath
 * JSONPath.set(map, "$.name", "Jane");
 * // map.get("name") = "Jane"
 *
 * // Remove value using JSONPath
 * JSONPath.remove(map, "$.age");
 * // map.size() = 1 (age field removed)
 *
 * // Check if path exists
 * boolean exists = JSONPath.contains(map, "$.name");
 * // exists = true
 * }</pre>
 *
 * <p>Supported JSONPath expressions:
 * <ul>
 *   <li><code>$</code> - Root object/element</li>
 *   <li><code>@</code> - Current object/element</li>
 *   <li><code>. or []</code> - Child operator</li>
 *   <li><code>..</code> - Recursive descent</li>
 *   <li><code>*</code> - Wildcard</li>
 *   <li><code>[]</code> - Array index or filter</li>
 *   <li><code>[start:end:step]</code> - Array slice</li>
 *   <li><code>?()</code> - Filter expression</li>
 * </ul>
 *
 * @see <a href="https://github.com/json-path/JsonPath">JSONPath specification</a>
 * @since 2.0.0
 */
public abstract class JSONPath {
    static final JSONReader.Context PARSE_CONTEXT = JSONFactory.createReadContext();

    /**
     * Context for JSON reading operations
     */
    JSONReader.Context readerContext;

    /**
     * Context for JSON writing operations
     */
    JSONWriter.Context writerContext;

    /**
     * The JSONPath expression string
     */
    final String path;

    /**
     * Feature flags for this JSONPath
     */
    final long features;

    /**
     * Constructs a JSONPath with the specified path and features
     *
     * @param path the JSONPath expression
     * @param features the features to apply
     */
    protected JSONPath(String path, Feature... features) {
        this.path = path;
        long featuresValue = 0;
        for (Feature feature : features) {
            featuresValue |= feature.mask;
        }
        this.features = featuresValue;
    }

    /**
     * Constructs a JSONPath with the specified path and feature flags
     *
     * @param path the JSONPath expression
     * @param features the feature flags to apply
     */
    protected JSONPath(String path, long features) {
        this.path = path;
        this.features = features;
    }

    /**
     * Gets the parent JSONPath of this path
     *
     * @return the parent JSONPath, or null if this is the root path
     */
    public abstract JSONPath getParent();

    /**
     * Checks if this path ends with a filter
     *
     * @return true if this path ends with a filter, false otherwise
     */
    public boolean endsWithFilter() {
        return false;
    }

    /**
     * Checks if this path represents a previous reference
     *
     * @return true if this path is a previous reference, false otherwise
     */
    public boolean isPrevious() {
        return false;
    }

    /**
     * Returns the string representation of this JSONPath
     *
     * @return the JSONPath expression string
     */
    @Override
    public final String toString() {
        return path;
    }

    /**
     * Extracts a value from JSON string using the specified path
     *
     * @param json the JSON string to extract from
     * @param path the JSONPath expression
     * @return the extracted value, or null if not found
     */
    public static Object extract(String json, String path) {
        JSONReader jsonReader = JSONReader.of(json);
        JSONPath jsonPath = JSONPath.of(path);
        return jsonPath.extract(jsonReader);
    }

    /**
     * Extracts a value from JSON string using the specified path and features
     *
     * @param json the JSON string to extract from
     * @param path the JSONPath expression
     * @param features the features to apply during extraction
     * @return the extracted value, or null if not found
     */
    public static Object extract(String json, String path, Feature... features) {
        JSONReader jsonReader = JSONReader.of(json);
        JSONPath jsonPath = JSONPath.of(path, features);
        return jsonPath.extract(jsonReader);
    }

    /**
     * Evaluates the JSONPath expression on a JSON string
     *
     * @param str the JSON string to evaluate
     * @param path the JSONPath expression
     * @return the evaluated result, or null if not found
     */
    public static Object eval(String str, String path) {
        return extract(str, path);
    }

    /**
     * Evaluates the JSONPath expression on an object
     *
     * @param rootObject the root object to evaluate
     * @param path the JSONPath expression
     * @return the evaluated result, or null if not found
     */
    public static Object eval(Object rootObject, String path) {
        return JSONPath.of(path)
                .eval(rootObject);
    }

    /**
     * Sets a value in a JSON string using the specified path and returns the modified JSON
     *
     * @param json the JSON string to modify
     * @param path the JSONPath expression
     * @param value the value to set
     * @return the modified JSON string
     */
    public static String set(String json, String path, Object value) {
        Object object = JSON.parse(json);
        JSONPath.of(path)
                .set(object, value);
        return JSON.toJSONString(object);
    }

    /**
     * Checks if the specified path exists in the object
     *
     * @param rootObject the root object to check
     * @param path the JSONPath expression
     * @return true if the path exists, false otherwise
     */
    public static boolean contains(Object rootObject, String path) {
        if (rootObject == null) {
            return false;
        }

        JSONPath jsonPath = JSONPath.of(path);
        return jsonPath.contains(rootObject);
    }

    /**
     * Sets a value in an object using the specified path
     *
     * @param rootObject the root object to modify
     * @param path the JSONPath expression
     * @param value the value to set
     * @return the modified root object
     */
    public static Object set(Object rootObject, String path, Object value) {
        JSONPath.of(path)
                .set(rootObject, value);

        return rootObject;
    }

    /**
     * Sets a callback function for the specified path in an object
     *
     * @param rootObject the root object to modify
     * @param path the JSONPath expression
     * @param callback the callback function to set
     * @return the modified root object
     */
    public static Object setCallback(Object rootObject, String path, Function callback) {
        JSONPath.of(path)
                .setCallback(rootObject, callback);

        return rootObject;
    }

    /**
     * Sets a callback function for the specified path in an object
     *
     * @param rootObject the root object to modify
     * @param path the JSONPath expression
     * @param callback the callback function to set
     * @return the modified root object
     */
    public static Object setCallback(Object rootObject, String path, BiFunction callback) {
        JSONPath.of(path)
                .setCallback(rootObject, callback);

        return rootObject;
    }

    /**
     * Removes a value from a JSON string using the specified path and returns the modified JSON
     *
     * @param json the JSON string to modify
     * @param path the JSONPath expression
     * @return the modified JSON string
     */
    public static String remove(String json, String path) {
        Object object = JSON.parse(json);

        JSONPath.of(path)
                .remove(object);

        return JSON.toJSONString(object);
    }

    /**
     * Removes a value from an object using the specified path
     *
     * @param rootObject the root object to modify
     * @param path the JSONPath expression
     */
    public static void remove(Object rootObject, String path) {
        JSONPath.of(path)
                .remove(rootObject);
    }

    /**
     * Gets all paths in the object
     *
     * @param javaObject the object to get paths from
     * @return a map of paths to values
     */
    public static Map<String, Object> paths(Object javaObject) {
        Map<Object, String> values = new IdentityHashMap<>();
        Map<String, Object> paths = new LinkedHashMap<>();

        RootPath.INSTANCE.paths(values, paths, "$", javaObject);
        return paths;
    }

    /**
     * Recursively collects paths and values from the object
     *
     * @param values map to store object to path mappings
     * @param paths map to store path to value mappings
     * @param parent the parent path
     * @param javaObject the object to process
     */
    void paths(Map<Object, String> values, Map paths, String parent, Object javaObject) {
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

                String path;
                if (key instanceof String) {
                    String strKey = (String) key;
                    boolean escape = strKey.isEmpty();
                    if (!escape) {
                        char c0 = strKey.charAt(0);
                        escape = !((c0 >= 'a' && c0 <= 'z') || (c0 >= 'A' && c0 <= 'Z') || c0 == '_');
                        if (!escape) {
                            for (int i = 1; i < strKey.length(); i++) {
                                char ch = strKey.charAt(i);
                                escape = !((ch >= 'a' && ch <= 'z')
                                        || (ch >= 'A' && ch <= 'Z')
                                        || (ch >= '0' && ch <= '9')
                                        || ch == '_');
                                if (escape) {
                                    break;
                                }
                            }
                        }
                    }
                    if (escape) {
                        path = parent + '[' + JSON.toJSONString(strKey, JSONWriter.Feature.UseSingleQuotes) + ']';
                    } else {
                        path = parent + "." + strKey;
                    }
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

    /**
     * Checks if this path is a reference
     *
     * @return true if this path is a reference, false otherwise
     */
    public abstract boolean isRef();

    /**
     * Adds values to an array at the specified root object
     *
     * @param root the root object
     * @param values the values to add
     */
    public void arrayAdd(Object root, Object... values) {
        Object result = eval(root);
        if (result == null) {
            set(root, JSONArray.of(values));
            return;
        }

        if (result instanceof Collection) {
            Collection collection = (Collection) result;
            collection.addAll(Arrays.asList(values));
        }
    }

    /**
     * Checks if the path exists in the object
     *
     * @param object the object to check
     * @return true if the path exists, false otherwise
     */
    public abstract boolean contains(Object object);

    /**
     * Evaluates the path on the object
     *
     * @param object the object to evaluate
     * @return the evaluation result
     */
    public abstract Object eval(Object object);

    /**
     * Creates a new reading context
     *
     * @return a new JSONReader.Context
     */
    protected JSONReader.Context createContext() {
        return JSONFactory.createReadContext();
    }

    /**
     * Extracts a value from a JSON string
     *
     * @param jsonStr the JSON string to extract from
     * @return the extracted value, or null if input is null
     */
    public Object extract(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }

        try (JSONReader jsonReader = JSONReader.of(jsonStr, createContext())) {
            return extract(jsonReader);
        }
    }

    /**
     * Extracts a value from a JSON byte array
     *
     * @param jsonBytes the JSON bytes to extract from
     * @return the extracted value, or null if input is null
     */
    public Object extract(byte[] jsonBytes) {
        if (jsonBytes == null) {
            return null;
        }

        try (JSONReader jsonReader = JSONReader.of(jsonBytes, createContext())) {
            return extract(jsonReader);
        }
    }

    /**
     * Extracts a value from a JSON byte array with specified offset, length and charset
     *
     * @param jsonBytes the JSON bytes to extract from
     * @param off the offset in the byte array
     * @param len the number of bytes to read
     * @param charset the charset to use
     * @return the extracted value, or null if input is null
     */
    public Object extract(byte[] jsonBytes, int off, int len, Charset charset) {
        if (jsonBytes == null) {
            return null;
        }

        try (JSONReader jsonReader = JSONReader.of(jsonBytes, off, len, charset, createContext())) {
            return extract(jsonReader);
        }
    }

    /**
     * Extracts a value using the provided JSONReader
     *
     * @param jsonReader the JSONReader to use
     * @return the extracted value
     */
    public abstract Object extract(JSONReader jsonReader);

    /**
     * Extracts a scalar value using the provided JSONReader
     *
     * @param jsonReader the JSONReader to use
     * @return the extracted scalar value
     */
    public abstract String extractScalar(JSONReader jsonReader);

    /**
     * Gets the reading context, creating it if necessary
     *
     * @return the JSONReader.Context
     */
    public JSONReader.Context getReaderContext() {
        if (readerContext == null) {
            readerContext = JSONFactory.createReadContext();
        }
        return readerContext;
    }

    /**
     * Sets the reading context
     *
     * @param context the context to set
     * @return this JSONPath instance
     */
    public JSONPath setReaderContext(JSONReader.Context context) {
        this.readerContext = context;
        return this;
    }

    /**
     * Gets the writing context, creating it if necessary
     *
     * @return the JSONWriter.Context
     */
    public JSONWriter.Context getWriterContext() {
        if (writerContext == null) {
            writerContext = JSONFactory.createWriteContext();
        }
        return writerContext;
    }

    /**
     * Sets the writing context
     *
     * @param writerContext the context to set
     * @return this JSONPath instance
     */
    public JSONPath setWriterContext(JSONWriter.Context writerContext) {
        this.writerContext = writerContext;
        return this;
    }

    /**
     * Sets a value in the object
     *
     * @param object the object to modify
     * @param value the value to set
     */
    public abstract void set(Object object, Object value);

    /**
     * Sets a value in the object with specified reader features
     *
     * @param object the object to modify
     * @param value the value to set
     * @param readerFeatures the reader features to apply
     */
    public abstract void set(Object object, Object value, JSONReader.Feature... readerFeatures);

    /**
     * Sets a callback function for the object
     *
     * @param object the object to modify
     * @param callback the callback function to set
     */
    public void setCallback(Object object, Function callback) {
        setCallback(
                object,
                new JSONPathFunction.BiFunctionAdapter(callback)
        );
    }

    /**
     * Sets a callback function for the object
     *
     * @param object the object to modify
     * @param callback the callback function to set
     */
    public abstract void setCallback(Object object, BiFunction callback);

    /**
     * Sets an integer value in the object
     *
     * @param object the object to modify
     * @param value the integer value to set
     */
    public abstract void setInt(Object object, int value);

    /**
     * Sets a long value in the object
     *
     * @param object the object to modify
     * @param value the long value to set
     */
    public abstract void setLong(Object object, long value);

    /**
     * Removes a value from the object
     *
     * @param object the object to modify
     * @return true if removal was successful, false otherwise
     */
    public abstract boolean remove(Object object);

    /**
     * Extracts a value using the provided JSONReader and passes it to the consumer
     *
     * @param jsonReader the JSONReader to use
     * @param consumer the consumer to accept the extracted value
     */
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

    /**
     * Extracts a scalar value using the provided JSONReader and passes it to the consumer
     *
     * @param jsonReader the JSONReader to use
     * @param consumer the consumer to accept the extracted scalar value
     */
    public void extractScalar(JSONReader jsonReader, ValueConsumer consumer) {
        Object object = extractScalar(jsonReader);
        if (object == null) {
            consumer.acceptNull();
            return;
        }

        String str = object.toString();
        consumer.accept(str);
    }

    /**
     * Extracts a Long value using the provided JSONReader
     *
     * @param jsonReader the JSONReader to use
     * @return the extracted Long value, or null if the value was null
     */
    public Long extractInt64(JSONReader jsonReader) {
        long value = extractInt64Value(jsonReader);
        if (jsonReader.wasNull) {
            return null;
        }
        return value;
    }

    /**
     * Extracts a long value using the provided JSONReader
     *
     * @param jsonReader the JSONReader to use
     * @return the extracted long value, or 0 if the value was null
     */
    public long extractInt64Value(JSONReader jsonReader) {
        Object object = extract(jsonReader);
        if (object == null) {
            jsonReader.wasNull = true;
            return 0L;
        }

        if (object instanceof Number) {
            return ((Number) object).longValue();
        }

        Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(object.getClass(), long.class);
        if (typeConvert == null) {
            throw new JSONException("can not convert to long : " + object);
        }
        Object converted = typeConvert.apply(object);
        return (Long) converted;
    }

    /**
     * Extracts an Integer value using the provided JSONReader
     *
     * @param jsonReader the JSONReader to use
     * @return the extracted Integer value, or null if the value was null
     */
    public Integer extractInt32(JSONReader jsonReader) {
        int intValue = extractInt32Value(jsonReader);
        if (jsonReader.wasNull) {
            return null;
        }
        return intValue;
    }

    /**
     * Extracts an int value using the provided JSONReader
     *
     * @param jsonReader the JSONReader to use
     * @return the extracted int value, or 0 if the value was null
     */
    public int extractInt32Value(JSONReader jsonReader) {
        Object object = extract(jsonReader);
        if (object == null) {
            jsonReader.wasNull = true;
            return 0;
        }
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        Function typeConvert = JSONFactory.getDefaultObjectReaderProvider().getTypeConvert(object.getClass(), int.class);
        if (typeConvert == null) {
            throw new JSONException("can not convert to int : " + object);
        }
        return (Integer) typeConvert.apply(object);
    }

    /**
     * Compiles a JSONPath expression (deprecated, use {@link #of(String)} instead)
     *
     * @param path the JSONPath expression to compile
     * @return the compiled JSONPath
     * @deprecated use {@link #of(String)} instead
     */
    @Deprecated
    public static JSONPath compile(String path) {
        return of(path);
    }

    /**
     * Compiles a JSONPath expression for a specific object class
     *
     * @param strPath the JSONPath expression to compile
     * @param objectClass the class of the object to use with this path
     * @return the compiled JSONPath
     */
    public static JSONPath compile(String strPath, Class objectClass) {
        JSONPath path = of(strPath);
        JSONFactory.JSONPathCompiler compiler = JSONFactory.getDefaultJSONPathCompiler();
        return compiler.compile(objectClass, path);
    }

    /**
     * Creates a single segment JSONPath from a segment
     *
     * @param segment the segment to create a path from
     * @return the created JSONPath
     */
    static JSONPathSingle of(JSONPathSegment segment) {
        String prefix;
        if (segment instanceof JSONPathSegment.MultiIndexSegment || segment instanceof JSONPathSegmentIndex) {
            prefix = "$";
        } else {
            prefix = "$.";
        }
        String path = prefix + segment.toString();

        if (segment instanceof JSONPathSegmentName) {
            return new JSONPathSingleName(path, (JSONPathSegmentName) segment);
        }
        return new JSONPathSingle(segment, path);
    }

    /**
     * Creates a JSONPath from a path string
     *
     * @param path the JSONPath expression
     * @return the created JSONPath
     */
    public static JSONPath of(String path) {
        if ("#-1".equals(path)) {
            return PreviousPath.INSTANCE;
        }

        return new JSONPathParser(path)
                .parse();
    }

    /**
     * Creates a typed JSONPath from a path string and type
     *
     * @param path the JSONPath expression
     * @param type the type of the result
     * @return the created JSONPath
     */
    public static JSONPath of(String path, Type type) {
        JSONPath jsonPath = of(path);
        return JSONPathTyped.of(jsonPath, type);
    }

    /**
     * Creates a typed JSONPath from a path string, type and features
     *
     * @param path the JSONPath expression
     * @param type the type of the result
     * @param features the features to apply
     * @return the created JSONPath
     */
    public static JSONPath of(String path, Type type, Feature... features) {
        JSONPath jsonPath = of(path, features);
        return JSONPathTyped.of(jsonPath, type);
    }

    /**
     * Creates a multi-path JSONPath
     *
     * @param paths the JSONPath expressions
     * @param types the types of the results
     * @return the created JSONPath
     * @since 2.0.20
     */
    public static JSONPath of(String[] paths, Type[] types) {
        return of(paths, types, null, null, (ZoneId) null);
    }

    /**
     * Creates a multi-path JSONPath
     *
     * @param paths the JSONPath expressions
     * @param types the types of the results
     * @param features the reader features to apply
     * @return the created JSONPath
     * @since 2.0.20
     */
    public static JSONPath of(String[] paths, Type[] types, JSONReader.Feature... features) {
        return of(paths, types, null, null, null, features);
    }

    /**
     * Creates a multi-path JSONPath
     *
     * @param paths the JSONPath expressions
     * @param types the types of the results
     * @param formats the formats to apply
     * @param pathFeatures the path features
     * @param zoneId the zone ID
     * @param features the reader features to apply
     * @return the created JSONPath
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
        boolean sameMultiLength = true;
        JSONPathMulti firstMulti = null;
        Map<JSONPath, List<Integer>> pathMap = new LinkedHashMap<>();
        for (int i = 0; i < jsonPaths.length; i++) {
            JSONPath path = jsonPaths[i];
            pathMap.computeIfAbsent(path, k -> new ArrayList<>()).add(i);

            if (i == 0) {
                if (path instanceof JSONPathMulti) {
                    firstMulti = (JSONPathMulti) path;
                } else {
                    sameMultiLength = false;
                }
            } else {
                if (sameMultiLength) {
                    if (path instanceof JSONPathMulti) {
                        if (((JSONPathMulti) path).segments.size() != firstMulti.segments.size()) {
                            sameMultiLength = false;
                        }
                    }
                }
            }

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
                        if (multi.segments.get(0) instanceof JSONPathSegment.AllSegment
                                || multi.segments.get(1) instanceof JSONPathSegment.AllSegment
                                || !(three instanceof JSONPathSegmentName)) {
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
        boolean duplicate = pathMap.size() != jsonPaths.length;

        long featuresValue = JSONReader.Feature.of(features);

        if (allSingleName && !duplicate) {
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

        if ((allTwoName && !duplicate) || allTwoIndexPositive) {
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
                } else {
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
        } else if (allThreeName && !duplicate) {
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

        if (sameMultiLength && paths.length > 1) {
            boolean samePrefix = true;
            boolean sameType = true;
            int lastIndex = firstMulti.segments.size() - 1;
            JSONPathSegment lastSegment = firstMulti.segments.get(lastIndex);

            for (int i = 0; i < lastIndex; i++) {
                JSONPathSegment segment = firstMulti.segments.get(i);
                for (int j = 1; j < paths.length; j++) {
                    JSONPath jsonPath = jsonPaths[j];

                    JSONPathSegment segment1;
                    if (jsonPath instanceof JSONPathMulti) {
                        JSONPathMulti path = (JSONPathMulti) jsonPath;
                        segment1 = path.segments.get(i);
                    } else if (jsonPath instanceof JSONPathSingleName) {
                        segment1 = ((JSONPathSingleName) jsonPath).segment;
                    } else if (jsonPath instanceof JSONPathSingleIndex) {
                        segment1 = ((JSONPathSingleIndex) jsonPath).segment;
                    } else {
                        segment1 = null;
                    }

                    if (!segment.equals(segment1)) {
                        samePrefix = false;
                        break;
                    }
                }
                if (!samePrefix) {
                    break;
                }
            }

            if (samePrefix) {
                for (int i = 1; i < paths.length; i++) {
                    JSONPathMulti path = (JSONPathMulti) jsonPaths[i];
                    if (!lastSegment.getClass().equals(path.segments.get(lastIndex).getClass())) {
                        sameType = false;
                        break;
                    }
                }
                if (sameType) {
                    List<JSONPathSegment> prefixSegments = firstMulti.segments.subList(0, lastIndex - 1);
                    String prefixPath = null;
                    int dotIndex = firstMulti.path.lastIndexOf('.');
                    if (dotIndex != -1) {
                        prefixPath = firstMulti.path.substring(0, dotIndex - 1);
                    }
                    if (prefixPath != null) {
                        JSONPathMulti prefix = new JSONPathMulti(prefixPath, prefixSegments);
                        if (lastSegment instanceof JSONPathSegmentIndex) {
                            JSONPath[] indexPaths = new JSONPath[paths.length];
                            for (int i = 0; i < jsonPaths.length; i++) {
                                JSONPathMulti path = (JSONPathMulti) jsonPaths[i];
                                JSONPathSegmentIndex lastSegmentIndex = (JSONPathSegmentIndex) path.segments.get(lastIndex);
                                indexPaths[i] = new JSONPathSingleIndex(lastSegmentIndex.toString(), lastSegmentIndex);
                            }
                            return new JSONPathTypedMultiIndexes(
                                    jsonPaths,
                                    prefix,
                                    indexPaths,
                                    types,
                                    formats,
                                    pathFeatures,
                                    zoneId,
                                    featuresValue
                            );
                        }
                    }
                }
            }
        }

        return new JSONPathTypedMulti(jsonPaths, types, formats, pathFeatures, zoneId, featuresValue);
    }

    /**
     * Creates a JSONPath from a path string with specified features
     *
     * @param path the JSONPath expression
     * @param features the features to apply
     * @return the created JSONPath
     */
    public static JSONPath of(String path, Feature... features) {
        if ("#-1".equals(path)) {
            return PreviousPath.INSTANCE;
        }

        return new JSONPathParser(path)
                .parse(features);
    }

    /**
     * Parses an operator from the JSONReader
     *
     * @param jsonReader the JSONReader to parse from
     * @return the parsed operator
     */
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
                    jsonReader.nextWithoutComment();
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
                } else if ("is".equalsIgnoreCase(fieldName)) {
                    operator = JSONPathFilter.Operator.EQ;
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
                if ("starts".equalsIgnoreCase(fieldName)) {
                    jsonReader.readFieldNameHashCodeUnquote();
                    fieldName = jsonReader.getFieldName();
                    if (!"with".equalsIgnoreCase(fieldName)) {
                        throw new JSONException("not support operator : " + fieldName);
                    }
                } else if (!"startsWith".equalsIgnoreCase(fieldName)) {
                    throw new JSONException("not support operator : " + fieldName);
                }

                operator = JSONPathFilter.Operator.STARTS_WITH;
                break;
            }
            case 'e':
            case 'E':
                jsonReader.readFieldNameHashCodeUnquote();
                String fieldName = jsonReader.getFieldName();
                if ("ends".equalsIgnoreCase(fieldName)) {
                    jsonReader.readFieldNameHashCodeUnquote();
                    fieldName = jsonReader.getFieldName();
                    if (!"with".equalsIgnoreCase(fieldName)) {
                        throw new JSONException("not support operator : " + fieldName);
                    }
                } else if (!"endsWith".equalsIgnoreCase(fieldName)) {
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

    /**
     * Represents a previous path reference
     */
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
        public JSONPath getParent() {
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

    /**
     * Represents the root path ($)
     */
    static final class RootPath
            extends JSONPath {
        static final RootPath INSTANCE = new RootPath();

        private RootPath() {
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

        @Override
        public JSONPath getParent() {
            return null;
        }
    }

    /**
     * Represents the execution context for JSONPath operations
     */
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

    /**
     * Represents a sequence of values
     */
    static class Sequence {
        final List values;

        public Sequence(List values) {
            this.values = values;
        }
    }

    /**
     * Features that can be applied to JSONPath operations
     */
    public enum Feature {
        /**
         * Always return results as a list
         */
        AlwaysReturnList(1),

        /**
         * Return null on error instead of throwing exceptions
         */
        NullOnError(1 << 1),

        /**
         * Keep null values in results
         */
        KeepNullValue(1 << 2);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }
}
