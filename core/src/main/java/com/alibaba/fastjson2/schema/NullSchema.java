package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;

import java.util.Objects;

final class NullSchema
        extends JSONSchema {
    NullSchema(JSONObject input) {
        super(input);
    }

    @Override
    public Type getType() {
        return Type.Null;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            return SUCCESS;
        }

        return new ValidateResult(false, "expect type %s, but %s", Type.Null, value.getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        com.alibaba.fastjson2.schema.NullSchema that = (com.alibaba.fastjson2.schema.NullSchema) o;
        return Objects.equals(title, that.title) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description);
    }
}
