package com.alibaba.fastjson3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a JSON array.
 *
 * <pre>
 * JSONArray arr = new JSONArray();
 * arr.add("item");
 * String s = arr.getString(0);
 * </pre>
 */
public class JSONArray extends ArrayList<Object> {
    public JSONArray() {
    }

    public JSONArray(int initialCapacity) {
        super(initialCapacity);
    }

    public JSONArray(Collection<?> c) {
        super(c);
    }

    /**
     * Fluent add — returns this for chaining.
     */
    public JSONArray fluentAdd(Object element) {
        add(element);
        return this;
    }

    public String getString(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            return (String) val;
        }
        return val.toString();
    }

    public Integer getInteger(int index) {
        Object val = get(index);
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

    public int getIntValue(int index) {
        Integer val = getInteger(index);
        return val == null ? 0 : val;
    }

    public Long getLong(int index) {
        Object val = get(index);
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

    public long getLongValue(int index) {
        Long val = getLong(index);
        return val == null ? 0L : val;
    }

    public Boolean getBoolean(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val instanceof String) {
            return "true".equalsIgnoreCase((String) val);
        }
        if (val instanceof Number) {
            return ((Number) val).intValue() != 0;
        }
        throw new JSONException("Can not cast to Boolean, value: " + val);
    }

    public boolean getBooleanValue(int index) {
        Boolean val = getBoolean(index);
        return val != null && val;
    }

    public BigDecimal getBigDecimal(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }
        if (val instanceof Number) {
            return BigDecimal.valueOf(((Number) val).doubleValue());
        }
        if (val instanceof String) {
            return new BigDecimal((String) val);
        }
        throw new JSONException("Can not cast to BigDecimal, value: " + val);
    }

    public BigInteger getBigInteger(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof BigInteger) {
            return (BigInteger) val;
        }
        if (val instanceof Number) {
            return BigInteger.valueOf(((Number) val).longValue());
        }
        if (val instanceof String) {
            return new BigInteger((String) val);
        }
        throw new JSONException("Can not cast to BigInteger, value: " + val);
    }

    public Double getDouble(int index) {
        Object val = get(index);
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
            return Double.parseDouble((String) val);
        }
        throw new JSONException("Can not cast to Double, value: " + val);
    }

    public double getDoubleValue(int index) {
        Double val = getDouble(index);
        return val == null ? 0D : val;
    }

    public Float getFloat(int index) {
        Object val = get(index);
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
            return Float.parseFloat((String) val);
        }
        throw new JSONException("Can not cast to Float, value: " + val);
    }

    public float getFloatValue(int index) {
        Float val = getFloat(index);
        return val == null ? 0F : val;
    }

    public JSONObject getJSONObject(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof JSONObject) {
            return (JSONObject) val;
        }
        throw new JSONException("Can not cast to JSONObject, value: " + val);
    }

    public JSONArray getJSONArray(int index) {
        Object val = get(index);
        if (val == null) {
            return null;
        }
        if (val instanceof JSONArray) {
            return (JSONArray) val;
        }
        throw new JSONException("Can not cast to JSONArray, value: " + val);
    }

    /**
     * Convert to typed list.
     */
    public <T> List<T> toJavaList(Class<T> clazz) {
        // TODO: implement via ObjectReader
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
