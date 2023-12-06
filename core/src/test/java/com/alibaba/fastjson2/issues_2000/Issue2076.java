package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2076 {
    @Test
    public void test() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < 1000; i++) {
            array.add(new BigDecimal("0e-700"));
        }
        String str = JSON.toJSONString(array);
        JSONArray array1 = JSON.parseArray(str);
        assertEquals(array, array1);

        byte[] bytes = JSON.toJSONBytes(array);
        JSONArray array2 = JSON.parseArray(bytes);
        assertEquals(array, array2);
    }

    @Test
    public void test1() {
        BigDecimal decimal = new BigDecimal("0e-700");
        JSONWriter jsonWriter = JSONWriter.of();
        for (int i = 0; i < 1000; i++) {
            jsonWriter.writeDecimal(decimal);
        }
    }

    @Test
    public void test2() {
        BigDecimal decimal = new BigDecimal("0e-700");
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        for (int i = 0; i < 1000; i++) {
            jsonWriter.writeDecimal(decimal);
        }
    }
}
