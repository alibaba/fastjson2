package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;

public final class BooleanSchema
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
    public JSONObject toJSONObject() {
        return JSONObject.of("type", "boolean");
    }
}
