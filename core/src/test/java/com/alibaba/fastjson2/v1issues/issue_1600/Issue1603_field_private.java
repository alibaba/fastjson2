package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Issue1603_field_private extends TestCase {
    public void test_emptySet() throws Exception {
        Model_1 m = JSON.parseObject("{\"values\":[\"a\"]}", Model_1.class);
        assertEquals(0, m.values.size());
    }

    public void test_emptyList() throws Exception {
        Model_2 m = JSON.parseObject("{\"values\":[\"a\"]}", Model_2.class);
        assertEquals(0, m.values.size());
    }

    public void test_unmodifier() throws Exception {
        Model_3 m = JSON.parseObject("{\"values\":[\"a\"]}", Model_3.class);
        assertEquals(0, m.values.size());
    }

    static class Model_1 {
        public final Collection<String> values = Collections.emptySet();
    }

    static class Model_2 {
        public final Collection<String> values = Collections.emptyList();
    }

    static class Model_3 {
        public final Collection<String> values = Collections.unmodifiableList(new ArrayList<String>());
    }
}
