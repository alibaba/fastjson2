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

public class JSONArray extends ArrayList<Object> {
    private static final long serialVersionUID = 1L;

    static ObjectWriter<JSONArray> arrayWriter;
    static ObjectReader<JSONArray> arrayReader;
    static ObjectReader<JSONObject> objectReader;

    /**
     * default
     */
    public JSONArray() {
        super();
    }

    /**
     * @param initialCapacity the initial capacity of the list
     */
    public JSONArray(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @param collection the collection whose elements are to be placed into this list
     */
    public JSONArray(Collection<?> collection) {
        super(collection);
    }

    /**
     * @param items the array whose elements are to be placed into this list
     */
    public JSONArray(Object... items) {
        super(items.length);
        for (Object item : items) {
            add(item);
        }
    }

    /**
     * 返回此列表中指定位置的 {@link JSONArray}
     * Returns the {@link JSONArray} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link JSONArray} or null
     */
    public JSONArray getJSONArray(int index) {
        Object value = get(index);

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
     * 返回此列表中指定位置的 {@link JSONObject}
     * Returns the {@link JSONObject} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link JSONObject} or null
     */
    public JSONObject getJSONObject(int index) {
        Object value = get(index);

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
     * 返回此列表中指定位置的 {@link String}
     * Returns the {@link String} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link String} or null
     */
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

    /**
     * 返回此列表中指定位置的 {@link Double}
     * Returns the {@link Double} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link Double} or null
     */
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }

            return Double.parseDouble(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to Double");
    }

    /**
     * 返回此列表中指定位置的 double 数据
     * Returns a double at the specified location in this list.
     *
     * @param index index of the element to return
     * @return double
     */
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return 0D;
            }

            return Double.parseDouble(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to double");
    }

    /**
     * 返回此列表中指定位置的 {@link Float}
     * Returns the {@link Float} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link Float} or null
     */
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }

            return Float.parseFloat(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to Float");
    }

    /**
     * 返回此列表中指定位置的 float 数据
     * Returns a float at the specified location in this list.
     *
     * @param index index of the element to return
     * @return float
     */
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return 0F;
            }

            return Float.parseFloat(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to float");
    }

    /**
     * 返回此列表中指定位置的 {@link Long}
     * Returns the {@link Long} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link Long} or null
     */
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
            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }
            return Long.parseLong(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to Long");
    }

    /**
     * 返回此列表中指定位置的 long 数据
     * Returns a long at the specified location in this list.
     *
     * @param index index of the element to return
     * @return long
     */
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return 0;
            }

            return Long.parseLong(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to long");
    }

    /**
     * 返回此列表中指定位置的 {@link Integer}
     * Returns the {@link Integer} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link Integer} or null
     */
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
            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }
            return Integer.parseInt(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to Integer");
    }

    /**
     * 返回此列表中指定位置的 int 数据
     * Returns an int at the specified location in this list.
     *
     * @param index index of the element to return
     * @return int
     */
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return 0;
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to int");
    }

    /**
     * 返回此列表中指定位置的 {@link Short}
     * Returns the {@link Short} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link Short} or null
     */
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }

            return Short.parseShort(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to short");
    }

    /**
     * 返回此列表中指定位置的 short 数据
     * Returns a short at the specified location in this list.
     *
     * @param index index of the element to return
     * @return short
     */
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return 0;
            }

            return Short.parseShort(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to short");
    }

    /**
     * 返回此列表中指定位置的 {@link Byte}
     * Returns the {@link Byte} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link Byte} or null
     */
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }

            return Byte.parseByte(str);
        }

        throw new JSONException("can not cast " + value.getClass() + " to byte");
    }

    /**
     * 返回此列表中指定位置的 byte 数据
     * Returns a byte at the specified location in this list.
     *
     * @param index index of the element to return
     * @return byte
     */
    public byte getByteValue(int index) {
        Object value = get(index);

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
     * 返回此列表中指定位置的 {@link Boolean}
     * Returns the {@link Boolean} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link Boolean} or null
     */
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

            if (str.isEmpty() || str.equalsIgnoreCase("null")) {
                return null;
            }

            return str.equalsIgnoreCase("true") || str.equals("1");
        }

        throw new JSONException("can not convert to boolean : " + value);
    }

    /**
     * 返回此列表中指定位置的 boolean 数据
     * Returns a boolean at the specified location in this list.
     *
     * @param index index of the element to return
     * @return boolean
     */
    public boolean getBooleanValue(int index) {
        Object value = get(index);

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
     * 返回此列表中指定位置的 {@link BigInteger}
     * Returns the {@link BigInteger} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link BigInteger} or null
     */
    public BigInteger getBigInteger(int index) {
        Object value = get(index);

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
     * 返回此列表中指定位置的 {@link BigDecimal}
     * Returns the {@link BigDecimal} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link BigDecimal} or null
     */
    public BigDecimal getBigDecimal(int index) {
        Object value = get(index);

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
     * 返回此列表中指定位置的 {@link Date}
     * Returns the {@link Date} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link Date} or null
     */
    public Date getDate(int index) {
        Object value = get(index);

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
     * 返回此列表中指定位置的 {@link Instant}
     * Returns the {@link Instant} at the specified location in this list.
     *
     * @param index index of the element to return
     * @return {@link Instant} or null
     */
    public Instant getInstant(int index) {
        Object value = get(index);

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
     * 序列化为 JSON 字符串
     * Serialize to JSON {@link String}
     *
     * @return JSON {@link String}
     */
    @Override
    public String toString() {
        try (JSONWriter writer = JSONWriter.of()) {
            if (arrayWriter == null) {
                arrayWriter = writer.getObjectWriter(JSONArray.class, JSONArray.class);
            }
            arrayWriter.write(writer, this, null, null, 0);
            return writer.toString();
        }
    }

    /**
     * 序列化为 JSON 字符串
     * Serialize to JSON {@link String}
     *
     * @return JSON {@link String}
     */
    public String toJSONString() {
        return toString();
    }

    /**
     * 将此 {@link JSONArray} 转换为目标类型
     * Convert this {@link JSONArray} to the target type
     *
     * @param type converted goal type
     */
    public <T> T toJavaObject(Type type) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(type);
        return (T) objectReader.createInstance(this);
    }

    /**
     * 将此 {@link JSONArray} 的所有成员转换为目标列表
     * 警告 {@link JSONArray} 的每个成员必须实现 Map 接口
     * <p>
     * Convert all the members of this {@link JSONArray} into the target List,
     * warning that each member of the {@link JSONArray} must implement the Map interface
     *
     * <code>
     * String json = "[{\"id\": 1, \"name\": \"fastjson\"}, {\"id\": 2, \"name\": \"fastjson2\"}]";
     * JSONArray array = JSON.parseArray(json);
     * List<User> users = array.toJavaList(User.class);
     * </code>
     *
     * @param clazz converted goal class
     */
    public <T> List<T> toJavaList(Class<T> clazz) {
        List<T> list = new ArrayList<>(size());
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(clazz);

        for (Object item : this) {
            T classItem;
            if (item instanceof Map) {
                classItem = (T) objectReader.createInstance((Map) item);
                System.out.println(item);
            } else {
                throw new JSONException("TODO");
            }
            list.add(classItem);
        }

        return list;
    }

    /**
     * 返回此列表中指定位置元素的 {@link Type} 转换器转换结果
     * Returns the result of the {@link Type} converter conversion of the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @param type  converted goal type
     * @return <T> or null
     * @throws JSONException If no suitable conversion method is found
     */
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

    /**
     * 链式添加元素
     * Chained addition of elements
     *
     * <code>
     * JSONArray array = new JSONArray().fluentAdd(1).fluentAdd(2).fluentAdd(3);
     * </code>
     *
     * @param element element to be appended to this list
     */
    public JSONArray fluentAdd(Object element) {
        add(element);
        return this;
    }

    /**
     * 将多个元素打包成 {@link JSONArray}
     * Pack multiple elements as {@link JSONArray}
     *
     * @param items element set
     */
    public static JSONArray of(Object... items) {
        return new JSONArray(items);
    }

    /**
     * 将单元素打包成 {@link JSONArray}
     * Pack an element as {@link JSONArray}
     *
     * @param item target element
     */
    public static JSONArray of(Object item) {
        JSONArray array = new JSONArray(1);
        array.add(item);
        return array;
    }

    /**
     * 将两个元素打包成 {@link JSONArray}
     * Pack two elements as {@link JSONArray}
     *
     * @param first  first element
     * @param second second element
     */
    public static JSONArray of(Object first, Object second) {
        JSONArray array = new JSONArray(2);
        array.add(first);
        array.add(second);
        return array;
    }

    /**
     * 将三个元素打包成 {@link JSONArray}
     * Pack three elements as {@link JSONArray}
     *
     * @param first  first element
     * @param second second element
     * @param third  third element
     */
    public static JSONArray of(Object first, Object second, Object third) {
        JSONArray array = new JSONArray(3);
        array.add(first);
        array.add(second);
        array.add(third);
        return array;
    }
}
