package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class TypeConverts {
    static final class ToNumber
            implements Function {
        final Number defaultValue;

        ToNumber(Number defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object apply(Object o) {
            if (o == null) {
                return defaultValue;
            }

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? 1 : 0;
            }

            if (o instanceof Number) {
                return o;
            }

            throw new JSONException("can not cast to Number " + o.getClass());
        }
    }

    static class ToByte
            implements Function {
        final Byte defaultValue;

        ToByte(Byte defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object apply(Object o) {
            if (o == null) {
                return defaultValue;
            }

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? (byte) 1 : (byte) 0;
            }

            if (o instanceof Number) {
                return ((Number) o).byteValue();
            }

            throw new JSONException("can not cast to BigInteger " + o.getClass());
        }
    }

    static class ToShort
            implements Function {
        final Short defaultValue;

        ToShort(Short defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object apply(Object o) {
            if (o == null) {
                return defaultValue;
            }

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? (short) 1 : (short) 0;
            }

            if (o instanceof Number) {
                return ((Number) o).shortValue();
            }

            throw new JSONException("can not cast to Short " + o.getClass());
        }
    }

    static class ToInteger
            implements Function {
        final Integer defaultValue;

        ToInteger(Integer defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object apply(Object o) {
            if (o == null) {
                return defaultValue;
            }

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? 1 : 0;
            }

            if (o instanceof Number) {
                return ((Number) o).intValue();
            }

            throw new JSONException("can not cast to Integer " + o.getClass());
        }
    }

    static class ToLong
            implements Function {
        final Long defaultValue;

        ToLong(Long defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object apply(Object o) {
            if (o == null) {
                return defaultValue;
            }

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? 1L : 0L;
            }

            if (o instanceof Number) {
                return ((Number) o).longValue();
            }

            throw new JSONException("can not cast to Long " + o.getClass());
        }
    }

    static class ToFloat
            implements Function {
        final Float defaultValue;

        ToFloat(Float defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object apply(Object o) {
            if (o == null) {
                return defaultValue;
            }

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? 1F : 0F;
            }

            if (o instanceof Number) {
                return ((Number) o).floatValue();
            }

            throw new JSONException("can not cast to Float " + o.getClass());
        }
    }

    public static class ToDouble
            implements Function {
        final Double defaultValue;

        public ToDouble(Double defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object apply(Object o) {
            if (o == null) {
                return defaultValue;
            }

            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue() ? 1D : 0D;
            }

            if (o instanceof Number) {
                return ((Number) o).doubleValue();
            }

            throw new JSONException("can not cast to Float " + o.getClass());
        }
    }

    static class ToString
            implements Function {
        @Override
        public Object apply(Object o) {
            if (o == null) {
                return null;
            }
            return o.toString();
        }
    }

    static class ToBigInteger
            implements Function {
        @Override
        public Object apply(Object o) {
            if (o == null) {
                return null;
            }

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

            throw new JSONException("can not cast to BigInteger " + o.getClass());
        }
    }

    static class ToBigDecimal
            implements Function {
        @Override
        public Object apply(Object o) {
            if (o == null) {
                return null;
            }

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
                return BigDecimal.valueOf(((Number) o).doubleValue());
            }

            if (o instanceof BigInteger) {
                return new BigDecimal((BigInteger) o);
            }

            if (o instanceof String) {
                return new BigDecimal((String) o);
            }

            throw new JSONException("can not cast to BigDecimal " + o.getClass());
        }
    }

    static class StringToAny
            implements Function {
        final Object defaultValue;
        final Class targetClass;

        public StringToAny(Class targetClass, Object defaultValue) {
            this.targetClass = targetClass;
            this.defaultValue = defaultValue;
        }

        @Override
        public Object apply(Object from) {
            String str = (String) from;
            if (str == null || "null".equals(str) || "".equals(str)) {
                return defaultValue;
            }

            if (targetClass == byte.class || targetClass == Byte.class) {
                return Byte.parseByte(str);
            }

            if (targetClass == short.class || targetClass == Short.class) {
                return Short.parseShort(str);
            }

            if (targetClass == int.class || targetClass == Integer.class) {
                return Integer.parseInt(str);
            }

            if (targetClass == long.class || targetClass == Long.class) {
                return Long.parseLong(str);
            }

            if (targetClass == float.class || targetClass == Float.class) {
                return Float.parseFloat(str);
            }

            if (targetClass == double.class || targetClass == Double.class) {
                return Double.parseDouble(str);
            }

            if (targetClass == char.class || targetClass == Character.class) {
                return str.charAt(0);
            }

            if (targetClass == boolean.class || targetClass == Boolean.class) {
                return "true".equals(str);
            }

            if (targetClass == BigDecimal.class) {
                return new BigDecimal(str);
            }

            if (targetClass == BigInteger.class) {
                return new BigInteger(str);
            }

            if (targetClass == Collections.class || targetClass == List.class || targetClass == JSONArray.class) {
                if ("[]".equals(str)) {
                    return new JSONArray();
                }
            }

            throw new JSONException("can not convert to " + targetClass + ", value : " + str);
        }
    }
}
