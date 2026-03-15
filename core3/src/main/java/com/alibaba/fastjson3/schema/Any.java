package com.alibaba.fastjson3.schema;

public final class Any extends JSONSchema {
    public static final Any INSTANCE = new Any(true);
    public static final Any NOT_ANY = new Any(false);

    private final boolean any;

    private Any(boolean any) {
        super("", "");
        this.any = any;
    }

    @Override
    public Type getType() {
        return Type.Any;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        return any ? SUCCESS : FAIL_TYPE_NOT_MATCH;
    }
}
