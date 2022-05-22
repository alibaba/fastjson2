package com.alibaba.fastjson2.schema;

final class Any
        extends JSONSchema {
    public static final com.alibaba.fastjson2.schema.Any INSTANCE = new com.alibaba.fastjson2.schema.Any();
    public static final com.alibaba.fastjson2.schema.JSONSchema NOT_ANY = new Not(INSTANCE, null, null);

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
