package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue841 {
    @Test
    public void test() {
        char[] chars = new char[1024 * 1024];
        Arrays.fill(chars, '0');

        JSONArray array = new JSONArray();
        for (int i = 0; i < 128; i++) {
            array.add(new String(chars));
        }

        String str = JSON.toJSONString(array, JSONWriter.Feature.LargeObject);
        JSONArray array1 = JSON.parseArray(str);
        assertEquals(array.size(), array1.size());
        for (int i = 0; i < array.size(); i++) {
            assertEquals(array.get(i), array1.get(i));
        }
    }
}
