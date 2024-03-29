package com.alibaba.fastjson2.issue_2300;

import com.alibaba.fastjson.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 张治保
 * @since 2024/3/29
 */
public class Issue2348 {
    @Test
    void test() {
        ArrayList<Object> items = new ArrayList<>();
        HashMap<Object, Object> item = new HashMap<>();
        item.put("data", "data");
        items.add(item);
        items.add(item);
        List<HashMap> newItems = JSONArray.parseArray(JSONArray.toJSONString(items), HashMap.class);
        Assertions.assertEquals(items, newItems);
    }
}
