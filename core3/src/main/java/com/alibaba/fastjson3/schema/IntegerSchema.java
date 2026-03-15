package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class IntegerSchema extends JSONSchema {
    final long minimum;
    final long maximum;
    final boolean exclusiveMinimum;
    final boolean exclusiveMaximum;
    final long multipleOf;
    final BigDecimal multipleOfDecimal; // for fractional multipleOf values
    final Long constValue;
    final boolean typed;
    final boolean hasMinimum;
    final boolean hasMaximum;
    final boolean hasMultipleOf;

    public IntegerSchema(JSONObject input) {
        super(input);
        this.typed = "integer".equalsIgnoreCase(input.getString("type"));

        // Handle minimum - could be exclusive style (draft 2020-12) or boolean style (draft 4)
        Object minObj = input.get("minimum");
        Object exMinObj = input.get("exclusiveMinimum");
        if (exMinObj instanceof Boolean) {
            // Draft 4 style: exclusiveMinimum is boolean, minimum is the value
            this.exclusiveMinimum = (Boolean) exMinObj;
            this.hasMinimum = minObj instanceof Number;
            this.minimum = hasMinimum ? ((Number) minObj).longValue() : Long.MIN_VALUE;
        } else if (exMinObj instanceof Number) {
            // Draft 2020-12 style: exclusiveMinimum is the value itself
            this.exclusiveMinimum = true;
            this.hasMinimum = true;
            this.minimum = ((Number) exMinObj).longValue();
        } else {
            this.exclusiveMinimum = false;
            this.hasMinimum = minObj instanceof Number;
            this.minimum = hasMinimum ? ((Number) minObj).longValue() : Long.MIN_VALUE;
        }

        Object maxObj = input.get("maximum");
        Object exMaxObj = input.get("exclusiveMaximum");
        if (exMaxObj instanceof Boolean) {
            this.exclusiveMaximum = (Boolean) exMaxObj;
            this.hasMaximum = maxObj instanceof Number;
            this.maximum = hasMaximum ? ((Number) maxObj).longValue() : Long.MAX_VALUE;
        } else if (exMaxObj instanceof Number) {
            this.exclusiveMaximum = true;
            this.hasMaximum = true;
            this.maximum = ((Number) exMaxObj).longValue();
        } else {
            this.exclusiveMaximum = false;
            this.hasMaximum = maxObj instanceof Number;
            this.maximum = hasMaximum ? ((Number) maxObj).longValue() : Long.MAX_VALUE;
        }

        Object multipleOfObj = input.get("multipleOf");
        this.hasMultipleOf = multipleOfObj instanceof Number;
        this.multipleOf = hasMultipleOf ? ((Number) multipleOfObj).longValue() : 0;
        if (hasMultipleOf && multipleOfObj instanceof Double || multipleOfObj instanceof Float
                || multipleOfObj instanceof BigDecimal) {
            this.multipleOfDecimal = multipleOfObj instanceof BigDecimal bd ? bd
                    : BigDecimal.valueOf(((Number) multipleOfObj).doubleValue());
        } else {
            this.multipleOfDecimal = null;
        }

        Object constObj = input.get("const");
        if (constObj instanceof Number n) {
            this.constValue = n.longValue();
        } else {
            this.constValue = null;
        }
    }

    @Override
    public Type getType() {
        return Type.Integer;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = objectOf("type", "integer");
        if (hasMinimum) {
            obj.put(exclusiveMinimum ? "exclusiveMinimum" : "minimum", minimum);
        }
        if (hasMaximum) {
            obj.put(exclusiveMaximum ? "exclusiveMaximum" : "maximum", maximum);
        }
        if (hasMultipleOf) {
            obj.put("multipleOf", multipleOf);
        }
        if (constValue != null) {
            obj.put("const", constValue);
        }
        return obj;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }

        long longValue;
        if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
        ) {
            longValue = ((Number) value).longValue();
        } else if (value instanceof BigInteger bi) {
            if (bi.bitLength() > 63) {
                return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
            }
            longValue = bi.longValue();
        } else if (value instanceof BigDecimal bd) {
            // Check if it's actually an integer
            try {
                longValue = bd.longValueExact();
            } catch (ArithmeticException e) {
                return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
            }
        } else if (value instanceof Float f) {
            if (Float.isNaN(f) || Float.isInfinite(f) || f != Math.floor(f)) {
                return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
            }
            longValue = f.longValue();
        } else if (value instanceof Double d) {
            if (Double.isNaN(d) || Double.isInfinite(d) || d != Math.floor(d)) {
                return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
            }
            longValue = d.longValue();
        } else {
            return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
        }

        return validateLong(longValue);
    }

    @Override
    protected ValidateResult validateInternal(long value) {
        return validateLong(value);
    }

    private ValidateResult validateLong(long value) {
        if (constValue != null && value != constValue) {
            return new ValidateResult(false, "const not match, expect %s, but %s", constValue, value);
        }

        if (hasMinimum) {
            if (exclusiveMinimum) {
                if (value <= minimum) {
                    return new ValidateResult(false, "exclusiveMinimum not match, expect > %s, but %s", minimum, value);
                }
            } else {
                if (value < minimum) {
                    return new ValidateResult(false, "minimum not match, expect >= %s, but %s", minimum, value);
                }
            }
        }

        if (hasMaximum) {
            if (exclusiveMaximum) {
                if (value >= maximum) {
                    return new ValidateResult(false, "exclusiveMaximum not match, expect < %s, but %s", maximum, value);
                }
            } else {
                if (value > maximum) {
                    return new ValidateResult(false, "maximum not match, expect <= %s, but %s", maximum, value);
                }
            }
        }

        if (hasMultipleOf) {
            if (multipleOfDecimal != null) {
                // Fractional multipleOf: use BigDecimal arithmetic
                try {
                    BigDecimal[] dr = BigDecimal.valueOf(value).divideAndRemainder(multipleOfDecimal);
                    if (dr[1].compareTo(BigDecimal.ZERO) != 0) {
                        return new ValidateResult(false, "multipleOf not match, expect multiple of %s, but %s", multipleOfDecimal, value);
                    }
                } catch (ArithmeticException e) {
                    return new ValidateResult(false, "multipleOf not match, expect multiple of %s, but %s", multipleOfDecimal, value);
                }
            } else if (multipleOf != 0) {
                if (value % multipleOf != 0) {
                    return new ValidateResult(false, "multipleOf not match, expect multiple of %s, but %s", multipleOf, value);
                }
            }
        }

        return SUCCESS;
    }
}
