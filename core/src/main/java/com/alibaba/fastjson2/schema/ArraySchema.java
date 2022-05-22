package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

public final class ArraySchema
        extends JSONSchema {
    final Map<String, JSONSchema> definitions;
    final Map<String, JSONSchema> defs;

    final boolean typed;

    final int maxLength;
    final int minLength;
    final JSONSchema itemSchema;
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
            for (Iterator<Map.Entry<String, Object>> it = definitions.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Object> entry = it.next();
                String entryKey = entry.getKey();
                JSONObject entryValue = (JSONObject) entry.getValue();
                JSONSchema schema = JSONSchema.of(entryValue, root == null ? this : root);
                this.definitions.put(entryKey, schema);
            }
        }

        JSONObject defs = input.getJSONObject("$defs");
        if (defs != null) {
            for (Iterator<Map.Entry<String, Object>> it = defs.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Object> entry = it.next();
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
            additionalItemsSupport = ((Boolean) items).booleanValue();
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
            this.itemSchema = JSONSchema.of((JSONObject) items, root);
        }

        if (additionalItems instanceof JSONObject) {
            additionalItem = JSONSchema.of((JSONObject) additionalItems, root == null ? this : root);
            additionalItemsSupport = true;
        } else if (additionalItems instanceof Boolean) {
            additionalItemsSupport = ((Boolean) additionalItems).booleanValue();
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
                    schema = ((Boolean) prefixItem).booleanValue() ? Any.INSTANCE : Any.NOT_ANY;
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

        Set uniqueItemsSet = null;

        if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            final int size = array.length;

            if (minLength >= 0 && size < minLength) {
                return new ValidateResult(false, "minLength not match, expect >= %s, but %s", minLength, size);
            }

            if (maxLength >= 0) {
                if (maxLength >= 0 && size > maxLength) {
                    return new ValidateResult(false, "maxLength not match, expect <= %s, but %s", maxLength, size);
                }
            }

            int containsCount = 0;
            for (int index = 0; index < array.length; index++) {
                Object item = array[index];

                boolean prefixMatch = false;
                if (index < prefixItems.length) {
                    ValidateResult result = prefixItems[index].validate(item);
                    if (!result.isSuccess()) {
                        return result;
                    }
                    prefixMatch = true;
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
                        uniqueItemsSet = new HashSet(size);
                    }

                    if (item instanceof BigDecimal) {
                        item = ((BigDecimal) item).stripTrailingZeros();
                    }

                    if (!uniqueItemsSet.add(item)) {
                        return UNIQUE_ITEMS_NOT_MATCH;
                    }
                }
            }

            if (this.contains != null && containsCount == 0) {
                return CONTAINS_NOT_MATCH;
            }

            if (minContains >= 0 && containsCount < minContains) {
                return new ValidateResult(false, "minContains not match, expect %s, but %s", minContains, containsCount);
            }

            if (maxContains >= 0 && containsCount > maxContains) {
                return new ValidateResult(false, "maxContains not match, expect %s, but %s", maxContains, containsCount);
            }

            if (!additionalItems) {
                if (size > prefixItems.length) {
                    return new ValidateResult(false, "additional items not match, max size %s, but %s", prefixItems.length, size);
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

        if (value.getClass().isArray()) {
            final int size = Array.getLength(value);

            if (minLength >= 0 && size < minLength) {
                return new ValidateResult(false, "minLength not match, expect >= %s, but %s", minLength, size);
            }

            if (maxLength >= 0) {
                if (maxLength >= 0 && size > maxLength) {
                    return new ValidateResult(false, "maxLength not match, expect <= %s, but %s", maxLength, size);
                }
            }

            int containsCount = 0;
            for (int index = 0; index < size; index++) {
                Object item = Array.get(value, index);

                boolean prefixMatch = false;
                if (index < prefixItems.length) {
                    ValidateResult result = prefixItems[index].validate(item);
                    if (!result.isSuccess()) {
                        return result;
                    }
                    prefixMatch = true;
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
                        uniqueItemsSet = new HashSet(size);
                    }

                    if (item instanceof BigDecimal) {
                        item = ((BigDecimal) item).stripTrailingZeros();
                    }

                    if (!uniqueItemsSet.add(item)) {
                        return UNIQUE_ITEMS_NOT_MATCH;
                    }
                }
            }
            if (this.contains != null && containsCount == 0) {
                return CONTAINS_NOT_MATCH;
            }

            if (minContains >= 0 && containsCount < minContains) {
                return new ValidateResult(false, "minContains not match, expect %s, but %s", minContains, containsCount);
            }

            if (maxContains >= 0 && containsCount > maxContains) {
                return new ValidateResult(false, "maxContains not match, expect %s, but %s", maxContains, containsCount);
            }

            if (!additionalItems) {
                if (size > prefixItems.length) {
                    return new ValidateResult(false, "additional items not match, max size %s, but %s", prefixItems.length, size);
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

        if (value instanceof Collection) {
            int size = ((Collection<?>) value).size();
            if (minLength >= 0 && size < minLength) {
                return new ValidateResult(false, "minLength not match, expect >= %s, but %s", minLength, size);
            }

            if (maxLength >= 0) {
                if (maxLength >= 0 && size > maxLength) {
                    return new ValidateResult(false, "maxLength not match, expect <= %s, but %s", maxLength, size);
                }
            }

            if (!additionalItems) {
                if (size > prefixItems.length) {
                    return new ValidateResult(false, "additional items not match, max size %s, but %s", prefixItems.length, size);
                }
            }

            int index = 0;
            int containsCount = 0;
            for (Iterator it = ((Iterable) value).iterator(); it.hasNext(); index++) {
                Object item = it.next();

                boolean prefixMatch = false;
                if (index < prefixItems.length) {
                    ValidateResult result = prefixItems[index].validate(item);
                    if (!result.isSuccess()) {
                        return result;
                    }
                    prefixMatch = true;
                } else if (itemSchema == null && additionalItem != null) {
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
                        uniqueItemsSet = new HashSet();
                    }

                    if (item instanceof BigDecimal) {
                        item = ((BigDecimal) item).stripTrailingZeros();
                    }

                    if (!uniqueItemsSet.add(item)) {
                        return UNIQUE_ITEMS_NOT_MATCH;
                    }
                }
            }

            if (this.contains != null) {
                if (minContains >= 0 && containsCount < minContains) {
                    return new ValidateResult(false, "minContains not match, expect %s, but %s", minContains, containsCount);
                } else {
                    if (containsCount == 0 && minContains != 0) {
                        return CONTAINS_NOT_MATCH;
                    }
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

        return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        com.alibaba.fastjson2.schema.ArraySchema that = (com.alibaba.fastjson2.schema.ArraySchema) o;
        return Objects.equals(title, that.title) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description);
    }
}
