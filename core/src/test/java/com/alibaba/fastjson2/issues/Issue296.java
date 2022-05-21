package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue296 {
    @Test
    public void test() {
        List<String> list = JSON.parseArray("[1]").toList(String.class);
        assertEquals(1, list.size());
        assertEquals("1", list.get(0));

        String[] values = JSONObject.of("values", JSONArray.of(1, 2L)).getObject("values", String[].class);
        assertEquals(2, values.length);
        assertEquals("1", values[0]);
        assertEquals("2", values[1]);

        int[] array = JSONArray.of("1", "2").to(int[].class);
        assertEquals(2, array.length);
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);
    }
}
