package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;

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
    protected ValidateResult validateInternal(Object value, ValidationHandler handler, String path) {
        if (value == null) {
            return SUCCESS;
        }

        ValidateResult result = new ValidateResult(false, "expect type %s, but %s", Type.Null, value.getClass());
        ValidateResult r = handleError(handler, value, path, result);
        return r != null ? r : result;
    }
}
