package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1016 {
    @Test
    public void test() {
        Map<String, String> map = new HashMap<>();
        map.put("2", "2");
        map.put("1", "1");
        assertEquals(
                "{\"1\":\"1\",\"2\":\"2\"}",
                JSON.toJSONString(map, JSONWriter.Feature.MapSortField)
        );
    }
}
