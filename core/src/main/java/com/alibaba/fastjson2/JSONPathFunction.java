package com.alibaba.fastjson2;

import com.alibaba.fastjson2.function.impl.ToDouble;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

final class JSONPathFunction
        extends JSONPathSegment
        implements JSONPathSegment.EvalSegment {
    static JSONPathFunction FUNC_TYPE = new JSONPathFunction(JSONPathFunction::type);
    static JSONPathFunction FUNC_DOUBLE = new JSONPathFunction(new ToDouble(null));
    static JSONPathFunction FUNC_FLOOR = new JSONPathFunction(JSONPathFunction::floor);
    static JSONPathFunction FUNC_CEIL = new JSONPathFunction(JSONPathFunction::ceil);
    static JSONPathFunction FUNC_ABS = new JSONPathFunction(JSONPathFunction::abs);
    static JSONPathFunction FUNC_NEGATIVE = new JSONPathFunction(JSONPathFunction::negative);
    static JSONPathFunction FUNC_EXISTS = new JSONPathFunction(JSONPathFunction::exists);
    static JSONPathFunction FUNC_LOWER = new JSONPathFunction(JSONPathFunction::lower);
    static JSONPathFunction FUNC_UPPER = new JSONPathFunction(JSONPathFunction::upper);
    static JSONPathFunction FUNC_TRIM = new JSONPathFunction(JSONPathFunction::trim);
    static JSONPathFunction FUNC_FIRST = new JSONPathFunction(JSONPathFunction::first);
    static JSONPathFunction FUNC_LAST = new JSONPathFunction(JSONPathFunction::last);

    final Function function;

    public JSONPathFunction(Function function) {
        this.function = function;
    }

    static Object floor(Object value) {
        if (value instanceof Double) {
            return Math.floor((Double) value);
        }

        if (value instanceof Float) {
            return Math.floor((Float) value);
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(0, RoundingMode.FLOOR);
        }

        if (value instanceof List) {
            List list = (List) value;
            for (int i = 0, l = list.size(); i < l; i++) {
                Object item = list.get(i);
                if (item instanceof Double) {
                    list.set(i, Math.floor((Double) item));
                } else if (item instanceof Float) {
                    list.set(i, Math.floor((Float) item));
                } else if (item instanceof BigDecimal) {
                    list.set(i, ((BigDecimal) item).setScale(0, RoundingMode.FLOOR));
                }
            }
        }

        return value;
    }

    static Object ceil(Object value) {
        if (value instanceof Double) {
            return Math.ceil((Double) value);
        }

        if (value instanceof Float) {
            return Math.ceil((Float) value);
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(0, RoundingMode.CEILING);
        }

        if (value instanceof List) {
            List list = (List) value;
            for (int i = 0, l = list.size(); i < l; i++) {
                Object item = list.get(i);
                if (item instanceof Double) {
                    list.set(i, Math.ceil((Double) item));
                } else if (item instanceof Float) {
                    list.set(i, Math.ceil((Float) item));
                } else if (item instanceof BigDecimal) {
                    list.set(i, ((BigDecimal) item).setScale(0, RoundingMode.CEILING));
                }
            }
        }

        return value;
    }

    static Object exists(Object value) {
        return value != null;
    }

    static Object negative(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            int intValue = ((Integer) value).intValue();
            if (intValue == Integer.MIN_VALUE) {
                return -(long) intValue;
            }
            return -intValue;
        }

        if (value instanceof Long) {
            long longValue = ((Long) value).longValue();
            if (longValue == Long.MIN_VALUE) {
                return BigInteger.valueOf(longValue).negate();
            }
            return -longValue;
        }

        if (value instanceof Byte) {
            byte byteValue = ((Byte) value).byteValue();
            if (byteValue == Byte.MIN_VALUE) {
                return -(short) byteValue;
            }

            return (byte) -byteValue;
        }

        if (value instanceof Short) {
            short shortValue = ((Short) value).shortValue();
            if (shortValue == Short.MIN_VALUE) {
                return -(int) shortValue;
            }

            return (short) -shortValue;
        }

        if (value instanceof Double) {
            return -((Double) value).doubleValue();
        }

        if (value instanceof Float) {
            return -((Float) value).floatValue();
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).negate();
        }

        if (value instanceof BigInteger) {
            return ((BigInteger) value).negate();
        }

        if (value instanceof List) {
            List list = (List) value;
            JSONArray values = new JSONArray(list.size());
            for (int i = 0, l = list.size(); i < l; i++) {
                Object item = list.get(i);
                Object negativeItem = negative(item);
                values.add(negativeItem);
            }
            return values;
        }

        return value;
    }

    static Object first(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof JSONPath.Sequence) {
            value = ((JSONPath.Sequence) value).values;
        }

        if (value instanceof List) {
            if (((List<?>) value).isEmpty()) {
                return null;
            }

            return ((List<?>) value).get(0);
        }

        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                return null;
            }

            return collection.iterator().next();
        }

        if (value.getClass().isArray()) {
            int len = Array.getLength(value);
            if (len == 0) {
                return null;
            }
            return Array.get(value, 0);
        }

        return value;
    }

    static Object last(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof JSONPath.Sequence) {
            value = ((JSONPath.Sequence) value).values;
        }

        if (value instanceof List) {
            List list = (List) value;
            int size = list.size();
            if (size == 0) {
                return null;
            }
            return list.get(size - 1);
        }

        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            if (collection.isEmpty()) {
                return null;
            }

            Object last = null;
            for (Iterator<?> it = collection.iterator(); it.hasNext(); ) {
                last = it.next();
            }
            return last;
        }

        if (value.getClass().isArray()) {
            int len = Array.getLength(value);
            if (len == 0) {
                return null;
            }
            return Array.get(value, len - 1);
        }

        return value;
    }

    static Object abs(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            int intValue = ((Integer) value).intValue();
            if (intValue < 0) {
                return -intValue;
            }
            return value;
        }

        if (value instanceof Long) {
            long longValue = ((Long) value).longValue();
            if (longValue < 0) {
                return -longValue;
            }
            return value;
        }

        if (value instanceof Byte) {
            byte byteValue = ((Byte) value).byteValue();
            if (byteValue < 0) {
                return (byte) -byteValue;
            }

            return value;
        }

        if (value instanceof Short) {
            short shortValue = ((Short) value).shortValue();
            if (shortValue < 0) {
                return (short) -shortValue;
            }

            return value;
        }

        if (value instanceof Double) {
            double doubleValue = ((Double) value).doubleValue();
            if (doubleValue < 0) {
                return -doubleValue;
            }

            return value;
        }

        if (value instanceof Float) {
            float floatValue = ((Float) value).floatValue();
            if (floatValue < 0) {
                return -floatValue;
            }

            return value;
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).abs();
        }

        if (value instanceof BigInteger) {
            return ((BigInteger) value).abs();
        }

        if (value instanceof List) {
            List list = (List) value;
            JSONArray values = new JSONArray(list.size());
            for (int i = 0, l = list.size(); i < l; i++) {
                Object item = list.get(i);
                values.add(abs(item));
            }
            return values;
        }

        throw new JSONException("abs not support " + value);
    }

    static String type(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Collection) {
            return "array";
        }

        if (value instanceof Number) {
            return "number";
        }

        if (value instanceof Boolean) {
            return "boolean";
        }

        if (value instanceof String
                || value instanceof UUID
                || value instanceof Enum) {
            return "string";
        }

        return "object";
    }

    static Object lower(Object value) {
        if (value == null) {
            return null;
        }
        String str;
        if (value instanceof String) {
            str = (String) value;
        } else {
            str = value.toString();
        }
        return str.toLowerCase();
    }

    static Object upper(Object value) {
        if (value == null) {
            return null;
        }
        String str;
        if (value instanceof String) {
            str = (String) value;
        } else {
            str = value.toString();
        }
        return str.toUpperCase();
    }

    static Object trim(Object value) {
        if (value == null) {
            return null;
        }
        String str;
        if (value instanceof String) {
            str = (String) value;
        } else {
            str = value.toString();
        }
        return str.trim();
    }

    @Override
    public void accept(JSONReader jsonReader, JSONPath.Context context) {
        if (context.parent == null) {
            context.root = jsonReader.readAny();
            context.eval = true;
        }
        eval(context);
    }

    @Override
    public void eval(JSONPath.Context context) {
        Object value = context.parent == null
                ? context.root
                : context.parent.value;

        context.value = function.apply(value);
    }

    static final class TypeFunction
            implements Function {
        static final TypeFunction INSTANCE = new TypeFunction();

        @Override
        public Object apply(Object object) {
            return type(object);
        }
    }

    static final class SizeFunction
            implements Function {
        static final SizeFunction INSTANCE = new SizeFunction();

        @Override
        public Object apply(Object value) {
            if (value == null) {
                return -1;
            }

            if (value instanceof Collection) {
                return ((Collection<?>) value).size();
            }

            if (value.getClass().isArray()) {
                return Array.getLength(value);
            }

            if (value instanceof Map) {
                return ((Map) value).size();
            }

            if (value instanceof JSONPath.Sequence) {
                return ((JSONPath.Sequence) value).values.size();
            }

            return 1;
        }
    }

    static final class BiFunctionAdapter
            implements BiFunction {
        private final Function function;

        BiFunctionAdapter(Function function) {
            this.function = function;
        }

        @Override
        public Object apply(Object o1, Object o2) {
            return function.apply(o2);
        }
    }

    abstract static class Index
            implements Function {
        protected abstract boolean eq(Object item);

        @Override
        public final Object apply(Object o) {
            if (o == null) {
                return null;
            }

            if (o instanceof List) {
                List list = (List) o;
                for (int i = 0; i < list.size(); i++) {
                    if (eq(list.get(i))) {
                        return i;
                    }
                }
                return -1;
            }

            if (o.getClass().isArray()) {
                int len = Array.getLength(o);
                for (int i = 0; i < len; i++) {
                    Object item = Array.get(o, i);
                    if (eq(item)) {
                        return i;
                    }
                }
                return -1;
            }

            if (eq(o)) {
                return 0;
            }

            return null;
        }
    }

    static final class IndexInt
            extends Index {
        final long value;
        transient BigDecimal decimalValue;

        public IndexInt(long value) {
            this.value = value;
        }

        protected boolean eq(Object item) {
            if (item instanceof Integer
                    || item instanceof Long
                    || item instanceof Byte
                    || item instanceof Short
            ) {
                if (((Number) item).longValue() == value) {
                    return true;
                }
            } else if (item instanceof Float || item instanceof Double) {
                double doubleValue = ((Number) item).doubleValue();
                if (doubleValue == value) {
                    return true;
                }
            } else if (item instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) item;
                decimal = decimal.stripTrailingZeros();
                if (decimalValue == null) {
                    decimalValue = BigDecimal.valueOf(value);
                }
                if (decimalValue.equals(decimal)) {
                    return true;
                }
            }
            return false;
        }
    }

    static final class IndexDecimal
            extends Index {
        final BigDecimal value;

        public IndexDecimal(BigDecimal value) {
            this.value = value;
        }

        @Override
        protected boolean eq(Object item) {
            if (item == null) {
                return false;
            }

            if (item instanceof BigDecimal) {
                BigDecimal decimal = (BigDecimal) item;
                decimal = decimal.stripTrailingZeros();
                return value.equals(decimal);
            }

            if (item instanceof Float || item instanceof Double) {
                double doubleValue = ((Number) item).doubleValue();
                BigDecimal decimal = new BigDecimal(doubleValue).stripTrailingZeros();
                return value.equals(decimal);
            }

            if (item instanceof String) {
                String str = (String) item;
                if (TypeUtils.isNumber(str)) {
                    BigDecimal decimal = new BigDecimal(str).stripTrailingZeros();
                    return value.equals(decimal);
                }
            }

            return false;
        }
    }

    static final class IndexString
            extends Index {
        final String value;

        public IndexString(String value) {
            this.value = value;
        }

        @Override
        protected boolean eq(Object item) {
            if (item == null) {
                return false;
            }
            return value.equals(item.toString());
        }
    }
}
