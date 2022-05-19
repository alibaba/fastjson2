package com.alibaba.fastjson2.schema;

class Not extends JSONSchema {
    JSONSchema schema;

    public Not(JSONSchema schema) {
        super(null, null);
        this.schema = schema;
    }

    @Override
    public Type getType() {
        return Type.AllOf;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (schema.validate(value).isSuccess()) {
            return FAIL_NOT;
        }
        return SUCCESS;
    }
}
