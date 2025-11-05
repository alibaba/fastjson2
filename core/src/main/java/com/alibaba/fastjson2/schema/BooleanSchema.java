package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;

/**
 * JSON Schema validator for boolean types.
 * Validates that values are of boolean type (true or false).
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * JSONSchema schema = JSONSchema.of(JSONObject.of("type", "boolean"));
 * schema.isValid(true);   // returns true
 * schema.isValid("true"); // returns false (not a boolean)
 * }</pre>
 */
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
