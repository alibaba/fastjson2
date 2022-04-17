package com.alibaba.fastjson2;

import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public class JSONArray extends ArrayList {
    private static final long serialVersionUID = 1L;

    static ObjectWriter<JSONArray> arrayWriter;
    static ObjectReader<JSONArray> arrayReader;
    static ObjectReader<JSONObject> objectReader;

    public JSONArray() {

    }

    public JSONArray(int initialCapacity) {
        super(initialCapacity);
    }

    public JSONArray(Collection collection) {
        super(collection);
    }

    public JSONArray(Object... items) {
        super(items.length);

        for (Object item : items) {
            add(item);
        }
    }

    public JSONArray fluentAdd(Object element) {
        add(element);
        return this;
    }

    public JSONArray getJSONArray(int index) {
        Object value = get(index);
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
        return (JSONArray) value;
    }

    public JSONObject getJSONObject(int index) {
        Object value = get(index);

        if (value == null) {
            return null;
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

        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }

        if (value instanceof Map) {
            return new JSONObject((Map) value);
        }

        return (JSONObject) value;
    }

    public <T> T getObject(int index, Type type) {
        Object value = get(index);
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

        throw new JSONException("can not convert from " + valueClass + " to " + type);
    }


    public double getDoubleValue(int index) {
        Object value = get(index);

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

    public float getFloatValue(int index) {
        Object value = get(index);

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

    public Float getFloat(int index) {
        Object value = get(index);

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

    public Double getDouble(int index) {
        Object value = get(index);

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

    public Long getLong(int index) {
        Object value = get(index);
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

    public long getLongValue(int index) {
        Object value = get(index);

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

    public Integer getInteger(int index) {
        Object value = get(index);
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

    public int getIntValue(int index) {
        Object value = get(index);

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

    public short getShortValue(int index) {
        Object value = get(index);

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

    public Short getShort(int index) {
        Object value = get(index);

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

    public Byte getByte(int index) {
        Object value = get(index);

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

    public short getByteValue(int index) {
        Object value = get(index);

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

    public boolean getBooleanValue(int index) {
        Object value = get(index);

        if (value == null) {
            return false;
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
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

    public Boolean getBoolean(int index) {
        Object value = get(index);

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

    public BigInteger getBigInteger(int index) {
        Object value = get(index);

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

    public BigDecimal getBigDecimal(int index) {
        Object value = get(index);

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

    public String getString(int index) {
        Object value = get(index);
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return JSON.toJSONString(value);
    }

    public java.util.Date getDate(int index) {
        Object value = get(index);

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

    public Instant getInstant(int index) {
        Object value = get(index);

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

    public <T> T toJavaObject(Type type) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(type);
        return (T) objectReader.createInstance(this);
    }

    public <T> List<T> toJavaList(Class<T> clazz) {
        List<T> list = new ArrayList<T>(this.size());

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(clazz);

        for (Object item : this) {
            T classItem;
            if (item instanceof Map) {
                classItem = (T) objectReader.createInstance((Map) item);
            } else {
                throw new JSONException("TODO");
            }
            list.add(classItem);
        }

        return list;
    }

    public String toString() {
        try (JSONWriter writer = JSONWriter.of()) {
            if (arrayWriter == null) {
                arrayWriter = writer.getObjectWriter(JSONArray.class, JSONArray.class);
            }
            arrayWriter.write(writer, this, null, null, 0);
            return writer.toString();
        }
    }

    public static JSONArray of(Object item) {
        return new JSONArray(1)
                .fluentAdd(item);
    }

    public static JSONArray of(Object first, Object second) {
        return new JSONArray(2)
                .fluentAdd(first)
                .fluentAdd(second);
    }

    public static JSONArray of(Object... items) {
        JSONArray array = new JSONArray(items.length);
        for (Object item : items) {
            array.add(item);
        }
        return array;
    }
}
