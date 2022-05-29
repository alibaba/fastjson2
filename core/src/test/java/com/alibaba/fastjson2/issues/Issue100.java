package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2_vo.Long1;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue100 {
    @Test
    public void test_int() {
        JSONObject json = new JSONObject();
        json.put("t1", 3234567890L);

        String tmp = json.toString();
        JSONObject json2 = JSON.parseObject(tmp);
        assertEquals("{\"t1\":3234567890}", json2.toString());
    }

    @Test
    public void test_int_neg() {
        JSONObject json = new JSONObject();
        json.put("t1", -3234567890L);

        String tmp = json.toString();
        JSONObject json2 = JSON.parseObject(tmp);
        assertEquals("{\"t1\":-3234567890}", json2.toString());
    }

    @Test
    public void test_long() {
        JSONObject json = new JSONObject();
        json.put("t1", new BigDecimal("10223372036854775808"));

        String tmp = json.toString();
        JSONObject json2 = JSON.parseObject(tmp);
        assertEquals("{\"t1\":10223372036854775808}", json2.toString());
    }

    @Test
    public void test_long_neg() {
        JSONObject json = new JSONObject();
        json.put("t1", new BigDecimal("-10223372036854775808"));

        String tmp = json.toString();
        JSONObject json2 = JSON.parseObject(tmp);
        assertEquals("{\"t1\":-10223372036854775808}", json2.toString());
    }

    @Test
    public void test_int_loop() {
        long valueInit = 3234567890L - 1000 * 1000;

        for (int i = 0; i < 10000; ++i) {
            long val = valueInit + i * 100000L;
            JSONObject json = new JSONObject();
            json.put("t1", val);

            String tmp = json.toString();
            JSONObject json2 = JSON.parseObject(tmp);
            assertEquals("{\"t1\":" + val + "}", json2.toString());
        }
    }

    @Test
    public void test_int_loop_neg() {
        long valueInit = -3234567890L + 1000 * 1000;

        for (int i = 0; i < 10000; ++i) {
            long val = valueInit - i * 100000L;
            JSONObject json = new JSONObject();
            json.put("t1", val);

            String tmp = json.toString();
            JSONObject json2 = JSON.parseObject(tmp);
            assertEquals("{\"t1\":" + val + "}", json2.toString());
        }
    }

    @Test
    public void test_long1() {
        JSONObject json = new JSONObject();
        json.put("v0000", 3234567890L);

        Long1 vo = json.toJavaObject(Long1.class);
        assertEquals(3234567890L, vo.getV0000());
    }
}
