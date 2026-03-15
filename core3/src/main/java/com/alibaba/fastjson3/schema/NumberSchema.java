package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class NumberSchema extends JSONSchema {
    final BigDecimal minimum;
    final BigDecimal maximum;
    final BigDecimal exclusiveMinimum;
    final BigDecimal exclusiveMaximum;
    final BigDecimal multipleOf;
    final boolean typed;

    public NumberSchema(JSONObject input) {
        super(input);
        this.typed = "number".equalsIgnoreCase(input.getString("type"));
        this.minimum = toBigDecimal(input.get("minimum"));
        this.maximum = toBigDecimal(input.get("maximum"));
        this.exclusiveMinimum = toBigDecimal(input.get("exclusiveMinimum"));
        this.exclusiveMaximum = toBigDecimal(input.get("exclusiveMaximum"));
        this.multipleOf = toBigDecimal(input.get("multipleOf"));
    }

    private static BigDecimal toBigDecimal(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof BigDecimal bd) {
            return bd;
        }
        if (val instanceof BigInteger bi) {
            return new BigDecimal(bi);
        }
        if (val instanceof Long l) {
            return BigDecimal.valueOf(l);
        }
        if (val instanceof Integer i) {
            return BigDecimal.valueOf(i);
        }
        if (val instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return null;
    }

    @Override
    public Type getType() {
        return Type.Number;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = objectOf("type", "number");
        if (minimum != null) {
            obj.put("minimum", minimum);
        }
        if (maximum != null) {
            obj.put("maximum", maximum);
        }
        if (exclusiveMinimum != null) {
            obj.put("exclusiveMinimum", exclusiveMinimum);
        }
        if (exclusiveMaximum != null) {
            obj.put("exclusiveMaximum", exclusiveMaximum);
        }
        if (multipleOf != null) {
            obj.put("multipleOf", multipleOf);
        }
        return obj;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }

        if (!(value instanceof Number)) {
            return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
        }

        // Reject NaN and Infinity
        if (value instanceof Double d && (Double.isNaN(d) || Double.isInfinite(d))) {
            return FAIL_TYPE_NOT_MATCH;
        }
        if (value instanceof Float f && (Float.isNaN(f) || Float.isInfinite(f))) {
            return FAIL_TYPE_NOT_MATCH;
        }

        BigDecimal decimalValue = toBigDecimal(value);
        if (decimalValue == null) {
            return FAIL_TYPE_NOT_MATCH;
        }
        return validateDecimal(decimalValue);
    }

    @Override
    protected ValidateResult validateInternal(long value) {
        return validateDecimal(BigDecimal.valueOf(value));
    }

    @Override
    protected ValidateResult validateInternal(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return FAIL_TYPE_NOT_MATCH;
        }
        return validateDecimal(BigDecimal.valueOf(value));
    }

    private ValidateResult validateDecimal(BigDecimal value) {
        if (minimum != null && value.compareTo(minimum) < 0) {
            return new ValidateResult(false, "minimum not match, expect >= %s, but %s", minimum, value);
        }

        if (maximum != null && value.compareTo(maximum) > 0) {
            return new ValidateResult(false, "maximum not match, expect <= %s, but %s", maximum, value);
        }

        if (exclusiveMinimum != null && value.compareTo(exclusiveMinimum) <= 0) {
            return new ValidateResult(false, "exclusiveMinimum not match, expect > %s, but %s", exclusiveMinimum, value);
        }

        if (exclusiveMaximum != null && value.compareTo(exclusiveMaximum) >= 0) {
            return new ValidateResult(false, "exclusiveMaximum not match, expect < %s, but %s", exclusiveMaximum, value);
        }

        if (multipleOf != null && multipleOf.compareTo(BigDecimal.ZERO) != 0) {
            try {
                BigDecimal[] divideResult = value.divideAndRemainder(multipleOf);
                if (divideResult[1].compareTo(BigDecimal.ZERO) != 0) {
                    return new ValidateResult(false, "multipleOf not match, expect multiple of %s, but %s", multipleOf, value);
                }
            } catch (ArithmeticException e) {
                return new ValidateResult(false, "multipleOf not match, expect multiple of %s, but %s", multipleOf, value);
            }
        }

        return SUCCESS;
    }
}
