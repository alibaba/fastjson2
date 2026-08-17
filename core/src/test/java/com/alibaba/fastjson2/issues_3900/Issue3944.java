package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3944 {
    @Test
    public void test_issue3944() {
        class A {
            A a;
            String b;
            public String getB() {
                return b;
            }
            public A getA() {
                return a;
            }
        }
        A a = new A();
        a.a = a;
        a.b = "test";
        assertEquals("{\"a\":{\"$ref\":\"..\"},\"b\":\"test\"}", JSON.toJSON(a, JSONWriter.Feature.ReferenceDetection).toString());
    }

    @Test
    public void test_JSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("a", 1);
        obj.put("b", "2");
        System.out.println();
        assertEquals("{\"a\":\"1\",\"b\":\"2\"}", JSON.toJSON(obj, JSONWriter.Feature.WriteNonStringValueAsString).toString());
    }

    @Test
    public void test_JSONArray() {
        JSONArray a = new JSONArray();
        a.add(1);
        a.add("2");
        assertEquals("[\"1\",\"2\"]", JSON.toJSON(a, JSONWriter.Feature.WriteNonStringValueAsString).toString());
    }

    @Test
    public void test_toJSON_OptimizationPath_WithContext() {
        class Bean {
            public int id = 101;
            public String name = "optimization";
        }
        Bean bean = new Bean();

        JSONObject jsonObject = (JSONObject) JSON.toJSON(bean, JSONWriter.Feature.PrettyFormat);
        assertTrue(jsonObject.toString().contains("\n"));
        assertTrue(jsonObject.toString().contains("id"));
    }

    @Test
    public void test_toJSON_FallbackPath_JSONObject_Circular() {
        class CyclicBean {
            public int id = 1;
            public CyclicBean self;
        }
        CyclicBean bean = new CyclicBean();
        bean.self = bean;

        JSONObject jsonObject = (JSONObject) JSON.toJSON(bean, JSONWriter.Feature.ReferenceDetection);
        assertTrue(jsonObject.toString().contains("$ref"));
    }

    @Test
    public void test_toJSON_FallbackPath_JSONArray_WriteType() {
        class Item {
            public int val = 999;
        }
        List<Item> list = new ArrayList<>();
        list.add(new Item());

        JSONArray jsonArray = (JSONArray) JSON.toJSON(list,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.PrettyFormat);

        assertTrue(jsonArray.toString().contains("\n"));
        assertTrue(jsonArray.toString().contains("@type"));
    }
}
