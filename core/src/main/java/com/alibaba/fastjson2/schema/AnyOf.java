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
                this.items[i] = (Boolean) item ? Any.INSTANCE : Any.NOT_ANY;
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
    protected ValidateResult validateInternal(Object value, ValidationHandler handler, String path) {
        for (JSONSchema item : items) {
            if (item.validateInternal(value, null, path) == SUCCESS) {
                return SUCCESS;
            }
        }

        ValidateResult result = handleError(handler, value, path, FAIL_ANY_OF);
        return result != null ? result : FAIL_ANY_OF;
    }

    public JSONObject toJSONObject() {
        return JSONObject.of("anyOf", this.items);
    }
}
