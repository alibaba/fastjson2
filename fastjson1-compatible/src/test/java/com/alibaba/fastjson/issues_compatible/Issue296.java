package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue296 {
    @Test
    public void test() {
        List<String> list = JSON.parseArray("[1]").toJavaList(String.class);
        assertEquals(1, list.size());
        assertEquals("1", list.get(0));

        String[] values = JSON.parseObject("{\"values\":[1,2]}").getObject("values", String[].class);
        assertEquals(2, values.length);
        assertEquals("1", values[0]);
        assertEquals("2", values[1]);

        int[] array = JSON.parseArray("[\"1\",\"2\"]").toJavaObject(int[].class);
        assertEquals(2, array.length);
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);
    }
}
