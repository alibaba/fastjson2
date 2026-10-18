package com.alibaba.fastjson2.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.alibaba.fastjson2.util.TypeUtils.BIGINT_INT32_MAX;
import static com.alibaba.fastjson2.util.TypeUtils.BIGINT_INT32_MIN;
import static com.alibaba.fastjson2.util.TypeUtils.BIGINT_INT64_MAX;
import static com.alibaba.fastjson2.util.TypeUtils.BIGINT_INT64_MIN;

public final class EnumSchema
        extends JSONSchema {
    final Set<Object> items;

    private static final BigDecimal BIGDECIMAL_INT64_MIN = new BigDecimal(BIGINT_INT64_MIN);
    private static final BigDecimal BIGDECIMAL_INT64_MAX = new BigDecimal(BIGINT_INT64_MAX);
    private static final int INT64_MAX_DIGITS = BIGINT_INT64_MAX.toString().length();

    EnumSchema(Object... items) {
        super(null, null);
        this.items = new LinkedHashSet<>(items.length);
        for (Object item : items) {
            this.items.add(canonicalize(item));
        }
    }

    @Override
    public Type getType() {
        return Type.Enum;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        if (items.contains(value) || items.contains(canonicalize(value))) {
            return SUCCESS;
        }

        if (value == null) {
            return FAIL_INPUT_NULL;
        }

        return new ValidateResult(false, "expect type %s, but %s", Type.Enum, value.getClass());
    }

    /**
     * Canonical key of a numeric value, so that enum members and instances of different
     * numeric types are compared by mathematical value: integral values become Integer or
     * Long when they fit, anything larger stays a compact stripped BigDecimal, other values
     * stay stripped BigDecimal. Double and Float are widened to double first and then follow
     * the same pipeline; non-finite values are left untouched.
     * <p>
     * 数值的规范化键：不同类型的 enum 成员与实例按数学数值比较。整数在能容纳时归一为
     * Integer 或 Long，更大者与其他值一样保留 stripped BigDecimal。Double 与 Float 先
     * 扩展为 double 再走同一条流水线；非有限值保持原样。
     */
    static Object canonicalize(Object value) {
        if (value instanceof BigDecimal) {
            BigDecimal decimal = ((BigDecimal) value).stripTrailingZeros();
            if (decimal.signum() == 0) {
                return 0;
            }
            if (decimal.scale() <= 0 && fitsInt64(decimal)) {
                return canonicalizeInteger(decimal.toBigIntegerExact());
            }
            return decimal;
        }

        if (value instanceof BigInteger) {
            return canonicalizeInteger((BigInteger) value);
        }

        if (value instanceof Long) {
            long longValue = (Long) value;
            if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                return (int) longValue;
            }
            return value;
        }

        if (value instanceof Double) {
            double doubleValue = (Double) value;
            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                return value;
            }
            return canonicalize(BigDecimal.valueOf(doubleValue));
        }

        if (value instanceof Float) {
            float floatValue = (Float) value;
            if (Float.isNaN(floatValue) || Float.isInfinite(floatValue)) {
                return value;
            }
            return canonicalize(BigDecimal.valueOf(floatValue));
        }

        return value;
    }

    static Object canonicalizeInteger(BigInteger value) {
        if (value.compareTo(BIGINT_INT32_MIN) >= 0 && value.compareTo(BIGINT_INT32_MAX) <= 0) {
            return value.intValue();
        }
        if (value.compareTo(BIGINT_INT64_MIN) >= 0 && value.compareTo(BIGINT_INT64_MAX) <= 0) {
            return value.longValue();
        }
        return new BigDecimal(value).stripTrailingZeros();
    }

    /**
     * Whether a stripped decimal with a non-positive scale fits in a long. Checking the digit count
     * first avoids expanding a large exponent into an integer.
     * <p>
     * <details><summary>中文</summary>
     * 判断非正 scale 的 stripped decimal 能否放进 long；先检查位数，避免将巨大指数展开为整数。
     * </details>
     */
    static boolean fitsInt64(BigDecimal decimal) {
        if ((long) decimal.precision() - decimal.scale() > INT64_MAX_DIGITS) {
            return false;
        }
        return decimal.compareTo(BIGDECIMAL_INT64_MIN) >= 0
                && decimal.compareTo(BIGDECIMAL_INT64_MAX) <= 0;
    }
}
