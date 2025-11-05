package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Function;

import static com.alibaba.fastjson2.util.TypeUtils.toBigDecimal;

/**
 * A JSON array that implements {@link List} and provides convenient methods for accessing elements.
 * <p>This is a fastjson1-compatible class that provides the same behavior as the original fastjson 1.x API.
 * JSONArray provides type-safe accessor methods for retrieving and converting array elements.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Create a JSONArray
 * JSONArray array = new JSONArray();
 * array.add("Alice");
 * array.add(30);
 * array.add(true);
 *
 * // Parse from JSON string
 * JSONArray array2 = JSON.parseArray("[1,2,3,4,5]");
 * int first = array2.getIntValue(0); // 1
 * int size = array2.size();          // 5
 *
 * // Parse typed list
 * String json = "[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]";
 * List<User> users = JSON.parseArray(json, User.class);
 * }</pre>
 */
public class JSONArray
        extends JSON
        implements List<Object>, Serializable, Cloneable {
    static ObjectReader<JSONArray> arrayReader;
    static ObjectReader<JSONObject> objectReader;

    private List list = new com.alibaba.fastjson2.JSONArray();

    protected transient Object relatedArray;
    protected transient Type componentType;

    public JSONArray() {
    }

    public JSONArray(List list) {
        this.list = list;
    }

    public JSONArray(int initialCapacity) {
        this.list = new ArrayList<>(initialCapacity);
    }

    /**
     * Retrieves a {@link Byte} value at the specified index.
     * <p>This is a fastjson1-compatible method. Supports conversion from numbers and strings.
     *
     * @param index the index of the element to return
     * @return the Byte value, or {@code null} if the value is null or an empty string
     * @throws JSONException if the value cannot be converted to a Byte
     * @throws NumberFormatException if the string value is not a valid byte
     * @throws IndexOutOfBoundsException if the index is out of range
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
            String str = (String) value;
            return toBigDecimal(str);
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? BigDecimal.ONE : BigDecimal.ZERO;
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to BigDecimal");
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return 0;
            }

            return Long.parseLong(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to long value");
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

            if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
                return null;
            }

            return Integer.parseInt(str);
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? Integer.valueOf(1) : Integer.valueOf(0);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Integer");
    }

    public Long getLong(int index) {
        Object value = list.get(index);
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
     * Retrieves a {@link JSONObject} value at the specified index.
     * <p>This is a fastjson1-compatible method. If the value is a string, it will be parsed as JSON.
     * If the value is a Map, it will be converted to a JSONObject.
     *
     * @param index the index of the element to return
     * @return the JSONObject value, or {@code null} if the value is null
     * @throws JSONException if the value cannot be converted to a JSONObject
     * @throws IndexOutOfBoundsException if the index is out of range
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONArray array = JSON.parseArray("[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]");
     * JSONObject first = array.getJSONObject(0);
     * String name = first.getString("name"); // "Alice"
     * }</pre>
     */
    public JSONObject getJSONObject(int index) {
        Object value = get(index);

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
            return new JSONObject(
                    writerAdapter.toJSONObject(value)
            );
        }

        return null;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(Object item) {
        return list.add(item);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean addAll(Collection c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return list.addAll(index, c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    public JSONArray fluentClear() {
        list.clear();
        return this;
    }

    public JSONArray fluentRemove(int index) {
        list.remove(index);
        return this;
    }

    public JSONArray fluentRemove(Object o) {
        list.remove(o);
        return this;
    }

    public JSONArray fluentSet(int index, Object element) {
        set(index, element);
        return this;
    }

    public JSONArray fluentRemoveAll(Collection<?> c) {
        list.removeAll(c);
        return this;
    }

    public JSONArray fluentAddAll(Collection<?> c) {
        list.addAll(c);
        return this;
    }

    /**
     * Returns a short value at the specified location in this {@link com.alibaba.fastjson.JSONArray}.
     *
     * @param index index of the element to return
     * @return short
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable short
     * @throws JSONException Unsupported type conversion to short value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    public short getShortValue(int index) {
        Object value = list.get(index);

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
     * Returns a float value at the specified location in this {@link com.alibaba.fastjson.JSONArray}.
     *
     * @param index index of the element to return
     * @return float
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable float
     * @throws JSONException Unsupported type conversion to float value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    public float getFloatValue(int index) {
        Object value = list.get(index);

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
     * Returns a double value at the specified location in this {@link com.alibaba.fastjson.JSONArray}.
     *
     * @param index index of the element to return
     * @return double
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable double
     * @throws JSONException Unsupported type conversion to double value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    public double getDoubleValue(int index) {
        Object value = list.get(index);

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

    @Override
    public boolean retainAll(Collection c) {
        return list.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return list.removeAll(c);
    }

    @Override
    public boolean containsAll(Collection c) {
        return list.containsAll(c);
    }

    @Override
    public Object get(int index) {
        return adaptResult(list.get(index));
    }

    /**
     * Returns a byte value at the specified location in this {@link com.alibaba.fastjson2.JSONArray}.
     *
     * @param index index of the element to return
     * @return byte
     * @throws NumberFormatException If the value of get is {@link String} and it contains no parsable byte
     * @throws JSONException Unsupported type conversion to byte value
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    public byte getByteValue(int index) {
        Object value = list.get(index);

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

    public BigInteger getBigInteger(int index) {
        Object value = list.get(index);

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

        if (value instanceof Boolean) {
            return (Boolean) value ? BigInteger.ONE : BigInteger.ZERO;
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to BigInteger");
    }

    public java.sql.Date getSqlDate(int index) {
        Object object = get(index);
        return TypeUtils.cast(object, java.sql.Date.class);
    }

    public Timestamp getTimestamp(int index) {
        Object object = get(index);
        return TypeUtils.cast(object, java.sql.Timestamp.class);
    }

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

    @Override
    public Object set(int index, Object element) {
        if (index == -1) {
            list.add(element);
            return null;
        }

        if (list.size() <= index) {
            for (int i = list.size(); i < index; ++i) {
                list.add(null);
            }
            list.add(element);
            return null;
        }

        return list.set(index, element);
    }

    @Override
    public void add(int index, Object element) {
        list.add(index, element);
    }

    @Override
    public Object remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    public String getString(int index) {
        Object value = list.get(index);
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (value instanceof Date) {
            long timeMillis = ((Date) value).getTime();
            return DateUtils.toString(timeMillis, false, DateUtils.DEFAULT_ZONE_ID);
        }

        if (value instanceof Boolean
                || value instanceof Character
                || value instanceof Number
                || value instanceof UUID
                || value instanceof Enum
                || value instanceof TemporalAccessor) {
            return value.toString();
        }

        return com.alibaba.fastjson2.JSON.toJSONString(value);
    }

    public JSONArray getJSONArray(int index) {
        Object value = get(index);

        if (value instanceof JSONArray) {
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
            return new JSONArray((List<?>) value);
        }

        return null;
    }

    public <T> T getObject(int index, Type type) {
        Object obj = list.get(index);
        if (type instanceof Class) {
            return (T) TypeUtils.cast(obj, (Class) type);
        } else {
            String json = JSON.toJSONString(obj);
            return JSON.parseObject(json, type);
        }
    }

    public <T> T getObject(int index, Class<T> clazz) {
        Object obj = list.get(index);
        if (obj == null) {
            return null;
        }

        if (clazz.isInstance(obj)) {
            return (T) obj;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(obj.getClass(), clazz);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        String json = JSON.toJSONString(obj);
        ObjectReader objectReader = provider.getObjectReader(clazz);
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
     * Converts this JSONArray to a typed {@link List} of objects.
     * <p>This is a fastjson1-compatible method. Each element in the array is automatically converted
     * to the specified type with proper field mapping and type conversion.
     *
     * @param <T> the type of elements in the list
     * @param clazz the class type of elements to convert to
     * @return a typed list containing the converted elements
     * @throws JSONException if any element cannot be converted to the specified type
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONArray array = JSON.parseArray("[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]");
     * List<User> users = array.toJavaList(User.class);
     * System.out.println(users.get(0).getName()); // "Alice"
     * }</pre>
     */
    public <T> List<T> toJavaList(Class<T> clazz) {
        List<T> list = new ArrayList<>(this.size());

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(clazz);

        for (Object item : this) {
            T classItem;
            if (item instanceof Map) {
                classItem = (T) objectReader.createInstance((Map) item, JSONReader.Feature.SupportSmartMatch.mask);
            } else {
                if (item == null || item.getClass() == clazz) {
                    list.add((T) item);
                    continue;
                }

                Function typeConvert = provider.getTypeConvert(item.getClass(), clazz);
                if (typeConvert != null) {
                    Object converted = typeConvert.apply(item);
                    list.add((T) converted);
                    continue;
                }
                // item is NOT null here
                throw new com.alibaba.fastjson2.JSONException(item.getClass() + " cannot be converted to " + clazz);
            }
            list.add(classItem);
        }

        return list;
    }

    /**
     * Adds an element to this JSONArray and returns the array for method chaining.
     * <p>This is a fluent API method that allows method chaining. This is a fastjson1-compatible method.
     *
     * @param e the element to add
     * @return this JSONArray instance for method chaining
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * JSONArray array = new JSONArray()
     *     .fluentAdd(1)
     *     .fluentAdd(2)
     *     .fluentAdd(3);
     * }</pre>
     */
    public JSONArray fluentAdd(Object e) {
        list.add(e);
        return this;
    }

    /**
     * Converts this JSONArray to a Java object of the specified class type.
     * <p>This is a fastjson1-compatible method. Useful when deserializing arrays into custom objects
     * that implement list-like behavior or accept array data in constructors.
     *
     * @param <T> the type of the object to return
     * @param clazz the class to convert this JSONArray to
     * @return a new instance of the specified class
     * @throws JSONException if the conversion fails
     */
    public <T> T toJavaObject(Class<T> clazz) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<T> objectReader = provider.getObjectReader(clazz);
        return objectReader.createInstance(this);
    }

    @Override
    public String toString() {
        return toJSONString(this);
    }

    @Override
    public JSONArray clone() {
        return new JSONArray(new ArrayList<Object>(list));
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof JSONArray) {
            return this.list.equals(((JSONArray) obj).list);
        }

        return this.list.equals(obj);
    }

    @Override
    public <T> T toJavaObject(Type type) {
        return com.alibaba.fastjson.util.TypeUtils.cast(this, type, ParserConfig.getGlobalInstance());
    }

    @Deprecated
    public Type getComponentType() {
        return componentType;
    }

    @Deprecated
    public void setComponentType(Type componentType) {
        this.componentType = componentType;
    }

    @Deprecated
    public Object getRelatedArray() {
        return relatedArray;
    }

    @Deprecated
    public void setRelatedArray(Object relatedArray) {
        this.relatedArray = relatedArray;
    }
}
