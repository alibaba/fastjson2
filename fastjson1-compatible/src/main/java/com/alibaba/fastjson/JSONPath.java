package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * JSONPath is used to query and manipulate JSON data using path expressions.
 * <p>This is a fastjson1-compatible class that provides the same behavior as the original fastjson 1.x API.
 * JSONPath expressions allow you to navigate complex JSON structures and extract specific values.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * String json = "{\"store\":{\"book\":[{\"title\":\"Book1\",\"price\":10},{\"title\":\"Book2\",\"price\":20}]}}";
 *
 * // Compile and evaluate
 * JSONPath path = JSONPath.compile("$.store.book[0].title");
 * Object result = path.eval(JSON.parseObject(json)); // "Book1"
 *
 * // Static evaluation
 * Object price = JSONPath.eval(json, "$.store.book[1].price"); // 20
 *
 * // Set value
 * JSONPath.set(json, "$.store.book[0].price", 15);
 * }</pre>
 */
public class JSONPath {
    private final com.alibaba.fastjson2.JSONPath path;

    private JSONPath(com.alibaba.fastjson2.JSONPath path) {
        this.path = path;
    }

    /**
     * Compiles a JSONPath expression.
     * <p>This is a fastjson1-compatible method.
     *
     * @param path the JSONPath expression to compile
     * @return a compiled JSONPath instance
     * @throws JSONException if the path expression is null or invalid
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONPath jsonPath = JSONPath.compile("$.store.book[*].title");
     * }</pre>
     */
    public static JSONPath compile(String path) {
        if (path == null) {
            throw new JSONException("jsonpath can not be null");
        }

        return new JSONPath(com.alibaba.fastjson2.JSONPath.of(path));
    }

    /**
     * Evaluates this JSONPath expression against the given object.
     * <p>This is a fastjson1-compatible method.
     *
     * @param object the object to evaluate the path against
     * @return the result of the path evaluation
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONObject obj = JSON.parseObject("{\"name\":\"Alice\",\"age\":30}");
     * JSONPath path = JSONPath.compile("$.name");
     * String name = (String) path.eval(obj); // "Alice"
     * }</pre>
     */
    public Object eval(Object object) {
        return path.eval(object);
    }

    /**
     * Sets a value at the path location in the given object.
     * <p>This is a fastjson1-compatible method.
     *
     * @param object the object to modify
     * @param value the value to set
     * @return {@code true} if the operation succeeded
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONObject obj = JSON.parseObject("{\"name\":\"Alice\"}");
     * JSONPath path = JSONPath.compile("$.name");
     * path.set(obj, "Bob");
     * System.out.println(obj.getString("name")); // "Bob"
     * }</pre>
     */
    public boolean set(Object object, Object value) {
        path.set(object, value);
        return true;
    }

    /**
     * Returns the string representation of this JSONPath expression.
     * <p>This is a fastjson1-compatible method.
     *
     * @return the path expression as a string
     */
    public String getPath() {
        return path.toString();
    }

    /**
     * Reads a value from JSON string using a JSONPath expression and converts it to the specified type.
     * <p>This is a fastjson1-compatible method.
     *
     * @param <T> the type of the result
     * @param json the JSON string
     * @param path the JSONPath expression
     * @param clazz the type to convert the result to
     * @param parserConfig the parser configuration
     * @return the extracted and converted value
     */
    public static <T> T read(String json, String path, Type clazz, ParserConfig parserConfig) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        JSONReader.Context context = JSON.createReadContext(JSON.DEFAULT_PARSER_FEATURE);
        JSONReader jsonReader = JSONReader.of(json, context);
        Object r = jsonPath.extract(jsonReader);
        return TypeUtils.cast(r, clazz, parserConfig);
    }

    public static <T> T read(String json, String path, Type clazz) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        Object r = jsonPath.extract(JSONReader.of(json));
        return TypeUtils.cast(r, clazz, ParserConfig.global);
    }

    /**
     * Evaluates a JSONPath expression against a JSON string.
     * <p>This is a fastjson1-compatible static method for convenient one-time path evaluation.
     *
     * @param rootObject the JSON string to evaluate against
     * @param path the JSONPath expression
     * @return the result of the path evaluation
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * String json = "{\"name\":\"Alice\",\"age\":30}";
     * Object name = JSONPath.eval(json, "$.name"); // "Alice"
     * }</pre>
     */
    public static Object eval(String rootObject, String path) {
        return JSON.adaptResult(com.alibaba.fastjson2.JSONPath.eval(rootObject, path));
    }

    /**
     * Evaluates a JSONPath expression against a Java object.
     * <p>This is a fastjson1-compatible static method for convenient one-time path evaluation.
     *
     * @param rootObject the object to evaluate against
     * @param path the JSONPath expression
     * @return the result of the path evaluation
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONObject obj = JSON.parseObject("{\"user\":{\"name\":\"Alice\"}}");
     * Object name = JSONPath.eval(obj, "$.user.name"); // "Alice"
     * }</pre>
     */
    public static Object eval(Object rootObject, String path) {
        return JSON.adaptResult(com.alibaba.fastjson2.JSONPath.of(path).eval(rootObject));
    }

    /**
     * Sets a value at the specified JSONPath location in the given object.
     * <p>This is a fastjson1-compatible static method.
     *
     * @param rootObject the object to modify
     * @param path the JSONPath expression
     * @param value the value to set
     * @return {@code true} if the operation succeeded
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONObject obj = JSON.parseObject("{\"name\":\"Alice\"}");
     * JSONPath.set(obj, "$.name", "Bob");
     * System.out.println(obj.getString("name")); // "Bob"
     * }</pre>
     */
    public static boolean set(Object rootObject, String path, Object value) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        jsonPath.setReaderContext(JSON.createReadContext(JSON.DEFAULT_PARSER_FEATURE));
        jsonPath.set(rootObject, value);
        return true;
    }

    /**
     * Returns all paths in the given Java object as a map.
     * <p>This is a fastjson1-compatible method. Useful for discovering the structure of complex objects.
     *
     * @param javaObject the object to extract paths from
     * @return a map of path strings to their values
     */
    public static Map<String, Object> paths(Object javaObject) {
        return com.alibaba.fastjson2.JSONPath.paths(javaObject);
    }

    /**
     * Adds values to an array at the specified JSONPath location.
     * <p>This is a fastjson1-compatible method.
     *
     * @param rootObject the object containing the array
     * @param path the JSONPath expression pointing to an array
     * @param values the values to add to the array
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONObject obj = JSON.parseObject("{\"items\":[1,2,3]}");
     * JSONPath.arrayAdd(obj, "$.items", 4, 5);
     * // items is now [1,2,3,4,5]
     * }</pre>
     */
    public static void arrayAdd(Object rootObject, String path, Object... values) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        jsonPath.arrayAdd(rootObject, values);
    }

    /**
     * Extracts a value from a JSON string using a JSONPath expression.
     * <p>This is a fastjson1-compatible method. More efficient than parsing the entire JSON first.
     *
     * @param json the JSON string
     * @param path the JSONPath expression
     * @return the extracted value
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * String json = "{\"store\":{\"book\":[{\"title\":\"Book1\"}]}}";
     * Object title = JSONPath.extract(json, "$.store.book[0].title"); // "Book1"
     * }</pre>
     */
    public static Object extract(String json, String path) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        JSONReader.Context context = JSON.createReadContext(JSON.DEFAULT_PARSER_FEATURE);
        JSONReader jsonReader = JSONReader.of(json, context);
        Object result = jsonPath.extract(jsonReader);
        return JSON.adaptResult(result);
    }

    /**
     * Removes the value at the specified JSONPath location from the given object.
     * <p>This is a fastjson1-compatible method.
     *
     * @param root the object to modify
     * @param path the JSONPath expression
     * @return {@code true} if the value was removed, {@code false} otherwise
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONObject obj = JSON.parseObject("{\"name\":\"Alice\",\"age\":30}");
     * boolean removed = JSONPath.remove(obj, "$.age");
     * // obj is now {"name":"Alice"}
     * }</pre>
     */
    public static boolean remove(Object root, String path) {
        return com.alibaba.fastjson2.JSONPath
                .of(path)
                .remove(root);
    }

    /**
     * Checks if a value exists at the specified JSONPath location in the given object.
     * <p>This is a fastjson1-compatible method.
     *
     * @param rootObject the object to check
     * @param path the JSONPath expression
     * @return {@code true} if a value exists at the path, {@code false} otherwise
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONObject obj = JSON.parseObject("{\"name\":\"Alice\"}");
     * boolean hasName = JSONPath.contains(obj, "$.name"); // true
     * boolean hasAge = JSONPath.contains(obj, "$.age");   // false
     * }</pre>
     */
    public static boolean contains(Object rootObject, String path) {
        if (rootObject == null) {
            return false;
        }
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        return jsonPath.contains(rootObject);
    }

    /**
     * Reads a value from a JSON string using a JSONPath expression.
     * <p>This is a fastjson1-compatible method.
     *
     * @param json the JSON string
     * @param path the JSONPath expression
     * @return the extracted value
     */
    public static Object read(String json, String path) {
        JSONReader.Context context = JSON.createReadContext(JSON.DEFAULT_PARSER_FEATURE);
        JSONReader jsonReader = JSONReader.of(json, context);
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        return jsonPath.extract(jsonReader);
    }
}
