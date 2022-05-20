package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

class OneOf extends JSONSchema {
    final JSONSchema[] items;

    public OneOf(JSONSchema[] items) {
        super(null, null);
        this.items = items;
    }

    public OneOf(JSONObject input, JSONSchema parent) {
        super(input);
        JSONArray items = input.getJSONArray("oneOf");
        if (items == null || items.isEmpty()) {
            throw new JSONException("oneOf not found");
        }

        this.items = new JSONSchema[items.size()];
        for (int i = 0; i < this.items.length; i++) {
            this.items[i] = JSONSchema.of(
                    items.getJSONObject(i),
                    parent
            );
        }
    }

    @Override
    public Type getType() {
        return Type.AllOf;
    }

    @Override
    public ValidateResult validate(Object value) {
        int count = 0;
        for (JSONSchema item : items) {
            ValidateResult result = item.validate(value);
            if (result.isSuccess()) {
                count++;
                if (count > 1) {
                    return FAIL_ONE_OF;
                }
            }
        }
        return count != 1 ? FAIL_ONE_OF : SUCCESS;
    }
}
