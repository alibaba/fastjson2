package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue536 {
    @Test
    public void test() {
        Map<String, Object> map;
        List objects = new ArrayList();
        for (int i = 0; i < 1; i++) {
            map = new HashMap();
            map.put("cursor", i);
            objects.add(map);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", objects);
        List data = jsonObject.getObject("data", List.class);
        assertEquals(1, data.size());
    }
}
