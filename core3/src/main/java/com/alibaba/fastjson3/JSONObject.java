package com.alibaba.fastjson3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a JSON object. Maintains insertion order.
 *
 * <pre>
 * JSONObject obj = new JSONObject();
 * obj.put("name", "test");
 * String name = obj.getString("name");
 * </pre>
 */
public class JSONObject extends LinkedHashMap<String, Object> {
    public JSONObject() {
    }

    public JSONObject(int initialCapacity) {
        super(initialCapacity);
    }

    public JSONObject(Map<String, Object> map) {
        super(map);
    }

    /**
     * Fluent put — returns this for chaining.
     */
    public JSONObject fluentPut(String key, Object value) {
        put(key, value);
        return this;
    }

    public String getString(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            return (String) val;
        }
        return val.toString();
    }

    public Integer getInteger(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Integer) {
            return (Integer) val;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return Integer.parseInt(str);
        }
        throw new JSONException("Can not cast to Integer, value: " + val);
    }

    public int getIntValue(String key) {
        Integer val = getInteger(key);
        return val == null ? 0 : val;
    }

    public Long getLong(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Long) {
            return (Long) val;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return Long.parseLong(str);
        }
        throw new JSONException("Can not cast to Long, value: " + val);
    }

    public long getLongValue(String key) {
        Long val = getLong(key);
        return val == null ? 0L : val;
    }

    public Boolean getBoolean(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return "true".equalsIgnoreCase(str);
        }
        if (val instanceof Number) {
            return ((Number) val).intValue() != 0;
        }
        throw new JSONException("Can not cast to Boolean, value: " + val);
    }

    public boolean getBooleanValue(String key) {
        Boolean val = getBoolean(key);
        return val != null && val;
    }

    public BigDecimal getBigDecimal(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }
        if (val instanceof BigInteger) {
            return new BigDecimal((BigInteger) val);
        }
        if (val instanceof Number) {
            return BigDecimal.valueOf(((Number) val).doubleValue());
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return new BigDecimal(str);
        }
        throw new JSONException("Can not cast to BigDecimal, value: " + val);
    }

    public BigInteger getBigInteger(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof BigInteger) {
            return (BigInteger) val;
        }
        if (val instanceof BigDecimal) {
            return ((BigDecimal) val).toBigInteger();
        }
        if (val instanceof Number) {
            return BigInteger.valueOf(((Number) val).longValue());
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return new BigInteger(str);
        }
        throw new JSONException("Can not cast to BigInteger, value: " + val);
    }

    public Double getDouble(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Double) {
            return (Double) val;
        }
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return Double.parseDouble(str);
        }
        throw new JSONException("Can not cast to Double, value: " + val);
    }

    public double getDoubleValue(String key) {
        Double val = getDouble(key);
        return val == null ? 0D : val;
    }

    public Float getFloat(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Float) {
            return (Float) val;
        }
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        if (val instanceof String) {
            String str = (String) val;
            if (str.isEmpty()) {
                return null;
            }
            return Float.parseFloat(str);
        }
        throw new JSONException("Can not cast to Float, value: " + val);
    }

    public float getFloatValue(String key) {
        Float val = getFloat(key);
        return val == null ? 0F : val;
    }

    @SuppressWarnings("unchecked")
    public JSONObject getJSONObject(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof JSONObject) {
            return (JSONObject) val;
        }
        if (val instanceof Map) {
            JSONObject obj = new JSONObject(((Map<String, Object>) val).size());
            obj.putAll((Map<String, Object>) val);
            return obj;
        }
        throw new JSONException("Can not cast to JSONObject, value: " + val);
    }

    public JSONArray getJSONArray(String key) {
        Object val = get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof JSONArray) {
            return (JSONArray) val;
        }
        throw new JSONException("Can not cast to JSONArray, value: " + val);
    }

    /**
     * Convert to typed Java object.
     */
    public <T> T toJavaObject(Class<T> clazz) {
        return ObjectMapper.shared().convertValue(this, clazz);
    }

    /**
     * Convert to JSON string.
     */
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
