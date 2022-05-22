package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;

import java.util.Objects;

final class BooleanSchema
        extends JSONSchema {
    BooleanSchema(JSONObject input) {
        super(input);
    }

    @Override
    public Type getType() {
        return Type.Boolean;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            return FAIL_INPUT_NULL;
        }

        if (value instanceof Boolean) {
            return SUCCESS;
        }

        return new ValidateResult(false, "expect type %s, but %s", Type.Boolean, value.getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        com.alibaba.fastjson2.schema.BooleanSchema that = (com.alibaba.fastjson2.schema.BooleanSchema) o;
        return Objects.equals(title, that.title) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description);
    }
}
