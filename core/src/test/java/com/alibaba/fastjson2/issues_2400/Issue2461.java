package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.text.NumberFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2461 {
    @Test
    public void test() {
        NumberFormat fmt = NumberFormat.getNumberInstance();

        long value = 123456789;
        String formatted = fmt.format(value);
        String json = JSONObject.of("value", formatted).toJSONString();
        Bean bean = JSON.parseObject(json, Bean.class);
        assertEquals(value, bean.value);
    }

    public static class Bean {
        public long value;
    }

    @Test
    public void testInt() {
        NumberFormat fmt = NumberFormat.getNumberInstance();

        int value = 123456789;
        String formatted = fmt.format(value);
        String json = JSONObject.of("value", formatted).toJSONString();
        BeanInt bean = JSON.parseObject(json, BeanInt.class);
        assertEquals(value, bean.value);
    }

    public static class BeanInt {
        public int value;
    }
}
