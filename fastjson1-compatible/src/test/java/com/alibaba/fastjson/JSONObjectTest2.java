package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONObjectTest2 {
    @Test
    public void test_0() throws Exception {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        JSONObject obj = new JSONObject(map);

        assertEquals(obj.size(), map.size());

        map.put("a", 1);
        assertEquals(obj.size(), map.size());
        assertEquals(obj.get("a"), map.get("a"));

        map.put("b", new int[]{1});
        JSONArray array = obj.getJSONArray("b");
        assertEquals(array.size(), 1);

        map.put("c", new JSONArray());
        JSONArray array2 = obj.getJSONArray("b");
        assertEquals(array2.size(), 1);

        assertEquals(obj.getByteValue("d"), 0);
        assertEquals(obj.getShortValue("d"), 0);
        assertTrue(obj.getFloatValue("d") == 0F);
        assertTrue(obj.getDoubleValue("d") == 0D);
        assertEquals(obj.getBigInteger("d"), null);
        assertEquals(obj.getSqlDate("d"), null);
        assertEquals(obj.getTimestamp("d"), null);

        JSONObject obj2 = (JSONObject) obj.clone();
        assertEquals(obj.size(), obj2.size());
    }
}
