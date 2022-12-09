package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderImplEnum;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONObject.NONE_DIRECT_FEATURES;

public class JSONArray
        extends ArrayList<Object> {
    private static final long serialVersionUID = 1L;

    static ObjectWriter<JSONArray> arrayWriter;

    /**
     * default
     */
    public JSONArray() {
        super();
    }

    /**
     * @param initialCapacity the initial capacity of the {@link JSONArray}
     * @throws IllegalArgumentException If the specified initial capacity is negative
     */
    public JSONArray(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * @param collection the collection whose elements are to be placed into this {@link JSONArray}
     * @throws NullPointerException If the specified collection is null
     */
    public JSONArray(Collection<?> collection) {
        super(collection);
    }

    /**
     * @param items the array whose elements are to be placed into this {@link JSONArray}
     * @throws NullPointerException If the specified items is null
     */
    public JSONArray(Object... items) {
        super(items.length);
        for (Object item : items) {
            super.add(item);
        }
    }

    /**
     * Replaces the element at the specified position with the specified element
     *
     * <pre>{@code
     *    JSONArray array = new JSONArray();
     *    array.add(-1); // [-1]
     *    array.add(2); // [-1,2]
     *    array.set(0, 1); // [1,2]
     *    array.set(4, 3); // [1,2,null,null,3]
     *    array.set(-1, -1); // [1,2,null,null,-1]
     *    array.set(-2, -2); // [1,2,null,-2,-1]
     *    array.set(-6, -6); // [-6,1,2,null,-2,-1]
     * }</pre>
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @since 2.0.3
     */
    @Override
    public Object set(int index, Object element) {
        int size = super.size();
        if (index < 0) {
            index += size;
            if (index < 0) {
                // left join elem
                super.add(0, element);
                return null;
            }
            return super.set(
                    index, element
            );
        }

        if (index < size) {
            return super.set(
                    index, element
            );
        }

        // max expansion (size + 4096)
        if (index < size + 4096) {
            while (index-- != size) {
                super.add(null);
            }
            super.add(element);
        }
        return null;
    }

    /**
     * Returns the {@link JSONArray} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link JSONArray} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    public JSONArray getJSONArray(int index) {
        Object value = get(index);

        if (value == null) {
            return null;
        }

        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }

        if (value instanceof String) {
            String str = (String) value;

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            JSONReader reader = JSONReader.of(str);
            return JSONFactory.ARRAY_READER.readObject(reader, null, null, 0);
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

    /**
     * Returns the {@link JSONObject} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link JSONObject} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public JSONObject getJSONObject(int index) {
        Object value = get(index);

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
     * Returns the {@link String} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link String} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    public String getString(int index) {
        Object value = get(index);

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
     * Returns the {@link Double} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link Double} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable double
     * @throws JSONException Unsupported type conversion to {@link Double}
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Double.parseDouble(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Double");
    }

    /**
     * Returns a double value at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return double
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable double
     * @throws JSONException Unsupported type conversion to double value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0D;
            }

            return Double.parseDouble(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to double value");
    }

    /**
     * Returns the {@link Float} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link Float} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable float
     * @throws JSONException Unsupported type conversion to {@link Float}
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Float.parseFloat(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Float");
    }

    /**
     * Returns a float value at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return float
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable float
     * @throws JSONException Unsupported type conversion to float value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0F;
            }

            return Float.parseFloat(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to float value");
    }

    /**
     * Returns the {@link Long} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link Long} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable long
     * @throws JSONException Unsupported type conversion to {@link Long}
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Long.parseLong(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Long");
    }

    /**
     * Returns a long value at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return long
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable long
     * @throws JSONException Unsupported type conversion to long value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0;
            }

            return Long.parseLong(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to long value");
    }

    /**
     * Returns the {@link Integer} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link Integer} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable int
     * @throws JSONException Unsupported type conversion to {@link Integer}
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Integer");
    }

    /**
     * Returns an int value at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return int
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable int
     * @throws JSONException Unsupported type conversion to int value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0;
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to int value");
    }

    /**
     * Returns the {@link Short} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link Short} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable short
     * @throws JSONException Unsupported type conversion to {@link Short}
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Short.parseShort(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Short");
    }

    /**
     * Returns a short value at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return short
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable short
     * @throws JSONException Unsupported type conversion to short value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0;
            }

            return Short.parseShort(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to short value");
    }

    /**
     * Returns the {@link Byte} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link Byte} or null
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable byte
     * @throws JSONException Unsupported type conversion to {@link Byte}
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Byte.parseByte(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Byte");
    }

    /**
     * Returns a byte value at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return byte
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable byte
     * @throws JSONException Unsupported type conversion to byte value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0;
            }

            return Byte.parseByte(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to byte value");
    }

    /**
     * Returns the {@link Boolean} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link Boolean} or null
     * @throws JSONException Unsupported type conversion to {@link Boolean}
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return "true".equalsIgnoreCase(str) || "1".equals(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Boolean");
    }

    /**
     * Returns a boolean value at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return boolean
     * @throws JSONException Unsupported type conversion to boolean value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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
            return "true".equalsIgnoreCase(str) || "1".equals(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to boolean value");
    }

    /**
     * Returns the {@link BigInteger} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link BigInteger} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @throws JSONException Unsupported type conversion to {@link BigInteger}
     * @throws NumberFormatException If the value of get is {@link String} and it is not a valid representation of {@link BigInteger}
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return new BigInteger(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to BigInteger");
    }

    /**
     * Returns the {@link BigDecimal} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link BigDecimal} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @throws JSONException Unsupported type conversion to {@link BigDecimal}
     * @throws NumberFormatException If the value of get is {@link String} and it is not a valid representation of {@link BigDecimal}
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
     * Returns the {@link Date} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link Date} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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
     * Returns the {@link Instant} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link Instant} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
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
            if ((writer.context.features & NONE_DIRECT_FEATURES) == 0) {
                writer.write(this);
            } else {
                writer.setRootObject(this);
                if (arrayWriter == null) {
                    arrayWriter = writer.getObjectWriter(JSONArray.class, JSONArray.class);
                }
                arrayWriter.write(writer, this, null, null, 0);
            }
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
     * @since 2.0.15
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
     * Convert this {@link JSONArray} to the specified Object
     *
     * <pre>{@code
     * JSONArray array = ...
     * List<User> users = array.to(new TypeReference<ArrayList<User>>(){}.getType());
     * }</pre>
     *
     * @param type specify the {@link Type} to be converted
     * @since 2.0.4
     */
    @SuppressWarnings("unchecked")
    public <T> T to(Type type) {
        if (type == String.class) {
            return (T) toString();
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<T> objectReader = provider.getObjectReader(type);
        return objectReader.createInstance(this);
    }

    /**
     * @since 2.0.9
     */
    @SuppressWarnings("unchecked")
    public <T> T to(Class<T> type) {
        if (type == String.class) {
            return (T) toString();
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<T> objectReader = provider.getObjectReader(type);
        return objectReader.createInstance(this);
    }

    /**
     * Convert this {@link JSONArray} to the specified Object
     *
     * @param type specify the {@link Type} to be converted
     * @deprecated since 2.0.4, please use {@link #to(Type)}
     */
    @Deprecated
    public <T> T toJavaObject(Type type) {
        return to(type);
    }

    /**
     * Convert all the members of this {@link JSONArray} into the specified Object.
     *
     * <pre>{@code
     * String json = "[{\"id\": 1, \"name\": \"fastjson\"}, {\"id\": 2, \"name\": \"fastjson2\"}]";
     * JSONArray array = JSON.parseArray(json);
     * List<User> users = array.toList(User.class);
     * }</pre>
     *
     * @param itemClass specify the {@code Class<T>} to be converted
     * @param features features to be enabled in parsing
     * @since 2.0.4
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> List<T> toList(Class<T> itemClass, JSONReader.Feature... features) {
        boolean fieldBased = false;
        long featuresValue = 0;
        for (JSONReader.Feature feature : features) {
            featuresValue |= feature.mask;
            if (feature == JSONReader.Feature.FieldBased) {
                fieldBased = true;
            }
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<?> objectReader = provider.getObjectReader(itemClass, fieldBased);

        List<T> list = new ArrayList<>(size());
        for (int i = 0; i < this.size(); i++) {
            Object item = this.get(i);

            T classItem;
            if (item instanceof JSONObject) {
                classItem = (T) objectReader.createInstance((Map) item, featuresValue);
            } else if (item instanceof Map) {
                classItem = (T) objectReader.createInstance((Map) item, featuresValue);
            } else if (item == null || itemClass.isInstance(item)) {
                classItem = (T) item;
            } else {
                Class<?> currentItemClass = item.getClass();
                Function typeConvert = provider.getTypeConvert(currentItemClass, itemClass);
                if (typeConvert != null) {
                    Object converted = typeConvert.apply(item);
                    list.add((T) converted);
                    continue;
                }

                throw new JSONException(
                        currentItemClass + " cannot be converted to " + itemClass
                );
            }
            list.add(classItem);
        }

        return list;
    }

    /**
     * Convert all the members of this {@link JSONArray} into the specified Object.
     *
     * <pre>{@code
     * String json = "[{\"id\": 1, \"name\": \"fastjson\"}, {\"id\": 2, \"name\": \"fastjson2\"}]";
     * JSONArray array = JSON.parseArray(json);
     * List<User> users = array.toList(User.class);
     * }</pre>
     *
     * @param itemClass specify the {@code Class<T>} to be converted
     * @param features features to be enabled in parsing
     * @since 2.0.4
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T[] toArray(Class<T> itemClass, JSONReader.Feature... features) {
        boolean fieldBased = false;
        long featuresValue = 0;
        for (JSONReader.Feature feature : features) {
            featuresValue |= feature.mask;
            if (feature == JSONReader.Feature.FieldBased) {
                fieldBased = true;
            }
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<?> objectReader = provider.getObjectReader(itemClass, fieldBased);

        T[] list = (T[]) Array.newInstance(itemClass, size());
        for (int i = 0; i < this.size(); i++) {
            Object item = this.get(i);

            T classItem;
            if (item instanceof JSONObject) {
                classItem = (T) objectReader.createInstance((Map) item, featuresValue);
            } else if (item instanceof Map) {
                classItem = (T) objectReader.createInstance((Map) item, featuresValue);
            } else if (item == null || itemClass.isInstance(item)) {
                classItem = (T) item;
            } else {
                Class<?> currentItemClass = item.getClass();
                Function typeConvert = provider.getTypeConvert(currentItemClass, itemClass);
                if (typeConvert != null) {
                    Object converted = typeConvert.apply(item);
                    list[i] = (T) converted;
                    continue;
                }

                throw new JSONException(
                        currentItemClass + " cannot be converted to " + itemClass
                );
            }
            list[i] = classItem;
        }

        return list;
    }

    /**
     * Convert all the members of this {@link JSONArray} into the specified Object.
     *
     * @param clazz specify the {@code Class<T>} to be converted
     * @param features features to be enabled in parsing
     * please use {@link #toList(Class, JSONReader.Feature...)}
     */
    public <T> List<T> toJavaList(Class<T> clazz, JSONReader.Feature... features) {
        return toList(clazz, features);
    }

    /**
     * Returns the result of the {@link Type} converter conversion of the element at the specified position in this {@link JSONArray}.
     *
     * <pre>{@code
     * JSONArray array = ...
     * User user = array.getObject(0, TypeReference<HashMap<String ,User>>(){}.getType());
     * }</pre>
     *
     * @param index index of the element to return
     * @param type specify the {@link Type} to be converted
     * @return {@code <T>} or null
     * @throws JSONException If no suitable conversion method is found
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T getObject(int index, Type type, JSONReader.Feature... features) {
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

        boolean fieldBased = false;
        long featuresValue = 0;
        for (JSONReader.Feature feature : features) {
            featuresValue |= feature.mask;
            if (feature == JSONReader.Feature.FieldBased) {
                fieldBased = true;
            }
        }

        if (value instanceof Map) {
            ObjectReader<T> objectReader = provider.getObjectReader(type, fieldBased);
            return objectReader.createInstance((Map) value, featuresValue);
        }

        if (value instanceof Collection) {
            ObjectReader<T> objectReader = provider.getObjectReader(type, fieldBased);
            return objectReader.createInstance((Collection) value);
        }

        Class clazz = TypeUtils.getMapping(type);
        if (clazz.isInstance(value)) {
            return (T) value;
        }

        String json = JSON.toJSONString(value);
        JSONReader jsonReader = JSONReader.of(json);
        jsonReader.context.config(features);

        ObjectReader objectReader = provider.getObjectReader(clazz, fieldBased);
        return (T) objectReader.readObject(jsonReader, null, null, 0);
    }

    /**
     * Returns the result of the {@link Type} converter conversion of the element at the specified position in this {@link JSONArray}.
     * <p>
     * {@code User user = jsonArray.getObject(0, User.class);}
     *
     * @param index index of the element to return
     * @param type specify the {@link Class} to be converted
     * @return {@code <T>} or null
     * @throws JSONException If no suitable conversion method is found
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T getObject(int index, Class<T> type, JSONReader.Feature... features) {
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

        boolean fieldBased = false;
        long featuresValue = 0;
        for (JSONReader.Feature feature : features) {
            featuresValue |= feature.mask;
            if (feature == JSONReader.Feature.FieldBased) {
                fieldBased = true;
            }
        }

        if (value instanceof Map) {
            ObjectReader<T> objectReader = provider.getObjectReader(type, fieldBased);
            return objectReader.createInstance((Map) value, featuresValue);
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
     * @since 2.0.3
     */
    public <T> T getObject(int index, Function<JSONObject, T> creator) {
        JSONObject object = getJSONObject(index);

        if (object == null) {
            return null;
        }

        return creator.apply(object);
    }

    public JSONObject addObject() {
        JSONObject object = new JSONObject();
        add(object);
        return object;
    }

    public JSONArray addArray() {
        JSONArray array = new JSONArray();
        add(array);
        return array;
    }

    /**
     * Chained addition of elements
     *
     * <pre>
     * JSONArray array = new JSONArray().fluentAdd(1).fluentAdd(2).fluentAdd(3);
     * </pre>
     *
     * @param element element to be appended to this list
     */
    public JSONArray fluentAdd(Object element) {
        add(element);
        return this;
    }

    /**
     * @since 2.0.3
     */
    public JSONArray fluentClear() {
        clear();
        return this;
    }

    /**
     * @since 2.0.3
     */
    public JSONArray fluentRemove(int index) {
        remove(index);
        return this;
    }

    /**
     * @since 2.0.3
     */
    public JSONArray fluentSet(int index, Object element) {
        set(index, element);
        return this;
    }

    /**
     * @since 2.0.3
     */
    public JSONArray fluentRemove(Object o) {
        remove(o);
        return this;
    }

    /**
     * @since 2.0.3
     */
    public JSONArray fluentRemoveAll(Collection<?> c) {
        removeAll(c);
        return this;
    }

    /**
     * @since 2.0.3
     */
    public JSONArray fluentAddAll(Collection<?> c) {
        addAll(c);
        return this;
    }

    /**
     * @since 2.0.3
     */
    public boolean isValid(JSONSchema schema) {
        return schema
                .validate(this)
                .isSuccess();
    }

    @Override
    public Object clone() {
        return new JSONArray(this);
    }

    /**
     * Pack multiple elements as {@link JSONArray}
     *
     * <pre>
     * JSONArray array = JSONArray.of(1, 2, "3", 4F, 5L, 6D, true);
     * </pre>
     *
     * @param items element set
     */
    public static JSONArray of(Object... items) {
        return new JSONArray(items);
    }

    /**
     * Pack an element as {@link JSONArray}
     *
     * <pre>
     * JSONArray array = JSONArray.of("fastjson");
     * </pre>
     *
     * @param item target element
     */
    public static JSONArray of(Object item) {
        JSONArray array = new JSONArray(1);
        array.add(item);
        return array;
    }

    /**
     * Pack two elements as {@link JSONArray}
     *
     * <pre>
     * JSONArray array = JSONArray.of("fastjson", 2);
     * </pre>
     *
     * @param first first element
     * @param second second element
     */
    public static JSONArray of(Object first, Object second) {
        JSONArray array = new JSONArray(2);
        array.add(first);
        array.add(second);
        return array;
    }

    /**
     * Pack three elements as {@link JSONArray}
     *
     * <pre>
     * JSONArray array = JSONArray.of("fastjson", 2, true);
     * </pre>
     *
     * @param first first element
     * @param second second element
     * @param third third element
     */
    public static JSONArray of(Object first, Object second, Object third) {
        JSONArray array = new JSONArray(3);
        array.add(first);
        array.add(second);
        array.add(third);
        return array;
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray}
     *
     * @param text the JSON {@link String} to be parsed
     * @param features features to be enabled in parsing
     */
    public static JSONArray parseArray(String text, JSONReader.Feature... features) {
        return JSON.parseArray(text, features);
    }

    /**
     * Parse JSON {@link String} into {@link JSONArray}
     *
     * @param text the JSON {@link String} to be parsed
     * @param features features to be enabled in parsing
     * @since 2.0.13
     */
    public static JSONArray parse(String text, JSONReader.Feature... features) {
        return JSON.parseArray(text, features);
    }

    /**
     *
     * Parse JSON {@link String} into {@link List}
     *
     * @param input the JSON {@link String} to be parsed
     * @param type specify the {@link Class} to be converted
     * @since 2.0.21
     */
    static <T> List<T> parseArray(String input, Class<T> type) {
        return JSON.parseArray(input, type);
    }
}
