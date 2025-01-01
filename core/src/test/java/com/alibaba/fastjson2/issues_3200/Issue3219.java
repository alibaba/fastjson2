package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.alibaba.fastjson2.JSONWriter.Feature.ReferenceDetection;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue3219 {
    @Test
    public void test() {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("xxx", "xxxx");
        innerMap.put("ttt", "tttt");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1~", innerMap);
        map.put("key2", innerMap);

        String josnStr1 = JSON.toJSONString(map, ReferenceDetection);
        Map map2 = JSON.parseObject(josnStr1, Map.class);
        assertSame(map2.get("key2"), map2.get("key1~"));
    }
}
