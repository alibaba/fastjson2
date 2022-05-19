package com.alibaba.fastjson2.schema;

class ConstString extends JSONSchema {
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
        if (!this.value.equals(value)) {
            return new ValidateResult.ConstFail(this.value, value);
        }

        return SUCCESS;
    }
}
