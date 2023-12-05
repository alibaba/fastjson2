package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
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

        Model m2 = JSON.parseObject(json, Model.class);
        assertEquals(1, m2.values.size());
        assertEquals(1, m2.subInvokes.size());

        Model m3 = JSON.parseObject(json.getBytes(StandardCharsets.UTF_8), Model.class);
        assertEquals(1, m3.values.size());
        assertEquals(1, m3.subInvokes.size());

        Model m4 = JSON.parseObject(json.toCharArray(), Model.class);
        assertEquals(1, m4.values.size());
        assertEquals(1, m4.subInvokes.size());
    }

    public static class Model {
        public Map<String, Value> values = new HashMap<String, Value>();
        public Map<String, Value> subInvokes = new HashMap<String, Value>();
    }

    public static class Value {
    }
}
