package com.alibaba.fastjson2.function.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StringToAny
        implements Function<String, Object> {
    private static final Map<Class, StringToAnyConverter> CONVERTERS_MAP = new HashMap<>();

    static {
        CONVERTERS_MAP.put(byte.class, new ByteConverter());
        CONVERTERS_MAP.put(Byte.class, new ByteConverter());
        CONVERTERS_MAP.put(short.class, new ShortConverter());
        CONVERTERS_MAP.put(Short.class, new ShortConverter());
        CONVERTERS_MAP.put(int.class, new IntegerConverter());
        CONVERTERS_MAP.put(Integer.class, new IntegerConverter());
        CONVERTERS_MAP.put(long.class, new LongConverter());
        CONVERTERS_MAP.put(Long.class, new LongConverter());
        CONVERTERS_MAP.put(float.class, new FloatConverter());
        CONVERTERS_MAP.put(Float.class, new FloatConverter());
        CONVERTERS_MAP.put(double.class, new DoubleConverter());
        CONVERTERS_MAP.put(Double.class, new DoubleConverter());
        CONVERTERS_MAP.put(char.class, new CharacterConverter());
        CONVERTERS_MAP.put(Character.class, new CharacterConverter());
        CONVERTERS_MAP.put(boolean.class, new BooleanConverter());
        CONVERTERS_MAP.put(Boolean.class, new BooleanConverter());
        CONVERTERS_MAP.put(BigDecimal.class, new BigDecimalConverter());
        CONVERTERS_MAP.put(BigInteger.class, new BigIntegerConverter());
    }

    final Object defaultValue;
    final Class targetClass;

    public StringToAny(Class targetClass, Object defaultValue) {
        this.targetClass = targetClass;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(String str) {
        if (str == null || "null".equals(str) || "".equals(str)) {
            return defaultValue;
        }

        StringToAnyConverter converter = CONVERTERS_MAP.get(targetClass);
        if (converter != null) {
            return converter.convert(str);
        }

        if (targetClass == Collections.class || targetClass == List.class || targetClass == JSONArray.class) {
            if ("[]".equals(str)) {
                return new JSONArray();
            }
        }

        throw new JSONException("can not convert to " + targetClass + ", value : " + str);
    }

    private abstract static class StringToAnyConverter {
        public abstract Object convert(String str);
    }

    private static class ByteConverter
            extends StringToAnyConverter {
        @Override
        public Object convert(String str) {
            return Byte.parseByte(str);
        }
    }

    private static class ShortConverter
            extends StringToAnyConverter {
        @Override
        public Object convert(String str) {
            return Short.parseShort(str);
        }
    }

    private static class IntegerConverter
            extends StringToAnyConverter {
        @Override
        public Object convert(String str) {
            return Integer.parseInt(str);
        }
    }

    private static class LongConverter
            extends StringToAnyConverter {
        @Override
        public Object convert(String str) {
            if (!IOUtils.isNumber(str)) {
                if (str.length() == 19) {
                    return DateUtils.parseMillis(str, DateUtils.DEFAULT_ZONE_ID);
                }
            }
            return Long.parseLong(str);
        }
    }

    private static class FloatConverter
            extends StringToAnyConverter {
        @Override
        public Object convert(String str) {
            return Float.parseFloat(str);
        }
    }

    private static class DoubleConverter
            extends StringToAnyConverter {
        @Override
        public Object convert(String str) {
            return Double.parseDouble(str);
        }
    }

    private static class CharacterConverter
            extends StringToAnyConverter {
        @Override
        public Object convert(String str) {
            return str.charAt(0);
        }
    }

    private static class BooleanConverter
            extends StringToAnyConverter {
        @Override
        public Object convert(String str) {
            return "true".equals(str);
        }
    }

    private static class BigDecimalConverter
            extends StringToAnyConverter {
        @Override
        public Object convert(String str) {
            return new BigDecimal(str);
        }
    }

    private static class BigIntegerConverter
            extends StringToAnyConverter {
        @Override
        public Object convert(String str) {
            return new BigInteger(str);
        }
    }
}
