package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

class AnyOf extends JSONSchema {
    final JSONSchema[] items;

    public AnyOf(JSONSchema[] items) {
        super(null, null);
        this.items = items;
    }

    public AnyOf(JSONObject input) {
        super(input);
        JSONArray items = input.getJSONArray("anyOf");
        if (items == null || items.isEmpty()) {
            throw new JSONException("anyOf not found");
        }

        this.items = new JSONSchema[items.size()];
        for (int i = 0; i < this.items.length; i++) {
            this.items[i] = items.getObject(i, JSONSchema::of);
        }
    }

    @Override
    public Type getType() {
        return Type.AllOf;
    }

    @Override
    public ValidateResult validate(Object value) {
        for (JSONSchema item : items) {
            ValidateResult result = item.validate(value);
            if (result.isSuccess()) {
                return SUCCESS;
            }
        }
        return FAIL_ANY_OF;
    }
}
