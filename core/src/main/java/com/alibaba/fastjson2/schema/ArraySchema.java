package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Array;
import java.util.*;

public final class ArraySchema extends JSONSchema {
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

    public ArraySchema(JSONObject input) {
        super(input);
        this.minLength = input.getIntValue("minItems", -1);
        this.maxLength = input.getIntValue("maxItems", -1);

        Object items = input.get("items");
        Object additionalItems = input.get("additionalItems");

        boolean additionalItemsSupport = false;
        if (items == null) {
            additionalItemsSupport = true;
            this.itemSchema = null;
        } else if (items instanceof Boolean) {
            additionalItemsSupport = ((Boolean) items).booleanValue();
            this.itemSchema = null;
        } else if (items instanceof JSONArray) {
            throw new JSONException("schema error, items : " + items);
        } else {
            additionalItemsSupport = true;
            this.itemSchema = JSONSchema.of((JSONObject) items);
        }

        if (additionalItems instanceof JSONObject) {
            additionalItem = JSONSchema.of((JSONObject) additionalItems);
            additionalItemsSupport = true;
        } else if (additionalItems instanceof Boolean) {
            additionalItemsSupport = ((Boolean) additionalItems).booleanValue();
            this.additionalItem = null;
        } else {
            this.additionalItem = null;
        }
        this.additionalItems = additionalItemsSupport;

        JSONArray prefixItems = input.getJSONArray("prefixItems");
        if (prefixItems == null) {
            this.prefixItems = new JSONSchema[0];
        } else {
            this.prefixItems = new JSONSchema[prefixItems.size()];
            for (int i = 0; i < prefixItems.size(); i++) {
                this.prefixItems[i] = prefixItems.getObject(i, JSONSchema::of);
            }
        }

        this.contains = input.getObject("contains", JSONSchema::of);
        this.minContains = input.getIntValue("minContains", -1);
        this.maxContains = input.getIntValue("maxContains", -1);

        this.uniqueItems = input.getBooleanValue("uniqueItems");
    }

    @Override
    public Type getType() {
        return Type.Array;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            return FAIL_INPUT_NULL;
        }

        Set uniqueItemsSet = null;

        if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            final int size = array.length;

            if (minLength >= 0 && size < minLength) {
                return new ValidateResult.MinLengthFail(minLength, size);
            }

            if (maxLength >= 0) {
                if (maxLength >= 0 && size > maxLength) {
                    return new ValidateResult.MaxLengthFail(maxLength, size);
                }
            }

            int containsCount = 0;
            for (int index = 0; index < array.length; index++) {
                Object item = array[index];

                if (itemSchema != null) {
                    ValidateResult result = itemSchema.validate(item);
                    if (!result.isSuccess()) {
                        return result;
                    }
                }

                if (index < prefixItems.length) {
                    ValidateResult result = prefixItems[index].validate(item);
                    if (!result.isSuccess()) {
                        return result;
                    }
                }

                if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                    ValidateResult result = this.contains.validate(item);
                    if (result.isSuccess()) {
                        containsCount++;
                    }
                }

                if (uniqueItems) {
                    if (uniqueItemsSet == null) {
                        uniqueItemsSet = new HashSet(size);
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
                return new ValidateResult.MinContainsFail(minContains, containsCount);
            }

            if (maxContains >= 0 && containsCount > maxContains) {
                return new ValidateResult.MaxContainsFail(maxContains, containsCount);
            }

            if (!additionalItems) {
                if (size > prefixItems.length) {
                    return new ValidateResult.AdditionalItemsFail(prefixItems.length, size);
                }
            }
            return SUCCESS;
        }
        if (value.getClass().isArray()) {
            final int size = Array.getLength(value);

            if (minLength >= 0 && size < minLength) {
                return new ValidateResult.MinLengthFail(minLength, size);
            }

            if (maxLength >= 0) {
                if (maxLength >= 0 && size > maxLength) {
                    return new ValidateResult.MaxLengthFail(maxLength, size);
                }
            }

            int containsCount = 0;
            for (int index = 0; index < size; index++) {
                Object item = Array.get(value, index);

                if (itemSchema != null) {
                    ValidateResult result = itemSchema.validate(item);
                    if (!result.isSuccess()) {
                        return result;
                    }
                }

                if (index < prefixItems.length) {
                    ValidateResult result = prefixItems[index].validate(item);
                    if (!result.isSuccess()) {
                        return result;
                    }
                }

                if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                    ValidateResult result = this.contains.validate(item);
                    if (result.isSuccess()) {
                        containsCount++;
                    }
                }

                if (uniqueItems) {
                    if (uniqueItemsSet == null) {
                        uniqueItemsSet = new HashSet(size);
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
                return new ValidateResult.MinContainsFail(minContains, containsCount);
            }
            if (maxContains >= 0 && containsCount > maxContains) {
                return new ValidateResult.MaxContainsFail(maxContains, containsCount);
            }

            if (!additionalItems) {
                if (size > prefixItems.length) {
                    return new ValidateResult.AdditionalItemsFail(prefixItems.length, size);
                }
            }
            return SUCCESS;
        }

        if (value instanceof Collection) {
            int size = ((Collection<?>) value).size();
            if (minLength >= 0 && size < minLength) {
                return new ValidateResult.MinLengthFail(minLength, size);
            }

            if (maxLength >= 0) {
                if (maxLength >= 0 && size > maxLength) {
                    return new ValidateResult.MaxLengthFail(maxLength, size);
                }
            }

            if (!additionalItems) {
                if (size >= prefixItems.length) {
                    return new ValidateResult.AdditionalItemsFail(prefixItems.length, size);
                }
            }

            int index = 0;
            int containsCount = 0;
            for (Iterator it = ((Iterable) value).iterator(); it.hasNext(); index++) {
                Object item = it.next();

                if (itemSchema != null) {
                    ValidateResult result = itemSchema.validate(item);
                    if (!result.isSuccess()) {
                        return result;
                    }
                }

                if (index < prefixItems.length) {
                    ValidateResult result = prefixItems[index].validate(item);
                    if (!result.isSuccess()) {
                        return result;
                    }
                } else if (additionalItem != null) {
                    ValidateResult result = additionalItem.validate(item);
                    if (!result.isSuccess()) {
                        return result;
                    }
                }

                if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                    ValidateResult result = this.contains.validate(item);
                    if (result.isSuccess()) {
                        containsCount++;
                    }
                }

                if (uniqueItems) {
                    if (uniqueItemsSet == null) {
                        uniqueItemsSet = new HashSet();
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
                return new ValidateResult.MinContainsFail(minContains, containsCount);
            }

            if (maxContains >= 0 && containsCount > maxContains) {
                return new ValidateResult.MaxContainsFail(maxContains, containsCount);
            }

            return SUCCESS;
        }

        return new ValidateResult.TypeNotMatchFail(Type.Array, value.getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.alibaba.fastjson2.schema.ArraySchema that = (com.alibaba.fastjson2.schema.ArraySchema) o;
        return Objects.equals(title, that.title) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description);
    }
}
