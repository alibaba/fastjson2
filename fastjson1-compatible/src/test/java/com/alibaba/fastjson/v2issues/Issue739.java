package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Issue739 {
    @Test
    public void test() {
        final String HEART_BEAT_EVENT = "xxx";
        String data = "{}";
        Event event = new Event();

        Optional.ofNullable(event)
                .filter(map -> HEART_BEAT_EVENT.equals(event))
                .map(
                        map -> JSON.parseObject(
                                data,
                                new TypeReference<Map<String, Event>>() {}
                        )
                );
    }

    public static class Event {
    }

    @Test
    public void test1() {
        JSONObject config = new JSONObject();
        config.put("uekms", new JSONArray());
        config.getJSONArray("uekms").stream().map(Object::toString).collect(Collectors.toList());
    }
}
