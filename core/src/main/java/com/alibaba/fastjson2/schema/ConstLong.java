package com.alibaba.fastjson2.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

final class ConstLong
        extends JSONSchema {
    final long value;

    ConstLong(long value) {
        super(null, null);
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.Const;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            return SUCCESS;
        }

        if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof BigInteger
                || value instanceof AtomicInteger
                || value instanceof AtomicLong
        ) {
            if (this.value != ((Number) value).longValue()) {
                return new ValidateResult(false, "const not match, expect %s, but %s", this.value, value);
            }
        } else if (value instanceof BigDecimal) {
            BigDecimal decimal = (BigDecimal) value;
            if (decimal.compareTo(BigDecimal.valueOf(this.value)) != 0) {
                return new ValidateResult(false, "const not match, expect %s, but %s", this.value, value);
            }
        } else if (value instanceof Float) {
            float floatValue = ((Float) value).floatValue();
            if (this.value != floatValue) {
                return new ValidateResult(false, "const not match, expect %s, but %s", this.value, value);
            }
        } else if (value instanceof Double) {
            double doubleValue = ((Double) value).doubleValue();
            if (this.value != doubleValue) {
                return new ValidateResult(false, "const not match, expect %s, but %s", this.value, value);
            }
        } else {
            return new ValidateResult(false, "const not match, expect %s, but %s", this.value, value);
        }

        return SUCCESS;
    }
}
