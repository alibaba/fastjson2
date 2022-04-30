package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3356 {
    @Test
    public void test_for_issue() {
        UUID uuid = UUID.randomUUID();

        JSONObject object = new JSONObject();
        object.put("1", "1");
        object.put(uuid.toString(), uuid.toString());
        object.put("A", "A");
        object.put("true", "true");
        assertEquals("1", object.get(1));
        assertEquals("true", object.get(true));
        assertEquals("A", object.get('A'));
    }
}
