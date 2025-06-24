package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2954 {
    @Test
    public void testB() {
        BigDecimal value = new BigDecimal("20.0");
        byte[] bytes = JSONB.toBytes(value);
        JSONReader jsonReader = JSONReader.ofJSONB(bytes);
        assertEquals(value, jsonReader.readBigDecimal());
        assertEquals(value.toPlainString(), JSONB.toJSONString(bytes));
    }
}
