package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;

public final class AllOf extends JSONSchema {
    final JSONSchema[] items;

    AllOf(JSONSchema[] items) {
        super("", "");
        this.items = items;
    }

    AllOf(JSONObject input, JSONSchema parent) {
        super(input);
        JSONArray array = input.getJSONArray("allOf");
        if (array != null && !array.isEmpty()) {
            this.items = new JSONSchema[array.size()];
            for (int i = 0; i < items.length; i++) {
                Object item = array.get(i);
                if (item instanceof Boolean b) {
                    items[i] = b ? Any.INSTANCE : Any.NOT_ANY;
                } else {
                    items[i] = JSONSchema.of((JSONObject) item, parent);
                }
            }
        } else {
            this.items = new JSONSchema[0];
        }
    }

    @Override
    public Type getType() {
        return Type.AllOf;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("allOf", items);
        return obj;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        for (JSONSchema item : items) {
            ValidateResult result = item.validate(value);
            if (!result.isSuccess()) {
                return result;
            }
        }
        return SUCCESS;
    }

    public ValidateResult validate(java.util.Map<?, ?> map) {
        for (JSONSchema item : items) {
            ValidateResult result;
            if (item instanceof ObjectSchema os) {
                result = os.validate(map);
            } else {
                result = item.validate(map);
            }
            if (!result.isSuccess()) {
                return result;
            }
        }
        return SUCCESS;
    }
}
