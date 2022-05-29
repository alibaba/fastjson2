package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue316 {
    @Test
    public void testList() {
        List<String> list = JSON.parseArray("[1,\"2\"]").toList(String.class);
        assertEquals(2, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
    }

    @Test
    public void testArray() {
        String[] array = JSON.parseArray("[1,\"2\"]").toArray(String.class);
        assertEquals(2, array.length);
        assertEquals("1", array[0]);
        assertEquals("2", array[1]);
    }

    @Test
    public void getArray() {
        JSONObject object = JSON.parseObject("{\"values\":[1,\"2\"]}");
        String[] array = object.getObject("values", String[].class);
        assertEquals(2, array.length);
        assertEquals("1", array[0]);
        assertEquals("2", array[1]);
    }

    @Test
    public void getList() {
        JSONObject object = JSON.parseObject("{\"values\":[1,\"2\"]}");
        List<String> list = object.getList("values", String.class);
        assertEquals(2, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
    }

    @Test
    public void parseArray() {
        List<String> list = JSON.parseArray("[1,\"2\"]", String.class);
        assertEquals(2, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
    }
}
