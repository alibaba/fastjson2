package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

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
}
