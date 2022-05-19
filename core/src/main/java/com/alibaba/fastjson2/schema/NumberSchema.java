package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

final class NumberSchema extends JSONSchema {
    final BigDecimal minimum;
    final long minimumLongValue;
    final boolean exclusiveMinimum;

    final BigDecimal maximum;
    final long maximumLongValue;
    final boolean exclusiveMaximum;

    final BigInteger multipleOf;

    NumberSchema(JSONObject input) {
        super(input);

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

        this.multipleOf = input.getBigInteger("multipleOf");
    }

    @Override
    public Type getType() {
        return Type.Number;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            return FAIL_INPUT_NULL;
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
                return new ValidateResult.TypeNotMatchFail(Type.Number, value.getClass());
            }

            if (minimum != null) {
                if (exclusiveMinimum
                        ? minimum.compareTo(decimalValue) >= 0
                        : minimum.compareTo(decimalValue) > 0) {
                    return new ValidateResult.MinimumFail(minimum, decimalValue, exclusiveMinimum);
                }
            }

            if (maximum != null) {
                if (exclusiveMaximum
                        ? maximum.compareTo(decimalValue) <= 0
                        : maximum.compareTo(decimalValue) < 0) {
                    return new ValidateResult.MaximumFail(maximum, decimalValue, exclusiveMaximum);
                }
            }

            if (multipleOf != null) {
                BigInteger bigInteger = decimalValue.toBigInteger();
                if (!decimalValue.equals(new BigDecimal(bigInteger)) || !bigInteger.mod(multipleOf).equals(BigInteger.ZERO)) {
                    return new ValidateResult.MultipleOfFail(multipleOf, decimalValue);
                }
            }
            return SUCCESS;
        }

        return new ValidateResult.TypeNotMatchFail(Type.Number, value.getClass());
    }

    @Override
    public ValidateResult validate(Integer value) {
        if (value == null) {
            return FAIL_INPUT_NULL;
        }

        return validate(value.longValue());
    }

    public ValidateResult validate(Float value) {
        if (value == null) {
            return FAIL_INPUT_NULL;
        }

        return validate(value.doubleValue());
    }

    public ValidateResult validate(Double value) {
        if (value == null) {
            return FAIL_INPUT_NULL;
        }

        return validate(value.doubleValue());
    }

    @Override
    public ValidateResult validate(Long value) {
        if (value == null) {
            return FAIL_INPUT_NULL;
        }

        return validate(value.longValue());
    }

    @Override
    public ValidateResult validate(long value) {
        BigDecimal decimalValue = BigDecimal.valueOf(value);

        if (minimum != null) {
            if (minimumLongValue != Long.MIN_VALUE) {
                if (exclusiveMinimum ? value <= minimumLongValue : value < minimumLongValue) {
                    return new ValidateResult.MinimumFail(minimum, decimalValue, exclusiveMinimum);
                }
            } else {
                if (exclusiveMinimum
                        ? minimum.compareTo(decimalValue) >= 0
                        : minimum.compareTo(decimalValue) > 0) {
                    return new ValidateResult.MinimumFail(minimum, decimalValue, exclusiveMaximum);
                }
            }
        }

        if (maximum != null) {
            if (maximumLongValue != Long.MIN_VALUE) {
                if (exclusiveMaximum ? value >= maximumLongValue : value > maximumLongValue) {
                    return new ValidateResult.MaximumFail(maximum, minimum, exclusiveMinimum);
                }
            } else if (exclusiveMaximum
                    ? maximum.compareTo(decimalValue) <= 0
                    : maximum.compareTo(decimalValue) < 0) {
                return new ValidateResult.MaximumFail(maximum, minimum, exclusiveMaximum);
            }
        }

        if (multipleOf != null) {
            BigInteger bigInteger = decimalValue.toBigInteger();
            if (!decimalValue.equals(new BigDecimal(bigInteger)) || !bigInteger.mod(multipleOf).equals(BigInteger.ZERO)) {
                return new ValidateResult.MultipleOfFail(multipleOf, decimalValue);
            }
        }
        return SUCCESS;
    }

    @Override
    public ValidateResult validate(double value) {

        if (minimum != null) {
            if (minimumLongValue != Long.MIN_VALUE) {
                if (exclusiveMinimum ? value <= minimumLongValue : value < minimumLongValue) {
                    return new ValidateResult.MinimumFail(minimum, value, exclusiveMinimum);
                }
            } else {
                double minimumDoubleValue = minimum.doubleValue();
                if (exclusiveMinimum ? value <= minimumDoubleValue : value < minimumDoubleValue) {
                    return new ValidateResult.MinimumFail(minimum, value, exclusiveMinimum);
                }
            }
        }

        if (maximum != null) {
            if (maximumLongValue != Long.MIN_VALUE) {
                if (exclusiveMaximum ? value >= maximumLongValue : value > maximumLongValue) {
                    return new ValidateResult.MaximumFail(maximum, value, exclusiveMinimum);
                }
            } else {
                double maximumDoubleValue = maximum.doubleValue();
                if (exclusiveMaximum ? value >= maximumDoubleValue : value > maximumDoubleValue) {
                    return new ValidateResult.MaximumFail(maximum, value, exclusiveMinimum);
                }
            }
        }

        if (multipleOf != null) {
            long multipleOfLongValue = multipleOf.longValue();
            if (value % multipleOfLongValue != 0) {
                return new ValidateResult.MultipleOfFail(multipleOf, value);
            }
        }
        return SUCCESS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.alibaba.fastjson2.schema.NumberSchema that = (com.alibaba.fastjson2.schema.NumberSchema) o;
        return Objects.equals(title, that.title)
                && Objects.equals(description, that.description)
                && Objects.equals(minimum, that.minimum)
                && Objects.equals(exclusiveMinimum, that.exclusiveMinimum)
                && Objects.equals(maximum, that.maximum)
                && Objects.equals(exclusiveMaximum, that.exclusiveMaximum)
                && Objects.equals(multipleOf, that.multipleOf)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, minimum, exclusiveMinimum, maximum, exclusiveMaximum, multipleOf);
    }
}
