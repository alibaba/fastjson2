package com.alibaba.fastjson.issue_1600;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1603_getter {
    @Test
    public void test_emptySet() throws Exception {
        Model_1 m = JSON.parseObject("{\"values\":[\"a\"]}", Model_1.class);
        assertEquals(0, m.values.size());
    }

    @Test
    public void test_emptyList() throws Exception {
        Model_2 m = JSON.parseObject("{\"values\":[\"a\"]}", Model_2.class);
        assertEquals(0, m.values.size());
    }

    @Test
    public void test_unmodifier() throws Exception {
        Model_3 m = JSON.parseObject("{\"values\":[\"a\"]}", Model_3.class);
        assertEquals(0, m.values.size());
    }

    public static class Model_1 {
        private final Collection<String> values = Collections.emptySet();

        public Collection<String> getValues() {
            return values;
        }
    }

    public static class Model_2 {
        private final Collection<String> values = Collections.emptyList();

        public Collection<String> getValues() {
            return values;
        }
    }

    public static class Model_3 {
        private final Collection<String> values = Collections.unmodifiableList(new ArrayList<String>());

        public Collection<String> getValues() {
            return values;
        }
    }
}
