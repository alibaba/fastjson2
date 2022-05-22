package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

final class NumberSchema
        extends JSONSchema {
    final BigDecimal minimum;
    final long minimumLongValue;
    final boolean exclusiveMinimum;

    final BigDecimal maximum;
    final long maximumLongValue;
    final boolean exclusiveMaximum;

    final BigDecimal multipleOf;
    final long multipleOfLongValue;
    final boolean typed;

    NumberSchema(JSONObject input) {
        super(input);

        this.typed = "number".equals(input.get("type"));

        Object exclusiveMinimum = input.get("exclusiveMinimum");
        BigDecimal minimum = input.getBigDecimal("minimum");
        if (exclusiveMinimum == Boolean.TRUE) {
            this.minimum = minimum;
            this.exclusiveMinimum = true;
        } else if (exclusiveMinimum instanceof Number) {
            this.minimum = input.getBigDecimal("exclusiveMinimum");
            this.exclusiveMinimum = true;
        } else {
            this.minimum = minimum;
            this.exclusiveMinimum = false;
        }

        if (this.minimum == null || !this.minimum.equals(BigDecimal.valueOf(this.minimum.longValue()))) {
            minimumLongValue = Long.MIN_VALUE;
        } else {
            minimumLongValue = this.minimum.longValue();
        }

        BigDecimal maximum = input.getBigDecimal("maximum");
        Object exclusiveMaximum = input.get("exclusiveMaximum");
        if (exclusiveMaximum == Boolean.TRUE) {
            this.maximum = maximum;
            this.exclusiveMaximum = true;
        } else if (exclusiveMaximum instanceof Number) {
            this.maximum = input.getBigDecimal("exclusiveMaximum");
            this.exclusiveMaximum = true;
        } else {
            this.maximum = maximum;
            this.exclusiveMaximum = false;
        }

        if (this.maximum == null || !this.maximum.equals(BigDecimal.valueOf(this.maximum.longValue()))) {
            maximumLongValue = Long.MIN_VALUE;
        } else {
            maximumLongValue = this.maximum.longValue();
        }

        this.multipleOf = input.getBigDecimal("multipleOf");
        if (this.multipleOf == null) {
            this.multipleOfLongValue = Long.MIN_VALUE;
        } else {
            long longValue = multipleOf.longValue();
            if (multipleOf.equals(BigDecimal.valueOf(longValue))) {
                this.multipleOfLongValue = longValue;
            } else {
                this.multipleOfLongValue = Long.MIN_VALUE;
            }
        }
    }

    @Override
    public Type getType() {
        return Type.Number;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }

        if (value instanceof Number) {
            Number number = (Number) value;

            if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long) {
                return validate(number.longValue());
            }

            if (number instanceof Float || number instanceof Double) {
                return validate(number.doubleValue());
            }

            BigDecimal decimalValue;
            if (number instanceof BigInteger) {
                decimalValue = new BigDecimal((BigInteger) number);
            } else if (number instanceof BigDecimal) {
                decimalValue = (BigDecimal) number;
            } else {
                return new ValidateResult(false, "expect type %s, but %s", Type.Number, value.getClass());
            }

            if (minimum != null) {
                if (exclusiveMinimum
                        ? minimum.compareTo(decimalValue) >= 0
                        : minimum.compareTo(decimalValue) > 0) {
                    return new ValidateResult(false, exclusiveMinimum ? "exclusiveMinimum not match, expect >= %s, but %s" : "minimum not match, expect >= %s, but %s", minimum, value);
                }
            }

            if (maximum != null) {
                if (exclusiveMaximum
                        ? maximum.compareTo(decimalValue) <= 0
                        : maximum.compareTo(decimalValue) < 0) {
                    return new ValidateResult(false, exclusiveMaximum ? "exclusiveMaximum not match, expect >= %s, but %s" : "maximum not match, expect >= %s, but %s", maximum, value);
                }
            }

            if (multipleOf != null) {
                if (decimalValue.divideAndRemainder(multipleOf)[1].abs().compareTo(BigDecimal.ZERO) > 0) {
                    return new ValidateResult(false, "multipleOf not match, expect multipleOf %s, but %s", multipleOf, decimalValue);
                }
            }

            return SUCCESS;
        }

        return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
    }

    @Override
    public ValidateResult validate(Integer value) {
        if (value == null) {
            return SUCCESS;
        }

        return validate(value.longValue());
    }

    @Override
    public ValidateResult validate(Float value) {
        if (value == null) {
            return SUCCESS;
        }

        return validate(value.doubleValue());
    }

    @Override
    public ValidateResult validate(Double value) {
        if (value == null) {
            return SUCCESS;
        }

        return validate(value.doubleValue());
    }

    @Override
    public ValidateResult validate(Long value) {
        if (value == null) {
            return SUCCESS;
        }

        return validate(value.longValue());
    }

    @Override
    public ValidateResult validate(long value) {
        BigDecimal decimalValue = null;

        if (minimum != null) {
            if (minimumLongValue != Long.MIN_VALUE) {
                if (exclusiveMinimum ? value <= minimumLongValue : value < minimumLongValue) {
                    return new ValidateResult(false, exclusiveMinimum ? "exclusiveMinimum not match, expect >= %s, but %s" : "minimum not match, expect >= %s, but %s", minimum, value);
                }
            } else {
                if (decimalValue == null) {
                    decimalValue = BigDecimal.valueOf(value);
                }
                if (exclusiveMinimum
                        ? minimum.compareTo(decimalValue) >= 0
                        : minimum.compareTo(decimalValue) > 0) {
                    return new ValidateResult(false, exclusiveMinimum ? "exclusiveMinimum not match, expect >= %s, but %s" : "minimum not match, expect >= %s, but %s", minimum, value);
                }
            }
        }

        if (maximum != null) {
            if (maximumLongValue != Long.MIN_VALUE) {
                if (exclusiveMaximum ? value >= maximumLongValue : value > maximumLongValue) {
                    return new ValidateResult(false, exclusiveMaximum ? "exclusiveMaximum not match, expect >= %s, but %s" : "maximum not match, expect >= %s, but %s", maximum, value);
                }
            } else {
                if (decimalValue == null) {
                    decimalValue = BigDecimal.valueOf(value);
                }

                if (exclusiveMaximum
                        ? maximum.compareTo(decimalValue) <= 0
                        : maximum.compareTo(decimalValue) < 0) {
                    return new ValidateResult(false, exclusiveMaximum ? "exclusiveMaximum not match, expect >= %s, but %s" : "maximum not match, expect >= %s, but %s", maximum, value);
                }
            }
        }

        if (multipleOf != null) {
            if (multipleOfLongValue != Long.MIN_VALUE) {
                if (value % multipleOfLongValue != 0) {
                    return new ValidateResult(false, "multipleOf not match, expect multipleOf %s, but %s", multipleOf, decimalValue);
                }
            }

            if (decimalValue == null) {
                decimalValue = BigDecimal.valueOf(value);
            }

            if (decimalValue.divideAndRemainder(multipleOf)[1].abs().compareTo(BigDecimal.ZERO) > 0) {
                return new ValidateResult(false, "multipleOf not match, expect multipleOf %s, but %s", multipleOf, value);
            }
        }

        return SUCCESS;
    }

    @Override
    public ValidateResult validate(double value) {
        if (minimum != null) {
            if (minimumLongValue != Long.MIN_VALUE) {
                if (exclusiveMinimum ? value <= minimumLongValue : value < minimumLongValue) {
                    return new ValidateResult(false, exclusiveMinimum ? "exclusiveMinimum not match, expect >= %s, but %s" : "minimum not match, expect >= %s, but %s", minimum, value);
                }
            } else {
                double minimumDoubleValue = minimum.doubleValue();
                if (exclusiveMinimum ? value <= minimumDoubleValue : value < minimumDoubleValue) {
                    return new ValidateResult(false, exclusiveMinimum ? "exclusiveMinimum not match, expect >= %s, but %s" : "minimum not match, expect >= %s, but %s", minimum, value);
                }
            }
        }

        if (maximum != null) {
            if (maximumLongValue != Long.MIN_VALUE) {
                if (exclusiveMaximum ? value >= maximumLongValue : value > maximumLongValue) {
                    return new ValidateResult(false, exclusiveMaximum ? "exclusiveMaximum not match, expect >= %s, but %s" : "maximum not match, expect >= %s, but %s", maximum, value);
                }
            } else {
                double maximumDoubleValue = maximum.doubleValue();
                if (exclusiveMaximum ? value >= maximumDoubleValue : value > maximumDoubleValue) {
                    return new ValidateResult(false, exclusiveMaximum ? "exclusiveMaximum not match, expect >= %s, but %s" : "maximum not match, expect >= %s, but %s", maximum, value);
                }
            }
        }

        if (multipleOf != null) {
            if (multipleOfLongValue != Long.MIN_VALUE) {
                if (value % multipleOfLongValue != 0) {
                    return new ValidateResult(false, "multipleOf not match, expect multipleOf %s, but %s", multipleOf, value);
                }
            }

            BigDecimal decimalValue = BigDecimal.valueOf(value);
            if (decimalValue.divideAndRemainder(multipleOf)[1].abs().compareTo(BigDecimal.ZERO) > 0) {
                return new ValidateResult(false, "multipleOf not match, expect multipleOf %s, but %s", multipleOf, decimalValue);
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
        com.alibaba.fastjson2.schema.NumberSchema that = (com.alibaba.fastjson2.schema.NumberSchema) o;
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
