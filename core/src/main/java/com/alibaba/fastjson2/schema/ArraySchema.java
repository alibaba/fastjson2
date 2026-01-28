package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSON;
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
    final boolean encoded;

    public ArraySchema(JSONObject input, JSONSchema root) {
        super(input);

        this.typed = "array".equals(input.get("type"));
        this.definitions = new LinkedHashMap<>();
        this.defs = new LinkedHashMap<>();
        this.encoded = input.getBooleanValue("encoded", false);

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
    protected ValidateResult validateInternal(Object value, ValidationHandler handler, String path) {
        if (value == null) {
            if (typed) {
                ValidateResult result = handleError(handler, null, path, FAIL_INPUT_NULL);
                return result != null ? result : FAIL_INPUT_NULL;
            }
            return SUCCESS;
        }

        if (encoded) {
            if (value instanceof String) {
                try {
                    value = JSON.parseArray((String) value);
                } catch (JSONException e) {
                    ValidateResult result = handleError(handler, value, path, FAIL_INPUT_NOT_ENCODED);
                    return result != null ? result : FAIL_INPUT_NOT_ENCODED;
                }
            } else {
                ValidateResult result = handleError(handler, value, path, FAIL_INPUT_NOT_ENCODED);
                return result != null ? result : FAIL_INPUT_NOT_ENCODED;
            }
        }

        if (value instanceof Object[]) {
            final Object[] items = (Object[]) value;
            return validateItems(value, items.length, i -> items[i], handler, path);
        }

        if (value.getClass().isArray()) {
            final int size = Array.getLength(value);
            final Object finalValue = value;
            return validateItems(finalValue, size, i -> Array.get(finalValue, i), handler, path);
        }

        if (value instanceof Collection) {
            final Collection<?> items = (Collection<?>) value;
            final Iterator<?> iterator = items.iterator();
            return validateItems(value, items.size(), i -> iterator.next(), handler, path);
        }

        if (typed) {
            ValidateResult result = handleError(handler, value, path, FAIL_TYPE_NOT_MATCH);
            return result != null ? result : FAIL_TYPE_NOT_MATCH;
        }

        return SUCCESS;
    }

    private ValidateResult validateItems(final Object value, final int size, IntFunction<Object> itemGetter, ValidationHandler handler, String path) {
        boolean totalSuccess = true;
        ValidateResult firstFail = null;

        if (minLength >= 0 && size < minLength) {
            ValidateResult result = new ValidateResult(false, "minLength not match, expect >= %s, but %s", minLength, size);
            ValidateResult r = handleError(handler, value, path, result);
            if (r != null) {
                return r;
            }

            totalSuccess = false;
            if (firstFail == null) {
                firstFail = result;
            }
        }

        if (maxLength >= 0 && size > maxLength) {
            ValidateResult result = new ValidateResult(false, "maxLength not match, expect <= %s, but %s", maxLength, size);
            ValidateResult r = handleError(handler, value, path, result);
            if (r != null) {
                return r;
            }

            totalSuccess = false;
            if (firstFail == null) {
                firstFail = result;
            }
        }

        if (!additionalItems && size > prefixItems.length) {
            ValidateResult result = new ValidateResult(false, "additional items not match, max size %s, but %s", prefixItems.length, size);
            ValidateResult r = handleError(handler, value, path, result);
            if (r != null) {
                return r;
            }

            totalSuccess = false;
            if (firstFail == null) {
                firstFail = result;
            }
        }

        final boolean isCollection = value instanceof Collection;
        Set<Object> uniqueItemsSet = null;
        int containsCount = 0;
        for (int index = 0; index < size; index++) {
            Object item = itemGetter.apply(index);
            String itemPath = path + "[" + index + "]";

            boolean prefixMatch = false;
            if (index < prefixItems.length) {
                ValidateResult result = prefixItems[index].validateInternal(item, handler, itemPath);
                if (!result.isSuccess()) {
                    if (handler == null || result.isAbort()) {
                        return result;
                    }

                    totalSuccess = false;
                    if (firstFail == null) {
                        firstFail = result;
                    }
                }
                prefixMatch = true;
            } else if (isCollection && itemSchema == null && additionalItem != null) { // 只有 Collection 才会执行这部分校验
                ValidateResult result = additionalItem.validateInternal(item, handler, itemPath);
                if (!result.isSuccess()) {
                    if (handler == null || result.isAbort()) {
                        return result;
                    }

                    totalSuccess = false;
                    if (firstFail == null) {
                        firstFail = result;
                    }
                }
            }

            if (!prefixMatch && itemSchema != null) {
                ValidateResult result = itemSchema.validateInternal(item, handler, itemPath);
                if (!result.isSuccess()) {
                    if (handler == null || result.isAbort()) {
                        return result;
                    }

                    totalSuccess = false;
                    if (firstFail == null) {
                        firstFail = result;
                    }
                }
            }

            if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                ValidateResult result = this.contains.validateInternal(item, null, itemPath);
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
                    ValidateResult r = handleError(handler, item, itemPath, UNIQUE_ITEMS_NOT_MATCH);
                    if (r != null) {
                        return r;
                    }

                    totalSuccess = false;
                    if (firstFail == null) {
                        firstFail = UNIQUE_ITEMS_NOT_MATCH;
                    }
                }
            }
        }

        if (!isCollection || this.contains != null) {
            ValidateResult containsError = null;
            if (minContains >= 0 && containsCount < minContains) {
                containsError = new ValidateResult(false, "minContains not match, expect %s, but %s", minContains, containsCount);
            } else if (maxContains >= 0 && containsCount > maxContains) {
                containsError = new ValidateResult(false, "maxContains not match, expect %s, but %s", maxContains, containsCount);
            } else if (this.contains != null && containsCount == 0 && minContains != 0) {
                containsError = CONTAINS_NOT_MATCH;
            }

            if (containsError != null) {
                ValidateResult r = handleError(handler, value, path, containsError);
                if (r != null) {
                    return r;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = containsError;
                }
            }
        }

        if (allOf != null) {
            ValidateResult result = allOf.validateInternal(value, handler, path);
            if (!result.isSuccess()) {
                if (handler == null || result.isAbort()) {
                    return result;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = result;
                }
            }
        }

        if (anyOf != null) {
            ValidateResult result = anyOf.validateInternal(value, handler, path);
            if (!result.isSuccess()) {
                if (handler == null || result.isAbort()) {
                    return result;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = result;
                }
            }
        }

        if (oneOf != null) {
            ValidateResult result = oneOf.validateInternal(value, handler, path);
            if (!result.isSuccess()) {
                if (handler == null || result.isAbort()) {
                    return result;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = result;
                }
            }
        }

        return totalSuccess ? SUCCESS : (firstFail != null ? firstFail : new ValidateResult(false, "Array validation failed"));
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

        return injectIfPresent(object, allOf, anyOf, oneOf);
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
