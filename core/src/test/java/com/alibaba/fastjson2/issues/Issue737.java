package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue737 {
    @Test
    public void test() {
        Map<Long, Long> map = new HashMap<>();
        map.put(11L, 111L);
        map.put(22L, 222L);
        String json = JSONObject.toJSONString(map);
        assertEquals("{22:222,11:111}", json);

        Map<Long, Long> parseObject = JSONObject.parseObject(json, new TypeReference<Map<Long, Long>>(){});
        assertEquals(2, parseObject.size());
        assertEquals(111L, parseObject.get(11L));
        assertEquals(222L, parseObject.get(22L));

        Map<Long, Long> parseObject1 = JSONObject.parseObject(json, new TypeReference<HashMap<Long, Long>>(){});
        assertEquals(2, parseObject1.size());
        assertEquals(111L, parseObject1.get(11L));
        assertEquals(222L, parseObject1.get(22L));

        Map<String, Long> parseObject2 = JSON.parseObject(
                json,
                new TypeReference<HashMap<String, Long>>(){}
        );
        assertEquals(2, parseObject2.size());
        assertEquals(111L, parseObject2.get("11"));
        assertEquals(222L, parseObject2.get("22"));
    }

    @Test
    public void test1() {
        Map<String, Long> map = JSON.parseObject("{null:123}", new TypeReference<HashMap<String, Long>>(){});
        assertEquals(1, map.size());
        assertEquals(123, map.get(null));

        assertThrows(JSONException.class, () -> JSON.parseObject("{null 123}", new TypeReference<HashMap<String, Long>>(){}));
    }
}
