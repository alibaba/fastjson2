package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue536 {

    private JSONObject jsonObject;

    @BeforeEach
    public void init(){
        List objects = new ArrayList();
        Map<String, Object> map = new HashMap<>();
        map.put("test", 1);
        objects.add(map);

        List<String> list = new ArrayList<>();
        list.add("1");
        objects.add(list);
        jsonObject = new JSONObject();
        jsonObject.put("data", objects);
    }

    @Test
    public void test(){
        List result = jsonObject.getObject("data", List.class);
        Map<String, Object> rmap = (Map<String, Object>) result.get(0);
        assertEquals(rmap.get("test"),1);
        List<String> rlist = (List<String>) result.get(1);
        assertEquals(rlist.get(0),"1");
    }
}
