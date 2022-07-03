package com.alibaba.fastjson2.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Set;

final class EnumSchema
        extends JSONSchema {
    static final BigInteger BIGINT_INT64_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    static final BigInteger BIGINT_INT64_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    static final BigInteger BIGINT_INT32_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
    static final BigInteger BIGINT_INT32_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    Set<Object> items;

    EnumSchema(Object... items) {
        super(null, null);
        this.items = new LinkedHashSet<>(items.length);
        for (Object item : items) {
            if (item instanceof BigDecimal) {
                BigDecimal decimal = ((BigDecimal) item).stripTrailingZeros();
                if (decimal.scale() == 0) {
                    BigInteger bigInt = decimal.toBigInteger();
                    if (bigInt.compareTo(BIGINT_INT32_MIN) >= 0 && bigInt.compareTo(BIGINT_INT32_MAX) <= 0) {
                        item = bigInt.intValue();
                    } else if (bigInt.compareTo(BIGINT_INT64_MIN) >= 0 && bigInt.compareTo(BIGINT_INT64_MAX) <= 0) {
                        item = bigInt.longValue();
                    } else {
                        item = bigInt;
                    }
                } else {
                    item = decimal;
                }
            }
            this.items.add(item);
        }
    }

    @Override
    public Type getType() {
        return Type.Enum;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value instanceof BigDecimal) {
            BigDecimal decimal = (BigDecimal) value;
            value = decimal.stripTrailingZeros();

            long longValue = decimal.longValue();
            if (decimal.compareTo(BigDecimal.valueOf(longValue)) == 0) {
                value = longValue;
            } else if (decimal.scale() == 0) {
                value = decimal.unscaledValue();
            }
        } else if (value instanceof BigInteger) {
            BigInteger bigInt = (BigInteger) value;
            if (bigInt.compareTo(BIGINT_INT64_MIN) >= 0 && bigInt.compareTo(BIGINT_INT64_MAX) <= 0) {
                value = bigInt.longValue();
            }
        }

        if (value instanceof Long) {
            long longValue = ((Long) value).longValue();
            if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                value = (int) longValue;
            }
        }

        if (!items.contains(value)) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            return new ValidateResult(false, "expect type %s, but %s", Type.Enum, value.getClass());
        }

        return SUCCESS;
    }
}
