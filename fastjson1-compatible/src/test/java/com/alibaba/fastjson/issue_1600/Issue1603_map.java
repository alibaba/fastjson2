package com.alibaba.fastjson.issue_1600;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1603_map {
    @Test
    public void test_emptyMap() throws Exception {
        Model_1 m = JSON.parseObject("{\"values\":{\"a\":1001}}", Model_1.class);
        assertEquals(0, m.values.size());
    }

    @Test
    public void test_unmodifiableMap() throws Exception {
        Model_2 m = JSON.parseObject("{\"values\":{\"a\":1001}}", Model_2.class);
        assertEquals(0, m.values.size());
    }

    public static class Model_1 {
        public final Map<String, Object> values = Collections.emptyMap();
    }

    public static class Model_2 {
        public final Map<String, Object> values = Collections.unmodifiableMap(new HashMap<String, Object>());
    }
}
