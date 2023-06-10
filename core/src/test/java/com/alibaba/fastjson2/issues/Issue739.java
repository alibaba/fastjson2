package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue739 {
    @Test
    public void test1() {
        List<String> strings = getUEKms();
        assertEquals(1, strings.size());
        assertEquals("abc", strings.get(0));
    }

    public static List<String> getUEKms() {
        JSONObject config = JSONObject.of("uekms", JSONArray.of("abc"));
        return config.getJSONArray("uekms").stream().map(Object::toString).collect(Collectors.toList());
    }

    public static class Event {
    }
}
