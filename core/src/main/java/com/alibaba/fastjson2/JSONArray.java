package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderImplEnum;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONObject.NONE_DIRECT_FEATURES;
import static com.alibaba.fastjson2.util.TypeUtils.toBigDecimal;

public class JSONArray
        extends ArrayList<Object> {
    private static final long serialVersionUID = 1L;

    static ObjectWriter<JSONArray> arrayWriter;

    /**
     * Creates an empty, mutable JSON array.
     * <details><summary>中文</summary>创建可修改的空 JSON 数组。</details>
     *

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
     * <p>Copies the element references in iteration order; nested objects are shared.</p>
     * <details><summary>中文</summary>按迭代顺序复制元素引用；嵌套对象仍共享。</details>
     *
     * @param collection the collection whose elements are to be placed into this {@link JSONArray}
     * @throws NullPointerException If the specified collection is null
     */
    public JSONArray(Collection<?> collection) {
        super(collection);
    }

    /**
     * <p>Copies the array elements into a mutable list. A null element is allowed, but a null array is not.</p>
     * <details><summary>中文</summary>将数组元素复制到可修改列表中；元素可为 null，数组本身不可为 null。</details>
     *
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
     * <p>Negative indexes count from the end; an index before the beginning prepends
     * the element. Nonnegative indexes can extend the array with nulls when the index
     * is less than {@code size() + 4096}; larger indexes leave the array unchanged.</p>
     * <details><summary>中文</summary>
     * 负索引从末尾计数，超出左侧边界时在开头插入。非负索引小于 size() + 4096 时可用 null 扩展数组，
     * 更大的索引不会修改数组。
     * </details>
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
     * @return the previous element, or null if an element was inserted or the array was unchanged
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
     * <p>Existing JSON arrays are returned directly. Collections and Java arrays are copied into a JSONArray
     * and replace the stored element. Strings beginning with {@code [} are parsed; other nonempty strings
     * become singleton arrays. String conversions do not replace the stored element. Null, empty strings,
     * case-insensitive {@code "null"}, and unsupported values return null.</p>
     * <details><summary>中文</summary>已有 JSONArray 直接返回；集合和 Java 数组转换后替换原元素。以 [ 开头的字符串被解析，其他非空字符串包装为单元素数组，字符串转换不修改原元素；空值或不支持的类型返回 null。</details>
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

            JSONReader reader = JSONReader.of(str);
            return JSONFactory.ARRAY_READER.readObject(reader, null, null, 0);
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
     * <p>Maps and supported beans are converted and replace the stored element. JSON object strings are parsed
     * without replacing the element. Existing JSONObject values are returned directly; null, empty strings
     * and case-insensitive {@code "null"} return null.</p>
     * <details><summary>中文</summary>Map 和支持的 Bean 转换后替换原元素；字符串解析不修改原元素。已有 JSONObject 直接返回；null、空字符串及忽略大小写的 null 字符串返回 null。</details>
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
            JSONObject object = new JSONObject((Map) value);
            set(index, object);
            return object;
        }

        Class valueClass = value.getClass();
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(valueClass);

        JSONObject jsonObject = (objectWriter instanceof ObjectWriterAdapter)
                ? ((ObjectWriterAdapter) objectWriter).toJSONObject(value)
                : (JSONObject) JSON.toJSON(value);

        set(index, jsonObject);
        return jsonObject;
    }

    /**
     * Returns the {@link String} at the specified location in this {@link JSONArray}.
     *
     * <p>Strings are returned unchanged. Scalar values use their text representation, dates use the default
     * time zone, and other values are serialized as JSON.</p>
     * <details><summary>中文</summary>字符串保持不变；标量转换为文本，日期使用默认时区，其他值序列化为 JSON。</details>
     *
     * @param index index of the element to return
     * @return the converted string, or null for a null element
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     */
    public String getString(int index) {
        return getString(index, null);
    }

    /**
     * Returns the {@link String} at the specified location in this {@link JSONArray}.
     *
     * <p>Strings are returned unchanged. Scalar values use their text representation, dates use the default
     * time zone, and other values are serialized as JSON.</p>
     * <details><summary>中文</summary>字符串保持不变；标量转换为文本，日期使用默认时区，其他值序列化为 JSON。</details>
     *
     * @param index index of the element to return
     * @param defaultValue the value returned for a null element
     * @return the converted string, or defaultValue for a null element
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

        return JSON.toJSONString(value);
    }

    /**
     * Returns the {@link Double} at the specified location in this {@link JSONArray}.
     *
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
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
     * <p>Null, empty strings and case-insensitive {@code "null"} return zero. Numeric conversions may
     * narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>null、空字符串及忽略大小写的 null 字符串返回零；数值转换可能按目标基本类型缩窄或截断。</details>
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
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
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
     * <p>Null, empty strings and case-insensitive {@code "null"} return zero. Numeric conversions may
     * narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>null、空字符串及忽略大小写的 null 字符串返回零；数值转换可能按目标基本类型缩窄或截断。</details>
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
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
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
     * <p>Null, empty strings and case-insensitive {@code "null"} return zero. Numeric conversions may
     * narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>null、空字符串及忽略大小写的 null 字符串返回零；数值转换可能按目标基本类型缩窄或截断。</details>
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
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
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
     * <p>Null, empty strings and case-insensitive {@code "null"} return zero. Numeric conversions may
     * narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>null、空字符串及忽略大小写的 null 字符串返回零；数值转换可能按目标基本类型缩窄或截断。</details>
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
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
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
     * <p>Null, empty strings and case-insensitive {@code "null"} return zero. Numeric conversions may
     * narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>null、空字符串及忽略大小写的 null 字符串返回零；数值转换可能按目标基本类型缩窄或截断。</details>
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
     * <p>Converts Number values using the target numeric type. String values are trimmed; empty strings
     * and case-insensitive {@code "null"} return null. Numeric conversion may lose precision or narrow.</p>
     * <details><summary>中文</summary>Number 按目标数值类型转换，可能损失精度或缩窄；字符串先去除首尾空白，空字符串或忽略大小写的 null 字符串返回 null。</details>
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
     * <p>Null, empty strings and case-insensitive {@code "null"} return zero. Numeric conversions may
     * narrow or truncate according to the target primitive type.</p>
     * <details><summary>中文</summary>null、空字符串及忽略大小写的 null 字符串返回零；数值转换可能按目标基本类型缩窄或截断。</details>
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
     * <p>Numbers are true only when {@link Number#intValue()} equals 1. Strings are true only for
     * case-insensitive {@code "true"} or {@code "1"}; empty strings and {@code "null"} return null.</p>
     * <details><summary>中文</summary>数值仅在 intValue() 为 1 时为 true；字符串仅 true（忽略大小写）或 1 为 true，空字符串和 null 字符串返回 null。</details>
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
     * <p>Uses {@link #getBoolean(int)} and returns false when that conversion returns null.</p>
     * <details><summary>中文</summary>使用 getBoolean 转换，其结果为 null 时返回 false。</details>
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
     * Returns the {@link Date} at the specified location in this {@link JSONArray}.
     *
     * <p>Numeric values are epoch milliseconds; numeric zero returns null. Existing Date values are
     * returned directly, including a Date at the epoch.</p>
     * <details><summary>中文</summary>数值表示纪元毫秒，数值零返回 null；已有 Date 直接返回，包括纪元时刻的 Date。</details>
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

        if (value instanceof String) {
            return DateUtils.parseDate((String) value);
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
     * Returns the {@link Date} at the specified location in this {@link JSONArray}.
     *
     * <p>The default is used whenever {@link #getDate(int)} returns null, including numeric zero.</p>
     * <details><summary>中文</summary>getDate 返回 null 时使用默认值，包括原值为数值零的情况。</details>
     *
     * @param index index of the element to return
     * @param defaultValue the value returned when date conversion returns null
     * @return {@link Date} or defaultValue
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.27
     */
    public Date getDate(int index, Date defaultValue) {
        Date date = getDate(index);
        if (date == null) {
            date = defaultValue;
        }
        return date;
    }

    /**
     * Returns the {@link Instant} at the specified location in this {@link JSONArray}.
     *
     * <p>Numeric values are epoch milliseconds; numeric zero returns null. Existing Instant values
     * are returned directly.</p>
     * <details><summary>中文</summary>数值表示纪元毫秒，数值零返回 null；已有 Instant 直接返回。</details>
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
     * Returns the {@link LocalDate} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link LocalDate} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public LocalDate getLocalDate(int index) {
        return getLocalDate(index, null);
    }

    /**
     * Returns the {@link LocalDate} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @param defaultValue default value to return if the element is null
     * @return {@link LocalDate} or defaultValue
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public LocalDate getLocalDate(int index, LocalDate defaultValue) {
        Object value = super.get(index);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return TypeUtils.cast(value, LocalDate.class);
    }

    /**
     * Returns the {@link LocalTime} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link LocalTime} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public LocalTime getLocalTime(int index) {
        return getLocalTime(index, null);
    }

    /**
     * Returns the {@link LocalTime} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @param defaultValue default value to return if the element is null
     * @return {@link LocalTime} or defaultValue
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public LocalTime getLocalTime(int index, LocalTime defaultValue) {
        Object value = super.get(index);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        }
        return TypeUtils.cast(value, LocalTime.class);
    }

    /**
     * Returns the {@link OffsetTime} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link OffsetTime} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public OffsetTime getOffsetTime(int index) {
        return getOffsetTime(index, null);
    }

    /**
     * Returns the {@link OffsetTime} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @param defaultValue default value to return if the element is null
     * @return {@link OffsetTime} or defaultValue
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public OffsetTime getOffsetTime(int index, OffsetTime defaultValue) {
        Object value = super.get(index);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof OffsetTime) {
            return (OffsetTime) value;
        }
        return TypeUtils.cast(value, OffsetTime.class);
    }

    /**
     * Returns the {@link LocalDateTime} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link LocalDateTime} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public LocalDateTime getLocalDateTime(int index) {
        return getLocalDateTime(index, null);
    }

    /**
     * Returns the {@link LocalDateTime} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @param defaultValue default value to return if the element is null
     * @return {@link LocalDateTime} or defaultValue
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public LocalDateTime getLocalDateTime(int index, LocalDateTime defaultValue) {
        Object value = super.get(index);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return TypeUtils.cast(value, LocalDateTime.class);
    }

    /**
     * Returns the {@link OffsetDateTime} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link OffsetDateTime} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public OffsetDateTime getOffsetDateTime(int index) {
        return getOffsetDateTime(index, null);
    }

    /**
     * Returns the {@link OffsetDateTime} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @param defaultValue default value to return if the element is null
     * @return {@link OffsetDateTime} or defaultValue
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public OffsetDateTime getOffsetDateTime(int index, OffsetDateTime defaultValue) {
        Object value = super.get(index);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof OffsetDateTime) {
            return (OffsetDateTime) value;
        }
        return TypeUtils.cast(value, OffsetDateTime.class);
    }

    /**
     * Returns the {@link ZonedDateTime} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @return {@link ZonedDateTime} or null
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public ZonedDateTime getZonedDateTime(int index) {
        return getZonedDateTime(index, null);
    }

    /**
     * Returns the {@link ZonedDateTime} at the specified location in this {@link JSONArray}.
     *
     * @param index index of the element to return
     * @param defaultValue default value to return if the element is null
     * @return {@link ZonedDateTime} or defaultValue
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size())}
     * @since 2.0.57
     */
    public ZonedDateTime getZonedDateTime(int index, ZonedDateTime defaultValue) {
        Object value = super.get(index);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof ZonedDateTime) {
            return (ZonedDateTime) value;
        }
        return TypeUtils.cast(value, ZonedDateTime.class);
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
     * Serialize Java Object to JSON {@link String} with specified {@link JSONWriter.Feature}s enabled
     * <details><summary>中文</summary>使用指定的写入特性将对象序列化为 JSON 字符串。</details>
     *
     * <p>A null input is serialized as the JSON text {@code "null"}.</p>
     * <details><summary>中文</summary>null 输入序列化为 JSON 文本 null。</details>
     *
     * @param object Java Object to be serialized into JSON {@link String}
     * @param features features to be enabled in serialization
     * @return the serialized JSON text
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
     * <p>Converts the array through the registered object reader. A String target produces JSON text.</p>
     * <details><summary>中文</summary>通过注册的对象读取器转换数组；String 目标类型得到 JSON 文本。</details>
     *
     * @param <T> the result type
     * @param type specify the {@link Type} to be converted
     * @return the converted value
     * @since 2.0.4
     */
    public <T> T to(Type type) {
        return to(type, 0L);
    }

    /**
     * Convert this {@link JSONArray} to the specified Object
     *
     * <pre>{@code
     * JSONArray array = ...
     * List<User> users = array.to(new TypeReference<ArrayList<User>>(){}.getType());
     * }</pre>
     *
     * <p>Converts the array through the registered object reader. A String target produces JSON text.</p>
     * <details><summary>中文</summary>通过注册的对象读取器转换数组；String 目标类型得到 JSON 文本。</details>
     *
     * @param <T> the result type
     * @param type specify the {@link Type} to be converted
     * @param features the combined JSONReader.Feature masks used for conversion
     * @return the converted value
     * @since 2.0.51
     */
    @SuppressWarnings("unchecked")
    public <T> T to(Type type, long features) {
        if (type == String.class) {
            return (T) toString();
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<T> objectReader = provider.getObjectReader(type);
        return objectReader.createInstance(this, features);
    }

    /**
     * Converts this array to the requested class through its registered object reader.
     * A String target produces JSON text.
     * <details><summary>中文</summary>通过注册的对象读取器转换为指定类；String 目标类型得到 JSON 文本。</details>
     *
     * @param <T> the result type
     * @param type the target class
     * @return the converted value
     * @since 2.0.9
     */
    @SuppressWarnings("unchecked")
    public <T> T to(Class<T> type) {
        if (type == String.class) {
            return (T) toString();
        }

        if (type == JSON.class) {
            return (T) this;
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
     * <p>Returns a new list without replacing this array's elements. Null elements are preserved;
     * elements already of the requested type may be shared with the result.</p>
     * <details><summary>中文</summary>返回新的容器，不替换原数组元素；保留 null 元素，已符合目标类型的元素可能共享。</details>
     *
     * @param <T> the element type
     * @param itemClass specify the {@code Class<T>} to be converted
     * @param features features to be enabled in parsing
     * @return a new list containing the converted elements
     * @throws JSONException if an element cannot be converted
     * @since 2.0.4
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> List<T> toList(Class<T> itemClass, JSONReader.Feature... features) {
        boolean fieldBased = false;
        long featuresValue = JSONFactory.defaultReaderFeatures;
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
     * User[] users = array.toArray(User.class);
     * }</pre>
     *
     * <p>Returns a new array without replacing this array's elements. Null elements are preserved;
     * elements already of the requested type may be shared with the result.</p>
     * <details><summary>中文</summary>返回新的容器，不替换原数组元素；保留 null 元素，已符合目标类型的元素可能共享。</details>
     *
     * @param <T> the element type
     * @param itemClass specify the {@code Class<T>} to be converted
     * @param features features to be enabled in parsing
     * @return a new array containing the converted elements
     * @throws JSONException if an element cannot be converted
     * @since 2.0.4
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T[] toArray(Class<T> itemClass, JSONReader.Feature... features) {
        boolean fieldBased = false;
        long featuresValue = JSONFactory.defaultReaderFeatures;
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
     * Map<String, User> users = array.getObject(0, new TypeReference<HashMap<String, User>>(){}.getType());
     * }</pre>
     *
     * <p>Converts the selected value without replacing the stored element. A null element returns null.</p>
     * <details><summary>中文</summary>转换指定元素但不替换原元素；原元素为 null 时返回 null。</details>
     *
     * @param <T> the result type
     * @param index index of the element to return
     * @param type specify the {@link Type} to be converted
     * @param features reader features used for conversion
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
        long featuresValue = JSONFactory.defaultReaderFeatures;
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
            return objectReader.createInstance((Collection) value, featuresValue);
        }

        if (type instanceof Class && ((Class<?>) type).isInstance(value)) {
            return (T) value;
        }

        String json = JSON.toJSONString(value);
        JSONReader jsonReader = JSONReader.of(json);
        jsonReader.context.config(features);

        // Preserve type arguments when converting a bean through its JSON representation.
        // <details><summary>中文</summary>通过 JSON 表示转换 Java 对象时保留泛型参数。</details>
        ObjectReader objectReader = provider.getObjectReader(type, fieldBased);
        return (T) objectReader.readObject(jsonReader, null, null, 0);
    }

    /**
     * Returns the result of the {@link Type} converter conversion of the element at the specified position in this {@link JSONArray}.
     * <p>
     * {@code User user = jsonArray.getObject(0, User.class);}
     *
     * <p>Converts the selected value without replacing the stored element. A null element returns null.</p>
     * <details><summary>中文</summary>转换指定元素但不替换原元素；原元素为 null 时返回 null。</details>
     *
     * @param <T> the result type
     * @param index index of the element to return
     * @param type specify the {@link Class} to be converted
     * @param features reader features used for conversion
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
        long featuresValue = JSONFactory.defaultReaderFeatures;
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
            return objectReader.createInstance((Collection) value, featuresValue);
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
     * Converts the element using {@link #getJSONObject(int)}, then applies the creator if the result is nonnull.
     * The preliminary conversion may replace the stored element.
     * <details><summary>中文</summary>先调用 getJSONObject 转换，结果非 null 时再调用创建函数；转换过程可能替换原元素。</details>
     *
     * @param <T> the result type
     * @param index the element index
     * @param creator the function applied to the converted JSONObject
     * @return the function result, or null if conversion returns null
     * @since 2.0.3
     */
    public <T> T getObject(int index, Function<JSONObject, T> creator) {
        JSONObject object = getJSONObject(index);

        if (object == null) {
            return null;
        }

        return creator.apply(object);
    }

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
     * <p>Uses the negative-index and bounded-expansion behavior of {@link #set(int, Object)}.</p>
     * <details><summary>中文</summary>遵循 set 的负索引及有界扩展行为。</details>
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
     * Checks if this {@link JSONArray} is valid against the specified {@link JSONSchema}.
     *
     * @param schema the {@link JSONSchema} to validate against
     * @return true if this {@link JSONArray} is valid against the schema, false otherwise
     * @since 2.0.3
     */
    public boolean isValid(JSONSchema schema) {
        return schema
                .validate(this)
                .isSuccess();
    }

    /**
     * Creates and returns a copy of this {@link JSONArray}.
     *
     * <p>The copy is shallow: element objects are shared with the original array.</p>
     * <details><summary>中文</summary>执行浅复制，元素对象与原数组共享。</details>
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
     * JSONArray array = JSONArray.copyOf(Collections.singletonList("fastjson"));
     * </pre>
     * <p>The result is mutable and shallow; null elements are retained.</p>
     * <details><summary>中文</summary>结果可修改且为浅复制，保留 null 元素。</details>
     *
     * @param collection the collection to copy, not null
     * @return a new mutable array in iteration order
     * @throws NullPointerException if collection is null
     * @since 2.0.22
     */
    public static JSONArray copyOf(Collection collection) {
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
     * Parses JSON array text. Null or empty input and the JSON null literal return null.
     * <details><summary>中文</summary>解析 JSON 数组文本；null、空输入或 JSON null 字面量返回 null。</details>
     *
     * @param text the JSON array text
     * @param features reader features enabled for parsing
     * @return the parsed JSONArray, or null
     * @see JSON#parseArray(String, JSONReader.Feature...)
     */
    public static JSONArray parseArray(String text, JSONReader.Feature... features) {
        return JSON.parseArray(text, features);
    }

    /**
     * Parses JSON array text and converts each element to the requested class.
     * <details><summary>中文</summary>解析 JSON 数组文本，并将各元素转换为指定类。</details>
     *
     * @param <T> the element type
     * @param text the JSON array text
     * @param type the element class
     * @param features reader features enabled for parsing
     * @return the parsed list, or null for null, empty, or JSON null input
     * @see JSON#parseArray(String, Class, JSONReader.Feature...)
     */
    public static <T> List<T> parseArray(String text, Class<T> type, JSONReader.Feature... features) {
        return JSON.parseArray(text, type, features);
    }

    /**
     * Parses JSON array text. Null or empty input and the JSON null literal return null.
     * <details><summary>中文</summary>解析 JSON 数组文本；null、空输入或 JSON null 字面量返回 null。</details>
     *
     * @param text the JSON array text
     * @param features reader features enabled for parsing
     * @return the parsed JSONArray, or null
     * @see JSON#parseArray(String, JSONReader.Feature...)
     * @since 2.0.13
     */
    public static JSONArray parse(String text, JSONReader.Feature... features) {
        return JSON.parseArray(text, features);
    }

    /**
     * Parses JSON array text and converts each element to the requested class.
     * <details><summary>中文</summary>解析 JSON 数组文本，并将各元素转换为指定类。</details>
     *
     * @param <T> the element type
     * @param input the JSON array text
     * @param type the element class
     * @return the parsed list, or null for null, empty, or JSON null input
     * @see JSON#parseArray(String, Class)
     * @since 2.0.24
     */
    public static <T> List<T> parseArray(String input, Class<T> type) {
        return JSON.parseArray(input, type);
    }

    /**
     * Converts a Java value through {@link JSON#toJSON(Object, JSONWriter.Feature...)} and requires an array result.
     * An existing JSONArray may be returned unchanged; this method is not a deep-copy operation.
     * <details><summary>中文</summary>通过 JSON.toJSON 转换并要求结果为 JSONArray；已有 JSONArray 可能直接返回，并非深复制操作。</details>
     *
     * @param obj the value to convert, or null
     * @return the resulting JSONArray, or null for a null input
     * @throws ClassCastException if conversion produces a non-array value
     */
    public static JSONArray from(Object obj) {
        return (JSONArray) JSON.toJSON(obj);
    }

    /**
     * Converts a Java value through {@link JSON#toJSON(Object, JSONWriter.Feature...)} and requires an array result.
     * An existing JSONArray may be returned unchanged; this method is not a deep-copy operation.
     * <details><summary>中文</summary>通过 JSON.toJSON 转换并要求结果为 JSONArray；已有 JSONArray 可能直接返回，并非深复制操作。</details>
     *
     * @param obj the value to convert, or null
     * @param writeFeatures serialization features used during conversion
     * @return the resulting JSONArray, or null for a null input
     * @throws ClassCastException if conversion produces a non-array value
     */
    public static JSONArray from(Object obj, JSONWriter.Feature... writeFeatures) {
        return (JSONArray) JSON.toJSON(obj, writeFeatures);
    }
}
