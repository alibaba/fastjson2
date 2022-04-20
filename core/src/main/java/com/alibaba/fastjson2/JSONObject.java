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

    public JSONObject() {
        super();
    }

    public JSONObject(int initialCapacity) {
        super(initialCapacity);
    }

    public JSONObject(Map map) {
        super(map);
    }

    public JSONObject fluentPut(String key, Object value) {
        put(key, value);
        return this;
    }

    public JSONArray getJSONArray(String key) {
        Object value = get(key);

        if (value instanceof JSONArray){
            return (JSONArray) value;
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
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
            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return null;
            }
            return Integer.parseInt(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to Integer");
    }

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
            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return null;
            }
            return Long.parseLong(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to Long");
    }

    public BigInteger getBigInteger(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toBigInteger();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return null;
            }
            return new BigInteger(str);
        }

        if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                ||  value instanceof Long
                ||  value instanceof Float
                ||  value instanceof Double
        ) {
            long longValue = ((Number) value).longValue();
            return BigInteger.valueOf(longValue);
        }

        throw new JSONException("can not cast " + value.getClass() + " to Long");
    }

    public BigDecimal getBigDecimal(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }

        if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                ||  value instanceof Long) {
            long longValue = ((Number) value).longValue();
            return BigDecimal.valueOf(longValue);
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return null;
            }
            return new BigDecimal(str);
        }

        if (value instanceof Float
                || value instanceof Double) {
            double doubleValue = ((Number) value).doubleValue();
            return BigDecimal.valueOf(doubleValue);
        }

        throw new JSONException("can not cast " + value.getClass() + " to Long");
    }

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

    public JSONObject getJSONObject(String key) {
        Object value = get(key);

        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
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

    public <T> T toJavaObject(Class<T> type) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(type);
        return (T) objectReader.createInstance(this);
    }

    public <T> T toJavaObject(Type type) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(type);
        return (T) objectReader.createInstance(this);
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return 0D;
            }

            return Double.parseDouble(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to double");
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return 0F;
            }

            return Float.parseFloat(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to float");
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return null;
            }

            return Float.parseFloat(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to Float");
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return null;
            }

            return Double.parseDouble(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to Double");
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return 0;
            }

            return Long.parseLong(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to long");
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return 0;
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to int");
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return 0;
            }

            return Short.parseShort(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to short");
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return null;
            }

            return Short.parseShort(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to short");
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return null;
            }

            return Byte.parseByte(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to byte");
    }

    public short getByteValue(String key) {
        Object value = get(key);

        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return 0;
            }

            return Byte.parseByte(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to byte");
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return false;
            }

            return str.equalsIgnoreCase("true");
        }

        throw new JSONException("can not convert to boolean : " + value);
    }

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

            if (str.isEmpty()
                    || str.equalsIgnoreCase("null")) {
                return null;
            }

            return str.equalsIgnoreCase("true");
        }

        throw new JSONException("can not convert to boolean : " + value);
    }

    public java.util.Date getDate(String key) {
        Object value = get(key);

        if (value == null || value instanceof Date) {
            return (java.util.Date) value;
        }

        if (value instanceof Number) {
            long millis = ((Number) value).longValue();
            if (millis == 0) {
                return null;
            }
            return new java.util.Date(millis);
        }

        return TypeUtils.toDate(value);
    }

    public Instant getInstant(String key) {
        Object value = get(key);

        if (value == null || value instanceof Instant) {
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

    public String toJSONString() {
        return toString();
    }

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

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        final String methodName = method.getName();

        if (parameterTypes.length == 1) {
            if (methodName.equals("equals")) {
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
                name = methodName;

                if (!name.startsWith("set")) {
                    throw new JSONException("illegal setter");
                }

                name = name.substring(3);
                if (name.length() == 0) {
                    throw new JSONException("illegal setter");
                }
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            }

            put(name, args[0]);
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
                name = methodName;
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
                } else if (name.equals("hashCode")) {
                    return this.hashCode();
                } else if (name.equals("toString")) {
                    return this.toString();
                } else if (name.startsWith("entrySet")) {
                    return this.entrySet();
                } else if (name.equals("size")) {
                    return this.size();
                } else {
                    throw new JSONException("illegal getter : " + name);
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

    public static JSONObject of(String key, Object value) {
        return new JSONObject(1)
                .fluentPut(key, value);
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }
}