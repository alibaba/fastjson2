package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue336 {
    @Test
    public void test() {
        HashMap<String, Object> map = new HashMap<>();
        Date date = new Date();
        map.put("date", date);
        String jsonString = JSON.toJSONString(map, JSONWriter.Feature.WriteClassName);
        Map result = JSON.parseObject(jsonString, Map.class);
        assertEquals(date.getTime(), ((Date) result.get("date")).getTime());
    }
}
