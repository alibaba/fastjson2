package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3352 {
    @Test
    public void test_for_issue() {
        UUID uuid = UUID.randomUUID();

        JSONObject object = new JSONObject();
        object.put("1", "1");
        object.put("A", "A");
        object.put("true", "true");
        object.put(uuid.toString(), uuid);

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
