package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2187 {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.items = new ArrayList<>();
        bean.items.add(new Item());
        JSONObject jsonObject = (JSONObject) JSON.toJSON(bean);
        JSONArray items = (JSONArray) jsonObject.get("items");
        assertEquals(1, items.size());
    }

    @Data
    public static class Bean {
        List<Item> items;
    }

    public static class Item {
    }
}
