package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1120 {
    @Test
    public void test() {
        JSONObject object = JSON.parseObject("{\"rate\": 0.000000006E3}");
        BigDecimal rate = object.getBigDecimal("rate");
        assertEquals(BigDecimal.valueOf(Double.parseDouble("0.000000006E3")), rate);
    }

    @Test
    public void test1() {
        Bean bean = JSON.parseObject("{\"rate\": 0.000000006E3}", Bean.class);
    }

    public static class Bean {
        public BigDecimal rate;
    }
}
