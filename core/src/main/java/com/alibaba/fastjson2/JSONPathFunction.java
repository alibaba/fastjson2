package com.alibaba.fastjson2;

import com.alibaba.fastjson2.function.impl.ToDouble;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
}
