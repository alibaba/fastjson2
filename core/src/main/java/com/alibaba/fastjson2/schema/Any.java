package com.alibaba.fastjson2.schema;

class Any extends JSONSchema {
    public final static com.alibaba.fastjson2.schema.Any INSTANCE = new com.alibaba.fastjson2.schema.Any();

    public Any() {
        super(null, null);
    }

    @Override
    public Type getType() {
        return Type.Any;
    }

    @Override
    public ValidateResult validate(Object value) {
        return SUCCESS;
    }
}
