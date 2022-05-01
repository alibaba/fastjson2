package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public class JSONObject extends LinkedHashMap implements InvocationHandler {

    private static final long serialVersionUID = 1L;

    static ObjectReader<JSONArray> arrayReader;
    static ObjectWriter<JSONObject> objectWriter;
    static ObjectReader<JSONObject> objectReader;

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
     * @param loadFactor      the load factor
     * @throws IllegalArgumentException If the initial capacity is negative or the load factor is negative
     * @since 2.0.2
     */
    public JSONObject(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * @param initialCapacity the initial capacity = (number of elements to store / load factor) + 1
     * @param loadFactor      the load factor
     * @param accessOrder     the ordering mode - true for access-order, false for insertion-order
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
        ) {
            return super.get(
                    key.toString()
            );
        }

        return super.get(key);
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return super.entrySet();
    }

    @Override
    public Set<String> keySet() {
        return super.keySet();
    }

    @Override
    public boolean containsKey(Object key) {
        boolean result = super.containsKey(key);
        if (!result) {
            if (key instanceof Number
                    || key instanceof Character
                    || key instanceof Boolean
                    || key instanceof UUID
            ) {
                result = super.containsKey(key.toString());
            }
        }
        return result;
    }

    /**
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     */
    public Object getOrDefault(String key, Object defaultValue) {
        return super.getOrDefault(
                key, defaultValue
        );
    }

    /**
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @since 2.0.2
     */
    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        if (key instanceof Number
                || key instanceof Character
                || key instanceof Boolean
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

        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }

            JSONReader reader = JSONReader.of(str);
            if (arrayReader == null) {
                arrayReader = reader.getObjectReader(JSONArray.class);
            }
            return arrayReader.readObject(reader, 0);
        }

        if (value instanceof Collection) {
            return new JSONArray((Collection<?>) value);
        }

        return null;
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

        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }

            JSONReader reader = JSONReader.of(str);
            if (objectReader == null) {
                objectReader = reader.getObjectReader(JSONObject.class);
            }
            return objectReader.readObject(reader, 0);
        }

        if (value instanceof Map) {
            return new JSONObject((Map) value);
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

        return JSON.toJSONString(value);
    }

    /**
     * Returns the {@link Double} of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Double} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable double
     * @throws JSONException         Unsupported type conversion to {@link Double}
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
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
     * @throws JSONException         Unsupported type conversion to double value
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
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
     * @throws JSONException         Unsupported type conversion to {@link Float}
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
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
     * @throws JSONException         Unsupported type conversion to float value
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
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
     * @throws JSONException         Unsupported type conversion to {@link Long}
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
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
     * @throws JSONException         Unsupported type conversion to long value
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return 0;
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
     * @throws JSONException         Unsupported type conversion to {@link Integer}
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
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
     * @throws JSONException         Unsupported type conversion to int value
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return 0;
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
     * @throws JSONException         Unsupported type conversion to {@link Short}
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
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
     * @throws JSONException         Unsupported type conversion to short value
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
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
     * @throws JSONException         Unsupported type conversion to {@link Byte}
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
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
     * @throws JSONException         Unsupported type conversion to byte value
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }

            return str.equalsIgnoreCase("true") || str.equals("1");
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
            return str.equalsIgnoreCase("true") || str.equals("1");
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to boolean value");
    }

    /**
     * Returns the {@link BigInteger} of the associated keys in this {@link JSONObject}.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link BigInteger} or null
     * @throws JSONException         Unsupported type conversion to {@link BigInteger}
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
            String str = (String) value;

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
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
     * @throws JSONException         Unsupported type conversion to {@link BigDecimal}
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }

            return new BigDecimal(str);
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

        if (value instanceof Number) {
            long millis = ((Number) value).longValue();
            if (millis == 0) {
                return null;
            }
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
            if (objectWriter == null) {
                objectWriter = writer.getObjectWriter(JSONObject.class);
            }
            objectWriter.write(writer, this, null, null, 0);
            return writer.toString();
        }
    }

    /**
     * Serialize to JSON {@link String}
     *
     * @return JSON {@link String}
     */
    public String toJSONString() {
        return toString();
    }

    /**
     * Convert this {@link JSONObject} to the specified Object
     *
     * {@code Map<String, User> users = jsonObject.toJavaObject(new TypeReference<HashMap<String, User>>(){}.getType());}
     *
     * @param type specify the {@link Type} to be converted
     */
    @SuppressWarnings("unchecked")
    public <T> T toJavaObject(Type type) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<T> objectReader = provider.getObjectReader(type);
        return objectReader.createInstance(this);
    }

    /**
     * Convert this {@link JSONObject} to the specified Object
     *
     * {@code User user = jsonObject.toJavaObject(User.class);}
     *
     * @param clazz specify the {@code Class<T>} to be converted
     */
    @SuppressWarnings("unchecked")
    public <T> T toJavaObject(Class<T> clazz) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<T> objectReader = provider.getObjectReader(clazz);
        return objectReader.createInstance(this);
    }

    /**
     * Returns the result of the {@link Type} converter conversion of the associated value in this {@link JSONObject}.
     *
     * {@code User user = jsonObject.getObject("user", User.class);}
     *
     * @param key  the key whose associated value is to be returned
     * @param type specify the {@link Type} to be converted
     * @return {@code <T>} or null
     * @throws JSONException If no suitable conversion method is found
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T getObject(String key, Type type) {
        Object value = super.get(key);

        if (value == null) {
            return null;
        }

        Class<?> valueClass = value.getClass();
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(valueClass, type);
        if (typeConvert != null) {
            return (T) typeConvert.apply(value);
        }

        if (value instanceof Map) {
            ObjectReader<T> objectReader = provider.getObjectReader(type);
            return objectReader.createInstance((Map) value);
        }

        if (value instanceof Collection) {
            ObjectReader<T> objectReader = provider.getObjectReader(type);
            return objectReader.createInstance((Collection) value);
        }

        Class clazz = TypeUtils.getMapping(type);
        if (clazz.isInstance(value)) {
            return (T) value;
        }

        String json = JSON.toJSONString(value);
        JSONReader jsonReader = JSONReader.of(json);

        ObjectReader objectReader = provider.getObjectReader(clazz);
        return (T) objectReader.readObject(jsonReader);
    }

    /**
     * @param proxy  proxy object, currently useless
     * @param method methods that need reflection
     * @param args   parameters of invoke
     * @throws UnsupportedOperationException  If reflection for this method is not supported
     * @throws ArrayIndexOutOfBoundsException If the length of args does not match the length of the method parameter
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length == 1) {
            if (methodName.equals("equals")) {
                return this.equals(args[0]);
            }

            if (method.getReturnType() != void.class) {
                throw new JSONException("This method '" + methodName + "' is not a setter");
            }

            String name = null;
            JSONField annotation = method.getAnnotation(JSONField.class);
            if (annotation != null) {
                name = annotation.name();
                if (name.isEmpty()) {
                    name = null;
                }
            }

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

        if (parameterTypes.length == 0) {
            if (method.getReturnType() == void.class) {
                throw new JSONException("This method '" + methodName + "' is not a getter");
            }

            String name = null;
            JSONField annotation = method.getAnnotation(JSONField.class);
            if (annotation != null) {
                name = annotation.name();
                if (name.isEmpty()) {
                    name = null;
                }
            }

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
                    if (name.equals("isEmpty")) {
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
                } else if (name.equals("hashCode")) {
                    return this.hashCode();
                } else if (name.equals("toString")) {
                    return this.toString();
                } else if (name.startsWith("entrySet")) {
                    return this.entrySet();
                } else if (name.equals("size")) {
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
     * Chained addition of elements
     *
     * <pre>
     * JSONObject object = new JSONObject().fluentPut("a", 1).fluentPut("b", 2).fluentPut("c", 3);
     * </pre>
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    public JSONObject fluentPut(String key, Object value) {
        put(key, value);
        return this;
    }

    /**
     * Pack a pair of key-values as {@link JSONObject}
     *
     * <pre>
     * JSONObject jsonObject = JSONObject.of("name", "fastjson2");
     * </pre>
     *
     * @param key   the key of the element
     * @param value the value of the element
     */
    public static JSONObject of(String key, Object value) {
        JSONObject object = new JSONObject(2);
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
        JSONObject object = new JSONObject(3);
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
        JSONObject object = new JSONObject(5);
        object.put(k1, v1);
        object.put(k2, v2);
        object.put(k3, v3);
        return object;
    }

    /**
     * See {@link JSON#parseObject} for details
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }
}
