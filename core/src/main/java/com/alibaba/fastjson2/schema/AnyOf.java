package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

final class AnyOf
        extends JSONSchema {
    final JSONSchema[] items;

    public AnyOf(JSONSchema[] items) {
        super(null, null);
        this.items = items;
    }

    public AnyOf(JSONObject input, JSONSchema parent) {
        super(input);
        JSONArray items = input.getJSONArray("anyOf");
        if (items == null || items.isEmpty()) {
            throw new JSONException("anyOf not found");
        }

        this.items = new JSONSchema[items.size()];
        for (int i = 0; i < this.items.length; i++) {
            Object item = items.get(i);
            if (item instanceof Boolean) {
                this.items[i] = ((Boolean) item).booleanValue() ? Any.INSTANCE : Any.NOT_ANY;
            } else {
                this.items[i] = JSONSchema.of((JSONObject) item, parent);
            }
        }
    }

    @Override
    public Type getType() {
        return Type.AnyOf;
    }

    @Override
    public ValidateResult validate(Object value) {
        for (JSONSchema item : items) {
            ValidateResult result = item.validate(value);
            if (result == SUCCESS) {
                return SUCCESS;
            }
        }
        return FAIL_ANY_OF;
    }
}
