package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public final class ArraySchema
        extends JSONSchema {
    final Map<String, JSONSchema> definitions;
    final Map<String, JSONSchema> defs;

    final boolean typed;

    final int maxLength;
    final int minLength;
    JSONSchema itemSchema;
    final JSONSchema[] prefixItems;
    final boolean additionalItems;
    final JSONSchema additionalItem;
    final JSONSchema contains;
    final int minContains;
    final int maxContains;
    final boolean uniqueItems;

    final AllOf allOf;
    final AnyOf anyOf;
    final OneOf oneOf;

    public ArraySchema(JSONObject input, JSONSchema root) {
        super(input);

        this.typed = "array".equals(input.get("type"));
        this.definitions = new LinkedHashMap<>();
        this.defs = new LinkedHashMap<>();

        JSONObject definitions = input.getJSONObject("definitions");
        if (definitions != null) {
            for (Map.Entry<String, Object> entry : definitions.entrySet()) {
                String entryKey = entry.getKey();
                JSONObject entryValue = (JSONObject) entry.getValue();
                JSONSchema schema = JSONSchema.of(entryValue, root == null ? this : root);
                this.definitions.put(entryKey, schema);
            }
        }

        JSONObject defs = input.getJSONObject("$defs");
        if (defs != null) {
            for (Map.Entry<String, Object> entry : defs.entrySet()) {
                String entryKey = entry.getKey();
                JSONObject entryValue = (JSONObject) entry.getValue();
                JSONSchema schema = JSONSchema.of(entryValue, root == null ? this : root);
                this.defs.put(entryKey, schema);
            }
        }

        this.minLength = input.getIntValue("minItems", -1);
        this.maxLength = input.getIntValue("maxItems", -1);

        Object items = input.get("items");
        Object additionalItems = input.get("additionalItems");
        JSONArray prefixItems = input.getJSONArray("prefixItems");

        boolean additionalItemsSupport;
        if (items == null) {
            additionalItemsSupport = true;
            this.itemSchema = null;
        } else if (items instanceof Boolean) {
            additionalItemsSupport = (Boolean) items;
            this.itemSchema = null;
        } else if (items instanceof JSONArray) {
            if (prefixItems == null) {
                prefixItems = (JSONArray) items;
            } else {
                throw new JSONException("schema error, items : " + items);
            }
            this.itemSchema = null;
            additionalItemsSupport = true;
        } else {
            additionalItemsSupport = true;
            this.itemSchema = JSONSchema.of((JSONObject) items, root != null ? root : this);
        }

        if (additionalItems instanceof JSONObject) {
            additionalItem = JSONSchema.of((JSONObject) additionalItems, root == null ? this : root);
            additionalItemsSupport = true;
        } else if (additionalItems instanceof Boolean) {
            additionalItemsSupport = (Boolean) additionalItems;
            this.additionalItem = null;
        } else {
            this.additionalItem = null;
        }
//        ((itemSchema != null && !(itemSchema instanceof Any))
//                || this.prefixItems.length > 0)
        if (itemSchema != null && !(itemSchema instanceof Any)) {
            additionalItemsSupport = true;
        } else if (prefixItems == null && !(items instanceof Boolean)) {
            additionalItemsSupport = true;
        }
        this.additionalItems = additionalItemsSupport;

        if (prefixItems == null) {
            this.prefixItems = new JSONSchema[0];
        } else {
            this.prefixItems = new JSONSchema[prefixItems.size()];
            for (int i = 0; i < prefixItems.size(); i++) {
                JSONSchema schema;

                Object prefixItem = prefixItems.get(i);
                if (prefixItem instanceof Boolean) {
                    schema = (Boolean) prefixItem ? Any.INSTANCE : Any.NOT_ANY;
                } else {
                    JSONObject jsonObject = (JSONObject) prefixItem;
                    schema = JSONSchema.of(jsonObject, root == null ? this : root);
                }

                this.prefixItems[i] = schema;
            }
        }

        this.contains = input.getObject("contains", JSONSchema::of);
        this.minContains = input.getIntValue("minContains", -1);
        this.maxContains = input.getIntValue("maxContains", -1);

        this.uniqueItems = input.getBooleanValue("uniqueItems");

        allOf = allOf(input, null);
        anyOf = anyOf(input, null);
        oneOf = oneOf(input, null);
    }

    @Override
    public Type getType() {
        return Type.Array;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }

        if (value instanceof Object[]) {
            final Object[] items = (Object[]) value;
            return validateItems(value, items.length, i -> items[i]);
        }

        if (value.getClass().isArray()) {
            final int size = Array.getLength(value);
            return validateItems(value, size, i -> Array.get(value, i));
        }

        if (value instanceof Collection) {
            final Collection<?> items = (Collection<?>) value;
            final Iterator<?> iterator = items.iterator();
            return validateItems(value, items.size(), i -> iterator.next());
        }

        return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
    }

    private ValidateResult validateItems(final Object value, final int size, IntFunction<Object> itemGetter) {
        if (minLength >= 0 && size < minLength) {
            return new ValidateResult(false, "minLength not match, expect >= %s, but %s", minLength, size);
        }

        if (maxLength >= 0 && size > maxLength) {
            return new ValidateResult(false, "maxLength not match, expect <= %s, but %s", maxLength, size);
        }

        if (!additionalItems && size > prefixItems.length) {
            return new ValidateResult(false, "additional items not match, max size %s, but %s", prefixItems.length, size);
        }

        final boolean isCollection = value instanceof Collection;
        Set<Object> uniqueItemsSet = null;
        int containsCount = 0;
        for (int index = 0; index < size; index++) {
            Object item = itemGetter.apply(index);

            boolean prefixMatch = false;
            if (index < prefixItems.length) {
                ValidateResult result = prefixItems[index].validate(item);
                if (!result.isSuccess()) {
                    return result;
                }
                prefixMatch = true;
            } else if (isCollection && itemSchema == null && additionalItem != null) { // 只有 Collection 才会执行这部分校验
                ValidateResult result = additionalItem.validate(item);
                if (!result.isSuccess()) {
                    return result;
                }
            }

            if (!prefixMatch && itemSchema != null) {
                ValidateResult result = itemSchema.validate(item);
                if (!result.isSuccess()) {
                    return result;
                }
            }

            if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                ValidateResult result = this.contains.validate(item);
                if (result == SUCCESS) {
                    containsCount++;
                }
            }

            if (uniqueItems) {
                if (uniqueItemsSet == null) {
                    uniqueItemsSet = new HashSet<>(size, 1F);
                }

                if (item instanceof BigDecimal) {
                    item = ((BigDecimal) item).stripTrailingZeros();
                }

                if (!uniqueItemsSet.add(item)) {
                    return UNIQUE_ITEMS_NOT_MATCH;
                }
            }
        }

        if (!isCollection || this.contains != null) {
            if (minContains >= 0 && containsCount < minContains) {
                return new ValidateResult(false, "minContains not match, expect %s, but %s", minContains, containsCount);
            }

            if (isCollection) { // Collection 和 数组 的部分校验规则不一样
                if (containsCount == 0 && minContains != 0) {
                    return CONTAINS_NOT_MATCH;
                }
            } else if (this.contains != null && containsCount == 0) {
                return CONTAINS_NOT_MATCH;
            }

            if (maxContains >= 0 && containsCount > maxContains) {
                return new ValidateResult(false, "maxContains not match, expect %s, but %s", maxContains, containsCount);
            }
        }

        if (allOf != null) {
            ValidateResult result = allOf.validate(value);
            if (!result.isSuccess()) {
                return result;
            }
        }

        if (anyOf != null) {
            ValidateResult result = anyOf.validate(value);
            if (!result.isSuccess()) {
                return result;
            }
        }

        if (oneOf != null) {
            ValidateResult result = oneOf.validate(value);
            if (!result.isSuccess()) {
                return result;
            }
        }

        return SUCCESS;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put("type", "array");

        if (maxLength != -1) {
            object.put("maxLength", maxLength);
        }

        if (minLength != -1) {
            object.put("minLength", minLength);
        }

        if (itemSchema != null) {
            object.put("items", itemSchema);
        }

        if (prefixItems != null && prefixItems.length != 0) {
            object.put("prefixItems", prefixItems);
        }

        if (!additionalItems) {
            object.put("additionalItems", additionalItems);
        }

        if (additionalItem != null) {
            object.put("additionalItem", additionalItem);
        }

        if (contains != null) {
            object.put("contains", contains);
        }

        if (minContains != -1) {
            object.put("minContains", minContains);
        }

        if (maxContains != -1) {
            object.put("maxContains", maxContains);
        }

        if (uniqueItems) {
            object.put("uniqueItems", uniqueItems);
        }

        if (allOf != null) {
            object.put("allOf", allOf);
        }

        if (anyOf != null) {
            object.put("anyOf", anyOf);
        }

        if (oneOf != null) {
            object.put("oneOf", oneOf);
        }
        return object;
    }

    @Override
    public void accept(Predicate<JSONSchema> v) {
        if (v.test(this)) {
            if (itemSchema != null) {
                itemSchema.accept(v);
            }
        }
    }

    public JSONSchema getItemSchema() {
        return itemSchema;
    }
}
