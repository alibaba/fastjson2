package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * Keeps typed enum alternatives and negation together for validation and export.
 */
final class TypeArrayEnumNotSchema
        extends JSONSchema {
    private final AllOf allOf;
    private final String definition;

    TypeArrayEnumNotSchema(JSONSchema[] items, JSONObject input) {
        super(null, null);
        this.allOf = new AllOf(new JSONSchema[]{new AnyOf(items), ofNot(input, null)});
        this.definition = input.toJSONString();
    }

    @Override
    public Type getType() {
        return Type.AllOf;
    }

    @Override
    public JSONObject toJSONObject() {
        // Return an independent object graph while retaining the immutable definition snapshot.
        return JSON.parseObject(definition);
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        return allOf.validate(value);
    }
}
