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
    protected ValidateResult validateInternal(Object value, ValidationHandler handler, String path) {
        if (value == null) {
            ValidateResult result = handleError(handler, null, path, FAIL_INPUT_NULL);
            return result != null ? result : FAIL_INPUT_NULL;
        }

        if (value instanceof Boolean) {
            return SUCCESS;
        }

        ValidateResult result = new ValidateResult(false, "expect type %s, but %s", Type.Boolean, value.getClass());
        ValidateResult r = handleError(handler, value, path, result);
        return r != null ? r : result;
    }

    @Override
    public JSONObject toJSONObject() {
        return JSONObject.of("type", "boolean");
    }
}
