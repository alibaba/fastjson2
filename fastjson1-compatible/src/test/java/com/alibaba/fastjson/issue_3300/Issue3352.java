package com.alibaba.fastjson.issue_3300;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3352 {
    @Test
    public void test_for_issue() {
        UUID uuid = UUID.randomUUID();

        JSONObject object = new JSONObject();
        Map map = object.getInnerMap();
        map.put("1", "1");
        map.put("A", "A");
        map.put("true", "true");
        map.put(uuid.toString(), uuid);

        assertTrue(object.containsKey(1));
        assertTrue(object.containsKey("1"));
        assertTrue(object.containsKey('A'));
        assertTrue(object.containsKey("A"));
        assertTrue(object.containsKey(true));
        assertTrue(object.containsKey("true"));
        assertTrue(object.containsKey(uuid));
        assertTrue(object.containsKey(uuid.toString()));
    }
}
