/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.Wrapper;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.util.TypeUtils.toBigDecimal;

/**
 * A JSON object that implements a {@link Map} with string keys and object values.
 * <p>This is a fastjson1-compatible class that provides the same behavior as the original fastjson 1.x API.
 * JSONObject provides convenient methods for accessing and converting values to various Java types.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Create a JSONObject
 * JSONObject obj = new JSONObject();
 * obj.put("name", "Alice");
 * obj.put("age", 30);
 *
 * // Parse from JSON string
 * JSONObject obj2 = JSON.parseObject("{\"name\":\"Bob\",\"age\":25}");
 * String name = obj2.getString("name");
 * int age = obj2.getIntValue("age");
 *
 * // Convert to Java object
 * User user = obj2.toJavaObject(User.class);
 * }</pre>
 *
 * @author wenshao[szujobs@hotmail.com]
 */
public class JSONObject
        extends JSON
        implements Map<String, Object>, Cloneable, Serializable, InvocationHandler, Wrapper {
    static ObjectReader<JSONArray> arrayReader;
    static ObjectReader<JSONObject> objectReader;

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private final Map<String, Object> map;

    public JSONObject() {
        this(DEFAULT_INITIAL_CAPACITY, false);
    }

    public JSONObject(Map<String, Object> map) {
        if (map == null) {
            throw new IllegalArgumentException("map is null.");
        }
        this.map = map;
    }

    public JSONObject(boolean ordered) {
        this(DEFAULT_INITIAL_CAPACITY, ordered);
    }

    public JSONObject(int initialCapacity) {
        this(initialCapacity, false);
    }

    public JSONObject(int initialCapacity, boolean ordered) {
        if (ordered) {
            map = new LinkedHashMap<>(initialCapacity);
        } else {
            map = new HashMap<>(initialCapacity);
        }
    }

    public static <T> T toJavaObject(JSON json, Class<T> clazz) {
        return com.alibaba.fastjson2.util.TypeUtils.cast(json, clazz, JSONFactory.getDefaultObjectReaderProvider());
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        boolean result = map.containsKey(key);
        if (!result) {
            if (key instanceof Number
                    || key instanceof Character
                    || key instanceof Boolean
                    || key instanceof UUID
            ) {
                result = map.containsKey(key.toString());
            }
        }
        return result;
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        Object val = map.get(key);

        if (val == null
                && (key instanceof Number || key instanceof Boolean || key instanceof Character)) {
            val = map.get(key.toString());
        }

        return adaptResult(val);
    }

    /**
     * Retrieves a {@link JSONObject} value associated with the specified key.
     * <p>This is a fastjson1-compatible method. If the value is a string, it will be parsed as JSON.
     * If the value is a Map, it will be converted to a JSONObject.
     *
     * @param key the key whose associated value is to be returned
     * @return the {@link JSONObject} value, or {@code null} if the key doesn't exist or the value is null
     * @throws JSONException if the value cannot be converted to a JSONObject
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONObject user = obj.getJSONObject("user");
     * String name = user.getString("name");
     * }</pre>
     */
    public JSONObject getJSONObject(String key) {
        Object value = map.get(key);

        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            JSONReader reader = JSONReader.of(str);
            if (objectReader == null) {
                objectReader = reader.getObjectReader(JSONObject.class);
            }
            return objectReader.readObject(reader, null, null, 0);
        }

        if (value instanceof Map) {
            return new JSONObject((Map) value);
        }

        if (value == null) {
            return null;
        }

        Class valueClass = value.getClass();
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(valueClass);
        if (objectWriter instanceof ObjectWriterAdapter) {
            ObjectWriterAdapter writerAdapter = (ObjectWriterAdapter) objectWriter;
            return new JSONObject(writerAdapter.toJSONObject(value));
        }

        return null;
    }

    /**
     * Retrieves a {@link JSONArray} value associated with the specified key.
     * <p>This is a fastjson1-compatible method. If the value is a string, it will be parsed as JSON.
     * If the value is a List, it will be converted to a JSONArray.
     *
     * @param key the key whose associated value is to be returned
     * @return the {@link JSONArray} value, or {@code null} if the key doesn't exist or the value is null
     * @throws JSONException if the value cannot be converted to a JSONArray
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONArray items = obj.getJSONArray("items");
     * for (int i = 0; i < items.size(); i++) {
     *     JSONObject item = items.getJSONObject(i);
     *     // Process item
     * }
     * }</pre>
     */
    public JSONArray getJSONArray(String key) {
        Object value = map.get(key);

        if (value == null || value instanceof JSONArray) {
            return (JSONArray) value;
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

        if (value instanceof List) {
            return new JSONArray((List) value);
        }

        String jsonString = JSON.toJSONString(value);
        return JSON.parseArray(jsonString);
    }

    /**
     * Retrieves a value and converts it to the specified class type.
     * <p>This is a fastjson1-compatible method that handles automatic type conversion.
     *
     * @param <T> the type of the object to return
     * @param key the key whose associated value is to be returned
     * @param clazz the class to convert the value to
     * @return the value converted to type {@code T}, or {@code null} if the key doesn't exist
     * @throws JSONException if the value cannot be converted to the specified type
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * User user = obj.getObject("user", User.class);
     * List<String> tags = obj.getObject("tags", List.class);
     * }</pre>
     */
    public <T> T getObject(String key, Class<T> clazz) {
        return getObject(key, clazz, new Feature[0]);
    }

    /**
     * Retrieves a value and converts it to the specified class type with parser features.
     * <p>This is a fastjson1-compatible method that handles automatic type conversion.
     *
     * @param <T> the type of the object to return
     * @param key the key whose associated value is to be returned
     * @param clazz the class to convert the value to
     * @param features optional parser features to control conversion behavior
     * @return the value converted to type {@code T}, or {@code null} if the key doesn't exist
     * @throws JSONException if the value cannot be converted to the specified type
     */
    public <T> T getObject(String key, Class<T> clazz, Feature... features) {
        Object obj = map.get(key);
        if (obj == null) {
            return null;
        }

        if (clazz == Object.class && obj instanceof JSONObject) {
            return (T) obj;
        }

        if (clazz != Object.class && clazz.isInstance(obj)) {
            return (T) obj;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(obj.getClass(), clazz);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        if (obj instanceof String) {
            String str = (String) obj;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
        }

        String json = JSON.toJSONString(obj);
        JSONReader jsonReader = JSONReader.of(
                json,
                JSON.createReadContext(
                        JSONFactory.getDefaultObjectReaderProvider(),
                        JSON.DEFAULT_PARSER_FEATURE, features
                )
        );

        boolean fieldBased = jsonReader.getContext().isEnabled(JSONReader.Feature.FieldBased);
        ObjectReader objectReader = provider.getObjectReader(clazz, fieldBased);

        String defaultDateFormat = JSON.DEFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            jsonReader
                    .getContext()
                    .setDateFormat(defaultDateFormat);
        }

        return (T) objectReader.readObject(jsonReader, null, null, 0);
    }

    public <T> T getObject(String key, TypeReference typeReference) {
        Object obj = map.get(key);
        Type type;
        if (obj == null || typeReference == null || (type = typeReference.getType()) == null) {
            return (T) obj;
        }

        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (clazz != Object.class && clazz.isInstance(obj)) {
                return (T) obj;
            }
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(obj.getClass(), type);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        if (obj instanceof String) {
            String str = (String) obj;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
        }

        String json = JSON.toJSONString(obj);
        ObjectReader objectReader = provider.getObjectReader(type);
        JSONReader jsonReader = JSONReader.of(json);

        String defaultDateFormat = JSON.DEFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            jsonReader
                    .getContext()
                    .setDateFormat(defaultDateFormat);
        }

        return (T) objectReader.readObject(jsonReader, null, null, 0);
    }

    /**
     * Retrieves a {@link Boolean} value associated with the specified key.
     * <p>This is a fastjson1-compatible method. Supports conversion from boolean, number, and string values.
     * Numbers are treated as {@code true} if equal to 1. Strings are treated as {@code true} if they equal
     * "true" (case-insensitive) or "1".
     *
     * @param key the key whose associated value is to be returned
     * @return the Boolean value, or {@code null} if the key doesn't exist or the value is null/empty string
     * @throws JSONException if the value cannot be converted to a Boolean
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * Boolean active = obj.getBoolean("active");
     * if (active != null && active) {
     *     // Process active user
     * }
     * }</pre>
     */
    public Boolean getBoolean(String key) {
        Object value = map.get(key);

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

    public Byte getByte(String key) {
        Object value = map.get(key);

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

    public byte[] getBytes(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        if (value instanceof String) {
            return IOUtils.decodeBase64((String) value);
        }
        throw new com.alibaba.fastjson.JSONException("can not cast to byte[], value : " + value);
    }

    public <T> T getObject(String key, Type type) {
        return getObject(key, type, new Feature[0]);
    }

    public <T> T getObject(String key, Type type, Feature... features) {
        Object obj = map.get(key);
        if (obj == null) {
            return null;
        }

        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (clazz != Object.class && clazz.isInstance(obj)) {
                return (T) obj;
            }
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(obj.getClass(), type);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        if (obj instanceof String && ((String) obj).isEmpty()) {
            return null;
        }

        String json = JSON.toJSONString(obj);
        JSONReader jsonReader = JSONReader.of(
                json,
                JSON.createReadContext(
                        JSONFactory.getDefaultObjectReaderProvider(),
                        DEFAULT_PARSER_FEATURE,
                        features
                )
        );

        boolean fieldBased = jsonReader.getContext().isEnabled(JSONReader.Feature.FieldBased);
        ObjectReader objectReader = provider.getObjectReader(type, fieldBased);

        String defaultDateFormat = JSON.DEFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            jsonReader
                    .getContext()
                    .setDateFormat(defaultDateFormat);
        }

        return (T) objectReader.readObject(jsonReader, null, null, 0);
    }

    public boolean getBooleanValue(String key) {
        Object value = get(key);
        Boolean booleanVal = com.alibaba.fastjson2.util.TypeUtils.toBoolean(value);
        if (booleanVal == null) {
            return false;
        }

        return booleanVal;
    }

    public byte getByteValue(String key) {
        Object value = map.get(key);

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

    public Short getShort(String key) {
        Object value = map.get(key);

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

    public short getShortValue(String key) {
        Object value = map.get(key);

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
     * Retrieves an {@link Integer} value associated with the specified key.
     * <p>This is a fastjson1-compatible method. Supports conversion from numbers, strings, and booleans.
     * Booleans are converted to 1 (true) or 0 (false). String values with decimals are truncated.
     *
     * @param key the key whose associated value is to be returned
     * @return the Integer value, or {@code null} if the key doesn't exist or the value is null/empty string
     * @throws JSONException if the value cannot be converted to an Integer
     * @throws NumberFormatException if the string value is not a valid number
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * Integer age = obj.getInteger("age");
     * if (age != null && age >= 18) {
     *     // Process adult user
     * }
     * }</pre>
     */
    public Integer getInteger(String key) {
        Object value = map.get(key);

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

        if (value instanceof Boolean) {
            return (Boolean) value ? Integer.valueOf(1) : Integer.valueOf(0);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Integer");
    }

    /**
     * Retrieves an int value associated with the specified key.
     * <p>This is a fastjson1-compatible method. Returns 0 if the value is null or cannot be found.
     *
     * @param key the key whose associated value is to be returned
     * @return the int value, or 0 if the key doesn't exist or the value is null/empty string
     * @throws JSONException if the value cannot be converted to an int
     * @throws NumberFormatException if the string value is not a valid number
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * int count = obj.getIntValue("count");
     * System.out.println("Count: " + count);
     * }</pre>
     */
    public int getIntValue(String key) {
        Object value = map.get(key);

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
     * Retrieves a {@link Long} value associated with the specified key.
     * <p>This is a fastjson1-compatible method. Supports conversion from numbers and strings.
     * String values with decimals are truncated.
     *
     * @param key the key whose associated value is to be returned
     * @return the Long value, or {@code null} if the key doesn't exist or the value is null/empty string
     * @throws JSONException if the value cannot be converted to a Long
     * @throws NumberFormatException if the string value is not a valid number
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * Long id = obj.getLong("id");
     * Long timestamp = obj.getLong("timestamp");
     * }</pre>
     */
    public Long getLong(String key) {
        Object value = map.get(key);

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

    public long getLongValue(String key) {
        Object value = map.get(key);

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

    public Float getFloat(String key) {
        Object value = map.get(key);

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

    public float getFloatValue(String key) {
        Object value = map.get(key);

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

    public Double getDouble(String key) {
        Object value = map.get(key);

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

    public double getDoubleValue(String key) {
        Object value = map.get(key);

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

    public BigDecimal getBigDecimal(String key) {
        Object value = map.get(key);

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
            return toBigDecimal((String) value);
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? BigDecimal.ONE : BigDecimal.ZERO;
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to BigDecimal");
    }

    public BigInteger getBigInteger(String key) {
        Object value = map.get(key);

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

        if (value instanceof Boolean) {
            return (Boolean) value ? BigInteger.ONE : BigInteger.ZERO;
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to BigInteger");
    }

    /**
     * Retrieves a {@link String} value associated with the specified key.
     * <p>This is a fastjson1-compatible method. Any non-null value is converted to a string
     * using its {@code toString()} method.
     *
     * @param key the key whose associated value is to be returned
     * @return the String value, or {@code null} if the key doesn't exist or the value is null
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * String name = obj.getString("name");
     * String description = obj.getString("description");
     * }</pre>
     */
    public String getString(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        return value.toString();
    }

    public Date getDate(String key) {
        Object value = get(key);
        return com.alibaba.fastjson2.util.TypeUtils.toDate(value);
    }

    public java.sql.Date getSqlDate(String key) {
        Object value = get(key);
        return com.alibaba.fastjson2.util.TypeUtils.cast(
                value,
                java.sql.Date.class,
                JSONFactory.getDefaultObjectReaderProvider()
        );
    }

    public java.sql.Timestamp getTimestamp(String key) {
        Object value = get(key);
        return com.alibaba.fastjson2.util.TypeUtils.cast(
                value,
                Timestamp.class,
                JSONFactory.getDefaultObjectReaderProvider()
        );
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    /**
     * Associates the specified value with the specified key and returns this JSONObject.
     * <p>This is a fluent API method that allows method chaining. This is a fastjson1-compatible method.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return this JSONObject instance for method chaining
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONObject obj = new JSONObject()
     *     .fluentPut("name", "Alice")
     *     .fluentPut("age", 30)
     *     .fluentPut("active", true);
     * }</pre>
     */
    public JSONObject fluentPut(String key, Object value) {
        map.put(key, value);
        return this;
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    public JSONObject fluentPutAll(Map<? extends String, ?> m) {
        map.putAll(m);
        return this;
    }

    @Override
    public void clear() {
        map.clear();
    }

    public JSONObject fluentClear() {
        map.clear();
        return this;
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    public JSONObject fluentRemove(Object key) {
        map.remove(key);
        return this;
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Object clone() {
        return new JSONObject(map instanceof LinkedHashMap //
                ? new LinkedHashMap<>(map) //
                : new HashMap<>(map)
        );
    }

    @Override
    public boolean equals(Object obj) {
        return this.map.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
            if ("equals".equals(method.getName())) {
                return this.equals(args[0]);
            }

            Class<?> returnType = method.getReturnType();
            if (returnType != void.class) {
                throw new JSONException("illegal setter");
            }

            String name = null;
            JSONField annotation = method.getAnnotation(JSONField.class);
            if (annotation != null) {
                if (annotation.name().length() != 0) {
                    name = annotation.name();
                }
            }

            if (name == null) {
                name = method.getName();

                if (!name.startsWith("set")) {
                    throw new JSONException("illegal setter");
                }

                name = name.substring(3);
                if (name.length() == 0) {
                    throw new JSONException("illegal setter");
                }
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            }

            map.put(name, args[0]);
            return null;
        }

        if (parameterTypes.length == 0) {
            Class<?> returnType = method.getReturnType();
            if (returnType == void.class) {
                throw new JSONException("illegal getter");
            }

            String name = null;
            JSONField annotation = method.getAnnotation(JSONField.class);
            if (annotation != null) {
                if (annotation.name().length() != 0) {
                    name = annotation.name();
                }
            }

            if (name == null) {
                name = method.getName();
                if (name.startsWith("get")) {
                    name = name.substring(3);
                    if (name.length() == 0) {
                        throw new JSONException("illegal getter");
                    }
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                } else if (name.startsWith("is")) {
                    name = name.substring(2);
                    if (name.length() == 0) {
                        throw new JSONException("illegal getter");
                    }
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                } else if (name.startsWith("hashCode")) {
                    return this.hashCode();
                } else if (name.startsWith("toString")) {
                    return this.toString();
                } else {
                    throw new JSONException("illegal getter");
                }
            }

            Object value = map.get(name);
            return TypeUtils.cast(value, method.getGenericReturnType(), ParserConfig.getGlobalInstance());
        }

        throw new UnsupportedOperationException(method.toGenericString());
    }

    public Map<String, Object> getInnerMap() {
        return this.map;
    }

    public <T> T toJavaObject(Type type) {
        if (type instanceof Class) {
            return (T) JSONFactory.getDefaultObjectReaderProvider().getObjectReader(type).createInstance(this, 0L);
        }
        String str = com.alibaba.fastjson2.JSON.toJSONString(this);
        return com.alibaba.fastjson2.JSON.parseObject(str, type);
    }

    /**
     * Converts this JSONObject to a Java object of the specified class type.
     * <p>This is a fastjson1-compatible method that performs automatic field mapping
     * and type conversion.
     *
     * @param <T> the type of the object to return
     * @param clazz the class to convert this JSONObject to
     * @return a new instance of the specified class with values populated from this JSONObject
     * @throws JSONException if the conversion fails
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONObject json = JSON.parseObject("{\"name\":\"Alice\",\"age\":30}");
     * User user = json.toJavaObject(User.class);
     * System.out.println(user.getName()); // "Alice"
     * }</pre>
     */
    public <T> T toJavaObject(Class<T> clazz) {
        if (clazz == Map.class) {
            return (T) this;
        }

        if (clazz == Object.class && !containsKey(JSON.DEFAULT_TYPE_KEY)) {
            return (T) this;
        }

        if (clazz.isInstance(this)) {
            return (T) this;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(clazz);
        return (T) objectReader.createInstance(this, JSONReader.Feature.SupportSmartMatch.mask);
    }

    public <T> T toJavaObject(Class<T> clazz, ParserConfig config, int features) {
        if (clazz == Map.class) {
            return (T) this;
        }

        if (clazz == Object.class && !containsKey(JSON.DEFAULT_TYPE_KEY)) {
            return (T) this;
        }

        return TypeUtils.castToJavaBean(this, clazz, config);
    }

    @Override
    public String toString() {
        return com.alibaba.fastjson2.JSON.toJSONString(this, JSONWriter.Feature.ReferenceDetection);
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        if (iface == Map.class) {
            return (T) map;
        }
        return (T) this;
    }

    static final class Creator
            implements Supplier<Map> {
        static final Creator INSTANCE = new Creator();
        @Override
        public Map get() {
            return new JSONObject();
        }
    }
}
