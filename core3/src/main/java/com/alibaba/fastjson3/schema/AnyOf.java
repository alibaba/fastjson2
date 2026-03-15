package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;

public final class AnyOf extends JSONSchema {
    final JSONSchema[] items;

    AnyOf(JSONSchema[] items) {
        super("", "");
        this.items = items;
    }

    AnyOf(JSONObject input, JSONSchema parent) {
        super(input);
        JSONArray array = input.getJSONArray("anyOf");
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
        return Type.AnyOf;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("anyOf", items);
        return obj;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        for (JSONSchema item : items) {
            ValidateResult result = item.validate(value);
            if (result.isSuccess()) {
                return SUCCESS;
            }
        }
        return FAIL_ANY_OF;
    }

    @Override
    protected ValidateResult validateInternal(Object value, EvaluationContext ctx) {
        boolean anyMatch = false;
        for (JSONSchema item : items) {
            EvaluationContext branch = ctx.branch();
            ValidateResult result = item.validate(value, branch);
            if (result.isSuccess()) {
                ctx.merge(branch);
                anyMatch = true;
            }
        }
        return anyMatch ? SUCCESS : FAIL_ANY_OF;
    }

    public ValidateResult validate(java.util.Map<?, ?> map) {
        for (JSONSchema item : items) {
            ValidateResult result;
            if (item instanceof ObjectSchema os) {
                result = os.validate(map);
            } else {
                result = item.validate(map);
            }
            if (result.isSuccess()) {
                return SUCCESS;
            }
        }
        return FAIL_ANY_OF;
    }

    public ValidateResult validate(java.util.Map<?, ?> map, EvaluationContext ctx) {
        boolean anyMatch = false;
        for (JSONSchema item : items) {
            EvaluationContext branch = ctx.branch();
            ValidateResult result = item.validate(map, branch);
            if (result.isSuccess()) {
                ctx.merge(branch);
                anyMatch = true;
            }
        }
        return anyMatch ? SUCCESS : FAIL_ANY_OF;
    }
}
