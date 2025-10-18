package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayBugTest {
    @Test
    public void test_for_array() {
        assertEquals("[\"e1\"]",
                JSONPath.of("$.arr", String.class, JSONPath.Feature.UnwrapStringArray)
                        .extract("{\"arr\":[\"e1\"]}"));

        assertEquals("e1",
                JSONPath.of("$.arr", String.class)
                        .extract("{\"arr\":[\"e1\"]}"));
    }
}
