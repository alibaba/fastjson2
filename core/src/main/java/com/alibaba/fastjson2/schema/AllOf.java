package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

class AllOf extends JSONSchema {
    final JSONSchema[] items;

    public AllOf(JSONSchema[] items) {
        super(null, null);
        this.items = items;
    }

    public AllOf(JSONObject input) {
        super(input);
        JSONArray items = input.getJSONArray("allOf");
        if (items == null || items.isEmpty()) {
            throw new JSONException("allOf not found");
        }

        this.items = new JSONSchema[items.size()];
        Type type = null;
        for (int i = 0; i < this.items.length; i++) {
            JSONObject itemObject = items.getJSONObject(i);
            JSONSchema item = null;
            if (!itemObject.containsKey("type") && type != null) {
                switch (type) {
                    case String:
                        item = new StringSchema(itemObject);
                        break;
                    case Integer:
                        item = new IntegerSchema(itemObject);
                        break;
                    case Number:
                        item = new NumberSchema(itemObject);
                        break;
                    case Boolean:
                        item = new BooleanSchema(itemObject);
                        break;
                    case Array:
                        item = new ArraySchema(itemObject);
                        break;
                    case Object:
                        item = new ObjectSchema(itemObject);
                        break;
                    default:
                        break;
                }
            }

            if (item == null) {
                item = JSONSchema.of(itemObject);
            }
            type = item.getType();
            this.items[i] = item;
        }
    }

    @Override
    public Type getType() {
        return Type.AllOf;
    }

    @Override
    public ValidateResult validate(Object value) {
        for (JSONSchema item : items) {
            ValidateResult result = item.validate(value);
            if (!result.isSuccess()) {
                return result;
            }
        }
        return SUCCESS;
    }
}
