package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParseArrayTest {
    @Test
    public void test_0() throws Exception {
        List<Object> list = JSON.parseArray("[{}, {}]", new Type[]{TreeMap.class, HashMap.class});
        assertTrue(list.get(0) instanceof TreeMap);
        assertTrue(list.get(1) instanceof HashMap);
    }

    @Test
    public void test_1() throws Exception {
        List<Object> list = JSON.parseArray("[1, 2, \"abc\"]", new Type[]{int.class, Integer.class, String.class});
        assertTrue(list.get(0) instanceof Integer);
        assertTrue(list.get(1) instanceof Integer);
        assertTrue(list.get(2) instanceof String);
    }

    @Test
    public void test_2() throws Exception {
        List<Object> list = JSON.parseArray("[1, null, \"abc\"]", new Type[]{int.class, Integer.class, String.class});
        assertTrue(list.get(0) instanceof Integer);
        assertTrue(list.get(1) == null);
        assertTrue(list.get(2) instanceof String);
    }
}
