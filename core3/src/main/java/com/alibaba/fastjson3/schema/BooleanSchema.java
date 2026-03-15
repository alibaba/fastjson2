package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONObject;

public final class BooleanSchema extends JSONSchema {
    final boolean typed;

    public BooleanSchema(JSONObject input) {
        super(input);
        this.typed = "boolean".equalsIgnoreCase(input.getString("type"));
    }

    @Override
    public Type getType() {
        return Type.Boolean;
    }

    @Override
    public JSONObject toJSONObject() {
        return objectOf("type", "boolean");
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }
        if (value instanceof Boolean) {
            return SUCCESS;
        }
        return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
    }
}
