package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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

        float[] floatArray = JSONArray.of("1", 2, 3L, BigDecimal.valueOf(4)).to(float[].class);
        assertEquals(4, floatArray.length);
        assertEquals(1, floatArray[0]);
        assertEquals(2, floatArray[1]);
        assertEquals(3, floatArray[2]);
        assertEquals(4, floatArray[3]);

        double[] doubles = JSONArray.of("1", 2, 3L, BigDecimal.valueOf(4)).to(double[].class);
        assertEquals(4, doubles.length);
        assertEquals(1, doubles[0]);
        assertEquals(2, doubles[1]);
        assertEquals(3, doubles[2]);
        assertEquals(4, doubles[3]);

        Float[] floats = JSONArray.of("1", 2, 3L, BigDecimal.valueOf(4)).to(Float[].class);
        assertEquals(4, floats.length);
        assertEquals(1, floats[0]);
        assertEquals(2, floats[1]);
        assertEquals(3, floats[2]);
        assertEquals(4, floats[3]);
    }
}
