package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONObject;

import java.util.*;

public final class ArraySchema extends JSONSchema {
    final Map<String, JSONSchema> definitions;
    final Map<String, JSONSchema> defs;
    final boolean typed;
    final int minItems;
    final int maxItems;
    final JSONSchema itemSchema;
    final JSONSchema[] prefixItems;
    final boolean additionalItems;
    final JSONSchema additionalItemSchema;
    final JSONSchema contains;
    final int minContains;
    final int maxContains;
    final boolean uniqueItems;
    final AllOf allOf;
    final AnyOf anyOf;
    final OneOf oneOf;
    final boolean encoded;
    final JSONSchema unevaluatedItemsSchema;
    final boolean hasUnevaluatedItems;
    final JSONObject rawInput;

    public ArraySchema(JSONObject input, JSONSchema root) {
        super(input);

        this.rawInput = input;
        this.typed = "array".equalsIgnoreCase(input.getString("type"));
        this.minItems = getInt(input, "minItems", -1);
        this.maxItems = getInt(input, "maxItems", -1);
        this.uniqueItems = getBool(input, "uniqueItems", false);
        this.encoded = getBool(input, "encoded", false);

        // Parse definitions
        JSONObject defsObj = input.getJSONObject("definitions");
        this.definitions = new LinkedHashMap<>();
        if (defsObj != null) {
            for (Map.Entry<String, Object> entry : defsObj.entrySet()) {
                Object val = entry.getValue();
                JSONSchema s = val instanceof Boolean b ? (b ? Any.INSTANCE : Any.NOT_ANY)
                        : JSONSchema.of((JSONObject) val, root == null ? this : root);
                this.definitions.put(entry.getKey(), s);
            }
        }

        JSONObject defs2Obj = input.getJSONObject("$defs");
        this.defs = new LinkedHashMap<>();
        if (defs2Obj != null) {
            for (Map.Entry<String, Object> entry : defs2Obj.entrySet()) {
                Object val = entry.getValue();
                JSONSchema s = val instanceof Boolean b ? (b ? Any.INSTANCE : Any.NOT_ANY)
                        : JSONSchema.of((JSONObject) val, root == null ? this : root);
                this.defs.put(entry.getKey(), s);
            }
        }

        // Parse items
        Object itemsObj = input.get("items");
        if (itemsObj instanceof JSONObject itemObj) {
            this.itemSchema = JSONSchema.of(itemObj, root == null ? this : root);
        } else if (itemsObj instanceof Boolean b) {
            this.itemSchema = b ? Any.INSTANCE : Any.NOT_ANY;
        } else {
            this.itemSchema = null;
        }

        // Parse prefixItems (draft 2020-12 tuple validation)
        JSONArray prefixArr = input.getJSONArray("prefixItems");
        if (prefixArr != null && !prefixArr.isEmpty()) {
            this.prefixItems = new JSONSchema[prefixArr.size()];
            for (int i = 0; i < prefixItems.length; i++) {
                Object item = prefixArr.get(i);
                if (item instanceof Boolean b) {
                    prefixItems[i] = b ? Any.INSTANCE : Any.NOT_ANY;
                } else {
                    prefixItems[i] = JSONSchema.of((JSONObject) item, root == null ? this : root);
                }
            }
        } else {
            this.prefixItems = null;
        }

        // Parse additionalItems
        Object additionalItemsObj = input.get("additionalItems");
        if (additionalItemsObj instanceof Boolean b) {
            this.additionalItems = b;
            this.additionalItemSchema = null;
        } else if (additionalItemsObj instanceof JSONObject addObj) {
            this.additionalItems = false;
            this.additionalItemSchema = JSONSchema.of(addObj, root);
        } else {
            this.additionalItems = true;
            this.additionalItemSchema = null;
        }

        // Parse contains (can be Boolean or JSONObject)
        Object containsRaw = input.get("contains");
        if (containsRaw instanceof Boolean b) {
            this.contains = b ? Any.INSTANCE : Any.NOT_ANY;
        } else if (containsRaw instanceof JSONObject containsObj) {
            this.contains = JSONSchema.of(containsObj, root);
        } else {
            this.contains = null;
        }
        this.minContains = getInt(input, "minContains", -1);
        this.maxContains = getInt(input, "maxContains", -1);

        // Parse composition
        allOf = JSONSchema.allOf(input, null);
        anyOf = JSONSchema.anyOf(input, null);
        oneOf = JSONSchema.oneOf(input, null);

        // Parse unevaluatedItems
        Object unevalItems = input.get("unevaluatedItems");
        if (unevalItems instanceof Boolean b) {
            this.unevaluatedItemsSchema = b ? Any.INSTANCE : Any.NOT_ANY;
            this.hasUnevaluatedItems = true;
        } else if (unevalItems instanceof JSONObject obj) {
            this.unevaluatedItemsSchema = JSONSchema.of(obj, root);
            this.hasUnevaluatedItems = true;
        } else {
            this.unevaluatedItemsSchema = null;
            this.hasUnevaluatedItems = false;
        }
    }

    @Override
    public Type getType() {
        return Type.Array;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = objectOf("type", "array");
        if (itemSchema != null) {
            obj.put("items", itemSchema.toJSONObject());
        }
        if (prefixItems != null && prefixItems.length > 0) {
            JSONArray arr = new JSONArray(prefixItems.length);
            for (JSONSchema pi : prefixItems) {
                arr.add(pi.toJSONObject());
            }
            obj.put("prefixItems", arr);
        }
        if (!additionalItems) {
            if (additionalItemSchema != null) {
                obj.put("additionalItems", additionalItemSchema.toJSONObject());
            } else {
                obj.put("additionalItems", false);
            }
        }
        if (minItems >= 0) {
            obj.put("minItems", minItems);
        }
        if (maxItems >= 0) {
            obj.put("maxItems", maxItems);
        }
        if (uniqueItems) {
            obj.put("uniqueItems", true);
        }
        if (contains != null) {
            obj.put("contains", contains.toJSONObject());
        }
        if (minContains >= 0) {
            obj.put("minContains", minContains);
        }
        if (maxContains >= 0) {
            obj.put("maxContains", maxContains);
        }
        return obj;
    }

    public JSONSchema getItemSchema() {
        return itemSchema;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        if (hasUnevaluatedItems) {
            return validateInternal(value, new EvaluationContext());
        }
        return validateInternalNoCtx(value);
    }

    @Override
    protected ValidateResult validateInternal(Object value, EvaluationContext ctx) {
        Object resolved = resolveValue(value);
        if (resolved == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }
        if (resolved == FAIL_INPUT_NOT_ENCODED) {
            return FAIL_INPUT_NOT_ENCODED;
        }

        int size;
        java.util.function.IntFunction<Object> getter;

        if (resolved instanceof List<?> list) {
            size = list.size();
            getter = list::get;
        } else if (resolved instanceof Object[] arr) {
            size = arr.length;
            getter = i -> arr[i];
        } else {
            return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
        }

        return validateItemsWithContext(size, getter, ctx);
    }

    private ValidateResult validateInternalNoCtx(Object value) {
        Object resolved = resolveValue(value);
        if (resolved == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }
        if (resolved == FAIL_INPUT_NOT_ENCODED) {
            return FAIL_INPUT_NOT_ENCODED;
        }

        int size;
        java.util.function.IntFunction<Object> getter;

        if (resolved instanceof List<?> list) {
            size = list.size();
            getter = list::get;
        } else if (resolved instanceof Object[] arr) {
            size = arr.length;
            getter = i -> arr[i];
        } else {
            return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
        }

        return validateItems(size, getter);
    }

    private Object resolveValue(Object value) {
        if (value == null) {
            return null;
        }
        if (encoded) {
            if (value instanceof String str) {
                try {
                    return JSON.parseArray(str);
                } catch (JSONException e) {
                    return FAIL_INPUT_NOT_ENCODED;
                }
            } else {
                return FAIL_INPUT_NOT_ENCODED;
            }
        }
        if (value instanceof List<?>) {
            return value;
        }
        if (value instanceof Collection<?> col) {
            return new ArrayList<>(col);
        }
        if (value instanceof Object[]) {
            return value;
        }
        return value; // non-array type
    }

    private ValidateResult validateItemsWithContext(int size, java.util.function.IntFunction<Object> getter,
                                                     EvaluationContext ctx) {
        // Check minItems/maxItems
        if (minItems >= 0 && size < minItems) {
            return new ValidateResult(false, "minItems not match, expect >= %s, but %s", minItems, size);
        }
        if (maxItems >= 0 && size > maxItems) {
            return new ValidateResult(false, "maxItems not match, expect <= %s, but %s", maxItems, size);
        }

        // Validate prefixItems (tuple validation)
        if (prefixItems != null) {
            for (int i = 0; i < Math.min(prefixItems.length, size); i++) {
                ctx.addIndex(i);
                ValidateResult result = prefixItems[i].validate(getter.apply(i), ctx);
                if (!result.isSuccess()) {
                    return result.atPath("[" + i + "]");
                }
            }

            if (size > prefixItems.length) {
                if (itemSchema != null) {
                    // items applies to all items beyond prefixItems — marks all as evaluated
                    for (int i = prefixItems.length; i < size; i++) {
                        ctx.addIndex(i);
                        ValidateResult result = itemSchema.validate(getter.apply(i), ctx);
                        if (!result.isSuccess()) {
                            return result.atPath("[" + i + "]");
                        }
                    }
                } else if (!additionalItems) {
                    if (additionalItemSchema != null) {
                        for (int i = prefixItems.length; i < size; i++) {
                            ctx.addIndex(i);
                            ValidateResult result = additionalItemSchema.validate(getter.apply(i), ctx);
                            if (!result.isSuccess()) {
                                return result.atPath("[" + i + "]");
                            }
                        }
                    } else {
                        return new ValidateResult(false, "additionalItems not allowed, expect %s items, but %s",
                                prefixItems.length, size);
                    }
                }
            }
        } else if (itemSchema != null) {
            // items with no prefixItems — marks ALL as evaluated
            for (int i = 0; i < size; i++) {
                ctx.addIndex(i);
                ValidateResult result = itemSchema.validate(getter.apply(i), ctx);
                if (!result.isSuccess()) {
                    return result.atPath("[" + i + "]");
                }
            }
        }

        // Check uniqueItems
        if (uniqueItems && size > 1) {
            Set<Object> seen = new HashSet<>(size);
            for (int i = 0; i < size; i++) {
                if (!seen.add(getter.apply(i))) {
                    return UNIQUE_ITEMS_NOT_MATCH;
                }
            }
        }

        // Check contains — marks matched indices as evaluated
        if (contains != null) {
            int matchCount = 0;
            for (int i = 0; i < size; i++) {
                if (contains.validate(getter.apply(i)).isSuccess()) {
                    matchCount++;
                    ctx.addIndex(i);
                }
            }
            int min = minContains >= 0 ? minContains : 1;
            if (matchCount < min) {
                return CONTAINS_NOT_MATCH;
            }
            if (maxContains >= 0 && matchCount > maxContains) {
                return CONTAINS_NOT_MATCH;
            }
        }

        // Check composition with context
        if (allOf != null || anyOf != null || oneOf != null) {
            List<Object> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(getter.apply(i));
            }
            if (allOf != null) {
                ValidateResult result = allOf.validate(list, ctx);
                if (!result.isSuccess()) {
                    return result;
                }
            }
            if (anyOf != null) {
                ValidateResult result = anyOf.validate(list, ctx);
                if (!result.isSuccess()) {
                    return result;
                }
            }
            if (oneOf != null) {
                ValidateResult result = oneOf.validate(list, ctx);
                if (!result.isSuccess()) {
                    return result;
                }
            }
        }

        // Apply unevaluatedItems
        if (hasUnevaluatedItems) {
            for (int i = 0; i < size; i++) {
                if (!ctx.isIndexEvaluated(i)) {
                    ValidateResult result = unevaluatedItemsSchema.validate(getter.apply(i));
                    if (!result.isSuccess()) {
                        return new ValidateResult(false, "unevaluated item at index %s not allowed", i);
                    }
                    ctx.addIndex(i);
                }
            }
        }

        return SUCCESS;
    }

    private ValidateResult validateItems(int size, java.util.function.IntFunction<Object> getter) {
        // Check minItems/maxItems
        if (minItems >= 0 && size < minItems) {
            return new ValidateResult(false, "minItems not match, expect >= %s, but %s", minItems, size);
        }
        if (maxItems >= 0 && size > maxItems) {
            return new ValidateResult(false, "maxItems not match, expect <= %s, but %s", maxItems, size);
        }

        // Validate prefixItems (tuple validation)
        if (prefixItems != null) {
            for (int i = 0; i < Math.min(prefixItems.length, size); i++) {
                ValidateResult result = prefixItems[i].validate(getter.apply(i));
                if (!result.isSuccess()) {
                    return result.atPath("[" + i + "]");
                }
            }

            // Check additional items beyond prefixItems
            if (size > prefixItems.length) {
                if (itemSchema != null) {
                    for (int i = prefixItems.length; i < size; i++) {
                        ValidateResult result = itemSchema.validate(getter.apply(i));
                        if (!result.isSuccess()) {
                            return result.atPath("[" + i + "]");
                        }
                    }
                } else if (!additionalItems) {
                    if (additionalItemSchema != null) {
                        for (int i = prefixItems.length; i < size; i++) {
                            ValidateResult result = additionalItemSchema.validate(getter.apply(i));
                            if (!result.isSuccess()) {
                                return result.atPath("[" + i + "]");
                            }
                        }
                    } else {
                        return new ValidateResult(false, "additionalItems not allowed, expect %s items, but %s",
                                prefixItems.length, size);
                    }
                }
            }
        } else if (itemSchema != null) {
            // Validate all items against single schema
            for (int i = 0; i < size; i++) {
                ValidateResult result = itemSchema.validate(getter.apply(i));
                if (!result.isSuccess()) {
                    return result.atPath("[" + i + "]");
                }
            }
        }

        // Check uniqueItems
        if (uniqueItems && size > 1) {
            Set<Object> seen = new HashSet<>(size);
            for (int i = 0; i < size; i++) {
                if (!seen.add(getter.apply(i))) {
                    return UNIQUE_ITEMS_NOT_MATCH;
                }
            }
        }

        // Check contains
        if (contains != null) {
            int matchCount = 0;
            for (int i = 0; i < size; i++) {
                if (contains.validate(getter.apply(i)).isSuccess()) {
                    matchCount++;
                }
            }

            int min = minContains >= 0 ? minContains : 1;
            if (matchCount < min) {
                return CONTAINS_NOT_MATCH;
            }
            if (maxContains >= 0 && matchCount > maxContains) {
                return CONTAINS_NOT_MATCH;
            }
        }

        // Check composition
        // Build the value for composition validation
        if (allOf != null || anyOf != null || oneOf != null) {
            List<Object> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(getter.apply(i));
            }
            if (allOf != null) {
                ValidateResult result = allOf.validate(list);
                if (!result.isSuccess()) {
                    return result;
                }
            }
            if (anyOf != null) {
                ValidateResult result = anyOf.validate(list);
                if (!result.isSuccess()) {
                    return result;
                }
            }
            if (oneOf != null) {
                ValidateResult result = oneOf.validate(list);
                if (!result.isSuccess()) {
                    return result;
                }
            }
        }

        return SUCCESS;
    }
}
