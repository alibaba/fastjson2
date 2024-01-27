package com.alibaba.fastjson2.function.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.Function;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.fastjson2.util.TypeUtils.toBigDecimal;

public final class ToAny
        implements Function {
    private final Class targetClass;
    private Object defaultValue;

    public ToAny(Class targetClass) {
        this(targetClass, null);
    }

    public ToAny(Class targetClass, Object defaultValue) {
        this.targetClass = targetClass;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(Object o) {
        if (o == null) {
            return defaultValue;
        }
        if (targetClass.isInstance(o)) {
            return o;
        }

        if (targetClass == String.class) {
            return o.toString();
        }

        if (targetClass == BigDecimal.class) {
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO;
            }

            if (o instanceof Byte
                    || o instanceof Short
                    || o instanceof Integer
                    || o instanceof Long
                    || o instanceof AtomicInteger
                    || o instanceof AtomicLong
            ) {
                return BigDecimal.valueOf(((Number) o).longValue());
            }

            if (o instanceof Float || o instanceof Double) {
                double doubleValue = ((Number) o).doubleValue();
                return toBigDecimal(doubleValue);
            }

            if (o instanceof BigInteger) {
                return new BigDecimal((BigInteger) o);
            }

            if (o instanceof String) {
                return new BigDecimal((String) o);
            }
        } else if (targetClass == BigInteger.class) {
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? BigInteger.ONE : BigInteger.ZERO;
            }

            if (o instanceof Byte
                    || o instanceof Short
                    || o instanceof Integer
                    || o instanceof Long
                    || o instanceof AtomicInteger
                    || o instanceof AtomicLong
                    || o instanceof Float
                    || o instanceof Double
            ) {
                return BigInteger.valueOf(((Number) o).longValue());
            }

            if (o instanceof BigDecimal) {
                return ((BigDecimal) o).toBigInteger();
            }
        } else if (targetClass == Boolean.class) {
            if (o instanceof Number) {
                return ((Number) o).intValue() == 1;
            }

            if (o instanceof String) {
                String str = (String) o;
                switch (str) {
                    case "true":
                    case "TRUE":
                    case "True":
                    case "T":
                    case "Y":
                    case "YES":
                    case "Yes":
                    case "yes":
                        return Boolean.TRUE;
                    case "false":
                    case "FALSE":
                    case "False":
                    case "F":
                    case "N":
                    case "NO":
                    case "no":
                    case "No":
                        return Boolean.FALSE;
                    case "null":
                        return defaultValue;
                    default:
                        break;
                }
            }
        } else if (targetClass == Byte.class) {
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? (byte) 1 : (byte) 0;
            }

            if (o instanceof Number) {
                return ((Number) o).byteValue();
            }
        } else if (targetClass == Double.class) {
            if (o instanceof Number) {
                return ((Number) o).doubleValue();
            }

            if (o instanceof String) {
                String str = (String) o;
                if (str.isEmpty()) {
                    return defaultValue;
                }

                return Double.parseDouble(str);
            }

            if (o instanceof List) {
                List list = (List) o;
                JSONArray array = new JSONArray(list.size());
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    array.add(apply(item));
                }
                return array;
            }

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? 1D : 0D;
            }
        } else if (targetClass == Float.class) {
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? 1F : 0F;
            }

            if (o instanceof Number) {
                return ((Number) o).floatValue();
            }
        } else if (targetClass == Integer.class) {
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? 1 : 0;
            }

            if (o instanceof Number) {
                return ((Number) o).intValue();
            }
        } else if (targetClass == Long.class) {
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? 1L : 0L;
            }

            if (o instanceof Number) {
                return ((Number) o).longValue();
            }
        } else if (targetClass == Short.class) {
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? (short) 1 : (short) 0;
            }

            if (o instanceof Number) {
                return ((Number) o).shortValue();
            }
        } else if (targetClass == Number.class) {
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? 1 : 0;
            }

            if (o instanceof Number) {
                return o;
            }
        }

        throw new JSONException("can not cast to " + targetClass.getName() + " " + o.getClass());
    }
}
