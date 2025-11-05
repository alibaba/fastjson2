package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2989 {
    @Test
    public void test_getJSONObject_for_JSONArray() { //先放 JavaBean/数组 → 再 get → 再修改 → 再 get 需要看到修改
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = JSONObject.from(new ComplexStruc("v1"));
        jsonArray.add(jsonObject);
        jsonObject.computeIfPresent("value", (k, v) -> "v4");
        assertEquals("v4", jsonArray.getJSONObject(0).getString("value"));

        JSONArray jsonArray2 = new JSONArray();
        jsonArray2.add(new ComplexStruc("v1"));
        JSONObject jsonObject2 = jsonArray2.getJSONObject(0);
        jsonObject2.computeIfPresent("value", (k, v) -> "v4");
        assertEquals("v4", jsonArray2.getJSONObject(0).getString("value"));
    }

    @Test
    public void test_getJSONArray_for_JSONArray() {
        JSONArray jsonArray = new JSONArray();
        JSONArray innerArray = JSONArray.from(Arrays.asList("a", "b"));
        jsonArray.add(innerArray);
        innerArray.add("c");
        assertEquals(3, jsonArray.getJSONArray(0).size());

        JSONArray jsonArray2 = new JSONArray();
        List<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        jsonArray2.add(list);
        JSONArray arr2 = jsonArray2.getJSONArray(0);
        arr2.add("c");
        assertEquals(3, jsonArray2.getJSONArray(0).size());

        JSONArray jsonArray3 = new JSONArray();
        jsonArray3.add(new String[]{"a", "b"});
        JSONArray arr3 = jsonArray3.getJSONArray(0);
        arr3.add("c");
        assertEquals(3, jsonArray3.getJSONArray(0).size());
    }

    @Test
    public void test_getJSONObject_for_JSONObject() {
        JSONObject root1 = new JSONObject();
        JSONObject obj = JSONObject.from(new ComplexStruc("v1"));
        root1.put("bean", obj);
        obj.computeIfPresent("value", (k, v) -> "v4");
        assertEquals("v4", root1.getJSONObject("bean").getString("value"));

        JSONObject root2 = new JSONObject();
        root2.put("bean", new ComplexStruc("v1"));
        JSONObject tmp = root2.getJSONObject("bean");
        tmp.computeIfPresent("value", (k, v) -> "v4");
        assertEquals("v4", root2.getJSONObject("bean").getString("value"));
    }

    @Test
    public void test_getJSONArray_for_JSONObject() {
        JSONObject root1 = new JSONObject();
        JSONArray arr = JSONArray.from(Arrays.asList("a", "b"));
        root1.put("list", arr);
        arr.add("c");
        assertEquals(3, root1.getJSONArray("list").size());

        JSONObject root2 = new JSONObject();
        root2.put("list", Arrays.asList("a", "b"));
        JSONArray arr2 = root2.getJSONArray("list");
        arr2.add("c");
        assertEquals(3, root2.getJSONArray("list").size());

        JSONObject root3 = new JSONObject();
        root3.put("arr", new String[]{"a", "b"});
        JSONArray arr3 = root3.getJSONArray("arr");
        arr3.add("c");
        assertEquals(3, root3.getJSONArray("arr").size());
    }

    @Data
    @AllArgsConstructor
    public static class ComplexStruc {
        String value;
    }
}
