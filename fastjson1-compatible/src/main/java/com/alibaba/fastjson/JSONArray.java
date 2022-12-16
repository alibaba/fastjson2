package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

public class JSONArray
        extends JSON
        implements List, Serializable, Cloneable {
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
        return list.get(index);
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
            return (T) JSON.parseObject(json, type);
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

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            jsonReader
                    .getContext()
                    .setDateFormat(defaultDateFormat);
        }

        return (T) objectReader.readObject(jsonReader, null, null, 0);
    }

    public <T> List<T> toJavaList(Class<T> clazz) {
        List<T> list = new ArrayList<T>(this.size());

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(clazz);

        for (Object item : this) {
            T classItem;
            if (item instanceof Map) {
                classItem = (T) objectReader.createInstance((Map) item, 0L);
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

                throw new com.alibaba.fastjson2.JSONException(
                        (item == null ? "null" : item.getClass()) + " cannot be converted to " + clazz
                );
            }
            list.add(classItem);
        }

        return list;
    }

    public JSONArray fluentAdd(Object e) {
        list.add(e);
        return this;
    }

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
