package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.TypeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.fastjson2.util.TypeUtils.*;

final class IntegerSchema
        extends JSONSchema {
    final boolean typed;
    final long minimum;
    final boolean exclusiveMinimum;

    final long maximum;
    final boolean exclusiveMaximum;

    final long multipleOf;

    final Long constValue;

    IntegerSchema(JSONObject input) {
        super(input);
        this.typed = "integer".equalsIgnoreCase(input.getString("type")) || input.getBooleanValue("required");

        Object exclusiveMinimum = input.get("exclusiveMinimum");

        long minimum = input.getLongValue("minimum", Long.MIN_VALUE);
        if (exclusiveMinimum == Boolean.TRUE) {
            this.exclusiveMinimum = true;
            this.minimum = minimum;
        } else if (exclusiveMinimum instanceof Number) {
            this.exclusiveMinimum = true;
            this.minimum = input.getLongValue("exclusiveMinimum");
        } else {
            this.minimum = minimum;
            this.exclusiveMinimum = false;
        }

        long maximum = input.getLongValue("maximum", Long.MIN_VALUE);
        Object exclusiveMaximum = input.get("exclusiveMaximum");
        if (exclusiveMaximum == Boolean.TRUE) {
            this.exclusiveMaximum = true;
            this.maximum = maximum;
        } else if (exclusiveMaximum instanceof Number) {
            this.exclusiveMaximum = true;
            this.maximum = input.getLongValue("exclusiveMaximum");
        } else {
            this.exclusiveMaximum = false;
            this.maximum = maximum;
        }

        this.multipleOf = input.getLongValue("multipleOf", 0);

        this.constValue = input.getLong("const");
    }

    @Override
    public Type getType() {
        return Type.Integer;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }

        Class valueClass = value.getClass();
        if (valueClass == Byte.class
                || valueClass == Short.class
                || valueClass == Integer.class
                || valueClass == Long.class
                || valueClass == BigInteger.class
                || valueClass == AtomicInteger.class
                || valueClass == AtomicLong.class
        ) {
            boolean isInt64 = true;
            if (valueClass == BigInteger.class) {
                isInt64 = isInt64((BigInteger) value);
            }

            long longValue = ((Number) value).longValue();

            if (minimum != Long.MIN_VALUE) {
                if (exclusiveMinimum ? longValue <= minimum : longValue < minimum) {
                    return new ValidateResult(false, exclusiveMinimum ? "exclusiveMinimum not match, expect >= %s, but %s" : "minimum not match, expect >= %s, but %s", minimum, value);
                }
            }

            if (maximum != Long.MIN_VALUE) {
                if (exclusiveMaximum ? longValue >= maximum : longValue > maximum) {
                    return new ValidateResult(false, exclusiveMaximum ? "exclusiveMaximum not match, expect >= %s, but %s" : "maximum not match, expect >= %s, but %s", maximum, value);
                }
            }

            if (multipleOf != 0) {
                if (longValue % multipleOf != 0) {
                    return new ValidateResult(false, "multipleOf not match, expect multipleOf %s, but %s", multipleOf, value);
                }
            }

            if (constValue != null) {
                if (this.constValue != longValue || !isInt64) {
                    return new ValidateResult(false, "const not match, expect %s, but %s", this.constValue, value);
                }
            }

            return SUCCESS;
        }

        if (value instanceof BigDecimal) {
            BigDecimal decimal = (BigDecimal) value;
            boolean integer = TypeUtils.isInteger(decimal);
            if (integer) {
                BigInteger unscaleValue = decimal.toBigInteger();
                if (constValue != null) {
                    boolean equals = false;
                    if (isInt64(unscaleValue)) {
                        equals = this.constValue == unscaleValue.longValue();
                    }
                    if (!equals) {
                        return new ValidateResult(false, "const not match, expect %s, but %s", this.constValue, value);
                    }
                }

                return SUCCESS;
            }

            if (constValue != null) {
                return new ValidateResult(false, "const not match, expect %s, but %s", this.constValue, value);
            }
        }

        if (constValue != null) {
            if (value instanceof Float) {
                float floatValue = (Float) value;
                if (this.constValue != floatValue) {
                    return new ValidateResult(false, "const not match, expect %s, but %s", this.constValue, value);
                }
            } else if (value instanceof Double) {
                double doubleValue = (Double) value;
                if (this.constValue != doubleValue) {
                    return new ValidateResult(false, "const not match, expect %s, but %s", this.constValue, value);
                }
            } else if (value instanceof String) {
                String str = (String) value;
                boolean equals = false;
                if (TypeUtils.isInteger(str) && str.length() < 21) {
                    try {
                        long longValue = Long.parseLong(str);
                        equals = constValue == longValue;
                    } catch (NumberFormatException ignored) {
                        // ignored
                    }
                }
                if (!equals) {
                    return new ValidateResult(false, "const not match, expect %s, but %s", this.constValue, value);
                }
            }
        }

        return typed ? new ValidateResult(false, "expect type %s, but %s", Type.Integer, valueClass) : SUCCESS;
    }

    @Override
    public ValidateResult validate(long longValue) {
        if (minimum != Long.MIN_VALUE) {
            if (exclusiveMinimum ? longValue <= minimum : longValue < minimum) {
                return new ValidateResult(false, exclusiveMinimum ? "exclusiveMinimum not match, expect >= %s, but %s" : "minimum not match, expect >= %s, but %s", minimum, longValue);
            }
        }

        if (maximum != Long.MIN_VALUE) {
            if (exclusiveMaximum ? longValue >= maximum : longValue > maximum) {
                return new ValidateResult(false, exclusiveMaximum ? "exclusiveMaximum not match, expect >= %s, but %s" : "maximum not match, expect >= %s, but %s", maximum, longValue);
            }
        }

        if (multipleOf != 0) {
            if (longValue % multipleOf != 0) {
                return new ValidateResult(false, "multipleOf not match, expect multipleOf %s, but %s", multipleOf, longValue);
            }
        }

        if (constValue != null) {
            if (this.constValue != longValue) {
                return new ValidateResult(false, "const not match, expect %s, but %s", this.constValue, longValue);
            }
        }

        return SUCCESS;
    }

    @Override
    public ValidateResult validate(Long value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }

        long longValue = value;
        if (minimum != Long.MIN_VALUE) {
            if (exclusiveMinimum ? longValue <= minimum : longValue < minimum) {
                return new ValidateResult(false, exclusiveMinimum ? "exclusiveMinimum not match, expect >= %s, but %s" : "minimum not match, expect >= %s, but %s", minimum, value);
            }
        }

        if (maximum != Long.MIN_VALUE) {
            if (exclusiveMaximum ? longValue >= maximum : longValue > maximum) {
                return new ValidateResult(false, exclusiveMaximum ? "exclusiveMaximum not match, expect >= %s, but %s" : "maximum not match, expect >= %s, but %s", maximum, value);
            }
        }

        if (multipleOf != 0) {
            if (longValue % multipleOf != 0) {
                return new ValidateResult(false, "multipleOf not match, expect multipleOf %s, but %s", multipleOf, longValue);
            }
        }

        if (constValue != null) {
            if (this.constValue != longValue) {
                return new ValidateResult(false, "const not match, expect %s, but %s", this.constValue, value);
            }
        }
        return SUCCESS;
    }

    @Override
    public ValidateResult validate(Integer value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }

        long longValue = value.longValue();
        if (minimum != Long.MIN_VALUE) {
            if (exclusiveMinimum ? longValue <= minimum : longValue < minimum) {
                return new ValidateResult(false, exclusiveMinimum ? "exclusiveMinimum not match, expect >= %s, but %s" : "minimum not match, expect >= %s, but %s", minimum, value);
            }
        }

        if (maximum != Long.MIN_VALUE) {
            if (exclusiveMaximum ? longValue >= maximum : longValue > maximum) {
                return new ValidateResult(false, exclusiveMaximum ? "exclusiveMaximum not match, expect >= %s, but %s" : "maximum not match, expect >= %s, but %s", maximum, value);
            }
        }

        if (multipleOf != 0) {
            if (longValue % multipleOf != 0) {
                return new ValidateResult(false, "multipleOf not match, expect multipleOf %s, but %s", multipleOf, longValue);
            }
        }

        if (constValue != null) {
            if (this.constValue != longValue) {
                return new ValidateResult(false, "const not match, expect %s, but %s", this.constValue, value);
            }
        }

        return SUCCESS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        com.alibaba.fastjson2.schema.IntegerSchema that = (com.alibaba.fastjson2.schema.IntegerSchema) o;
        return Objects.equals(title, that.title)
                && Objects.equals(description, that.description)
                && Objects.equals(minimum, that.minimum)
                && Objects.equals(exclusiveMinimum, that.exclusiveMinimum)
                && Objects.equals(maximum, that.maximum)
                && Objects.equals(exclusiveMaximum, that.exclusiveMaximum)
                && Objects.equals(multipleOf, that.multipleOf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, minimum, exclusiveMinimum, maximum, exclusiveMaximum, multipleOf);
    }
}
