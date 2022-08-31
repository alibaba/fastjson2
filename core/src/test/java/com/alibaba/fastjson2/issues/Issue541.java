package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue541 {
    @Test
    public void test() {
        Map<String, Object> map = null;
        List objects = new ArrayList();
        for (int i = 0; i < 10; i++) {
            map = new HashMap();
            map.put("cursor", i);
            map.put("msg", "11111");
            objects.add(map);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", objects);
        List data = jsonObject.getObject("data", List.class);
        Map first = (Map) data.get(0);
        assertFalse(first.isEmpty());
    }
}
