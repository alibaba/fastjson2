package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.*;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.JDKUtils.ANDROID;
import static com.alibaba.fastjson2.util.JDKUtils.GRAAL;
import static com.alibaba.fastjson2.util.TypeUtils.toBigDecimal;

@SuppressWarnings("deprecation")
public class JSONObject
        extends LinkedHashMap<String, Object>
        implements InvocationHandler {
    private static final long serialVersionUID = 1L;

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
    public JSONObject(Map<String, ?> map) {
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
     * @param key the key whose associated value is to be returned
     * @return {@link JSONArray} or null
     */
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

            return JSON.parseArray(str);
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
     * Returns the {@link JSONObject} of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link JSONObject} or null
     */
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

            return JSON.parseObject(str);
        }

        if (value instanceof Map) {
            JSONObject object = new JSONObject((Map) value);
            put(key, object);
            return object;
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
        return getString(key, null);
    }

    /**
     * Returns the {@link String} of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return {@link String} or null
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
            return value.toString();
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
     * @param key the key whose associated value is to be returned
     * @return byte
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable byte
     * @throws JSONException Unsupported type conversion to byte value
     */
    public byte getByteValue(String key) {
        Byte value = getByte(key);
        return value == null ? 0 : value;
    }

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
    public byte[] toJSONBBytes(JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.of(features)) {
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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        int parameterCount = method.getParameterCount();

        Class<?> returnType = method.getReturnType();
        if (parameterCount == 1) {
            if ("equals".equals(methodName)) {
                return this.equals(args[0]);
            }

            Class<?> proxyInterface = null;
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
                throw new JSONException("not support convert to " + returnType.getName() + ", from " + value.getClass());
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
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            try {
                Method nameMethod = annotationType.getMethod("name");
                Object result = nameMethod.invoke(annotation);
                if (result instanceof String && !((String) result).isEmpty()) {
                    name = (String) result;
                    break;
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
        return name;
    }

    /**
     * Creates and puts a new JSONArray with the specified name.
     *
     * @param name the name for the new JSONArray
     * @return the created JSONArray
     */
    public JSONArray putArray(String name) {
        JSONArray array = new JSONArray();
        put(name, array);
        return array;
    }

    /**
     * Creates and puts a new JSONObject with the specified name.
     *
     * @param name the name for the new JSONObject
     * @return the created JSONObject
     */
    public JSONObject putObject(String name) {
        JSONObject object = new JSONObject();
        put(name, object);
        return object;
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
     * @see JSONObject#JSONObject(Map)
     */
    @Override
    public JSONObject clone() {
        return new JSONObject(this);
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
        boolean valueMaybeNull = false;
        for (int i = 0; i < kvArrayLength; i++) {
            Object keyObj = kvArray[i++];
            if (!(keyObj instanceof String)) {
                throw new JSONException("The value corresponding to the even bit index of kvArray is key, which cannot be null and must be of type string");
            }
            String key = (String) keyObj;
            if (valueMaybeNull) {
                if (jsonObject.containsKey(key)) {
                    throw new JSONException("The value corresponding to the even bit index of kvArray is key and cannot be duplicated");
                }
                jsonObject.put(key, kvArray[i]);
            } else {
                Object old = jsonObject.put(key, kvArray[i]);
                if (old != null) {
                    throw new JSONException("The value corresponding to the even bit index of kvArray is key and cannot be duplicated");
                }
                valueMaybeNull = kvArray[i] == null;
            }
        }
        return jsonObject;
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

    /**
     * See {@link JSON#toJSON} for details
     */
    public static JSONObject from(Object obj) {
        return (JSONObject) JSON.toJSON(obj);
    }

    /**
     * See {@link JSON#toJSON} for details
     */
    public static JSONObject from(Object obj, JSONWriter.Feature... writeFeatures) {
        return (JSONObject) JSON.toJSON(obj, writeFeatures);
    }

    public boolean isArray(Object key) {
        Object object = super.get(key);
        return object instanceof JSONArray || object != null && object.getClass().isArray();
    }
}
