package com.alibaba.fastjson2;


import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import static com.alibaba.fastjson2.JSONObject.NONE_DIRECT_FEATURES;
import static com.alibaba.fastjson2.util.TypeUtils.toBigDecimal;

public class JSONArray
        extends ArrayList<Object> {
    private static final long serialVersionUID = 1L;

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
        super.addAll(Arrays.asList(items));
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

            if (str.charAt(0) != '[') {
                return JSONArray.of(str);
            }

            return JSON.parseArray(str);
        }

        if (value instanceof Collection) {
            JSONArray array = new JSONArray((Collection<?>) value);
            set(index, array);
            return array;
        }

        if (value instanceof Object[]) {
            JSONArray array = JSONArray.of((Object[]) value);
            set(index, array);
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
            set(index, jsonArray);
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

            return JSON.parseObject(str);
        }

        if (value instanceof Map) {
            JSONObject object = new JSONObject((Map) value);
            set(index, object);
            return object;
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
        return getString(index, null);
    }

    /**
     * Returns the {@link String} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @param defaultValue the default mapping of the index
     * @return {@link String} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    public String getString(int index, String defaultValue) {
        Object value = get(index);

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
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Double.parseDouble(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to double");
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
        Double value = getDouble(index);
        return value == null ? 0D : value;
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
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Float.parseFloat(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to float");
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
        Float value = getFloat(index);
        return value == null ? 0F : value;
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
            String str = ((String) value).trim();

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
            String str = ((String) value).trim();

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
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Short.parseShort(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to short");
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
        Short value = getShort(index);
        return value == null ? 0 : value;
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
            String str = ((String) value).trim();

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Byte.parseByte(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to byte");
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
        Byte value = getByte(index);
        return value == null ? 0 : value;
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

        throw new JSONException("Can not cast '" + value.getClass() + "' to boolean");
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
        Boolean value = getBoolean(index);
        return value != null && value;
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
            if ((writer.context.features & NONE_DIRECT_FEATURES) != 0) {
                throw new JSONException("not support feature ReferenceDetection / NotWriteEmptyArray / NotWriteDefaultValue");
            }
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
    public byte[] toJSONBBytes(JSONWriter.Feature... features) {
        try (JSONWriter writer = JSONWriter.of(features)) {
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
     * @since 2.0.51
     */

    /**
     * @since 2.0.9
     */

    /**
     * Convert this {@link JSONArray} to the specified Object
     *
     * @param type specify the {@link Type} to be converted
     * @deprecated since 2.0.4, please use {@link #to(Type)}
     */
    @Deprecated

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


    /**
     * Adds a new {@link JSONObject} to the end of this {@link JSONArray}.
     *
     * @return the newly created {@link JSONObject}
     * @since 2.0.3
     */
    public JSONObject addObject() {
        JSONObject object = new JSONObject();
        add(object);
        return object;
    }

    /**
     * Adds a new {@link JSONArray} to the end of this {@link JSONArray}.
     *
     * @return the newly created {@link JSONArray}
     * @since 2.0.3
     */
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
     * @return this {@link JSONArray} instance
     * @since 2.0.3
     */
    public JSONArray fluentAdd(Object element) {
        add(element);
        return this;
    }

    /**
     * Chained clear operation that removes all elements from this {@link JSONArray}.
     *
     * @return this {@link JSONArray} instance
     * @since 2.0.3
     */
    public JSONArray fluentClear() {
        clear();
        return this;
    }

    /**
     * Chained remove operation that removes the element at the specified position.
     *
     * @param index the index of the element to be removed
     * @return this {@link JSONArray} instance
     * @since 2.0.3
     */
    public JSONArray fluentRemove(int index) {
        remove(index);
        return this;
    }

    /**
     * Chained set operation that replaces the element at the specified position.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return this {@link JSONArray} instance
     * @since 2.0.3
     */
    public JSONArray fluentSet(int index, Object element) {
        set(index, element);
        return this;
    }

    /**
     * Chained remove operation that removes the first occurrence of the specified element.
     *
     * @param o element to be removed from this list, if present
     * @return this {@link JSONArray} instance
     * @since 2.0.3
     */
    public JSONArray fluentRemove(Object o) {
        remove(o);
        return this;
    }

    /**
     * Chained remove operation that removes from this list all of its elements that are contained in the specified collection.
     *
     * @param c collection containing elements to be removed from this list
     * @return this {@link JSONArray} instance
     * @since 2.0.3
     */
    public JSONArray fluentRemoveAll(Collection<?> c) {
        removeAll(c);
        return this;
    }

    /**
     * Chained add operation that appends all of the elements in the specified collection to the end of this list.
     *
     * @param c collection containing elements to be added to this list
     * @return this {@link JSONArray} instance
     * @since 2.0.3
     */
    public JSONArray fluentAddAll(Collection<?> c) {
        addAll(c);
        return this;
    }

    /**
     * Creates and returns a copy of this {@link JSONArray}.
     *
     * @return a clone of this instance
     */
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
     * Returns an {@link JSONArray} containing the elements of the given Collection, in its iteration order.
     *
     * <pre>
     * JSONArray array = JSONArray.copyOf(List.of("fastjson"));
     * </pre>
     * @since 2.0.22
     */
    public static JSONArray copyOf(Collection<?> collection) {
        return new JSONArray(collection);
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
     * See {@link JSON#toJSON} for details
     */
    public static JSONArray from(Object obj) {
        return (JSONArray) JSON.toJSON(obj);
    }

    /**
     * See {@link JSON#toJSON} for details
     */
    public static JSONArray from(Object obj, JSONWriter.Feature... writeFeatures) {
        return (JSONArray) JSON.toJSON(obj, writeFeatures);
    }
}
