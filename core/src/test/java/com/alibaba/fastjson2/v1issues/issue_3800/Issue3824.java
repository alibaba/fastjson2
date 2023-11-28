package com.alibaba.fastjson2.v1issues.issue_3800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Issue3824 {
    @Test
    public void test_for_issue3824() throws JSONException {
        Map<String, Object> result = new HashMap<>();
        XX a = new XX();
        a.a = 100;
        List<XX> t = Arrays.asList(a);
        Map<String, Object> y = new HashMap<>();
        y.put("2_wdk", t);
        Map<String, Object> x = new HashMap<>();
        x.put("y", y);
        result.put("a.b.c", x);
        result.put("x", a);
        String s = JSON.toJSONString(result);
        JSONAssert.assertEquals("{\"a.b.c\":{\"y\":{\"2_wdk\":[{\"a\":100}]}},\"x\":{\"a\":100}}", s, true);
        JSONObject revert = JSON.parseObject(s);
        JSONAssert.assertEquals("{\"a.b.c\":{\"y\":{\"2_wdk\":[{\"a\":100}]}},\"x\":{\"a\":100}}", JSON.toJSONString(revert), true);
    }

    public static class XX {
        int a;

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }
    }
}
