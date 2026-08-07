package com.alibaba.fastjson.issue_4200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.BeforeFilter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4287 {
    @Test
    public void test12() {
        Map<String, Object> map = new HashMap<>();
        map.put("k1", "v1");
        AtomicInteger counter = new AtomicInteger();
        JSON.toJSONString(map, new BeforeFilter() {
            @Override
            public void writeBefore(Object object) {
                counter.incrementAndGet();
            }
        });
        assertEquals(1, counter.get());
    }
}
