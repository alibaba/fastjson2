package com.alibaba.fastjson.issue_1400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1480 {
    @Test
    public void test_for_issue() throws Exception {
        Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
        map.put(1, 10);
        map.put(2, 4);
        map.put(3, 5);
        map.put(4, 5);
        map.put(37306, 98);
        map.put(36796, 9);

        String json = JSON.toJSONString(map);
        assertEquals("{1:10,2:4,3:5,4:5,37306:98,36796:9}", json);
        assertEquals("{\"1\":10,\"2\":4,\"3\":5,\"4\":5,\"37306\":98,\"36796\":9}", JSON.toJSONString(map, SerializerFeature.WriteNonStringKeyAsString));

        Map<Integer, Integer> map1 = JSON.parseObject(json, new TypeReference<HashMap<Integer, Integer>>() {
        }.getType());

        assertEquals(map1.get(Integer.valueOf(1)), Integer.valueOf(10));
        assertEquals(map1.get(Integer.valueOf(2)), Integer.valueOf(4));
        assertEquals(map1.get(Integer.valueOf(3)), Integer.valueOf(5));
        assertEquals(map1.get(Integer.valueOf(4)), Integer.valueOf(5));
        assertEquals(map1.get(Integer.valueOf(37306)), Integer.valueOf(98));
        assertEquals(map1.get(Integer.valueOf(36796)), Integer.valueOf(9));

        JSONObject map2 = JSON.parseObject("{35504:1,1:10,2:4,3:5,4:5,37306:98,36796:9\n" + "}");

        assertEquals(map2.get(Integer.valueOf(1)), Integer.valueOf(10));
        assertEquals(map2.get(Integer.valueOf(2)), Integer.valueOf(4));
        assertEquals(map2.get(Integer.valueOf(3)), Integer.valueOf(5));
        assertEquals(map2.get(Integer.valueOf(4)), Integer.valueOf(5));
        assertEquals(map2.get(Integer.valueOf(37306)), Integer.valueOf(98));
        assertEquals(map2.get(Integer.valueOf(36796)), Integer.valueOf(9));
    }
}
