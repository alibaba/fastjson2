package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;

public final class AllOf extends JSONSchema {
    final JSONSchema[] items;
    final boolean shareEvaluations; // true for $ref+siblings, false for allOf keyword

    AllOf(JSONSchema[] items) {
        super("", "");
        this.items = items;
        this.shareEvaluations = false;
    }

    AllOf(JSONSchema[] items, boolean shareEvaluations) {
        super("", "");
        this.items = items;
        this.shareEvaluations = shareEvaluations;
    }

    AllOf(JSONObject input, JSONSchema parent) {
        super(input);
        this.shareEvaluations = false;
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
        // Check if any sub-schema needs evaluation context (unevaluatedProperties/Items)
        if (needsEvaluationContext()) {
            return validateInternal(value, new EvaluationContext());
        }
        for (JSONSchema item : items) {
            ValidateResult result = item.validate(value);
            if (!result.isSuccess()) {
                return result;
            }
        }
        return SUCCESS;
    }

    private boolean needsEvaluationContext() {
        for (JSONSchema item : items) {
            if (item instanceof ObjectSchema os && os.hasUnevaluatedProperties) {
                return true;
            }
            if (item instanceof ArraySchema as && as.hasUnevaluatedItems) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ValidateResult validateInternal(Object value, EvaluationContext ctx) {
        if (shareEvaluations) {
            // $ref + siblings: items share the same context (sequential accumulation)
            for (JSONSchema item : items) {
                ValidateResult result = item.validate(value, ctx);
                if (!result.isSuccess()) {
                    return result;
                }
            }
            return SUCCESS;
        }
        // allOf keyword: cousin isolation — each item branches from ORIGINAL ctx
        EvaluationContext[] branches = new EvaluationContext[items.length];
        for (int i = 0; i < items.length; i++) {
            branches[i] = ctx.branch();
            ValidateResult result = items[i].validate(value, branches[i]);
            if (!result.isSuccess()) {
                return result;
            }
        }
        for (EvaluationContext branch : branches) {
            ctx.merge(branch);
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

    public ValidateResult validate(java.util.Map<?, ?> map, EvaluationContext ctx) {
        if (shareEvaluations) {
            for (JSONSchema item : items) {
                ValidateResult result = item.validate(map, ctx);
                if (!result.isSuccess()) {
                    return result;
                }
            }
            return SUCCESS;
        }
        EvaluationContext[] branches = new EvaluationContext[items.length];
        for (int i = 0; i < items.length; i++) {
            branches[i] = ctx.branch();
            ValidateResult result = items[i].validate(map, branches[i]);
            if (!result.isSuccess()) {
                return result;
            }
        }
        for (EvaluationContext branch : branches) {
            ctx.merge(branch);
        }
        return SUCCESS;
    }
}
