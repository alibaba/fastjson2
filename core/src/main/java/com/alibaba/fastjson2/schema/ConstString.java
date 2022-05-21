package com.alibaba.fastjson2.schema;

final class ConstString extends JSONSchema {
    final String value;

    ConstString(String value) {
        super(null, null);
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.Const;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            return SUCCESS;
        }

        if (!this.value.equals(value)) {
            return ValidateResult.fail("const not match, expect %s, but %s", this.value, value);
        }

        return SUCCESS;
    }
}
