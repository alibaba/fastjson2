package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertSame;

public class TestSpecial_2 {
    @Test
    public void test_special() {
        Model model = new Model();
        Value value = new Value();
        model.values.put("com.ibatis.sqlmap.client.SqlMapExecutor@queryForObject(String,Object)", value);
        model.subInvokes.put("com.ibatis.sqlmap.client.SqlMapExecutor@queryForObject(String,Object)", value);

        String json = JSON.toJSONString(model, JSONWriter.Feature.ReferenceDetection);
        System.out.println(json);

        Model m2 = JSON.parseObject(json, Model.class);
        assertEquals(1, m2.values.size());
        assertEquals(1, m2.subInvokes.size());

//        assertSame(m2.values.values().iterator().next(), m2.subInvokes.values().iterator().next());
    }

    public static class Model {
        public Map<String, Value> values = new HashMap<String, Value>();
        public Map<String, Value> subInvokes = new HashMap<String, Value>();
    }

    public static class Value {
    }
}
