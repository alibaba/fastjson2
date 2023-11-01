package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

/**
 * AllOf 类继承了 JSONSchema 类，表示 JSON Schema 中的 allOf 关键字。
 * allOf 关键字指示所有提供的 schema 都必须对输入数据进行验证，只有当所有提供的 schema 都验证成功时，allOf 才视为验证成功。
 */
final class AllOf extends JSONSchema {
    final JSONSchema[] items; // 用于存储 allOf 关键字的各个子 schema

    /**
     * 构造函数，接受一个 JSONSchema 数组，用于初始化 AllOf 对象。
     * @param items 用于验证输入数据的 JSONSchema 数组
     */
    public AllOf(JSONSchema[] items) {
        super(null, null);
        this.items = items;
    }

    /**
     * 构造函数，从 JSON 对象构建 AllOf 对象。
     * @param input 用于构建 AllOf 对象的 JSON 对象
     * @param parent 父 JSONSchema 对象
     */
    public AllOf(JSONObject input, JSONSchema parent) {
        super(input);
        JSONArray items = input.getJSONArray("allOf");

        // 如果 "allOf" 键不存在或为空，则抛出异常
        if (items == null || items.isEmpty()) {
            throw new JSONException("allOf not found");
        }

        // 初始化 items 数组，用于存储子 schema
        this.items = new JSONSchema[items.size()];
        Type type = null;

        for (int i = 0; i < this.items.length; i++) {
            JSONSchema itemSchema = null;
            Object item = items.get(i);

            // 如果 item 是布尔值，根据其值选择 Any.INSTANCE 或 Any.NOT_ANY
            if (item instanceof Boolean) {
                itemSchema = (Boolean) item ? Any.INSTANCE : Any.NOT_ANY;
            } else {
                JSONObject itemObject = (JSONObject) item;

                // 如果 itemObject 没有 "type" 键，但存在 type，则根据 type 创建相应的 schema
                if (!itemObject.containsKey("type") && type != null) {
                    switch (type) {
                        case String:
                            itemSchema = new StringSchema(itemObject);
                            break;
                        case Integer:
                            itemSchema = new IntegerSchema(itemObject);
                            break;
                        case Number:
                            itemSchema = new NumberSchema(itemObject);
                            break;
                        case Boolean:
                            itemSchema = new BooleanSchema(itemObject);
                            break;
                        case Array:
                            itemSchema = new ArraySchema(itemObject, null);
                            break;
                        case Object:
                            itemSchema = new ObjectSchema(itemObject);
                            break;
                        default:
                            break;
                    }
                }

                // 如果未找到匹配的 itemSchema，则使用 JSONSchema.of 方法创建
                if (itemSchema == null) {
                    itemSchema = JSONSchema.of(itemObject, parent);
                }
            }

            type = itemSchema.getType();
            this.items[i] = itemSchema;
        }
    }

    /**
     * 获取 AllOf 对象的类型，返回 Type.AllOf。
     * @return AllOf 对象的类型
     */
    @Override
    public Type getType() {
        return Type.AllOf;
    }

    /**
     * 验证输入数据是否满足 allOf 关键字定义的所有子 schema。
     * @param value 待验证的输入数据
     * @return 验证结果，成功返回 SUCCESS，否则返回失败的验证结果
     */
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

