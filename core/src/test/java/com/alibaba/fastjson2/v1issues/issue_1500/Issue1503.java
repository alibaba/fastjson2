package com.alibaba.fastjson2.v1issues.issue_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1503 {
    @Test
    public void test_for_issue() throws Exception {
//        config.setAutoTypeSupport(true);
        Map<Long, Bean> map = new HashMap<Long, Bean>();
        map.put(null, new Bean());
        Map<Long, Bean> rmap = (Map<Long, Bean>) JSON.parse(JSON.toJSONString(map, JSONWriter.Feature.WriteClassName));
        String json = JSON.toJSONString(rmap);
        assertEquals("{\"@type\":\"java.util.HashMap\",\"null\":{\"@type\":\"com.alibaba.fastjson2.v1issues.issue_1500.Issue1503$Bean\"}}", json);
    }

    public static class Bean {
    }
}
