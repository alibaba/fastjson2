package com.alibaba.fastjson.issue_3100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3185 {
    @Test
    public void test_jsonpath_array() {
        List list1 = new LinkedList();
        Map map1 = new HashMap();
        Map map2 = new HashMap();
        map2.put("id", "123");
        map2.put("name", "张三");
        list1.add(map2);
        map1.put("list1", list1);

        JSONObject jsonObject = (JSONObject) JSON.toJSON(map1);
        JSONArray jsonArray = (JSONArray) jsonObject.get("list1");
        for (Object subJsonObject : jsonArray) {
            assertTrue(subJsonObject instanceof JSONObject);
        }
    }
}
