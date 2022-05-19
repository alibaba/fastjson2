package com.alibaba.fastjson2.schema;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

final class EnumSchema extends JSONSchema {
    Set<Object> items;

    EnumSchema(Object[] items) {
        super(null, null);
        this.items = new LinkedHashSet<>(items.length);
        for (Object name : items) {
            this.items.add(name);
        }
    }

    @Override
    public Type getType() {
        return Type.Enum;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            return FAIL_INPUT_NULL;
        }

        if (!items.contains(value)) {
            return new ValidateResult.TypeNotMatchFail(Type.Enum, value.getClass());
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
