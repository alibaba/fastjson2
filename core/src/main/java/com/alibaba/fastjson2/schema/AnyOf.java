package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

/**
 * AnyOf 类继承了 JSONSchema 类，表示 JSON Schema 中的 anyOf 关键字。
 * anyOf 关键字指示提供的多个 schema 中至少有一个必须对输入数据进行验证成功，即任意一个 schema 通过验证即认为整体验证成功。
 */
final class AnyOf extends JSONSchema {
    final JSONSchema[] items; // 用于存储 anyOf 关键字的各个子 schema

    /**
     * 构造函数，接受一个 JSONSchema 数组，用于初始化 AnyOf 对象。
     * @param items 用于验证输入数据的 JSONSchema 数组
     */
    public AnyOf(JSONSchema[] items) {
        super(null, null);
        this.items = items;
    }

    /**
     * 构造函数，从 JSON 对象构建 AnyOf 对象。
     * @param input 用于构建 AnyOf 对象的 JSON 对象
     * @param parent 父 JSONSchema 对象
     */
    public AnyOf(JSONObject input, JSONSchema parent) {
        super(input);
        JSONArray items = input.getJSONArray("anyOf");

        // 如果 "anyOf" 键不存在或为空，则抛出异常
        if (items == null || items.isEmpty()) {
            throw new JSONException("anyOf not found");
        }

        // 初始化 items 数组，用于存储子 schema
        this.items = new JSONSchema[items.size()];

        for (int i = 0; i < this.items.length; i++) {
            Object item = items.get(i);

            // 如果 item 是布尔值，根据其值选择 Any.INSTANCE 或 Any.NOT_ANY
            if (item instanceof Boolean) {
                this.items[i] = (Boolean) item ? Any.INSTANCE : Any.NOT_ANY;
            } else {
                this.items[i] = JSONSchema.of((JSONObject) item, parent);
            }
        }
    }

    /**
     * 获取 AnyOf 对象的类型，返回 Type.AnyOf。
     * @return AnyOf 对象的类型
     */
    @Override
    public Type getType() {
        return Type.AnyOf;
    }

    /**
     * 验证输入数据是否满足 anyOf 关键字定义的至少一个子 schema 的要求。
     * @param value 待验证的输入数据
     * @return 验证结果，只要有一个子 schema 验证成功即返回 SUCCESS，否则返回 FAIL_ANY_OF
     */
    @Override
    public ValidateResult validate(Object value) {
        for (JSONSchema item : items) {
            ValidateResult result = item.validate(value);
            if (result == SUCCESS) {
                return SUCCESS;
            }
        }
        return FAIL_ANY_OF;
    }
}

