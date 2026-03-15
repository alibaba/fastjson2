package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONObject;

public final class NullSchema extends JSONSchema {
    public NullSchema(JSONObject input) {
        super(input);
    }

    @Override
    public Type getType() {
        return Type.Null;
    }

    @Override
    public JSONObject toJSONObject() {
        return objectOf("type", "null");
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        return value == null ? SUCCESS : FAIL_TYPE_NOT_MATCH;
    }
}
