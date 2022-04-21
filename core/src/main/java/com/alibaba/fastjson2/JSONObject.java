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
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class JSONObject extends LinkedHashMap<String, Object> implements InvocationHandler {
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
     * @param initialCapacity the initial capacity
     */
    public JSONObject(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @param map the map whose mappings are to be placed in this map
     */
    public JSONObject(Map map) {
        super(map);
    }

    /**
     * Returns the {@link JSONArray} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link JSONArray} or null
     */
    public JSONArray getJSONArray(String key) {
        Object value = get(key);

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
     * Returns the {@link JSONObject} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link JSONObject} or null
     */
    public JSONObject getJSONObject(String key) {
        Object value = get(key);

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
     * Returns the {@link String} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link String} or null
     */
    public String getString(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return JSON.toJSONString(value);
    }

    /**
     * Returns the {@link Double} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Double} or null
     */
    public Double getDouble(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to Double");
    }

    /**
     * Returns a double value of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return double
     */
    public double getDoubleValue(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to double");
    }

    /**
     * Returns the {@link Float} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Float} or null
     */
    public Float getFloat(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to Float");
    }

    /**
     * Returns a float value of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return float
     */
    public float getFloatValue(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to float");
    }

    /**
     * Returns the {@link Long} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Long} or null
     */
    public Long getLong(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to Long");
    }

    /**
     * Returns a long value of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return long
     */
    public long getLongValue(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to long");
    }

    /**
     * Returns the {@link Integer} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Integer} or null
     */
    public Integer getInteger(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to Integer");
    }

    /**
     * Returns an int value of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return int
     */
    public int getIntValue(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to int");
    }

    /**
     * Returns the {@link Short} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Short} or null
     */
    public Short getShort(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to short");
    }

    /**
     * Returns a short value of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return short
     */
    public short getShortValue(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to short");
    }

    /**
     * Returns the {@link Byte} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Byte} or null
     */
    public Byte getByte(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to byte");
    }

    /**
     * Returns a byte value of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return byte
     */
    public byte getByteValue(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to byte");
    }

    /**
     * Returns the {@link Boolean} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link Boolean} or null
     */
    public Boolean getBoolean(String key) {
        Object value = get(key);

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

        throw new JSONException("can not convert to boolean : " + value);
    }

    /**
     * Returns a boolean value of the associated key in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return boolean
     */
    public boolean getBooleanValue(String key) {
        Object value = get(key);

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

        throw new JSONException("can not convert to boolean : " + value);
    }

    /**
     * Returns the {@link BigInteger} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link BigInteger} or null
     */
    public BigInteger getBigInteger(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to Long");
    }

    /**
     * Returns the {@link BigDecimal} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link BigDecimal} or null
     */
    public BigDecimal getBigDecimal(String key) {
        Object value = get(key);

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

        throw new JSONException("can not cast " + value.getClass() + " to Long");
    }

    /**
     * Returns the {@link BigInteger} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link BigInteger} or null
     */
    public Date getDate(String key) {
        Object value = get(key);

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
     * Returns the {@link BigInteger} of the associated keys in this object.
     *
     * @param key the key whose associated value is to be returned
     * @return {@link BigInteger} or null
     */
    public Instant getInstant(String key) {
        Object value = get(key);

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
     * Convert this {@link JSONObject} to the target type
     *
     * @param type converted goal type
     */
    public <T> T toJavaObject(Type type) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(type);
        return (T) objectReader.createInstance(this);
    }

    /**
     * Convert this {@link JSONObject} to the target type
     *
     * @param type converted goal type
     */
    public <T> T toJavaObject(Class<T> type) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(type);
        return (T) objectReader.createInstance(this);
    }

    /**
     * Returns the result of the {@link Type} converter conversion of the associated value in this object.
     *
     * @param key  the key whose associated value is to be returned
     * @param type converted goal type
     * @return <T> or null
     * @throws JSONException If no suitable conversion method is found
     */
    public <T> T getObject(String key, Type type) {
        Object value = get(key);
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
            Map map = (Map) value;

            ObjectReader objectReader = provider.getObjectReader(type);
            return (T) objectReader.createInstance(map);
        }

        if (value instanceof Collection) {
            ObjectReader objectReader = provider.getObjectReader(type);
            return (T) objectReader.createInstance((Collection) value);
        }

        Class clazz = TypeUtils.getMapping(type);
        if (clazz.isInstance(value)) {
            return (T) value;
        }

        String json = JSON.toJSONString(value);
        ObjectReader objectReader = provider.getObjectReader(clazz);
        JSONReader jsonReader = JSONReader.of(json);
        return (T) objectReader.readObject(jsonReader);
    }

    /**
     * @param proxy  proxy object, currently useless
     * @param method methods that need reflection
     * @param args   parameters of invoke
     * @throws UnsupportedOperationException If reflection for this method is not supported
     */
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
                if (annotation.name().length() != 0) {
                    name = annotation.name();
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
                if (annotation.name().length() != 0) {
                    name = annotation.name();
                }
            }

            if (name == null) {
                name = methodName;
                if (name.startsWith("get")) {
                    name = name.substring(3);
                    if (name.length() == 0) {
                        throw new JSONException("This method '" + methodName + "' is an illegal getter");
                    }
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                } else if (name.startsWith("is")) {
                    name = name.substring(2);
                    if (name.length() == 0) {
                        throw new JSONException("This method '" + methodName + "' is an illegal getter");
                    }
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
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
            }

            Object value = get(name);
            if (value == null && methodName.equals("isEmpty")) {
                return this.isEmpty();
            }

            Function typeConvert = JSONFactory
                .getDefaultObjectReaderProvider()
                .getTypeConvert(
                    value.getClass(),
                    method.getGenericReturnType()
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
     * <code>
     * JSONObject object = new JSONObject().fluentPut("a", 1).fluentPut("b", 2).fluentPut("c", 3);
     * </code>
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
     * @param key   the key of the element
     * @param value the value of the element
     */
    public static JSONObject of(String key, Object value) {
        JSONObject object = new JSONObject(1);
        object.put(key, value);
        return object;
    }

    /**
     * See {@link JSON#parseObject} for details
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }
}