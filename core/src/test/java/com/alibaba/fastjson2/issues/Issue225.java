package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue225 {
    @Test
    public void test() {
        JSONObject jsonObject = JSONObject.of("item_id", 101);

        jsonObject.nameFilter(
                (Object object, String name, Object value) -> name
        );
        assertEquals(1, jsonObject.size());
        assertEquals(101, jsonObject.get("item_id"));

        jsonObject.nameFilter(
                (Object object, String name, Object value) -> null
        );
        assertEquals(1, jsonObject.size());
        assertEquals(101, jsonObject.get("item_id"));

        jsonObject.nameFilter(
                (object, name, value) -> PropertyNamingStrategy.snakeToCamel(name)
        );
        assertEquals(1, jsonObject.size());
        assertEquals(101, jsonObject.get("itemId"));
    }

    @Test
    public void test1() {
        JSONObject jsonObject = JSONObject
                .of(
                        "items",
                        JSONArray.of(
                                JSONObject.of("item_id", 101)
                        )
                );

        jsonObject.nameFilter(
                (object, name, value) -> PropertyNamingStrategy.snakeToCamel(name)
        );

        assertEquals(
                101,
                jsonObject
                        .getJSONArray("items")
                        .getJSONObject(0)
                        .get("itemId")
        );
    }

    @Test
    public void test2() {
        JSONObject jsonObject = JSON.parseObject("{\"items\":[{\"item_id\":101}]}");

        jsonObject.nameFilter(
                (object, name, value) -> PropertyNamingStrategy.snakeToCamel(name)
        );

        assertEquals(
                101,
                jsonObject
                        .getJSONArray("items")
                        .getJSONObject(0)
                        .get("itemId")
        );
    }

    @Test
    public void test3() {
        JSONObject jsonObject = JSON.parseObject("{\"items\":[{\"item_id\":101}]}");

        jsonObject.valueFilter(
                (object, name, value) -> name.equals("item_id") ? ((Integer) value).intValue() + 1 : value
        );

        assertEquals(
                102,
                jsonObject
                        .getJSONArray("items")
                        .getJSONObject(0)
                        .get("item_id")
        );
    }

    @Test
    public void test4() {
        JSONObject jsonObject = JSON.parseObject("{\"root\":{\"items\":[[{\"item_id\":101}]]}}");

        jsonObject.valueFilter(
                (object, name, value) -> name.equals("item_id") ? ((Integer) value).intValue() + 1 : value
        );

        assertEquals(
                102,
                jsonObject
                        .getJSONObject("root")
                        .getJSONArray("items")
                        .getJSONArray(0)
                        .getJSONObject(0)
                        .get("item_id")
        );

        jsonObject.nameFilter(
                (object, name, value) -> PropertyNamingStrategy.snakeToCamel(name)
        );

        assertEquals(
                102,
                jsonObject
                        .getJSONObject("root")
                        .getJSONArray("items")
                        .getJSONArray(0)
                        .getJSONObject(0)
                        .get("itemId")
        );
    }
}
