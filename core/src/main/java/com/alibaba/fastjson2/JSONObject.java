package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderImplEnum;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.*;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.BeanUtils.getAnnotations;
import static com.alibaba.fastjson2.util.JDKUtils.ANDROID;
import static com.alibaba.fastjson2.util.JDKUtils.GRAAL;
import static com.alibaba.fastjson2.util.TypeUtils.toBigDecimal;

public class JSONObject
        extends LinkedHashMap<String, Object>
        implements InvocationHandler {
    private static final long serialVersionUID = 1L;

    static ObjectReader<JSONArray> arrayReader;
    static final long NONE_DIRECT_FEATURES = ReferenceDetection.mask
            | PrettyFormat.mask
            | NotWriteEmptyArray.mask
            | NotWriteDefaultValue.mask;

    /**
     * Creates an empty, mutable JSON object in insertion order.
     * <details><summary>中文</summary>创建按插入顺序排列且可修改的空 JSON 对象。</details>
     *

     */
    public JSONObject() {
        super();
    }

    /**
     * <p>Capacity and load-factor arguments have the same meaning as in {@link LinkedHashMap}.</p>
     * <details><summary>中文</summary>容量和负载因子参数的含义与 LinkedHashMap 相同。</details>
     *
     * @param initialCapacity the initial hash-table capacity
     * @throws IllegalArgumentException If the initial capacity is negative
     */
    public JSONObject(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * <p>Capacity and load-factor arguments have the same meaning as in {@link LinkedHashMap}.</p>
     * <details><summary>中文</summary>容量和负载因子参数的含义与 LinkedHashMap 相同。</details>
     *
     * @param initialCapacity the initial hash-table capacity
     * @param loadFactor the load factor
     * @throws IllegalArgumentException If the initial capacity is negative or the load factor is nonpositive or NaN
     * @since 2.0.2
     */
    public JSONObject(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * <p>Capacity and load-factor arguments have the same meaning as in {@link LinkedHashMap}.</p>
     * <details><summary>中文</summary>容量和负载因子参数的含义与 LinkedHashMap 相同。</details>
     *
     * @param initialCapacity the initial hash-table capacity
     * @param loadFactor the load factor
     * @param accessOrder the ordering mode - true for access-order, false for insertion-order
     * @throws IllegalArgumentException If the initial capacity is negative or the load factor is nonpositive or NaN
     * @since 2.0.2
     */
    public JSONObject(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    /**
     * <p>Copies the mappings in iteration order. Keys and values are shared; nested values are not cloned.</p>
     * <details><summary>中文</summary>按迭代顺序复制映射，键和值仍共享，不克隆嵌套值。</details>
     *
     * @param map the map whose mappings are to be placed in this map
     * @throws NullPointerException If the specified map is null
     */
    @SuppressWarnings("unchecked")
    public JSONObject(Map map) {
        super(map);
    }

    /**
     * Returns the Object of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     */
    public Object get(String key) {
        return super.get(key);
    }

    /**
     * Returns the Object of the associated keys in this {@link JSONObject}.
     *
     * <p>For Number, Character, Boolean and UUID keys, a nonnull value under the string form of the key
     * takes precedence. If that lookup returns null, the original key is tried.</p>
     * <details><summary>中文</summary>对于 Number、Character、Boolean 和 UUID 键，优先返回字符串键对应的非 null 值，否则再查找原键。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return the associated value, or null if absent or mapped to null
     * @since 2.0.2
     */
    @Override
    public Object get(Object key) {
        if (key instanceof Number
                || key instanceof Character
                || key instanceof Boolean
                || key instanceof UUID
        ) {
            Object value = super.get(key.toString());
            if (value != null) {
                return value;
            }
        }

        return super.get(key);
    }

    /**
     * Evaluates a JSONPath against this object.
     * <details><summary>中文</summary>以当前对象为根节点计算 JSONPath。</details>
     *
     * @param jsonPath the JSONPath expression
     * @return the selected value, or the result defined by the expression
     * @throws JSONException if the expression is invalid
     */
    public Object getByPath(String jsonPath) {
        JSONPath path = JSONPath.of(jsonPath);
        if (path instanceof JSONPathSingleName) {
            String name = ((JSONPathSingleName) path).name;
            return get(name);
        }
        return path.eval(this);
    }

    /**
     * Returns true if this map contains a mapping for the specified key
     *
     * @param key the key whose presence in this map is to be tested
     */
    public boolean containsKey(String key) {
        return super.containsKey(key);
    }

    /**
     * Returns true if this map contains a mapping for the specified key
     *
     * <p>Number, Character, Boolean and UUID keys match either the original key or its string form.
     * A mapping with a null value still counts as present.</p>
     * <details><summary>中文</summary>Number、Character、Boolean 和 UUID 键匹配原键或其字符串形式；值为 null 的映射也视为存在。</details>
     *
     * @param key the key whose presence in this map is to be tested
     */
    @Override
    public boolean containsKey(Object key) {
        if (key instanceof Number
                || key instanceof Character
                || key instanceof Boolean
                || key instanceof UUID
        ) {
            return super.containsKey(key) || super.containsKey(key.toString());
        }

        return super.containsKey(key);
    }

    /**
     * <p>A present null mapping returns null, not the default.</p>
     * <details><summary>中文</summary>已存在且值为 null 的映射返回 null，不使用默认值。</details>
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the value returned when no matching key exists
     */
    public Object getOrDefault(String key, Object defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    /**
     * <p>A present null mapping returns null, not the default. Number, Character, Boolean and UUID keys
     * are looked up only by their string form in this overload.</p>
     * <details><summary>中文</summary>已存在且值为 null 的映射返回 null，不使用默认值。此重载对 Number、Character、Boolean 和 UUID 键只查找其字符串形式。</details>
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the value returned when no matching key exists
     * @since 2.0.2
     */
    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        if (key instanceof Number
                || key instanceof Character
                || key instanceof Boolean
                || key instanceof UUID
        ) {
            return super.getOrDefault(
                    key.toString(), defaultValue
            );
        }

        return super.getOrDefault(
                key, defaultValue
        );
    }

    /**
     * Iterates over the JSONArray elements associated with the given key.
     *
     * @param key the key whose associated JSONArray is to be iterated
     * @param action the action to be performed for each JSONObject element
     * @since 2.0.52
     * @deprecated Typo in the method name. Use {@link #forEachArrayObject(String, Consumer) forEachArrayObject} instead
     */
    @Deprecated
    public void forEchArrayObject(String key, Consumer<JSONObject> action) {
        forEachArrayObject(key, action);
    }

    /**
     * Iterates over the JSONArray elements associated with the given key.
     *
     * <p>Uses {@link #getJSONArray(String)} and {@link JSONArray#getJSONObject(int)} for conversion,
     * so stored containers may be replaced. No action is performed if the array conversion returns null;
     * a null element is passed to the action as null.</p>
     * <details><summary>中文</summary>通过 getJSONArray 和 getJSONObject 转换，可能替换存储的容器；数组转换结果为 null 时不执行操作，null 元素会作为 null 传给回调。</details>
     *
     * @param key the key whose associated JSONArray is to be iterated
     * @param action the action to be performed for each JSONObject element
     */
    public void forEachArrayObject(String key, Consumer<JSONObject> action) {
        JSONArray array = getJSONArray(key);
        if (array == null) {
            return;
        }

        for (int i = 0; i < array.size(); i++) {
            action.accept(
                    array.getJSONObject(i));
        }
    }

    /**
     * Returns the {@link JSONArray} of the associated keys in this {@link JSONObject}.
     *
     * <p>Collections and Java arrays are copied into a JSONArray and replace the mapping. Existing
     * JSONArrays are returned directly; JSONObject values and nonempty strings not starting with {@code [}
     * become singleton arrays without changing the mapping. Other strings are parsed as JSON arrays.
     * Missing/null values, empty strings, case-insensitive {@code "null"} and unsupported values return null.</p>
     * <details><summary>中文</summary>集合和 Java 数组转换后替换映射；已有 JSONArray 直接返回，JSONObject 和非 [ 开头的非空字符串包装为单元素数组且不修改映射。其他字符串按数组解析；缺失、空值及不支持类型返回 null。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return {@link JSONArray} or null
     */
    @SuppressWarnings("unchecked")
    public JSONArray getJSONArray(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }

        if (value instanceof JSONObject) {
            return JSONArray.of(value);
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            if (str.charAt(0) != '[') {
                return JSONArray.of(str);
            }

            JSONReader reader = JSONReader.of(str);
            if (arrayReader == null) {
                arrayReader = reader.getObjectReader(JSONArray.class);
            }
            return arrayReader.readObject(reader, null, null, 0);
        }

        if (value instanceof Collection) {
            JSONArray array = new JSONArray((Collection<?>) value);
            put(key, array);
            return array;
        }

        if (value instanceof Object[]) {
            JSONArray array = JSONArray.of((Object[]) value);
            put(key, array);
            return array;
        }

        Class<?> valueClass = value.getClass();
        if (valueClass.isArray()) {
            int length = Array.getLength(value);
            JSONArray jsonArray = new JSONArray(length);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(value, i);
                jsonArray.add(item);
            }
            put(key, jsonArray);
            return jsonArray;
        }

        return null;
    }

    /**
     * Returns a list of objects of the specified type from the associated JSONArray in this {@link JSONObject}.
     *
     * @param <T> the type of elements in the list
     * @param key the key whose associated value is to be returned
     * @param itemClass the class of the items in the list
     * @param features features to be enabled in parsing
     * @return a list of objects or null
     */
    public <T> List<T> getList(String key, Class<T> itemClass, JSONReader.Feature... features) {
        JSONArray jsonArray = getJSONArray(key);
        if (jsonArray == null) {
            return null;
        }
        return jsonArray.toList(itemClass, features);
    }

    /**
     * Returns the {@link JSONObject} of the associated keys in this {@link JSONObject}.
     *
     * <p>Maps and supported beans are converted and replace the mapping. JSON object strings are parsed
     * without changing the mapping. Existing JSONObject values are returned directly. Missing/null values,
     * empty strings, case-insensitive {@code "null"} and unsupported values return null.</p>
     * <details><summary>中文</summary>Map 和支持的 Bean 转换后替换映射；字符串解析不修改映射。已有 JSONObject 直接返回；缺失、空值及不支持的类型返回 null。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return {@link JSONObject} or null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public JSONObject getJSONObject(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            JSONReader reader = JSONReader.of(str);
            return JSONFactory.OBJECT_READER.readObject(reader, null, null, 0);
        }

        if (value instanceof Map) {
            JSONObject object = new JSONObject((Map) value);
            put(key, object);
            return object;
        }

        Class valueClass = value.getClass();
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(valueClass);
        if (objectWriter instanceof ObjectWriterAdapter) {
            ObjectWriterAdapter writerAdapter = (ObjectWriterAdapter) objectWriter;
            JSONObject jsonObject = writerAdapter.toJSONObject(value);
            put(key, jsonObject);
            return jsonObject;
        }

        return null;
    }
    /**
     * Returns the {@link String} of the associated keys in this {@link JSONObject}.
     *
     * <p>Strings are returned unchanged. Scalar values use their text representation, dates use the default
     * time zone, and other values are serialized as JSON.</p>
     * <details><summary>中文</summary>字符串保持不变；标量转换为文本，日期使用默认时区，其他值序列化为 JSON。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return the converted string, or null for a missing or null mapping
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Returns the {@link String} of the associated keys in this {@link JSONObject}.
     *
     * <p>Strings are returned unchanged. Scalar values use their text representation, dates use the default
     * time zone, and other values are serialized as JSON.</p>
     * <details><summary>中文</summary>字符串保持不变；标量转换为文本，日期使用默认时区，其他值序列化为 JSON。</details>
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the value returned for a missing or null mapping
     * @return the converted string, or defaultValue for a missing or null mapping
     */
    public String getString(String key, String defaultValue) {
        Object value = super.get(key);

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (value instanceof Date) {
            long timeMillis = ((Date) value).getTime();
            return DateUtils.toString(timeMillis, false, DateUtils.DEFAULT_ZONE_ID);
        }

        if (value instanceof Boolean
                || value instanceof Character
                || value instanceof Number
                || value instanceof UUID
                || value instanceof Enum
                || value instanceof TemporalAccessor) {
            return value.toString();
        }

        return JSON.toJSONString(value);
    }

    /**
     * Returns the {@link Double} of the associated keys in this {@link JSONObject}.
     *
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Double} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable double
     * @throws JSONException Unsupported type conversion to {@link Double}
     */
    public Double getDouble(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof String) {
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Double.parseDouble(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to double");
    }

    /**
     * Returns a double value of the associated keys in this {@link JSONObject}.
     *
     * <p>Missing/null values, empty strings and case-insensitive {@code "null"} return zero.
     * Numeric conversions may narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>缺失、null、空字符串及忽略大小写的 null 字符串返回零；数值可能按目标基本类型缩窄或截断。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return double
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable double
     * @throws JSONException Unsupported type conversion to double value
     */
    public double getDoubleValue(String key) {
        Double value = getDouble(key);
        return value == null ? 0D : value;
    }

    /**
     * Returns the {@link Float} of the associated keys in this {@link JSONObject}.
     *
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Float} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable float
     * @throws JSONException Unsupported type conversion to {@link Float}
     */
    public Float getFloat(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Float) {
            return (Float) value;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Float.parseFloat(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to float");
    }

    /**
     * Returns a float value of the associated keys in this {@link JSONObject}.
     *
     * <p>Missing/null values, empty strings and case-insensitive {@code "null"} return zero.
     * Numeric conversions may narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>缺失、null、空字符串及忽略大小写的 null 字符串返回零；数值可能按目标基本类型缩窄或截断。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return float
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable float
     * @throws JSONException Unsupported type conversion to float value
     */
    public float getFloatValue(String key) {
        Float value = getFloat(key);
        return value == null ? 0F : value;
    }

    /**
     * Returns the {@link Long} of the associated keys in this {@link JSONObject}.
     *
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Long} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable long
     * @throws JSONException Unsupported type conversion to {@link Long}
     */
    public Long getLong(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return ((Long) value);
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            if (str.indexOf('.') != -1) {
                return (long) Double.parseDouble(str);
            }

            return Long.parseLong(str);
        }

        if (value instanceof Boolean) {
            return (boolean) value ? Long.valueOf(1) : Long.valueOf(0);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Long");
    }

    /**
     * Returns a long value of the associated keys in this {@link JSONObject}.
     *
     * <p>Missing/null values, empty strings and case-insensitive {@code "null"} return zero.
     * Numeric conversions may narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>缺失、null、空字符串及忽略大小写的 null 字符串返回零；数值可能按目标基本类型缩窄或截断。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return long
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable long
     * @throws JSONException Unsupported type conversion to long value
     */
    public long getLongValue(String key) {
        return getLongValue(key, 0);
    }

    /**
     * Returns a long value of the associated keys in this {@link JSONObject}.
     *
     * <p>The default is used for missing/null values, empty strings and case-insensitive {@code "null"}.
     * Invalid numeric text still throws; the default does not suppress conversion errors.</p>
     * <details><summary>中文</summary>缺失、null、空字符串和忽略大小写的 null 字符串使用默认值；非法数字文本仍抛出异常，默认值不屏蔽转换错误。</details>
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return long
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable long
     * @throws JSONException Unsupported type conversion to long value
     */
    public long getLongValue(String key, long defaultValue) {
        Object value = super.get(key);

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return defaultValue;
            }

            if (str.indexOf('.') != -1) {
                return (long) Double.parseDouble(str);
            }

            return Long.parseLong(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to long value");
    }

    /**
     * Returns the {@link Integer} of the associated keys in this {@link JSONObject}.
     *
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Integer} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable int
     * @throws JSONException Unsupported type conversion to {@link Integer}
     */
    public Integer getInteger(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return ((Integer) value);
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            if (str.indexOf('.') != -1) {
                return (int) Double.parseDouble(str);
            }

            return Integer.parseInt(str);
        }

        if (value instanceof Boolean) {
            return (boolean) value ? Integer.valueOf(1) : Integer.valueOf(0);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Integer");
    }

    /**
     * Returns an int value of the associated keys in this {@link JSONObject}.
     *
     * <p>Missing/null values, empty strings and case-insensitive {@code "null"} return zero.
     * Numeric conversions may narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>缺失、null、空字符串及忽略大小写的 null 字符串返回零；数值可能按目标基本类型缩窄或截断。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return int
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable int
     * @throws JSONException Unsupported type conversion to int value
     */
    public int getIntValue(String key) {
        return getIntValue(key, 0);
    }

    /**
     * Returns an int value of the associated keys in this {@link JSONObject}.
     *
     * <p>The default is used for missing/null values, empty strings and case-insensitive {@code "null"}.
     * Invalid numeric text still throws; the default does not suppress conversion errors.</p>
     * <details><summary>中文</summary>缺失、null、空字符串和忽略大小写的 null 字符串使用默认值；非法数字文本仍抛出异常，默认值不屏蔽转换错误。</details>
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return int
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable int
     * @throws JSONException Unsupported type conversion to int value
     */
    public int getIntValue(String key, int defaultValue) {
        Object value = super.get(key);

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return defaultValue;
            }

            if (str.indexOf('.') != -1) {
                return (int) Double.parseDouble(str);
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to int value");
    }

    /**
     * Returns the {@link Short} of the associated keys in this {@link JSONObject}.
     *
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Short} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable short
     * @throws JSONException Unsupported type conversion to {@link Short}
     */
    public Short getShort(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Short) {
            return (Short) value;
        }

        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        if (value instanceof String) {
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Short.parseShort(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to short");
    }

    /**
     * Returns a short value of the associated keys in this {@link JSONObject}.
     *
     * <p>Missing/null values, empty strings and case-insensitive {@code "null"} return zero.
     * Numeric conversions may narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>缺失、null、空字符串及忽略大小写的 null 字符串返回零；数值可能按目标基本类型缩窄或截断。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return short
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable short
     * @throws JSONException Unsupported type conversion to short value
     */
    public short getShortValue(String key) {
        Short value = getShort(key);
        return value == null ? 0 : value;
    }

    /**
     * Returns the {@link Byte} of the associated keys in this {@link JSONObject}.
     *
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Byte} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable byte
     * @throws JSONException Unsupported type conversion to {@link Byte}
     */
    public Byte getByte(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (value instanceof String) {
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Byte.parseByte(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to byte");
    }

    /**
     * Returns a byte value of the associated keys in this {@link JSONObject}.
     *
     * <p>Missing/null values, empty strings and case-insensitive {@code "null"} return zero.
     * Numeric conversions may narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>缺失、null、空字符串及忽略大小写的 null 字符串返回零；数值可能按目标基本类型缩窄或截断。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return byte
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable byte
     * @throws JSONException Unsupported type conversion to byte value
     */
    public byte getByteValue(String key) {
        Byte value = getByte(key);
        return value == null ? 0 : value;
    }

    /**
     * Returns a stored byte array directly, or decodes a String with the basic Base64 decoder.
     * <details><summary>中文</summary>已有字节数组直接返回；字符串使用基本 Base64 解码器解码。</details>
     *
     * @param key the key to look up
     * @return the shared byte array, decoded bytes, or null for a missing or null mapping
     * @throws IllegalArgumentException if a String is not valid Base64
     * @throws JSONException if the value has an unsupported type
     */
    public byte[] getBytes(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        if (value instanceof String) {
            return Base64.getDecoder().decode((String) value);
        }
        throw new JSONException("can not cast to byte[], value : " + value);
    }

    /**
     * Returns the {@link Boolean} of the associated keys in this {@link JSONObject}.
     *
     * <p>Numbers are true only when {@link Number#intValue()} equals 1. Strings are true only for
     * case-insensitive {@code "true"} or {@code "1"}; empty strings and {@code "null"} return null.</p>
     * <details><summary>中文</summary>数值仅在 intValue() 为 1 时为 true；字符串仅 true（忽略大小写）或 1 为 true，空字符串和 null 字符串返回 null。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Boolean} or null
     * @throws JSONException Unsupported type conversion to {@link Boolean}
     */
    public Boolean getBoolean(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return "true".equalsIgnoreCase(str) || "1".equals(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to boolean");
    }

    /**
     * Returns a boolean value of the associated key in this object.
     *
     * <p>Uses {@link #getBoolean(String)} and returns false when that conversion returns null.</p>
     * <details><summary>中文</summary>使用 getBoolean 转换，其结果为 null 时返回 false。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return boolean
     * @throws JSONException Unsupported type conversion to boolean value
     */
    public boolean getBooleanValue(String key) {
        Boolean value = getBoolean(key);
        return value != null && value;
    }

    /**
     * Returns a boolean value of the associated key in this object.
     *
     * <p>The default is used whenever {@link #getBoolean(String)} returns null, including missing values
     * and empty or case-insensitive {@code "null"} strings. Other unrecognized strings return false.</p>
     * <details><summary>中文</summary>getBoolean 返回 null 时使用默认值，包括缺失、空字符串和 null 字符串；其他无法识别的字符串返回 false。</details>
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return boolean
     * @throws JSONException Unsupported type conversion to boolean value
     */
    public boolean getBooleanValue(String key, boolean defaultValue) {
        Boolean value = getBoolean(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Returns the {@link BigInteger} of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link BigInteger} or null
     * @throws JSONException Unsupported type conversion to {@link BigInteger}
     * @throws NumberFormatException If the value of get is {@link String} and it is not a valid representation of {@link BigInteger}
     */
    public BigInteger getBigInteger(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            if (value instanceof BigInteger) {
                return (BigInteger) value;
            }

            if (value instanceof BigDecimal) {
                return ((BigDecimal) value).toBigInteger();
            }

            long longValue = ((Number) value).longValue();
            return BigInteger.valueOf(longValue);
        }

        if (value instanceof String) {
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return new BigInteger(str);
        }

        if (value instanceof Boolean) {
            return (boolean) value ? BigInteger.ONE : BigInteger.ZERO;
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to BigInteger");
    }

    /**
     * Returns the {@link BigDecimal} of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link BigDecimal} or null
     * @throws JSONException Unsupported type conversion to {@link BigDecimal}
     * @throws NumberFormatException If the value of get is {@link String} and it is not a valid representation of {@link BigDecimal}
     */
    public BigDecimal getBigDecimal(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            }

            if (value instanceof BigInteger) {
                return new BigDecimal((BigInteger) value);
            }

            if (value instanceof Float) {
                float floatValue = (Float) value;
                return toBigDecimal(floatValue);
            }

            if (value instanceof Double) {
                double doubleValue = (Double) value;
                return toBigDecimal(doubleValue);
            }

            long longValue = ((Number) value).longValue();
            return BigDecimal.valueOf(longValue);
        }

        if (value instanceof String) {
            return toBigDecimal(((String) value).trim());
        }

        if (value instanceof Boolean) {
            return (boolean) value ? BigDecimal.ONE : BigDecimal.ZERO;
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to BigDecimal");
    }

    /**
     * Returns the {@link Date} of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Date} or null
     */
    public Date getDate(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Date) {
            return (Date) value;
        }

        if (value instanceof String) {
            return DateUtils.parseDate((String) value);
        }

        if (value instanceof Number) {
            long millis = ((Number) value).longValue();
            return new Date(millis);
        }

        return TypeUtils.toDate(value);
    }

    /**
     * @since 2.0.27
     */
    public Date getDate(String key, Date defaultValue) {
        Date date = getDate(key);
        if (date == null) {
            date = defaultValue;
        }
        return date;
    }

    /**
     * Returns the associated value as an {@link Instant}. Numeric values are epoch
     * milliseconds; numeric zero and null values return null.
     * <details><summary>中文</summary>将值转换为 Instant；数值表示纪元毫秒，数值零和 null 返回 null。</details>
     *
     * @param key the key whose associated value is to be returned
     * @return the converted instant, or null
     */
    public Instant getInstant(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Instant) {
            return (Instant) value;
        }

        if (value instanceof Number) {
            long millis = ((Number) value).longValue();
            if (millis == 0) {
                return null;
            }
            return Instant.ofEpochMilli(millis);
        }

        return TypeUtils.toInstant(value);
    }

    /**
     * Returns the value converted to {@link LocalDate}. An existing instance is returned directly.
     * Missing or null mappings return null.
     * <details><summary>中文</summary>将值转换为 LocalDate，已有实例直接返回。缺失或 null 映射返回 null。</details>
     *
     * @param key the key to look up
     * @return the converted value, or null
     * @since 2.0.57
     */
    public LocalDate getLocalDate(String key) {
        return getLocalDate(key, null);
    }

    /**
     * Returns the value converted to {@link LocalDate}. An existing instance is returned directly.
     * The default applies only to missing or null mappings; conversion failures are not replaced by the default.
     * <details><summary>中文</summary>将值转换为 LocalDate，已有实例直接返回。仅缺失或 null 映射使用默认值，转换失败不会被默认值替代。</details>
     *
     * @param key the key to look up
     * @param defaultValue the value for a missing or null mapping
     * @return the converted value, or defaultValue for a missing or null mapping
     * @since 2.0.57
     */
    public LocalDate getLocalDate(String key, LocalDate defaultValue) {
        Object value = super.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return TypeUtils.cast(value, LocalDate.class);
    }

    /**
     * Returns the value converted to {@link LocalTime}. An existing instance is returned directly.
     * Missing or null mappings return null.
     * <details><summary>中文</summary>将值转换为 LocalTime，已有实例直接返回。缺失或 null 映射返回 null。</details>
     *
     * @param key the key to look up
     * @return the converted value, or null
     * @since 2.0.57
     */
    public LocalTime getLocalTime(String key) {
        return getLocalTime(key, null);
    }

    /**
     * Returns the value converted to {@link LocalTime}. An existing instance is returned directly.
     * The default applies only to missing or null mappings; conversion failures are not replaced by the default.
     * <details><summary>中文</summary>将值转换为 LocalTime，已有实例直接返回。仅缺失或 null 映射使用默认值，转换失败不会被默认值替代。</details>
     *
     * @param key the key to look up
     * @param defaultValue the value for a missing or null mapping
     * @return the converted value, or defaultValue for a missing or null mapping
     * @since 2.0.57
     */
    public LocalTime getLocalTime(String key, LocalTime defaultValue) {
        Object value = super.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        }
        return TypeUtils.cast(value, LocalTime.class);
    }

    /**
     * Returns the value converted to {@link OffsetTime}. An existing instance is returned directly.
     * Missing or null mappings return null.
     * <details><summary>中文</summary>将值转换为 OffsetTime，已有实例直接返回。缺失或 null 映射返回 null。</details>
     *
     * @param key the key to look up
     * @return the converted value, or null
     * @since 2.0.57
     */
    public OffsetTime getOffsetTime(String key) {
        return getOffsetTime(key, null);
    }

    /**
     * Returns the value converted to {@link OffsetTime}. An existing instance is returned directly.
     * The default applies only to missing or null mappings; conversion failures are not replaced by the default.
     * <details><summary>中文</summary>将值转换为 OffsetTime，已有实例直接返回。仅缺失或 null 映射使用默认值，转换失败不会被默认值替代。</details>
     *
     * @param key the key to look up
     * @param defaultValue the value for a missing or null mapping
     * @return the converted value, or defaultValue for a missing or null mapping
     * @since 2.0.57
     */
    public OffsetTime getOffsetTime(String key, OffsetTime defaultValue) {
        Object value = super.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof OffsetTime) {
            return (OffsetTime) value;
        }
        return TypeUtils.cast(value, OffsetTime.class);
    }

    /**
     * Returns the value converted to {@link LocalDateTime}. An existing instance is returned directly.
     * Missing or null mappings return null.
     * <details><summary>中文</summary>将值转换为 LocalDateTime，已有实例直接返回。缺失或 null 映射返回 null。</details>
     *
     * @param key the key to look up
     * @return the converted value, or null
     * @since 2.0.57
     */
    public LocalDateTime getLocalDateTime(String key) {
        return getLocalDateTime(key, null);
    }

    /**
     * Returns the value converted to {@link LocalDateTime}. An existing instance is returned directly.
     * The default applies only to missing or null mappings; conversion failures are not replaced by the default.
     * <details><summary>中文</summary>将值转换为 LocalDateTime，已有实例直接返回。仅缺失或 null 映射使用默认值，转换失败不会被默认值替代。</details>
     *
     * @param key the key to look up
     * @param defaultValue the value for a missing or null mapping
     * @return the converted value, or defaultValue for a missing or null mapping
     * @since 2.0.57
     */
    public LocalDateTime getLocalDateTime(String key, LocalDateTime defaultValue) {
        Object value = super.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return TypeUtils.cast(value, LocalDateTime.class);
    }

    /**
     * Returns the value converted to {@link OffsetDateTime}. An existing instance is returned directly.
     * Missing or null mappings return null.
     * <details><summary>中文</summary>将值转换为 OffsetDateTime，已有实例直接返回。缺失或 null 映射返回 null。</details>
     *
     * @param key the key to look up
     * @return the converted value, or null
     * @since 2.0.57
     */
    public OffsetDateTime getOffsetDateTime(String key) {
        return getOffsetDateTime(key, null);
    }

    /**
     * Returns the value converted to {@link OffsetDateTime}. An existing instance is returned directly.
     * The default applies only to missing or null mappings; conversion failures are not replaced by the default.
     * <details><summary>中文</summary>将值转换为 OffsetDateTime，已有实例直接返回。仅缺失或 null 映射使用默认值，转换失败不会被默认值替代。</details>
     *
     * @param key the key to look up
     * @param defaultValue the value for a missing or null mapping
     * @return the converted value, or defaultValue for a missing or null mapping
     * @since 2.0.57
     */
    public OffsetDateTime getOffsetDateTime(String key, OffsetDateTime defaultValue) {
        Object value = super.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof OffsetDateTime) {
            return (OffsetDateTime) value;
        }
        return TypeUtils.cast(value, OffsetDateTime.class);
    }

    /**
     * Returns the value converted to {@link ZonedDateTime}. An existing instance is returned directly.
     * Missing or null mappings return null.
     * <details><summary>中文</summary>将值转换为 ZonedDateTime，已有实例直接返回。缺失或 null 映射返回 null。</details>
     *
     * @param key the key to look up
     * @return the converted value, or null
     * @since 2.0.57
     */
    public ZonedDateTime getZonedDateTime(String key) {
        return getZonedDateTime(key, null);
    }

    /**
     * Returns the value converted to {@link ZonedDateTime}. An existing instance is returned directly.
     * The default applies only to missing or null mappings; conversion failures are not replaced by the default.
     * <details><summary>中文</summary>将值转换为 ZonedDateTime，已有实例直接返回。仅缺失或 null 映射使用默认值，转换失败不会被默认值替代。</details>
     *
     * @param key the key to look up
     * @param defaultValue the value for a missing or null mapping
     * @return the converted value, or defaultValue for a missing or null mapping
     * @since 2.0.57
     */
    public ZonedDateTime getZonedDateTime(String key, ZonedDateTime defaultValue) {
        Object value = super.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof ZonedDateTime) {
            return (ZonedDateTime) value;
        }
        return TypeUtils.cast(value, ZonedDateTime.class);
    }

    /**
     * Serialize to JSON {@link String}
     *
     * @return JSON {@link String}
     */
    @Override
    public String toString() {
        try (JSONWriter writer = JSONWriter.of()) {
            writer.setRootObject(this);
            writer.write(this);
            return writer.toString();
        }
    }

    /**
     * Serialize to JSON {@link String}
     *
     * @param features features to be enabled in serialization
     * @return JSON {@link String}
     */
    public String toString(JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.of(features)) {
            writer.setRootObject(this);
            writer.write(this);
            return writer.toString();
        }
    }

    /**
     * Serialize to JSON {@link String}
     *
     * @param features features to be enabled in serialization
     * @return JSON {@link String}
     */
    public String toJSONString(JSONWriter.Feature... features) {
        return toString(features);
    }

    /**
     * Serialize Java Object to JSON {@link String} with specified {@link JSONWriter.Feature}s enabled
     *
     * <p>A null input is serialized as the JSON text {@code "null"}.</p>
     * <details><summary>中文</summary>null 输入序列化为 JSON 文本 null。</details>
     *
     * @param object Java Object to be serialized into JSON {@link String}
     * @param features features to be enabled in serialization
     * @return the serialized JSON text
     * @since 2.0.6
     */
    public static String toJSONString(Object object, JSONWriter.Feature... features) {
        return JSON.toJSONString(object, features);
    }

    /**
     * Serialize to JSONB bytes
     *
     * @param features features to be enabled in serialization
     * @return JSONB bytes
     */
    public byte[] toJSONBBytes(JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.ofJSONB(features)) {
            writer.setRootObject(this);
            writer.write(this);
            return writer.getBytes();
        }
    }

    /**
     * Applies the supplied function directly to this object.
     * <details><summary>中文</summary>直接对当前对象调用传入函数。</details>
     *
     * @param <T> the result type
     * @param function the conversion function
     * @return the function result
     * @throws NullPointerException if function is null
     * @since 2.0.4
     */
    public <T> T to(Function<JSONObject, T> function) {
        return function.apply(this);
    }

    /**
     * Convert this {@link JSONObject} to the specified Object
     *
     * <pre>{@code
     * JSONObject obj = ...
     * Map<String, User> users = obj.to(new TypeReference<HashMap<String, User>>(){}.getType());
     * }</pre>
     *
     * <p>Uses the registered object reader for conversion. A String target produces JSON text.</p>
     * <details><summary>中文</summary>通过注册的读取器转换；String 目标类型得到 JSON 文本。</details>
     *
     * @param <T> the result type
     * @param type specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     * @return the converted value
     * @since 2.0.4
     */
    @SuppressWarnings("unchecked")
    public <T> T to(Type type, JSONReader.Feature... features) {
        long featuresValue = JSONFactory.defaultReaderFeatures;
        boolean fieldBased = false;
        for (JSONReader.Feature feature : features) {
            if (feature == JSONReader.Feature.FieldBased) {
                fieldBased = true;
            }
            featuresValue |= feature.mask;
        }

        if (type == String.class) {
            return (T) toString();
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<T> objectReader = provider.getObjectReader(type, fieldBased);
        return objectReader.createInstance(this, featuresValue);
    }

    /**
     * Convert this {@link JSONObject} to the specified Object
     *
     * <pre>{@code
     * JSONObject obj = ...
     * Map<String, User> users = obj.to(new TypeReference<HashMap<String, User>>(){});
     * }</pre>
     *
     * <p>Uses the registered object reader for conversion. A String target produces JSON text.</p>
     * <details><summary>中文</summary>通过注册的读取器转换；String 目标类型得到 JSON 文本。</details>
     *
     * @param <T> the result type
     * @param typeReference specify the {@link TypeReference} to be converted
     * @param features features to be enabled in parsing
     * @return the converted value
     * @since 2.0.7
     */
    public <T> T to(TypeReference<T> typeReference, JSONReader.Feature... features) {
        return to(typeReference.getType(), features);
    }

    /**
     * Convert this {@link JSONObject} to the specified Object
     *
     * <pre>{@code
     * JSONObject obj = ...
     * User user = obj.to(User.class);
     * }</pre>
     *
     * <p>Uses the registered object reader for conversion. A String target produces JSON text.
     * Void targets return null.</p>
     * <details><summary>中文</summary>通过注册的读取器转换；String 目标类型得到 JSON 文本。Void 目标类型返回 null。</details>
     *
     * @param <T> the result type
     * @param clazz specify the {@code Class<T>} to be converted
     * @param features features to be enabled in parsing
     * @return the converted value
     * @since 2.0.4
     */
    @SuppressWarnings("unchecked")
    public <T> T to(Class<T> clazz, JSONReader.Feature... features) {
        long featuresValue = JSONFactory.defaultReaderFeatures | JSONReader.Feature.of(features);
        boolean fieldBased = JSONReader.Feature.FieldBased.isEnabled(featuresValue);

        if (clazz == String.class) {
            return (T) toString();
        }

        if (clazz == JSON.class) {
            return (T) this;
        }

        if (clazz == Void.class || clazz == void.class) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<T> objectReader = provider.getObjectReader(clazz, fieldBased);
        return objectReader.createInstance(this, featuresValue);
    }

    /**
     * Populates an existing target object from this object through its registered reader.
     * The target is mutated; values are converted using the requested reader features.
     * <details><summary>中文</summary>通过注册的读取器将当前对象的值写入已有目标对象；目标会被修改，并按读取特性进行值转换。</details>
     *
     * @param object the existing target object, not null
     * @param features reader features used for conversion
     * @throws NullPointerException if object is null
     */
    public void copyTo(Object object, JSONReader.Feature... features) {
        long featuresValue = JSONFactory.defaultReaderFeatures | JSONReader.Feature.of(features);
        boolean fieldBased = JSONReader.Feature.FieldBased.isEnabled(featuresValue);
        Class clazz = object.getClass();
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(clazz, fieldBased);
        objectReader.accept(object, this, featuresValue);
    }

    /**
     * Convert this {@link JSONObject} to the specified Object
     *
     * @param clazz specify the {@code Class<T>} to be converted
     * @param features features to be enabled in parsing
     */
    public <T> T toJavaObject(Class<T> clazz, JSONReader.Feature... features) {
        return to(clazz, features);
    }

    /**
     * Convert this {@link JSONObject} to the specified Object
     *
     * @param type specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     * @deprecated since 2.0.4, please use {@link #to(Type, JSONReader.Feature...)}
     */
    public <T> T toJavaObject(Type type, JSONReader.Feature... features) {
        return to(type, features);
    }

    /**
     * Convert this {@link JSONObject} to the specified Object
     *
     * @param typeReference specify the {@link TypeReference} to be converted
     * @param features features to be enabled in parsing
     * @deprecated since 2.0.4, please use {@link #to(Type, JSONReader.Feature...)}
     */
    public <T> T toJavaObject(TypeReference<T> typeReference, JSONReader.Feature... features) {
        return to(typeReference, features);
    }

    /**
     * Returns the result of the {@link Type} converter conversion of the associated value in this {@link JSONObject}.
     * <p>
     * {@code User user = jsonObject.getObject("user", User.class);}
     *
     * <p>Converts the associated value without replacing the mapping. Missing or null mappings return null.</p>
     * <details><summary>中文</summary>转换对应值但不替换映射；缺失或 null 映射返回 null。</details>
     *
     * @param <T> the result type
     * @param key the key whose associated value is to be returned
     * @param type specify the {@link Class} to be converted
     * @param features reader features used for conversion
     * @return {@code <T>} or null
     * @throws JSONException If no suitable conversion method is found
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T getObject(String key, Class<T> type, JSONReader.Feature... features) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (type == Object.class && features.length == 0) {
            return (T) value;
        }

        boolean fieldBased = false;
        for (JSONReader.Feature feature : features) {
            if (feature == JSONReader.Feature.FieldBased) {
                fieldBased = true;
                break;
            }
        }

        Class<?> valueClass = value.getClass();
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(valueClass, type);
        if (typeConvert != null) {
            return (T) typeConvert.apply(value);
        }

        if (value instanceof Map) {
            ObjectReader<T> objectReader = provider.getObjectReader(type, fieldBased);
            return objectReader.createInstance((Map) value, features);
        }

        if (value instanceof Collection) {
            ObjectReader<T> objectReader = provider.getObjectReader(type, fieldBased);
            return objectReader.createInstance((Collection) value, features);
        }

        Class clazz = TypeUtils.getMapping(type);
        if (clazz.isInstance(value)) {
            return (T) value;
        }

        ObjectReader objectReader = null;

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }

            if (clazz.isEnum()) {
                objectReader = provider.getObjectReader(clazz, fieldBased);
                if (objectReader instanceof ObjectReaderImplEnum) {
                    long hashCode64 = Fnv.hashCode64(str);
                    ObjectReaderImplEnum enumReader = (ObjectReaderImplEnum) objectReader;
                    return (T) enumReader.getEnumByHashCode(hashCode64);
                }
            }
        }

        String json = JSON.toJSONString(value);
        JSONReader jsonReader = JSONReader.of(json);
        jsonReader.context.config(features);

        if (objectReader == null) {
            objectReader = provider.getObjectReader(clazz, fieldBased);
        }

        T object = (T) objectReader.readObject(jsonReader, null, null, 0L);
        if (!jsonReader.isEnd()) {
            throw new JSONException("not support input " + json);
        }
        return object;
    }

    /**
     * Returns the result of the {@link Type} converter conversion of the associated value in this {@link JSONObject}.
     * <p>
     * {@code User user = jsonObject.getObject("user", User.class);}
     *
     * <p>Converts the associated value without replacing the mapping. Missing or null mappings return null.</p>
     * <details><summary>中文</summary>转换对应值但不替换映射；缺失或 null 映射返回 null。</details>
     *
     * @param <T> the result type
     * @param key the key whose associated value is to be returned
     * @param type specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     * @return {@code <T>} or {@code null}
     * @throws JSONException If no suitable conversion method is found
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T getObject(String key, Type type, JSONReader.Feature... features) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (type == Object.class && features.length == 0) {
            return (T) value;
        }

        boolean fieldBased = false;
        for (JSONReader.Feature feature : features) {
            if (feature == JSONReader.Feature.FieldBased) {
                fieldBased = true;
                break;
            }
        }

        Class<?> valueClass = value.getClass();
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(valueClass, type);
        if (typeConvert != null) {
            return (T) typeConvert.apply(value);
        }

        if (value instanceof Map) {
            ObjectReader<T> objectReader = provider.getObjectReader(type, fieldBased);
            return objectReader.createInstance((Map) value, features);
        }

        if (value instanceof Collection) {
            ObjectReader<T> objectReader = provider.getObjectReader(type, fieldBased);
            return objectReader.createInstance((Collection) value, features);
        }

        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (clazz.isInstance(value)) {
                return (T) value;
            }
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
        }

        String json = JSON.toJSONString(value);
        JSONReader jsonReader = JSONReader.of(json);
        jsonReader.context.config(features);

        ObjectReader objectReader = provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readObject(jsonReader, null, null, 0);
    }

    /**
     * Returns the result of the {@link Type} converter conversion of the associated value in this {@link JSONObject}.
     * <p>
     * {@code User user = jsonObject.getObject("user", User.class);}
     *
     * @param key the key whose associated value is to be returned
     * @param typeReference specify the {@link TypeReference} to be converted
     * @param features features to be enabled in parsing
     * @return {@code <T>} or {@code null}
     * @throws JSONException If no suitable conversion method is found
     * @since 2.0.3
     */
    public <T> T getObject(String key, TypeReference<T> typeReference, JSONReader.Feature... features) {
        return getObject(key, typeReference.type, features);
    }

    /**
     * Converts the mapping using {@link #getJSONObject(String)}, then invokes the creator for a nonnull result.
     * The preliminary conversion may replace the mapping.
     * <details><summary>中文</summary>先通过 getJSONObject 转换映射，结果非 null 时调用创建函数；转换过程可能替换映射。</details>
     *
     * @param <T> the result type
     * @param key the key to look up
     * @param creator the function applied to the converted JSONObject
     * @return the function result, or null if conversion returns null
     * @since 2.0.4
     */
    public <T> T getObject(String key, Function<JSONObject, T> creator) {
        JSONObject object = getJSONObject(key);

        if (object == null) {
            return null;
        }

        return creator.apply(object);
    }

    /**
     * Handles method invocations on a proxy instance.
     *
     * @param proxy proxy object, currently useless
     * @param method methods that need reflection
     * @param args parameters of invoke
     * @return the result of the method invocation
     * @throws Throwable if an error occurs during method invocation
     * @throws UnsupportedOperationException If reflection for this method is not supported
     * @throws ArrayIndexOutOfBoundsException If the length of args does not match the length of the method parameter
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        int parameterCount = method.getParameterCount();

        Class<?> returnType = method.getReturnType();
        if (parameterCount == 1) {
            if ("equals".equals(methodName)) {
                return this.equals(args[0]);
            }

            Class proxyInterface = null;
            Class<?>[] interfaces = proxy.getClass().getInterfaces();
            if (interfaces.length == 1) {
                proxyInterface = interfaces[0];
            }

            if (returnType != void.class && returnType != proxyInterface) {
                throw new JSONException("This method '" + methodName + "' is not a setter");
            }

            String name = getJSONFieldName(method);

            if (name == null) {
                name = methodName;

                if (!name.startsWith("set")) {
                    throw new JSONException("This method '" + methodName + "' is not a setter");
                }

                name = name.substring(3);
                if (name.length() == 0) {
                    throw new JSONException("This method '" + methodName + "' is an illegal setter");
                }
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            }

            put(name, args[0]);

            if (returnType != void.class) {
                return proxy;
            }

            return null;
        }

        if (parameterCount == 0) {
            if (returnType == void.class) {
                throw new JSONException("This method '" + methodName + "' is not a getter");
            }

            String name = getJSONFieldName(method);

            Object value;
            if (name == null) {
                name = methodName;
                boolean with = false;
                int prefix;
                if ((name.startsWith("get") || (with = name.startsWith("with")))
                        && name.length() > (prefix = with ? 4 : 3)
                ) {
                    char[] chars = new char[name.length() - prefix];
                    name.getChars(prefix, name.length(), chars, 0);
                    if (chars[0] >= 'A' && chars[0] <= 'Z') {
                        chars[0] = (char) (chars[0] + 32);
                    }
                    String fieldName = new String(chars);
                    if (fieldName.isEmpty()) {
                        throw new JSONException("This method '" + methodName + "' is an illegal getter");
                    }

                    value = get(fieldName);
                    if (value == null) {
                        return null;
                    }
                } else if (name.startsWith("is")) {
                    if ("isEmpty".equals(name)) {
                        value = get("empty");
                        if (value == null) {
                            return this.isEmpty();
                        }
                    } else {
                        name = name.substring(2);
                        if (name.isEmpty()) {
                            throw new JSONException("This method '" + methodName + "' is an illegal getter");
                        }
                        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

                        value = get(name);
                        if (value == null) {
                            return false;
                        }
                    }
                } else if ("hashCode".equals(name)) {
                    return this.hashCode();
                } else if ("toString".equals(name)) {
                    return this.toString();
                } else if (name.startsWith("entrySet")) {
                    return this.entrySet();
                } else if ("size".equals(name)) {
                    return this.size();
                } else {
                    Class<?> declaringClass = method.getDeclaringClass();
                    if (declaringClass.isInterface()
                            && !Modifier.isAbstract(method.getModifiers())
                            && !ANDROID
                            && !GRAAL
                    ) {
                        // interface default method
                        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
                        MethodHandle methodHandle = lookup.findSpecial(
                                declaringClass,
                                method.getName(),
                                MethodType.methodType(returnType),
                                declaringClass
                        );
                        return methodHandle.invoke(proxy);
                    }
                    throw new JSONException("This method '" + methodName + "' is not a getter");
                }
            } else {
                value = get(name);
                if (value == null) {
                    return null;
                }
            }

            if (!returnType.isInstance(value)) {
                Function typeConvert = JSONFactory
                        .getDefaultObjectReaderProvider()
                        .getTypeConvert(
                                value.getClass(), method.getGenericReturnType()
                        );

                if (typeConvert != null) {
                    value = typeConvert.apply(value);
                }
            }

            return value;
        }

        throw new UnsupportedOperationException(method.toGenericString());
    }

    /**
     * Gets the JSON field name from the method's annotations.
     *
     * @param method the method to get the JSON field name from
     * @return the JSON field name, or null if not found
     * @since 2.0.4
     */
    private String getJSONFieldName(Method method) {
        String name = null;
        Annotation[] annotations = getAnnotations(method);
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            JSONField jsonField = BeanUtils.findAnnotation(annotation, JSONField.class);
            if (Objects.nonNull(jsonField)) {
                name = jsonField.name();
                if (name.isEmpty()) {
                    name = null;
                }
            } else if ("com.alibaba.fastjson.annotation.JSONField".equals(annotationType.getName())) {
                NameConsumer nameConsumer = new NameConsumer(annotation);
                BeanUtils.annotationMethods(annotationType, nameConsumer);
                if (nameConsumer.name != null) {
                    name = nameConsumer.name;
                }
            }
        }
        return name;
    }

    /**
     * Creates a new JSONArray, stores it under the name, and returns that new child.
     * Any previous mapping is replaced.
     * <details><summary>中文</summary>创建新的 JSONArray，存入指定键并返回新建的子容器；替换已有映射。</details>
     *
     * @param name the key for the new child
     * @return the newly created child, which is also stored in this object
     */
    public JSONArray putArray(String name) {
        JSONArray array = new JSONArray();
        put(name, array);
        return array;
    }

    /**
     * Creates a new JSONObject, stores it under the name, and returns that new child.
     * Any previous mapping is replaced.
     * <details><summary>中文</summary>创建新的 JSONObject，存入指定键并返回新建的子容器；替换已有映射。</details>
     *
     * @param name the key for the new child
     * @return the newly created child, which is also stored in this object
     */
    public JSONObject putObject(String name) {
        JSONObject object = new JSONObject();
        put(name, object);
        return object;
    }

    /**
     * Consumer for processing method names.
     *
     * @since 2.0.3
     */
    static class NameConsumer
            implements Consumer<Method> {
        final Annotation annotation;
        String name;

        NameConsumer(Annotation annotation) {
            this.annotation = annotation;
        }

        @Override
        public void accept(Method method) {
            String methodName = method.getName();
            if ("name".equals(methodName)) {
                try {
                    String result = (String) method.invoke(annotation);
                    if (!result.isEmpty()) {
                        name = result;
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    // nothing
                }
            }
        }
    }

    /**
     * Chained addition of elements
     *
     * <pre>
     * JSONObject object = new JSONObject().fluentPut("a", 1).fluentPut("b", 2).fluentPut("c", 3);
     * </pre>
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    public JSONObject fluentPut(String key, Object value) {
        put(key, value);
        return this;
    }

    /**
     * Checks if this JSONObject is valid according to the specified JSON schema.
     *
     * @param schema the JSON schema to validate against
     * @return true if this JSONObject is valid according to the schema, false otherwise
     * @since 2.0.4
     */
    public boolean isValid(JSONSchema schema) {
        return schema.isValid(this);
    }

    /**
     * Applies a name filter to an iterable collection.
     *
     * @param iterable the iterable collection to apply the filter to
     * @param nameFilter the name filter to apply
     * @since 2.0.3
     */
    static void nameFilter(Iterable<?> iterable, NameFilter nameFilter) {
        for (Object item : iterable) {
            if (item instanceof JSONObject) {
                ((JSONObject) item).nameFilter(nameFilter);
            } else if (item instanceof Iterable) {
                nameFilter((Iterable<?>) item, nameFilter);
            }
        }
    }

    /**
     * Applies a name filter to a map.
     *
     * @param map the map to apply the filter to
     * @param nameFilter the name filter to apply
     * @since 2.0.3
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static void nameFilter(Map map, NameFilter nameFilter) {
        JSONObject changed = null;
        for (Iterator<?> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            Object entryKey = entry.getKey();
            Object entryValue = entry.getValue();

            if (entryValue instanceof JSONObject) {
                ((JSONObject) entryValue).nameFilter(nameFilter);
            } else if (entryValue instanceof Iterable) {
                nameFilter((Iterable<?>) entryValue, nameFilter);
            }

            if (entryKey instanceof String) {
                String key = (String) entryKey;
                String processName = nameFilter.process(map, key, entryValue);
                if (processName != null && !processName.equals(key)) {
                    if (changed == null) {
                        changed = new JSONObject();
                    }
                    changed.put(processName, entryValue);
                    it.remove();
                }
            }
        }
        if (changed != null) {
            map.putAll(changed);
        }
    }

    /**
     * Applies a value filter to an iterable collection.
     *
     * @param iterable the iterable collection to apply the filter to
     * @param valueFilter the value filter to apply
     * @since 2.0.3
     */
    @SuppressWarnings("rawtypes")
    static void valueFilter(Iterable<?> iterable, ValueFilter valueFilter) {
        for (Object item : iterable) {
            if (item instanceof Map) {
                valueFilter((Map) item, valueFilter);
            } else if (item instanceof Iterable) {
                valueFilter((Iterable<?>) item, valueFilter);
            }
        }
    }

    /**
     * Applies a value filter to a map.
     *
     * @param map the map to apply the filter to
     * @param valueFilter the value filter to apply
     * @since 2.0.3
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static void valueFilter(Map map, ValueFilter valueFilter) {
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object entryKey = entry.getKey();
            Object entryValue = entry.getValue();

            if (entryValue instanceof Map) {
                valueFilter((Map) entryValue, valueFilter);
            } else if (entryValue instanceof Iterable) {
                valueFilter((Iterable<?>) entryValue, valueFilter);
            }

            if (entryKey instanceof String) {
                String key = (String) entryKey;
                Object applyValue = valueFilter.apply(map, key, entryValue);
                if (applyValue != entryValue) {
                    entry.setValue(applyValue);
                }
            }
        }
    }

    /**
     * Applies a value filter to this JSONObject.
     *
     * <p>Mutates values in this object and nested maps/iterables traversed by the filter.</p>
     * <details><summary>中文</summary>原地修改当前对象以及遍历到的嵌套 Map 和可迭代容器中的值。</details>
     *
     * @param valueFilter the value filter to apply
     * @since 2.0.3
     */
    public void valueFilter(ValueFilter valueFilter) {
        valueFilter(this, valueFilter);
    }

    /**
     * Applies a name filter to this JSONObject.
     *
     * <p>Renames keys in this object and nested maps/iterables in place. A replacement name that already
     * exists can overwrite its previous mapping.</p>
     * <details><summary>中文</summary>原地修改当前对象及遍历到的嵌套 Map 和可迭代容器中的键名；新键名已存在时可能覆盖原映射。</details>
     *
     * @param nameFilter the name filter to apply
     * @since 2.0.3
     */
    public void nameFilter(NameFilter nameFilter) {
        nameFilter(this, nameFilter);
    }

    /**
     * Returns a mutable, insertion-ordered shallow copy. Nested keys and values are shared with this object.
     * <details><summary>中文</summary>返回可修改且按插入顺序排列的浅复制，嵌套键和值与当前对象共享。</details>
     *
     * @return a new JSONObject containing the same mappings
     * @see #JSONObject(Map)
     */
    @Override
    public JSONObject clone() {
        return new JSONObject(this);
    }

    /**
     * Evaluates a JSONPath expression against this JSONObject.
     *
     * @param path the JSONPath expression to evaluate
     * @return the result of evaluating the JSONPath expression
     * @see JSONPath#paths(Object)
     */
    public Object eval(JSONPath path) {
        return path.eval(this);
    }

    /**
     * Returns the size of the value associated with the given key if it is a Map or Collection.
     * For other types, returns 0.
     *
     * @param key the key whose associated value's size is to be returned
     * @return the size of the value if it is a Map or Collection, otherwise 0
     * @since 2.0.24
     */
    public int getSize(String key) {
        Object value = get(key);
        if (value instanceof Map) {
            return ((Map<?, ?>) value).size();
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).size();
        }
        return 0;
    }

    /**
     * <pre>
     * JSONObject jsonObject = JSONObject.of();
     * </pre>
     */
    public static JSONObject of() {
        return new JSONObject();
    }

    /**
     * Pack a pair of key-values as {@link JSONObject}
     *
     * <pre>
     * JSONObject jsonObject = JSONObject.of("name", "fastjson2");
     * </pre>
     *
     * @param key the key of the element
     * @param value the value of the element
     */
    public static JSONObject of(String key, Object value) {
        JSONObject object = new JSONObject(1, 1F);
        object.put(key, value);
        return object;
    }

    /**
     * Pack two key-value pairs as {@link JSONObject}
     *
     * <pre>
     * JSONObject jsonObject = JSONObject.of("key1", "value1", "key2", "value2");
     * </pre>
     *
     * @param k1 first key
     * @param v1 first value
     * @param k2 second key
     * @param v2 second value
     * @since 2.0.2
     */
    public static JSONObject of(String k1, Object v1, String k2, Object v2) {
        JSONObject object = new JSONObject(2, 1F);
        object.put(k1, v1);
        object.put(k2, v2);
        return object;
    }

    /**
     * Pack three key-value pairs as {@link JSONObject}
     *
     * <pre>
     * JSONObject jsonObject = JSONObject.of("key1", "value1", "key2", "value2", "key3", "value3");
     * </pre>
     *
     * @param k1 first key
     * @param v1 first value
     * @param k2 second key
     * @param v2 second value
     * @param k3 third key
     * @param v3 third value
     * @since 2.0.2
     */
    public static JSONObject of(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        JSONObject object = new JSONObject(3);
        object.put(k1, v1);
        object.put(k2, v2);
        object.put(k3, v3);
        return object;
    }

    /**
     * Pack three key-value pairs as {@link JSONObject}
     *
     * <pre>
     * JSONObject jsonObject = JSONObject.of("key1", "value1", "key2", "value2", "key3", "value3", "key4", "value4");
     * </pre>
     *
     * @param k1 first key
     * @param v1 first value
     * @param k2 second key
     * @param v2 second value
     * @param k3 third key
     * @param v3 third value
     * @param k4 four key
     * @param v4 four value
     * @since 2.0.8
     */
    public static JSONObject of(
            String k1,
            Object v1,
            String k2,
            Object v2,
            String k3,
            Object v3,
            String k4,
            Object v4) {
        JSONObject object = new JSONObject(4, 1F);
        object.put(k1, v1);
        object.put(k2, v2);
        object.put(k3, v3);
        object.put(k4, v4);
        return object;
    }

    /**
     * Pack three key-value pairs as {@link JSONObject}
     *
     * <pre>
     * JSONObject jsonObject = JSONObject.of("key1", "value1", "key2", "value2", "key3", "value3", "key4", "value4", "key5", "value5");
     * </pre>
     *
     * @param k1 first key
     * @param v1 first value
     * @param k2 second key
     * @param v2 second value
     * @param k3 third key
     * @param v3 third value
     * @param k4 four key
     * @param v4 four value
     * @param k5 five key
     * @param v5 five value
     * @since 2.0.21
     */
    public static JSONObject of(
            String k1,
            Object v1,
            String k2,
            Object v2,
            String k3,
            Object v3,
            String k4,
            Object v4,
            String k5,
            Object v5

    ) {
        JSONObject object = new JSONObject(5);
        object.put(k1, v1);
        object.put(k2, v2);
        object.put(k3, v3);
        object.put(k4, v4);
        object.put(k5, v5);
        return object;
    }

    /**
     * Pack multiple key-value pairs as {@link JSONObject}
     *
     * <pre>
     * JSONObject jsonObject = JSONObject.of("key1", "value1", "key2", "value2", "key3", "value3", "key4", "value4", "key5", "value5", kvArray);
     * </pre>
     *
     * @param k1 first key
     * @param v1 first value
     * @param k2 second key
     * @param v2 second value
     * @param k3 third key
     * @param v3 third value
     * @param k4 four key
     * @param v4 four value
     * @param k5 five key
     * @param v5 five value
     * @param kvArray multiple key-value
     * @since 2.0.53
     */
    public static JSONObject of(
            String k1,
            Object v1,
            String k2,
            Object v2,
            String k3,
            Object v3,
            String k4,
            Object v4,
            String k5,
            Object v5,
            Object... kvArray

    ) {
        JSONObject object = new JSONObject(5);
        object.put(k1, v1);
        object.put(k2, v2);
        object.put(k3, v3);
        object.put(k4, v4);
        object.put(k5, v5);
        if (kvArray != null && kvArray.length > 0) {
            of(object, kvArray);
        }
        return object;
    }

    /**
     * Pack multiple key-value pairs as {@link JSONObject}
     *
     * <pre>
     * JSONObject jsonObject = JSONObject.of(Object... kvArray);
     * </pre>
     *
     * @param kvArray key-value
     * @since 2.0.53
     */
    private static JSONObject of(JSONObject jsonObject, Object... kvArray) {
        if (kvArray == null || kvArray.length == 0) {
            throw new JSONException("The kvArray cannot be empty");
        }
        final int kvArrayLength = kvArray.length;
        if ((kvArrayLength & 1) == 1) {
            throw new JSONException("The length of kvArray cannot be odd");
        }
        for (int i = 0; i < kvArrayLength; i++) {
            Object keyObj = kvArray[i++];
            if (!(keyObj instanceof String)) {
                throw new JSONException("The value corresponding to the even bit index of kvArray is key, which cannot be null and must be of type string");
            }
            String key = (String) keyObj;
            if (jsonObject.containsKey(key)) {
                throw new JSONException("The value corresponding to the even bit index of kvArray is key and cannot be duplicated");
            }
            jsonObject.put(key, kvArray[i]);
        }
        return jsonObject;
    }

    /**
     * Parses JSON text into the requested type. Null or empty input returns null.
     * <details><summary>中文</summary>将 JSON 文本解析为指定类型；null 或空输入返回 null。</details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param objectClass the target type
     * @return the parsed value, or null
     * @see JSON#parseObject(String, Class)
     */
    public static <T> T parseObject(String text, Class<T> objectClass) {
        return JSON.parseObject(text, objectClass);
    }

    /**
     * Parses JSON text into the requested type. Null or empty input returns null.
     * <details><summary>中文</summary>将 JSON 文本解析为指定类型；null 或空输入返回 null。</details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param objectClass the target type
     * @param features reader features enabled for parsing
     * @return the parsed value, or null
     * @see JSON#parseObject(String, Class, JSONReader.Feature...)
     */
    public static <T> T parseObject(String text, Class<T> objectClass, JSONReader.Feature... features) {
        return JSON.parseObject(text, objectClass, features);
    }

    /**
     * Parses JSON text into the requested type. Null or empty input returns null.
     * <details><summary>中文</summary>将 JSON 文本解析为指定类型；null 或空输入返回 null。</details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param objectType the target type
     * @param features reader features enabled for parsing
     * @return the parsed value, or null
     * @see JSON#parseObject(String, Type, JSONReader.Feature...)
     */
    public static <T> T parseObject(String text, Type objectType, JSONReader.Feature... features) {
        return JSON.parseObject(text, objectType, features);
    }

    /**
     * Parses JSON text into the requested type. Null or empty input returns null.
     * <details><summary>中文</summary>将 JSON 文本解析为指定类型；null 或空输入返回 null。</details>
     *
     * @param <T> the result type
     * @param text the JSON text
     * @param typeReference the target type, including generic arguments
     * @param features reader features enabled for parsing
     * @return the parsed value, or null
     * @see JSON#parseObject(String, TypeReference, JSONReader.Feature...)
     */
    public static <T> T parseObject(String text, TypeReference<T> typeReference, JSONReader.Feature... features) {
        return JSON.parseObject(text, typeReference, features);
    }

    /**
     * Parses JSON object text. Null or empty input and the JSON null literal return null.
     * <details><summary>中文</summary>解析 JSON 对象文本；null、空输入或 JSON null 字面量返回 null。</details>
     *
     * @param text the JSON object text
     * @return the parsed JSONObject, or null
     * @see JSON#parseObject(String)
     */
    public static JSONObject parseObject(String text) {
        return JSON.parseObject(text);
    }

    /**
     * Parses JSON object text with the requested reader features. This is an alias for
     * {@link JSON#parseObject(String, JSONReader.Feature...)}.
     * <details><summary>中文</summary>按指定读取特性解析 JSON 对象文本，是 JSON.parseObject 对应重载的别名。</details>
     *
     * @param text the JSON object text
     * @param features reader features enabled for parsing
     * @return the parsed JSONObject, or null for null, empty, or JSON null input
     * @since 2.0.13
     */
    public static JSONObject parse(String text, JSONReader.Feature... features) {
        return JSON.parseObject(text, features);
    }

    /**
     * Converts a Java value through {@link JSON#toJSON(Object, JSONWriter.Feature...)} and requires an object result.
     * An existing JSONObject may be returned unchanged; this method is not a deep-copy operation.
     * <details><summary>中文</summary>通过 JSON.toJSON 转换并要求结果为 JSONObject；已有 JSONObject 可能直接返回，并非深复制操作。</details>
     *
     * @param obj the value to convert, or null
     * @return the resulting JSONObject, or null for a null input
     * @throws ClassCastException if conversion produces a non-object value
     */
    public static JSONObject from(Object obj) {
        return (JSONObject) JSON.toJSON(obj);
    }

    /**
     * Converts a Java value through {@link JSON#toJSON(Object, JSONWriter.Feature...)} and requires an object result.
     * An existing JSONObject may be returned unchanged; this method is not a deep-copy operation.
     * <details><summary>中文</summary>通过 JSON.toJSON 转换并要求结果为 JSONObject；已有 JSONObject 可能直接返回，并非深复制操作。</details>
     *
     * @param obj the value to convert, or null
     * @param writeFeatures serialization features used during conversion
     * @return the resulting JSONObject, or null for a null input
     * @throws ClassCastException if conversion produces a non-object value
     */
    public static JSONObject from(Object obj, JSONWriter.Feature... writeFeatures) {
        return (JSONObject) JSON.toJSON(obj, writeFeatures);
    }

    /**
     * Tests whether the value stored under the exact key is a JSONArray or a Java array,
     * including a primitive array. Other Collection implementations do not match.
     * <details><summary>中文</summary>检查精确键对应的值是否为 JSONArray 或 Java 数组（含基本类型数组）；其他 Collection 不匹配。</details>
     *
     * @param key the exact map key; no string-key conversion is performed
     * @return true for a JSONArray or Java array, otherwise false
     */
    public boolean isArray(Object key) {
        Object object = super.get(key);
        return object instanceof JSONArray || object != null && object.getClass().isArray();
    }
}
