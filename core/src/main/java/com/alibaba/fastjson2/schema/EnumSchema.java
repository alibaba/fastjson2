package com.alibaba.fastjson2.schema;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

final class EnumSchema extends JSONSchema {
    Set<Object> items;

    EnumSchema(Object[] items) {
        super(null, null);
        this.items = new LinkedHashSet<>(items.length);
        for (Object item : items) {
            if (item instanceof BigDecimal) {
                item = ((BigDecimal) item).stripTrailingZeros();
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
            value = ((BigDecimal) value).stripTrailingZeros();

            long longValue = ((BigDecimal) value).longValue();
            if (((BigDecimal) value).compareTo(BigDecimal.valueOf(longValue)) == 0) {
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    value = (int) longValue;
                } else {
                    value = longValue;
                }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.alibaba.fastjson2.schema.EnumSchema that = (com.alibaba.fastjson2.schema.EnumSchema) o;
        return Objects.equals(title, that.title) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description);
    }
}
