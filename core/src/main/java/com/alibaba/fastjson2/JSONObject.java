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
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.AnnotationUtils.getAnnotations;

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
     * default
     */
    public JSONObject() {
        super();
    }

    /**
     * @param initialCapacity the initial capacity = (number of elements to store / load factor) + 1
     * @throws IllegalArgumentException If the initial capacity is negative
     */
    public JSONObject(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @param initialCapacity the initial capacity = (number of elements to store / load factor) + 1
     * @param loadFactor the load factor
     * @throws IllegalArgumentException If the initial capacity is negative or the load factor is negative
     * @since 2.0.2
     */
    public JSONObject(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * @param initialCapacity the initial capacity = (number of elements to store / load factor) + 1
     * @param loadFactor the load factor
     * @param accessOrder the ordering mode - true for access-order, false for insertion-order
     * @throws IllegalArgumentException If the initial capacity is negative or the load factor is negative
     * @since 2.0.2
     */
    public JSONObject(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    /**
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
     * @param key the key whose associated value is to be returned
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
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     */
    public Object getOrDefault(String key, Object defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    /**
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
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
     * Returns the {@link JSONArray} of the associated keys in this {@link JSONObject}.
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

            JSONReader reader = JSONReader.of(str);
            if (arrayReader == null) {
                arrayReader = reader.getObjectReader(JSONArray.class);
            }
            return arrayReader.readObject(reader, null, null, 0);
        }

        if (value instanceof Collection) {
            return new JSONArray((Collection<?>) value);
        }

        if (value instanceof Object[]) {
            return JSONArray.of((Object[]) value);
        }

        Class<?> valueClass = value.getClass();
        if (valueClass.isArray()) {
            int length = Array.getLength(value);
            JSONArray jsonArray = new JSONArray(length);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(value, i);
                jsonArray.add(item);
            }
            return jsonArray;
        }

        return null;
    }

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
            return new JSONObject((Map) value);
        }

        Class valueClass = value.getClass();
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(valueClass);
        if (objectWriter instanceof ObjectWriterAdapter) {
            ObjectWriterAdapter writerAdapter = (ObjectWriterAdapter) objectWriter;
            return writerAdapter.toJSONObject(value);
        }

        return null;
    }

    /**
     * Returns the {@link String} of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link String} or null
     */
    public String getString(String key) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (value instanceof Date) {
            long timeMillis = ((Date) value).getTime();
            return DateUtils.toString(timeMillis, false, IOUtils.DEFAULT_ZONE_ID);
        }

        if (value instanceof Boolean
                || value instanceof Character
                || value instanceof Number
                || value instanceof UUID
                || value instanceof Enum) {
            return value.toString();
        }

        return JSON.toJSONString(value);
    }

    /**
     * Returns the {@link Double} of the associated keys in this {@link JSONObject}.
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
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Double.parseDouble(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Double");
    }

    /**
     * Returns a double value of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return double
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable double
     * @throws JSONException Unsupported type conversion to double value
     */
    public double getDoubleValue(String key) {
        Object value = super.get(key);

        if (value == null) {
            return 0D;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0D;
            }

            return Double.parseDouble(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to double value");
    }

    /**
     * Returns the {@link Float} of the associated keys in this {@link JSONObject}.
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
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Float.parseFloat(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Float");
    }

    /**
     * Returns a float value of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return float
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable float
     * @throws JSONException Unsupported type conversion to float value
     */
    public float getFloatValue(String key) {
        Object value = super.get(key);

        if (value == null) {
            return 0F;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0F;
            }

            return Float.parseFloat(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to float value");
    }

    /**
     * Returns the {@link Long} of the associated keys in this {@link JSONObject}.
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
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            if (str.indexOf('.') != -1) {
                return (long) Double.parseDouble(str);
            }

            return Long.parseLong(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Long");
    }

    /**
     * Returns a long value of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return long
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable long
     * @throws JSONException Unsupported type conversion to long value
     */
    public long getLongValue(String key) {
        Object value = super.get(key);

        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0;
            }

            if (str.indexOf('.') != -1) {
                return (long) Double.parseDouble(str);
            }

            return Long.parseLong(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to long value");
    }

    /**
     * Returns a long value of the associated keys in this {@link JSONObject}.
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
            String str = (String) value;

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
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            if (str.indexOf('.') != -1) {
                return (int) Double.parseDouble(str);
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Integer");
    }

    /**
     * Returns an int value of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return int
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable int
     * @throws JSONException Unsupported type conversion to int value
     */
    public int getIntValue(String key) {
        Object value = super.get(key);

        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0;
            }

            if (str.indexOf('.') != -1) {
                return (int) Double.parseDouble(str);
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to int value");
    }

    /**
     * Returns an int value of the associated keys in this {@link JSONObject}.
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
            String str = (String) value;

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
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Short.parseShort(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Short");
    }

    /**
     * Returns a short value of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return short
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable short
     * @throws JSONException Unsupported type conversion to short value
     */
    public short getShortValue(String key) {
        Object value = super.get(key);

        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0;
            }

            return Short.parseShort(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to short value");
    }

    /**
     * Returns the {@link Byte} of the associated keys in this {@link JSONObject}.
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
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Byte.parseByte(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Byte");
    }

    /**
     * Returns a byte value of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return byte
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable byte
     * @throws JSONException Unsupported type conversion to byte value
     */
    public byte getByteValue(String key) {
        Object value = super.get(key);

        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0;
            }

            return Byte.parseByte(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to byte value");
    }

    /**
     * Returns the {@link Boolean} of the associated keys in this {@link JSONObject}.
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

        throw new JSONException("Can not cast '" + value.getClass() + "' to Boolean");
    }

    /**
     * Returns a boolean value of the associated key in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return boolean
     * @throws JSONException Unsupported type conversion to boolean value
     */
    public boolean getBooleanValue(String key) {
        Object value = super.get(key);

        if (value == null) {
            return false;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }

        if (value instanceof String) {
            String str = (String) value;
            return "true".equalsIgnoreCase(str) || "1".equals(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to boolean value");
    }

    /**
     * Returns a boolean value of the associated key in this object.
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return boolean
     * @throws JSONException Unsupported type conversion to boolean value
     */
    public boolean getBooleanValue(String key, boolean defaultValue) {
        Object value = super.get(key);

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }

        if (value instanceof String) {
            String str = (String) value;
            return "true".equalsIgnoreCase(str) || "1".equals(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to boolean value");
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

        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }

        if (value instanceof Number) {
            if (value instanceof BigDecimal) {
                return ((BigDecimal) value).toBigInteger();
            }

            long longValue = ((Number) value).longValue();
            return BigInteger.valueOf(longValue);
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return new BigInteger(str);
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

            if (value instanceof Float
                    || value instanceof Double) {
                // Floating point number have no cached BigDecimal
                return new BigDecimal(value.toString());
            }

            long longValue = ((Number) value).longValue();
            return BigDecimal.valueOf(longValue);
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return new BigDecimal(str);
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
     * Returns the {@link BigInteger} of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link BigInteger} or null
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
     * Serialize to JSON {@link String}
     *
     * @return JSON {@link String}
     */
    @Override
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
     * Serialize Java Object to JSON {@link String} with specified {@link JSONReader.Feature}s enabled
     *
     * @param object Java Object to be serialized into JSON {@link String}
     * @param features features to be enabled in serialization
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
    @SuppressWarnings("unchecked")
    public byte[] toJSONBBytes(JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.ofJSONB(features)) {
            writer.setRootObject(this);
            writer.write(this);
            return writer.getBytes();
        }
    }

    /**
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
     * @param type specify the {@link Type} to be converted
     * @param features features to be enabled in parsing
     * @since 2.0.4
     */
    @SuppressWarnings("unchecked")
    public <T> T to(Type type, JSONReader.Feature... features) {
        long featuresValue = 0L;
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
     * @param typeReference specify the {@link TypeReference} to be converted
     * @param features features to be enabled in parsing
     * @since 2.0.7
     */
    public <T> T to(TypeReference<?> typeReference, JSONReader.Feature... features) {
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
     * @param clazz specify the {@code Class<T>} to be converted
     * @param features features to be enabled in parsing
     * @since 2.0.4
     */
    @SuppressWarnings("unchecked")
    public <T> T to(Class<T> clazz, JSONReader.Feature... features) {
        long featuresValue = 0L;
        boolean fieldBased = false;
        for (JSONReader.Feature feature : features) {
            if (feature == JSONReader.Feature.FieldBased) {
                fieldBased = true;
            }
            featuresValue |= feature.mask;
        }

        if (clazz == String.class) {
            return (T) toString();
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<T> objectReader = provider.getObjectReader(clazz, fieldBased);
        return objectReader.createInstance(this, featuresValue);
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
    public <T> T toJavaObject(TypeReference<?> typeReference, JSONReader.Feature... features) {
        return to(typeReference, features);
    }

    /**
     * Returns the result of the {@link Type} converter conversion of the associated value in this {@link JSONObject}.
     * <p>
     * {@code User user = jsonObject.getObject("user", User.class);}
     *
     * @param key the key whose associated value is to be returned
     * @param type specify the {@link Class} to be converted
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
            return objectReader.createInstance((Collection) value);
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
            return objectReader.createInstance((Collection) value);
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
    public <T> T getObject(String key, TypeReference<?> typeReference, JSONReader.Feature... features) {
        return getObject(key, typeReference.type, features);
    }

    /**
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
     * @param proxy proxy object, currently useless
     * @param method methods that need reflection
     * @param args parameters of invoke
     * @throws UnsupportedOperationException If reflection for this method is not supported
     * @throws ArrayIndexOutOfBoundsException If the length of args does not match the length of the method parameter
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        int parameterCount = method.getParameterCount();

        if (parameterCount == 1) {
            if ("equals".equals(methodName)) {
                return this.equals(args[0]);
            }

            if (method.getReturnType() != void.class) {
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
            return null;
        }

        if (parameterCount == 0) {
            if (method.getReturnType() == void.class) {
                throw new JSONException("This method '" + methodName + "' is not a getter");
            }

            String name = getJSONFieldName(method);

            Object value;
            if (name == null) {
                name = methodName;
                if (name.startsWith("get")) {
                    name = name.substring(3);
                    if (name.isEmpty()) {
                        throw new JSONException("This method '" + methodName + "' is an illegal getter");
                    }
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

                    value = get(name);
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
                    throw new JSONException("This method '" + methodName + "' is not a getter");
                }
            } else {
                value = get(name);
                if (value == null) {
                    return null;
                }
            }

            Function typeConvert = JSONFactory
                    .getDefaultObjectReaderProvider()
                    .getTypeConvert(
                            value.getClass(), method.getGenericReturnType()
                    );

            if (typeConvert != null) {
                return typeConvert.apply(value);
            }

            return value;
        }

        throw new UnsupportedOperationException(method.toGenericString());
    }

    /**
     * @since 2.0.4
     */
    private String getJSONFieldName(Method method) {
        String name = null;
        Annotation[] annotations = getAnnotations(method);
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            JSONField jsonField = AnnotationUtils.findAnnotation(annotation, JSONField.class);
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

    public JSONArray putArray(String name) {
        JSONArray array = new JSONArray();
        put(name, array);
        return array;
    }

    public JSONObject putObject(String name) {
        JSONObject object = new JSONObject();
        put(name, object);
        return object;
    }

    /**
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
     * @since 2.0.4
     */
    public boolean isValid(JSONSchema schema) {
        return schema.isValid(this);
    }

    /**
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
     * @since 2.0.3
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
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
     * @since 2.0.3
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
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
     * @since 2.0.3
     */
    public void valueFilter(ValueFilter valueFilter) {
        valueFilter(this, valueFilter);
    }

    /**
     * @since 2.0.3
     */
    public void nameFilter(NameFilter nameFilter) {
        nameFilter(this, nameFilter);
    }

    /**
     * @see JSONObject#JSONObject(Map)
     */
    @Override
    public JSONObject clone() {
        return new JSONObject(this);
    }

    /**
     * @see JSONPath#paths(Object)
     */
    public Object eval(JSONPath path) {
        return path.eval(this);
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
        JSONObject object = new JSONObject(1);
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
        JSONObject object = new JSONObject(2);
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
     * @param k4 foud key
     * @param v4 foud value
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
        JSONObject object = new JSONObject(4);
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
     * @param k4 foud key
     * @param v4 foud value
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
     * See {@link JSON#parseObject} for details
     */
    public static <T> T parseObject(String text, Class<T> objectClass) {
        return JSON.parseObject(text, objectClass);
    }

    /**
     * See {@link JSON#parseObject} for details
     */
    public static <T> T parseObject(String text, Class<T> objectClass, JSONReader.Feature... features) {
        return JSON.parseObject(text, objectClass, features);
    }

    /**
     * See {@link JSON#parseObject} for details
     */
    public static <T> T parseObject(String text, Type objectType, JSONReader.Feature... features) {
        return JSON.parseObject(text, objectType, features);
    }

    /**
     * See {@link JSON#parseObject} for details
     */
    public static <T> T parseObject(String text, TypeReference<?> typeReference, JSONReader.Feature... features) {
        return JSON.parseObject(text, typeReference, features);
    }

    /**
     * See {@link JSON#parseObject} for details
     */
    public static JSONObject parseObject(String text) {
        return JSON.parseObject(text);
    }

    /**
     * See {@link JSON#parse} for details
     *
     * @since 2.0.13
     */
    public static JSONObject parse(String text, JSONReader.Feature... features) {
        return JSON.parseObject(text, features);
    }
}
