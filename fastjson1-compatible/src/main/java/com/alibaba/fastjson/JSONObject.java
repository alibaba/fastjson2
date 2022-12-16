/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.Wrapper;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

/**
 * @author wenshao[szujobs@hotmail.com]
 */
public class JSONObject
        extends JSON
        implements Map<String, Object>, Cloneable, Serializable, InvocationHandler, Wrapper {
    static ObjectReader<JSONArray> arrayReader;
    static ObjectReader<JSONObject> objectReader;

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private final Map<String, Object> map;

    public JSONObject() {
        this(DEFAULT_INITIAL_CAPACITY, false);
    }

    public JSONObject(Map<String, Object> map) {
        if (map == null) {
            throw new IllegalArgumentException("map is null.");
        }
        this.map = map;
    }

    public JSONObject(boolean ordered) {
        this(DEFAULT_INITIAL_CAPACITY, ordered);
    }

    public JSONObject(int initialCapacity) {
        this(initialCapacity, false);
    }

    public JSONObject(int initialCapacity, boolean ordered) {
        if (ordered) {
            map = new LinkedHashMap<String, Object>(initialCapacity);
        } else {
            map = new HashMap<String, Object>(initialCapacity);
        }
    }

    public static <T> T toJavaObject(JSON json, Class<T> clazz) {
        return com.alibaba.fastjson2.util.TypeUtils.cast(json, clazz, JSONFactory.getDefaultObjectReaderProvider());
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        boolean result = map.containsKey(key);
        if (!result) {
            if (key instanceof Number
                    || key instanceof Character
                    || key instanceof Boolean
                    || key instanceof UUID
            ) {
                result = map.containsKey(key.toString());
            }
        }
        return result;
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        Object val = map.get(key);

        if (val == null
                && (key instanceof Number || key instanceof Boolean || key instanceof Character)) {
            val = map.get(key.toString());
        }

        return val;
    }

    public JSONObject getJSONObject(String key) {
        Object value = map.get(key);

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
            return new JSONObject(writerAdapter.toJSONObject(value));
        }

        return null;
    }

    public JSONArray getJSONArray(String key) {
        Object value = map.get(key);

        if (value == null || value instanceof JSONArray) {
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
            return new JSONArray((List) value);
        }

        String jsonString = JSON.toJSONString(value);
        return JSON.parseArray(jsonString);
    }

    public <T> T getObject(String key, Class<T> clazz) {
        return getObject(key, clazz, new Feature[0]);
    }
    //
    public <T> T getObject(String key, Class<T> clazz, Feature... features) {
        Object obj = map.get(key);
        if (obj == null) {
            return null;
        }

        if (clazz == Object.class && obj instanceof JSONObject) {
            return (T) obj;
        }

        if (clazz != Object.class && clazz.isInstance(obj)) {
            return (T) obj;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(obj.getClass(), clazz);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        if (obj instanceof String) {
            String str = (String) obj;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
        }

        String json = JSON.toJSONString(obj);
        JSONReader jsonReader = JSONReader.of(
                json,
                JSON.createReadContext(
                        JSONFactory.getDefaultObjectReaderProvider(),
                        JSON.DEFAULT_PARSER_FEATURE, features
                )
        );

        boolean fieldBased = jsonReader.getContext().isEnabled(JSONReader.Feature.FieldBased);
        ObjectReader objectReader = provider.getObjectReader(clazz, fieldBased);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            jsonReader
                    .getContext()
                    .setDateFormat(defaultDateFormat);
        }

        return (T) objectReader.readObject(jsonReader, null, null, 0);
    }

    public <T> T getObject(String key, TypeReference typeReference) {
        Object obj = map.get(key);
        if (typeReference == null) {
            return (T) obj;
        }

        Type type = typeReference.getType();

        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (clazz != Object.class && clazz.isInstance(obj)) {
                return (T) obj;
            }
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(obj.getClass(), type);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        if (obj instanceof String) {
            String str = (String) obj;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
        }

        String json = JSON.toJSONString(obj);
        ObjectReader objectReader = provider.getObjectReader(type);
        JSONReader jsonReader = JSONReader.of(json);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            jsonReader
                    .getContext()
                    .setDateFormat(defaultDateFormat);
        }

        return (T) objectReader.readObject(jsonReader, null, null, 0);
    }

    public Boolean getBoolean(String key) {
        Object value = map.get(key);

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

    public Byte getByte(String key) {
        Object value = map.get(key);

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

    public byte[] getBytes(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        if (value instanceof String) {
            return IOUtils.decodeBase64((String) value);
        }
        throw new com.alibaba.fastjson.JSONException("can not cast to byte[], value : " + value);
    }

    public <T> T getObject(String key, Type type) {
        return getObject(key, type, new Feature[0]);
    }

    public <T> T getObject(String key, Type type, Feature... features) {
        Object obj = map.get(key);
        if (obj == null) {
            return null;
        }

        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (clazz != Object.class && clazz.isInstance(obj)) {
                return (T) obj;
            }
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(obj.getClass(), type);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        if (obj instanceof String && ((String) obj).isEmpty()) {
            return null;
        }

        String json = JSON.toJSONString(obj);
        JSONReader jsonReader = JSONReader.of(
                json,
                JSON.createReadContext(
                        JSONFactory.getDefaultObjectReaderProvider(),
                        DEFAULT_PARSER_FEATURE,
                        features
                )
        );

        boolean fieldBased = jsonReader.getContext().isEnabled(JSONReader.Feature.FieldBased);
        ObjectReader objectReader = provider.getObjectReader(type, fieldBased);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            jsonReader
                    .getContext()
                    .setDateFormat(defaultDateFormat);
        }

        return (T) objectReader.readObject(jsonReader, null, null, 0);
    }

    public boolean getBooleanValue(String key) {
        Object value = get(key);
        Boolean booleanVal = com.alibaba.fastjson2.util.TypeUtils.toBoolean(value);
        if (booleanVal == null) {
            return false;
        }

        return booleanVal.booleanValue();
    }

    public byte getByteValue(String key) {
        Object value = map.get(key);

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

    public Short getShort(String key) {
        Object value = map.get(key);

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

    public short getShortValue(String key) {
        Object value = map.get(key);

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

    public Integer getInteger(String key) {
        Object value = map.get(key);

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

            if (str.indexOf('.') != -1) {
                return (int) Double.parseDouble(str);
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Integer");
    }

    //
    public int getIntValue(String key) {
        Object value = map.get(key);

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

            if (str.indexOf('.') != -1) {
                return (int) Double.parseDouble(str);
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to int value");
    }

    //
    public Long getLong(String key) {
        Object value = map.get(key);

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

            if (str.indexOf('.') != -1) {
                return (long) Double.parseDouble(str);
            }

            return Long.parseLong(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to Long");
    }

    public long getLongValue(String key) {
        Object value = map.get(key);

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

            if (str.indexOf('.') != -1) {
                return (long) Double.parseDouble(str);
            }

            return Long.parseLong(str);
        }

        throw new JSONException("Can not cast '" + value.getClass() + "' to long value");
    }

    public Float getFloat(String key) {
        Object value = map.get(key);

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

    public float getFloatValue(String key) {
        Object value = map.get(key);

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

    public Double getDouble(String key) {
        Object value = map.get(key);

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

    public double getDoubleValue(String key) {
        Object value = map.get(key);

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

    public BigDecimal getBigDecimal(String key) {
        Object value = map.get(key);

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

    public BigInteger getBigInteger(String key) {
        Object value = map.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }

        if (value instanceof Number) {
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

    public String getString(String key) {
        Object value = get(key);

        if (value == null) {
            return null;
        }

        return value.toString();
    }

    public Date getDate(String key) {
        Object value = get(key);
        return com.alibaba.fastjson2.util.TypeUtils.toDate(value);
    }

    public java.sql.Date getSqlDate(String key) {
        Object value = get(key);
        return com.alibaba.fastjson2.util.TypeUtils.cast(
                value,
                java.sql.Date.class,
                JSONFactory.getDefaultObjectReaderProvider()
        );
    }

    public java.sql.Timestamp getTimestamp(String key) {
        Object value = get(key);
        return com.alibaba.fastjson2.util.TypeUtils.cast(
                value,
                Timestamp.class,
                JSONFactory.getDefaultObjectReaderProvider()
        );
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    public JSONObject fluentPut(String key, Object value) {
        map.put(key, value);
        return this;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        map.putAll(m);
    }

    public JSONObject fluentPutAll(Map<? extends String, ? extends Object> m) {
        map.putAll(m);
        return this;
    }

    @Override
    public void clear() {
        map.clear();
    }

    public JSONObject fluentClear() {
        map.clear();
        return this;
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    public JSONObject fluentRemove(Object key) {
        map.remove(key);
        return this;
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Object clone() {
        return new JSONObject(map instanceof LinkedHashMap //
                ? new LinkedHashMap<String, Object>(map) //
                : new HashMap<String, Object>(map)
        );
    }

    @Override
    public boolean equals(Object obj) {
        return this.map.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
            if ("equals".equals(method.getName())) {
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
                name = method.getName();

                if (!name.startsWith("set")) {
                    throw new JSONException("illegal setter");
                }

                name = name.substring(3);
                if (name.length() == 0) {
                    throw new JSONException("illegal setter");
                }
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            }

            map.put(name, args[0]);
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
                name = method.getName();
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
                } else if (name.startsWith("hashCode")) {
                    return this.hashCode();
                } else if (name.startsWith("toString")) {
                    return this.toString();
                } else {
                    throw new JSONException("illegal getter");
                }
            }

            Object value = map.get(name);
            return TypeUtils.cast(value, method.getGenericReturnType(), ParserConfig.getGlobalInstance());
        }

        throw new UnsupportedOperationException(method.toGenericString());
    }

    public Map<String, Object> getInnerMap() {
        return this.map;
    }

    public <T> T toJavaObject(Type type) {
        if (type instanceof Class) {
            return (T) JSONFactory.getDefaultObjectReaderProvider().getObjectReader(type).createInstance(this, 0L);
        }
        String str = com.alibaba.fastjson2.JSON.toJSONString(this);
        return com.alibaba.fastjson2.JSON.parseObject(str, type);
    }

    public <T> T toJavaObject(Class<T> clazz) {
        if (clazz == Map.class) {
            return (T) this;
        }

        if (clazz == Object.class && !containsKey(JSON.DEFAULT_TYPE_KEY)) {
            return (T) this;
        }

        if (clazz.isInstance(this)) {
            return (T) this;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader objectReader = provider.getObjectReader(clazz);
        return (T) objectReader.createInstance(this, 0L);
    }

    public <T> T toJavaObject(Class<T> clazz, ParserConfig config, int features) {
        if (clazz == Map.class) {
            return (T) this;
        }

        if (clazz == Object.class && !containsKey(JSON.DEFAULT_TYPE_KEY)) {
            return (T) this;
        }

        return TypeUtils.castToJavaBean(this, clazz, config);
    }

    @Override
    public String toString() {
        return com.alibaba.fastjson2.JSON.toJSONString(this, JSONWriter.Feature.ReferenceDetection);
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        if (iface == Map.class) {
            return (T) map;
        }
        return (T) this;
    }
}
