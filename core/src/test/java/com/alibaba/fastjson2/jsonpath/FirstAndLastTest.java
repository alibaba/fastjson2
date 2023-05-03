package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FirstAndLastTest {
    @Test
    public void first() {
        assertEquals(1, JSONPath.extract("[1,2,3]", "$.first()"));
    }

    @Test
    public void first1() {
        assertEquals(1, JSONPath.extract("{\"values\":[1,2,3]}", "$.values.first()"));
    }

    @Test
    public void first2() {
        assertEquals(1, JSONPath.extract("[{\"values\":[1,2,3]}]", "$.values.first()"));
    }

    @Test
    public void first3() {
        assertEquals(1, JSONPath.extract("[{\"value\":1},{\"value\":2},{\"value\":3}]", "$.value.first()"));
    }

    @Test
    public void firstEmpty() {
        assertEquals(null, JSONPath.extract("[]", "$.first()"));
    }

    @Test
    public void firstCollection() {
        assertEquals(1, JSONPath.eval(Collections.singleton(1), "$.first()"));
    }

    @Test
    public void firstArray() {
        assertEquals(1, JSONPath.eval(new int[]{1, 2, 3}, "$.first()"));
    }

    @Test
    public void firstArrayEmpty() {
        assertEquals(null, JSONPath.eval(new int[]{}, "$.first()"));
    }

    @Test
    public void last() {
        assertEquals(3, JSONPath.extract("[1,2,3]", "$.last()"));
    }

    @Test
    public void last1() {
        assertEquals(3, JSONPath.extract("{\"values\":[1,2,3]}", "$.values.last()"));
    }

    @Test
    public void last2() {
        assertEquals(3, JSONPath.extract("[{\"values\":[1,2,3]}]", "$.values.last()"));
    }

    @Test
    public void last3() {
        assertEquals(3, JSONPath.extract("[{\"value\":1},{\"value\":2},{\"value\":3}]", "$.value.last()"));
    }

    @Test
    public void lastEmpty() {
        assertEquals(null, JSONPath.extract("[]", "$.last()"));
    }

    @Test
    public void lastCollection() {
        assertEquals(1, JSONPath.eval(Collections.singleton(1), "$.last()"));
    }

    @Test
    public void lastArray() {
        assertEquals(3, JSONPath.eval(new int[]{1, 2, 3}, "$.last()"));
    }

    @Test
    public void lastArrayEmpty() {
        assertEquals(null, JSONPath.eval(new int[]{}, "$.last()"));
    }
}
