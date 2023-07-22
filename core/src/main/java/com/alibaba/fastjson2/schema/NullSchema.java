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
    public ValidateResult validate(Object value) {
        if (value == null) {
            return SUCCESS;
        }

        return new ValidateResult(false, "expect type %s, but %s", Type.Null, value.getClass());
    }
}
