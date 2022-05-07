package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;

import java.math.BigDecimal;
import java.util.function.Function;

public class TypeConverts {
    public static final Function<Number, BigDecimal> NUMBER_TO_DECIMAL = o -> o == null ? null : BigDecimal.valueOf(o.longValue());
    public static final Function<Number, Integer> NUMBER_TO_INTEGER = o -> o == null ? null : o.intValue();
    public static final Function<Number, Integer> NUMBER_TO_INTEGER_VALUE = o -> o == null ? 0 : ((Number) o).intValue();
    public static final Function<Number, Long> NUMBER_TO_LONG = o -> o == null ? 0 : o.longValue();
    public static final Function<String, Integer> STRING_TO_INTEGER = o -> o == null || "null".equals(o) || "".equals(o) ? null : Integer.parseInt(o);
    public static final Function<String, BigDecimal> STRING_TO_DECIMAL = o -> o == null || "null".equals(o) ? null : new BigDecimal(o);
    public static final Function<BigDecimal, Integer> DECIMAL_TO_INTEGER = o -> o == null ? null : ((BigDecimal) o).intValueExact();
    public static final Function<String, Boolean> STRING_TO_BOOLEAN_VALUE = o -> o == null || "null".equals(o) || "".equals(o) ? false : o.equals("true");
    public static final Function<String, Long> STRING_TO_LONG = o -> o == null || "null".equals(o) || "".equals(o) ? null : Long.parseLong(o);
    public static final Function<String, Long> STRING_TO_LONG_VALUE = o -> o == null || "null".equals(o) || "".equals(o) ? 0 : Long.parseLong(o);

    static class StringToShort implements Function {
        private Short defaultValue;

        public StringToShort(Short defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object apply(Object from) {
            String str = (String) from;
            return str == null || "null".equals(str) || "".equals(str)
                    ? defaultValue
                    : Short.parseShort(str);
        }
    }

    static class StringToNumber implements Function {
        final Number defaultValue;
        final Class numberClass;

        public StringToNumber(Class numberClass, Number defaultValue) {
            this.numberClass = numberClass;
            this.defaultValue = defaultValue;
        }

        @Override
        public Object apply(Object from) {
            String str = (String) from;
            if (str == null || "null".equals(str) || "".equals(str)) {
                return defaultValue;
            }

            if (numberClass == short.class || numberClass == Short.class) {
                return Short.parseShort(str);
            }

            if (numberClass == float.class || numberClass == Float.class) {
                return Float.parseFloat(str);
            }

            if (numberClass == double.class || numberClass == Double.class) {
                return Double.parseDouble(str);
            }

            throw new JSONException("can not convert to " + numberClass + ", value : " + str);
        }
    }
}
